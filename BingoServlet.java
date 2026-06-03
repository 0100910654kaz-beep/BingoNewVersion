package servlet;

import java.io.IOException;
import java.util.UUID;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/BingoServlet")
public class BingoServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static BingoGame game;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if ("create".equals(action)) {
            String gameId = UUID.randomUUID().toString().substring(0, 6);
            game = new BingoGame(gameId);
            request.setAttribute("game", game);
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        if ("draw".equals(action)) {
            if (game != null) {
                game.drawNumber();
            }
            request.setAttribute("game", game);
            request.getRequestDispatcher("admin.jsp").forward(request, response);
            return;
        }

        if ("status".equals(action)) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            if (game == null) {
                response.getWriter().write("{\"started\":false}");
            } else {
                response.getWriter().write("{\"started\":true,\"drawnNumbers\":" + game.getDrawnNumbers().toString() + "}");
            }
            return;
        }

        request.setAttribute("game", game);
        request.getRequestDispatcher("admin.jsp").forward(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        String playerName = request.getParameter("playerName");

        if (game != null && playerName != null && !playerName.trim().isEmpty()) {
            if ("reach".equals(action)) {
                game.registerReachPlayer(playerName);
            } else if ("bingo".equals(action)) {
                game.registerBingoPlayer(playerName);
            }
        }
        response.sendRedirect("player.jsp");
    }
}
