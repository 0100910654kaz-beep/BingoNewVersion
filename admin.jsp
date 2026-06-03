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
    int ballCount = 0;
    if (game != null) {
        reverseDrawnNumbers.addAll(game.getDrawnNumbers());
        ballCount = reverseDrawnNumbers.size();
        Collections.reverse(reverseDrawnNumbers);
    }
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
        .big-number { font-size: 80px; font-weight: bold; color: #ff6b6b; background: #ffe3e3; display: inline-block; padding: 10px 50px; border-radius: 15px; margin: 15px 0; border: 3px solid #ff6b6b; }
        .control-box { margin: 20px 0; }
        .btn { display: inline-block; padding: 14px 28px; font-size: 20px; font-weight: bold; color: white; border: none; border-radius: 6px; cursor: pointer; margin: 10px; text-decoration: none; }
        .btn-draw { background-color: #2b8a3e; box-shadow: 0 4px #1e622b; }
        .btn-draw:active { transform: translateY(4px); box-shadow: none; }
        .btn-reset { background-color: #e63946; font-size: 16px; padding: 10px 20px; }
        .btn-screen { background-color: #4a90e2; font-size: 16px; padding: 10px 20px; }
        
        .flex-box { display: flex; justify-content: space-between; margin-top: 30px; gap: 20px; }
        .panel { flex: 1; background: #f9f9f9; padding: 15px; border-radius: 8px; text-align: left; box-shadow: inset 0 0 5px rgba(0,0,0,0.05); }
        .panel h3 { margin-top: 0; color: #2b3a42; border-bottom: 2px solid #ddd; padding-bottom: 5px; }
        
        .history-grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 8px; margin-top: 10px; }
        .history-cell { background: #ddd; padding: 8px; font-size: 16px; font-weight: bold; border-radius: 4px; text-align: center; color: #444; }
        .history-cell.newest { background: #ff6b6b; color: white; animation: blink 0.8s infinite alternate; }
        @keyframes blink { from { opacity: 1; } to { opacity: 0.7; } }
        ul { padding-left: 20px; }
        li { margin-bottom: 8px; font-size: 16px; }
    </style>

    <script>
        let screenWindow = null;

        // 🚀「次の数字を引く」クリック／Enter時に大画面を即座に最新にするトリガー
        function triggerDraw() {
            if (screenWindow && !screenWindow.closed) {
                setTimeout(updateProjectorData, 50);
            }
        }

        window.addEventListener("keydown", function(event) {
            if (event.key === "Enter") {
                let drawBtn = document.getElementById("drawButton");
                if (drawBtn) { 
                    event.preventDefault(); 
                    triggerDraw(); 
                    drawBtn.click(); 
                }
            }
            if (event.key === "Escape") { event.preventDefault(); confirmReset(); }
        });

        function confirmReset() {
            if (confirm("⚠️ 本当にビンゴゲームをリセットしますか？\n(プレイヤーのカードも含めすべての部屋データが消去されます)")) {
                window.location.href = "BingoServlet?action=reset";
            }
        }

        // 📺 3列プロジェクター大画面の生成
        function openProjectorScreen() {
            screenWindow = window.open("", "BingoProjector", "width=1300,height=800,top=50,left=50,resizable=yes");
            
            let htmlContent = '<html><head><title>ビンゴ中継大画面</title>' +
            '<style>' +
            'body { font-family: Arial, sans-serif; background-color: #111; color: white; padding: 20px; margin: 0; overflow-x: hidden; }' +
            '.header { text-align: center; font-size: 44px; color: #ffeb3b; font-weight: bold; margin-bottom: 25px; text-shadow: 0 0 10px rgba(255,235,59,0.5); letter-spacing: 2px; }' +
            '.main-layout { display: flex; justify-content: space-between; gap: 20px; align-items: stretch; max-width: 1400px; margin: 0 auto; height: calc(100vh - 120px); }' +
            '.left-col { flex: 1.1; background: #222; border-radius: 15px; padding: 20px; display: flex; flex-direction: column; justify-content: center; align-items: center; border: 2px solid #333; box-shadow: 0 8px 16px rgba(0,0,0,0.5); }' +
            '.num-title { font-size: 32px; color: #bbb; font-weight: bold; margin-bottom: 10px; }' +
            '.num-display { font-size: 190px; font-weight: bold; color: #fff; background: radial-gradient(circle, #ff6b6b 0%, #e63946 100%); padding: 30px 60px; border-radius: 25px; line-height: 1; box-shadow: 0 0 30px rgba(230,57,70,0.6); border: 4px solid #fff; min-width: 220px; text-align: center; }' +
            '.ball-counter { font-size: 26px; color: #ffb74d; margin-top: 20px; font-weight: bold; background: #333; padding: 8px 20px; border-radius: 20px; }' +
            '.center-col { flex: 1.5; background: #1a1a1a; border-radius: 15px; padding: 20px; border: 2px solid #333; display: flex; flex-direction: column; box-shadow: 0 8px 16px rgba(0,0,0,0.5); }' +
            '.col-title { font-size: 26px; color: #4fc3f7; font-weight: bold; margin-bottom: 15px; border-bottom: 3px solid #4fc3f7; padding-bottom: 5px; text-align: left; }' +
            '.grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 10px; overflow-y: auto; padding-right: 5px; }' +
            '.cell { background: #333; padding: 12px 0; font-size: 24px; font-weight: bold; border-radius: 8px; color: #888; text-align: center; border: 1px solid #444; }' +
            '.cell.new { background: #ff6b6b; color: white; font-size: 32px; box-shadow: 0 0 15px #ff6b6b; border: 2px solid #fff; animation: pulse 0.5s infinite alternate; }' +
            '@keyframes pulse { from { transform: scale(1); } to { transform: scale(1.03); } }' +
            '.right-col { flex: 1.3; background: #222; border-radius: 15px; padding: 20px; border: 2px solid #333; display: flex; flex-direction: column; gap: 20px; box-shadow: 0 8px 16px rgba(0,0,0,0.5); overflow-y: auto; text-align: left; }' +
            '.section-box { background: #1a1a1a; padding: 15px; border-radius: 10px; border-left: 6px solid #e91e63; }' +
            '.section-box.reach-box { border-left-color: #ff9800; }' +
            '.right-title { font-size: 24px; color: #fff; font-weight: bold; margin: 0 0 10px 0; }' +
            'ul { padding-left: 20px; margin: 0; color: #ddd; font-size: 19px; line-height: 1.6; }' +
            'li { margin-bottom: 8px; }' +
            '</style></head><body>' +
            
            '<div class="header">🎉 ビンゴ大会 抽選生中継 🎉</div>' +
            '<div class="main-layout">' +
                '<div class="left-col">' +
                    '<div class="num-title">現在の当選番号</div>' +
                    '<div class="num-display" id="p-num">待機中</div>' +
                    '<div class="ball-counter" id="p-ball">0 / 75 球</div>' +
                '</div>' +
                '<div class="center-col">' +
                    '<div class="col-title">📊 出た数字の履歴（最新が左上）</div>' +
                    '<div class="grid" id="p-grid"></div>' +
                '</div>' +
                '<div class="right-col">' +
                    '<div class="section-box">' +
                        '<div class="right-title" style="color:#e91e63;">🏆 ビンゴ達成者一覧</div>' +
                        '<ul id="bingoList"></ul>' +
                    '</div>' +
                    '<div class="section-box reach-box">' +
                        '<div class="right-title" style="color:#ff9800;">🔥 リーチ（全自動検知）</div>' +
                        '<ul id="adminReachList"></ul>' +
                    '</div>' +
                '</div>' +
            '</div>' +
            '</body></html>';
            
            screenWindow.document.open();
            screenWindow.document.write(htmlContent);
            screenWindow.document.close();
            updateProjectorData(); 
        }

        // 親画面のデータを子画面に同期するロジック
        function updateProjectorData() {
            if (screenWindow && !screenWindow.closed) {
                let pNum = screenWindow.document.getElementById('p-num');
                let pGrid = screenWindow.document.getElementById('p-grid');
                let pList = screenWindow.document.getElementById('bingoList'); 
                let pReach = screenWindow.document.getElementById('adminReachList'); 
                let pBall = screenWindow.document.getElementById('p-ball');

                if(pNum && document.querySelector('.big-number')) pNum.innerText = document.querySelector('.big-number').innerText;
                if(pBall && document.getElementById('adminBallCounter')) pBall.innerText = document.getElementById('adminBallCounter').innerText;
                if(pGrid && document.querySelector('.history-grid')) pGrid.innerHTML = document.querySelector('.history-grid').innerHTML.replace(/history-cell/g, 'cell').replace(/newest/g, 'new');
                if(pList && document.getElementById('bingoList')) pList.innerHTML = document.getElementById('bingoList').innerHTML;
                if(pReach && document.getElementById('adminReachList')) pReach.innerHTML = document.getElementById('adminReachList').innerHTML;
            }
        }

        // 5秒おきの非同期バックグラウンド同期
        setInterval(function() {
            fetch('BingoServlet?userType=admin')
                .then(response => response.text())
                .then(html => {
                    let parser = new DOMParser();
                    let doc = parser.parseFromString(html, 'text/html');
                    if(doc.querySelector('.admin-container')) {
                        document.querySelector('.admin-container').innerHTML = doc.querySelector('.admin-container').innerHTML;
                        updateProjectorData(); 
                    }
                }).catch(e => console.log("同期エラー:", e));
        }, 5000);

        // ロード完了時に即時同期
        window.addEventListener("DOMContentLoaded", function() {
            updateProjectorData();
        });
    </script>
</head>
<body>

<div class="admin-container">
    <div style="display: flex; justify-content: space-between; align-items: center;">
        <button type="button" class="btn btn-reset" onclick="confirmReset()">🔄 リセット [Esc]</button>
        <h1>🎤 司会者コントロール画面 🎤</h1>
        <button type="button" class="btn btn-screen" onclick="openProjectorScreen()">📺 大画面を開く</button>
    </div>

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
        <div class="big-number"><%= game.getDrawnNumbers().isEmpty() ? "待機中" : game.getDrawnNumbers().get(game.getDrawnNumbers().size() - 1) %></div>

        <div id="adminBallCounter" style="font-size:18px; font-weight:bold; color:#555; margin-bottom:10px;">
            現在の玉数: <%= ballCount %> / 75 球
        </div>

        <div class="control-box">
            <a href="BingoServlet?action=draw" id="drawButton" class="btn btn-draw" onclick="triggerDraw()">🎲 次の数字を引く [Enter]</a>
        </div>

        <div class="flex-box">
            <div class="panel" style="flex: 1.4;">
                <h3>📊 出た数字の履歴（最新が左上）</h3>
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
                <ul id="bingoList">
                    <% 
                    int rank = 1;
                    if (game.getBingoPlayers() != null) {
                        for (PlayerResult p : game.getBingoPlayers()) { 
                    %>
                        <li><strong><%= rank %>位</strong>: <%= p.getPlayerName() %> さん <span style="color:#e63946; font-weight:bold;">(🔑<%= p.getDrawnNumberAtBingo() %>番でビンゴ!)</span></li>
                    <% 
                        rank++;
                        }
                    } 
                    if (game.getBingoPlayers() == null || game.getBingoPlayers().isEmpty()) { 
                    %> 
                        <p style="color:#888;">まだビンゴした人はいません</p> 
                    <% 
                    } 
                    %>
                </ul>

                <h3 style="margin-top: 25px;">🔥 リーチの人（全自動検知）</h3>
                <ul id="adminReachList">
                    <% 
                    if (game.getReachPlayers() != null) {
                        for (PlayerResult p : game.getReachPlayers()) { 
                    %>
                        <li><strong><%= p.getPlayerName() %> さん</strong></li>
                    <% 
                        }
                    } 
                    if (game.getReachPlayers() == null || game.getReachPlayers().isEmpty()) { 
                    %> 
                        <p style="color:#888;">まだリーチの人はいません</p> 
                    <% 
                    } 
                    %>
                </ul>
            </div>
        </div>
    <% } %>
</div>
</body>
</html>
