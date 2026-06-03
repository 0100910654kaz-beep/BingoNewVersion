package servlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet("/BingoServlet")
public class BingoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String gameId = request.getParameter("gameId");
        ServletContext application = getServletContext();
        HttpSession session = request.getSession();
        
        // サーバーから現在のゲーム（部屋）を取得
        BingoGame game = (BingoGame) application.getAttribute("game");

        // ==========================================================
        // 【アクション 1】部屋の新規作成（司会者）
        // ==========================================================
        if ("create".equals(action)) {
            String validDaysStr = request.getParameter("validDays");
            int validDays = 8; 
            if (validDaysStr != null) {
                try {
                    validDays = Integer.parseInt(validDaysStr);
                } catch (NumberFormatException e) {
                    validDays = 8;
                }
            }
            
            String newGameId = "88888888"; 
            
            game = new BingoGame(newGameId, validDays);
            application.setAttribute("game", game);
            
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
            application.removeAttribute("game");
            application.removeAttribute("shuffledNumbers");
            
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        // ==========================================================
        // 共通チェック：部屋が存在しないとエラー
        // ==========================================================
        if (game == null) {
            request.setAttribute("error", "⚠️ 現在ビンゴゲームは開始されていないか、リセットされました。");
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        if (game.isExpired() || game.isPast2HoursFromLastBingo()) {
            application.removeAttribute("game"); 
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
                game.getDrawnNumbers().add(nextNumber);
                application.setAttribute("shuffledNumbers", shuffledNumbers);
            }
            
            request.setAttribute("game", game);
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        // ==========================================================
        // 【アクション 4】プレイヤーの参加ログイン（ここで5×5カードを生成！）
        // ==========================================================
        else if ("join".equals(action)) {
            String inputId = request.getParameter("gameId");
            String inputName = request.getParameter("playerName");
            
            if (game.getGameId().equals(inputId)) {
                String confirmedName = game.registerPlayer(inputName);
                
                // 🚀 【重要】大山さん仕様の5×5ビンゴカード（FREE付き）をここで自動生成！
                if (session.getAttribute("card") == null) {
                    List<List<String>> card = new ArrayList<>();
                    // B(1-15), I(16-30), N(31-45), G(46-60), O(61-75) のルールで数字を作ります
                    List<List<Integer>> columns = new ArrayList<>();
                    for (int i = 0; i < 5; i++) {
                        List<Integer> pool = new ArrayList<>();
                        for (int j = 1; j <= 15; j++) {
                            pool.add((i * 15) + j);
                        }
                        Collections.shuffle(pool);
                        columns.add(pool.subList(0, 5));
                    }
                    
                    // 縦列から横行の5×5の形に変換してカードを組み立て
                    for (int r = 0; r < 5; r++) {
                        List<String> row = new ArrayList<>();
                        for (int c = 0; c < 5; c++) {
                            if (r == 2 && c == 2) {
                                row.add("0"); // 真ん中はFREE（0）
                            } else {
                                row.add(String.valueOf(columns.get(c).get(r)));
                            }
                        }
                        card.add(row);
                    }
                    // セッション（記憶部屋）に「card」という名前でしっかり保存！
                    session.setAttribute("card", card);
                }
                
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
        // 【アクション 5】プレイヤーからの即時リーチ報告
        // ==========================================================
        else if ("reach".equals(action)) {
            String playerName = request.getParameter("playerName");
            if (playerName != null && !playerName.trim().isEmpty()) {
                game.addReachPlayer(playerName);
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // ==========================================================
        // 【アクション 6】プレイヤーからの即時ビンゴ報告
        // ==========================================================
        else if ("bingo".equals(action)) {
            String playerName = request.getParameter("playerName");
            if (playerName != null && !playerName.trim().isEmpty()) {
                game.addBingoPlayer(playerName); 
            }
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        // ==========================================================
        // 【定期通信用】10秒ごとのアップデート
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
