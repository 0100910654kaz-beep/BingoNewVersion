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



        // 司会者による部屋作成

        if ("create".equals(action)) {

            String gameId = UUID.randomUUID().toString().substring(0, 6);

            game = new BingoGame(gameId);

            request.setAttribute("game", game);

            request.getRequestDispatcher("admin.jsp").forward(request, response);

            return;

        }



        // 司会者による抽選

        if ("draw".equals(action)) {

            if (game != null) {

                game.drawNumber();

            }

            request.setAttribute("game", game);

            request.getRequestDispatcher("admin.jsp").forward(request, response);

            return;

        }



        // プレイヤーの新規参加処理

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



            request.setAttribute("game", game);

            request.setAttribute("confirmedPlayerName", playerName);

            request.getRequestDispatcher("index.jsp").forward(request, response);

            return;

        }



        // 【低燃費モード】非同期での状態更新

        if ("player".equals(userType)) {

            String playerName = request.getParameter("playerName");

            request.setAttribute("game", game);

            request.setAttribute("confirmedPlayerName", playerName);

            request.getRequestDispatcher("index.jsp").forward(request, response);

            return;

        }



        // リーチ・ビンゴのJavaScriptフェッチ処理

        if ("reach".equals(action) || "bingo".equals(action)) {

            String playerName = request.getParameter("playerName");

            if (game != null && playerName != null && !playerName.isEmpty()) {

                if ("reach".equals(action)) {

                    game.registerReachPlayer(playerName);

                } else {

                    game.registerBingoPlayer(playerName);

                }

                response.setStatus(HttpServletResponse.SC_OK);

            } else {

                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);

            }

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

