<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="servlet.BingoGame" %>
<%@ page import="servlet.PlayerResult" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%
    BingoGame game = (BingoGame) request.getAttribute("game");
    String gameId = (game != null) ? game.getGameId() : "まだ開始していません";

    List<Integer> reverseDrawnNumbers = new ArrayList<>();
    int totalDrawn = 0;
    if (game != null) {
        reverseDrawnNumbers.addAll(game.getDrawnNumbers());
        totalDrawn = reverseDrawnNumbers.size();
        Collections.reverse(reverseDrawnNumbers);
    }
    int remainingBalls = 75 - totalDrawn;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ビンゴ大会 - 司会者画面</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #eef2f3; padding: 20px; text-align: center; }
        .admin-container { max-width: 800px; margin: 0 auto; background: white; padding: 30px; border-radius: 12px; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
        h1 { color: #2b3a42; margin-bottom: 20px; }
        .info-panel { background: #f0f4f8; padding: 15px; border-radius: 8px; margin-bottom: 25px; font-size: 18px; }
        .big-number { font-size: 80px; font-weight: bold; color: #ff6b6b; background: #ffe3e3; display: inline-block; padding: 10px 50px; border-radius: 15px; margin: 15px 0; border: 3px solid #ff6b6b; min-width: 120px; }
        .control-box { margin: 20px 0; }
        .btn { display: inline-block; padding: 14px 28px; font-size: 20px; font-weight: bold; color: white; border: none; border-radius: 6px; cursor: pointer; margin: 10px; text-decoration: none; }
        .btn-draw { background-color: #2b8a3e; box-shadow: 0 4px #1e622b; border: none; }
        .btn-draw:active { transform: translateY(4px); box-shadow: none; }
        .btn-reset { background-color: #e63946; font-size: 16px; padding: 10px 20px; border: none; }
        
        .flex-box { display: flex; justify-content: space-between; margin-top: 30px; gap: 20px; }
        .panel { flex: 1; background: #f9f9f9; padding: 15px; border-radius: 8px; text-align: left; box-shadow: inset 0 0 5px rgba(0,0,0,0.05); }
        .panel h3 { margin-top: 0; color: #2b3a42; border-bottom: 2px solid #ddd; padding-bottom: 5px; }
        
        .history-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 8px; margin-top: 10px; }
        .history-cell { background: #ddd; padding: 8px; font-size: 16px; font-weight: bold; border-radius: 4px; text-align: center; color: #444; }
        .history-cell.newest { background: #ff6b6b; color: white; }
        ul { padding-left: 20px; }
        li { margin-bottom: 8px; font-size: 16px; }
    </style>
</head>
<body>

<div class="admin-container">
    <h1>🎤 司会者コントロール画面 🎤</h1>

    <% if (game == null) { %>
        <div class="info-panel" style="background: #ffe3e3;">
            <p>まだビンゴゲームの部屋が作成されていません。</p>
            <form action="BingoServlet" method="get">
                <input type="hidden" name="action" value="create">
                <button type="submit" class="btn btn-draw" style="box-shadow:none;">🚀 新規ビンゴ部屋を開始する</button>
            </form>
        </div>
    <% } else { %>
        <div class="info-panel">
            <strong>現在の管理部屋ID:</strong> <span style="color:#4a90e2; font-weight:bold;"><%= gameId %></span> &nbsp;&nbsp;|&nbsp;&nbsp;
            <strong>現在の総参加人数:</strong> <span style="color:#2b8a3e; font-weight:bold;"><%= game.getPlayerCount() %> 名</span>
        </div>

        <p style="font-size: 18px; margin-bottom: 0;">抽選された最新の数字</p>
        <div class="big-number"><%= game.getDrawnNumbers().isEmpty() ? "---" : game.getDrawnNumbers().get(game.getDrawnNumbers().size() - 1) %></div>

        <div style="font-size:18px; font-weight:bold; color:#555; margin-bottom:10px;">
            残りの玉数: <%= remainingBalls %> / 75 球
        </div>

        <div class="control-box">
            <a href="BingoServlet?action=draw" class="btn btn-draw">🎲 次の数字を引く [Enter]</a>
        </div>

        <div class="flex-box">
            <div class="panel" style="flex: 1.2;">
                <h3>📊 出た数字の履歴</h3>
                <div class="history-grid">
                    <% for (int i = 0; i < reverseDrawnNumbers.size(); i++) { 
                        int num = reverseDrawnNumbers.get(i);
                        if (i == 0) { %>
                            <div class="history-cell newest"><%= num %></div>
                        <% } else { %>
                            <div class="history-cell"><%= num %></div>
                        <% }
                    } %>
                </div>
            </div>

            <div class="panel">
                <h3>🏆 ビンゴ達成者一覧</h3>
                <ul>
                    <% 
                       List<String> rankedHTML = game.getRankedBingoListHTML();
                       for (String line : rankedHTML) { %>
                           <%= line %>
                    <% } %>
                </ul>

                <h3 style="margin-top: 25px;">🔥 リーチの人</h3>
                <ul>
                    <% for (PlayerResult p : game.getReachPlayers()) { %>
                        <li><strong><%= p.getPlayerName() %> さん</strong></li>
                    <% } %>
                </ul>
            </div>
        </div>
    <% } %>
</div>
</body>
</html>
