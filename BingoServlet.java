package servlet;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/BingoServlet")
public class BingoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String gameId = request.getParameter("gameId");
        ServletContext application = getServletContext();
        
        // サーバーから現在のゲーム（部屋）を取得
        BingoGame game = (BingoGame) application.getAttribute("game");

        // ==========================================================
        // 【アクション 1】部屋の新規作成（司会者）
        // ==========================================================
        if ("create".equals(action)) {
            String validDaysStr = request.getParameter("validDays");
            int validDays = 8; // デフォルトは通常製品版の8日間
            if (validDaysStr != null) {
                try {
                    validDays = Integer.parseInt(validDaysStr);
                } catch (NumberFormatException e) {
                    validDays = 8;
                }
            }
            
            // 固定の部屋番号（例: 88888888）またはランダムな8桁を生成
            // ここでは簡易的に固定ID、または大山さんの運用に合わせて設定
            String newGameId = "88888888"; 
            
            // 新しいゲームインスタンスを生成してサーバーに保存
            game = new BingoGame(newGameId, validDays);
            application.setAttribute("game", game);
            
            // 1〜75の数字をシャッフルしてサーバー側の別の箱に仕込んでおく
            List<Integer> shuffledNumbers = new CopyOnWriteArrayList<>();
            for (int i = 1; i <= 75; i++) {
                shuffledNumbers.add(i);
            }
            Collections.shuffle(shuffledNumbers);
            application.setAttribute("shuffledNumbers", shuffledNumbers);
            
            request.setAttribute("game", game);
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        // ==========================================================
        // 【アクション 2】ゲームのリセット（司会者 F5連動）
        // ==========================================================
        else if ("reset".equals(action)) {
            // ゲームデータを完全に消去（新しく作り直せる状態にする）
            application.removeAttribute("game");
            application.removeAttribute("shuffledNumbers");
            
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        // ==========================================================
        // 共通チェック：これ以降のアクションは「部屋」が存在しないとエラー
        // ==========================================================
        if (game == null) {
            request.setAttribute("error", "⚠️ 現在ビンゴゲームは開始されていないか、リセットされました。");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        // 【新機能】ロックタイマー（有効期限）および2時間放置の自動判定
        if (game.isExpired() || game.isPast2HoursFromLastBingo()) {
            application.removeAttribute("game"); // 自動消去
            application.removeAttribute("shuffledNumbers");
            request.setAttribute("error", "🔒 この部屋は安全のため自動ロック（削除）されました。新しく作り直してください。");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        // ==========================================================
        // 【アクション 3】次の番号を引く（司会者）
        // ==========================================================
        if ("draw".equals(action)) {
            @SuppressWarnings("unchecked")
            List<Integer> shuffledNumbers = (List<Integer>) application.getAttribute("shuffledNumbers");
            
            if (shuffledNumbers != null && !shuffledNumbers.isEmpty()) {
                int nextNumber = shuffledNumbers.remove(0);
                // BingoGame側の安全なリストに当選番号を追加（これで全員に同期される）
                game.getDrawnNumbers().add(nextNumber);
                application.setAttribute("shuffledNumbers", shuffledNumbers);
            }
            
            request.setAttribute("game", game);
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        // ==========================================================
        // 【アクション 4】プレイヤーの参加ログイン
        // ==========================================================
        else if ("join".equals(action)) {
            String inputId = request.getParameter("gameId");
            String inputName = request.getParameter("playerName");
            
            // 部屋番号が合致しているか確認
            if (game.getGameId().equals(inputId)) {
                // 名前を登録（空欄なら Player-A などの自動命名がここで発動）
                String confirmedName = game.registerPlayer(inputName);
                
                request.setAttribute("game", game);
                request.setAttribute("confirmedPlayerName", confirmedName);
                request.getRequestDispatcher("index.jsp").forward(request, response);
            } else {
                request.setAttribute("error", "⚠️ 部屋番号（ゲームID）が正しくありません。");
                request.getRequestDispatcher("index.jsp").forward(request, response);
            }
            return;
        }

        // ==========================================================
        // 【アクション 5】プレイヤーからの即時リーチ報告（裏通信）
        // ==========================================================
        else if ("reach".equals(action)) {
            String playerName = request.getParameter("playerName");
            if (playerName != null && !playerName.trim().isEmpty()) {
                game.addReachPlayer(playerName);
            }
            // 画面全体をリロードさせず、データだけを受け取って0秒で「OK」を返す（即時送信対応）
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // ==========================================================
        // 【アクション 6】プレイヤーからの即時ビンゴ報告（裏通信・同着対応）
        // ==========================================================
        else if ("bingo".equals(action)) {
            String playerName = request.getParameter("playerName");
            if (playerName != null && !playerName.trim().isEmpty()) {
                game.addBingoPlayer(playerName); // 内部で同着計算に必要な最新当選番号も一緒に記録
            }
            // 画面全体をリロードさせず、データだけを受け取って0秒で「OK」を返す（即時送信対応）
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // ==========================================================
        // 【定期通信用】10秒ごとのプレイヤー画面アップデート、または手動更新
        // ==========================================================
        String userType = request.getParameter("userType");
        request.setAttribute("game", game);
        
        if ("admin".equals(userType)) {
            request.getRequestDispatcher("admin.jsp").forward(request, response);
        } else {
            String playerName = request.getParameter("playerName");
            request.setAttribute("confirmedPlayerName", playerName);
            request.getRequestDispatcher("index.jsp").forward(request, response);
        }
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        doGet(request, response);
    }
}