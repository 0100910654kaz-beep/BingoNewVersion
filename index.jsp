<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="servlet.BingoGame" %>
<%@ page import="servlet.PlayerResult" %>
<%
    BingoGame game = (BingoGame) request.getAttribute("game");
    String error = (String) request.getAttribute("error");
    String gameId = (game != null) ? game.getGameId() : "";
    
    // サーブレットで確定した（自動命名含む）プレイヤー名を取得
    String playerName = (String) request.getAttribute("confirmedPlayerName");
    if (playerName == null) {
        playerName = request.getParameter("playerName");
    }
    if (playerName == null) {
        playerName = "";
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>ビンゴ大会 - プレイヤー画面</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; background-color: #f0f0f0; padding: 20px; }
        .container { max-width: 500px; margin: 0 auto; background: white; padding: 20px; border-radius: 10px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        h1 { color: #ff6b6b; }
        .error { color: red; font-weight: bold; margin-bottom: 20px; }
        .info-box { background: #eef2f3; padding: 10px; border-radius: 5px; margin-bottom: 20px; }
        .number-box { font-size: 48px; font-weight: bold; color: #2b2b2b; background: #ffe3e3; display: inline-block; padding: 10px 30px; border-radius: 10px; margin: 10px 0; }
        .btn { display: inline-block; padding: 12px 24px; font-size: 18px; font-weight: bold; color: white; border: none; border-radius: 5px; cursor: pointer; margin: 10px; text-decoration: none; }
        .btn-entry { background-color: #4caf50; }
        .btn-reach { background-color: #ff9800; }
        .btn-bingo { background-color: #e91e63; }
        .btn:disabled { background-color: #ccc !important; cursor: not-allowed; }
        .input-text { padding: 10px; font-size: 16px; width: 80%; max-width: 300px; margin-bottom: 10px; border: 1px solid #ccc; border-radius: 5px; text-align: center; }
        .list-box { text-align: left; background: #f9f9f9; padding: 10px; border-radius: 5px; margin-top: 20px; }
    </style>

    <% if (game != null) { %>
    <script>
        // 【大山さん大金星の低燃費モード】10秒ごとに裏側で静かに最新の数字や順位だけを問い合わせる
        function checkUpdate() {
            fetch('BingoServlet?userType=player&playerName=<%= java.net.URLEncoder.encode(playerName, "UTF-8") %>')
                .then(response => {
                    if (response.redirected) {
                        window.location.href = response.url;
                        return;
                    }
                    return response.text();
                })
                .then(html => {
                    if(!html) return;
                    let parser = new DOMParser();
                    let doc = parser.parseFromString(html, 'text/html');
                    
                    if (doc.querySelector('.error')) {
                        window.location.reload();
                        return;
                    }
                    
                    let newNumberBox = doc.querySelector('.number-box');
                    let newListBox = doc.querySelector('.list-box');
                    
                    if (newNumberBox) document.querySelector('.number-box').innerHTML = newNumberBox.innerHTML;
                    if (newListBox) document.querySelector('.list-box').innerHTML = newListBox.innerHTML;
                })
                .catch(err => console.log("通信エラー:", err));
        }

        setInterval(checkUpdate, 10000);

        // 【新機能】リーチ・ビンゴの「0秒即時送信」ギミック
        function sendAction(actionType, buttonElement) {
            // 連打によるサーバー破壊をガード（1.5秒間ボタンを無効化）
            buttonElement.disabled = true;
            setTimeout(() => { buttonElement.disabled = false; }, 1500);

            // 10秒を待たず、今この瞬間にサーバーへ電光石火で通知！
            let url = 'BingoServlet?action=' + actionType + '&playerName=' + encodeURIComponent('<%= playerName %>');
            
            fetch(url)
                .then(response => {
                    if(response.ok) {
                        // 送信が成功したら、手元の画面をすぐに最新状態に1回更新する
                        checkUpdate();
                    } else {
                        alert("通信が混み合っています。もう一度お試しください。");
                    }
                })
                .catch(err => alert("通信エラーが発生しました。"));
        }
    </script>
    <% } %>
</head>
<body>
<div class="container">
    <h1>🎉 ビンゴ大会 🎉</h1>

    <% if (error != null) { %>
        <div class="error"><%= error %></div>
    <% } %>

    <% if (game == null) { %>
        <div class="info-box">
            <p>参加する部屋の「部屋番号（ゲームID）」を入力してください。</p>
            <form action="BingoServlet" method="get">
                <input type="hidden" name="action" value="join">
                <input type="text" name="gameId" class="input-text" placeholder="8桁の部屋番号を入力" required><br>
                <input type="text" name="playerName" class="input-text" placeholder="あなたの名前（空欄でも参加可能）"><br>
                <button type="submit" class="btn btn-entry">ゲームに参加する</button>
            </form>
        </div>
    <% } else { %>
        <div class="info-box">
            <strong>部屋番号 (ID):</strong> <%= gameId %><br>
            <strong>あなたの名前:</strong> <span style="color:#2b8a3e; font-weight:bold;"><%= playerName %> さん</span>
        </div>

        <p>現在の最新の当選番号</p>
        <div class="number-box">
            <%= game.getDrawnNumbers().isEmpty() ? "待機中" : game.getDrawnNumbers().get(game.getDrawnNumbers().size() - 1) %>
        </div>

        <div style="margin-top: 20px;">
            <button type="button" class="btn btn-reach" onclick="sendAction('reach', this)">リーチ！</button>
            <button type="button" class="btn btn-bingo" onclick="sendAction('bingo', this)">ビンゴ！！</button>
        </div>

        <div class="list-box">
            <h3>📊 現在の状況</h3>
            <strong>出た数字一覧:</strong> <%= game.getDrawnNumbers() %><br><br>
            
            <strong>🏆 ビンゴ達成者（最新が上）:</strong>
            <ul>
                <% for (PlayerResult p : game.getBingoPlayers()) { %>
                    <li>
                        <strong><%= game.getPlayerRank(p.getPlayerName()) %>位</strong>: <%= p.getPlayerName() %> さん
                        <span style="font-size: 13px; color: #2b8a3e; font-weight: bold;">（<%= p.getDrawnNumberAtBingo() == 0 ? "初期" : p.getDrawnNumberAtBingo() %>番でビンゴ!）</span>
                    </li>
                <% } %>
            </ul>

            <strong>🔥 リーチの人:</strong>
            <% for (PlayerResult p : game.getReachPlayers()) { %>
                [<%= p.getPlayerName() %>さん] 
            <% } %>
        </div>
    <% } %>
</div>
</body>
</html>