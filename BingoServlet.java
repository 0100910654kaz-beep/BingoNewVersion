package servlet;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/BingoServlet")
public class BingoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static BingoGame game;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String userType = request.getParameter("userType");

        // 1. 部屋作成
        if ("create".equals(action)) {
            String gameId = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
            game = new BingoGame(gameId);
            request.setAttribute("game", game);
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        // 2. 抽選
        if ("draw".equals(action)) {
            if (game != null) {
                game.drawNumber();
            }
            request.setAttribute("game", game);
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        // 3. 完全初期化リセット [Esc連動]
        if ("reset".equals(action)) {
            game = null;
            request.setAttribute("game", null);
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        // 4. プレイヤー新規参加
        if ("join".equals(action)) {
            String gameId = request.getParameter("gameId");
            String playerName = request.getParameter("playerName");

            if (game == null || !game.getGameId().equalsIgnoreCase(gameId)) {
                request.setAttribute("error", "指定された部屋番号（" + gameId + "）が見つかりません。");
                request.getRequestDispatcher("index.jsp").forward(request, response);
                return;
            }

            if (playerName == null || playerName.trim().isEmpty()) {
                playerName = "プレイヤー_" + (game.getPlayerCount() + 1);
            } else {
                playerName = playerName.trim();
            }

            // 初回参加時にも自動スキャンを回して状態を安定させる
            game.updatePlayerStatus(playerName);

            request.setAttribute("game", game);
            request.setAttribute("confirmedPlayerName", playerName);
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        // 5. 【大金星】低燃費自動同期フェッチ
        if ("player".equals(userType)) {
            String playerName = request.getParameter("playerName");
            if (game != null && playerName != null && !playerName.isEmpty()) {
                // 通信が届くたびに裏側で全自動12ライン判定を実行！
                game.updatePlayerStatus(playerName);
            }
            request.setAttribute("game", game);
            request.setAttribute("confirmedPlayerName", playerName);
            request.getRequestDispatcher("index.jsp").forward(request, response);
            return;
        }

        // デフォルトは管理画面へ
        request.setAttribute("game", game);
        request.getRequestDispatcher("admin.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
