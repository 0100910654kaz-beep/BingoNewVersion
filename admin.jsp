<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="servlet.BingoGame" %>
<%@ page import="servlet.PlayerResult" %>
<%
    // サーブレット側から渡されたゲームオブジェクトを取得
    BingoGame game = (BingoGame) request.getAttribute("game");
    String gameId = (game != null) ? game.getGameId() : "";
    
    // 残り個数・出た個数の計算
    int drawnCount = (game != null) ? game.getDrawnNumbers().size() : 0;
    int remainingCount = 75 - drawnCount;
    // 現在のリアルタイム全参加人数
    int playerCount = (game != null) ? game.getPlayerCount() : 0;
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>ビンゴ大会 - 司会者管理画面 (完全製品版)</title>
    <style>
        body { font-family: Arial, sans-serif; text-align: center; background-color: #f4f7f6; padding: 20px; }
        .container { max-width: 700px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 0 15px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; margin-bottom: 5px; }
        .setup-box { background: #e3fafc; padding: 20px; border-radius: 8px; margin-bottom: 20px; border: 1px solid #99e9f2; }
        .game-box { background: #fff; border: 2px dashed #228be6; padding: 20px; border-radius: 8px; margin-bottom: 20px; }
        
        /* ステータス表示（上部） */
        .status-container { display: flex; justify-content: space-between; margin-bottom: 15px; }
        .counter-box { font-size: 20px; font-weight: bold; color: #2b8a3e; background: #ebfbee; padding: 10px; border-radius: 5px; border: 1px solid #b2f2bb; width: 48%; }
        .player-box { font-size: 20px; font-weight: bold; color: #1c7ed6; background: #e7f5ff; padding: 10px; border-radius: 5px; border: 1px solid #a5d8ff; width: 48%; }
        
        /* プロジェクター・大画面用の超巨大表示エリア */
        .screen-box { background: #1a1a1a; color: #fff; padding: 30px; border-radius: 10px; margin: 20px 0; box-shadow: inset 0 0 20px rgba(0,0,0,0.8); }
        .screen-title { color: #fab005; font-size: 18px; font-weight: bold; margin-bottom: 5px; letter-spacing: 2px; }
        .big-number { font-size: 110px; font-weight: bold; color: #ff3e3e; text-shadow: 0 0 15px rgba(255,62,62,0.6); margin: 10px 0; line-height: 1; }
        
        /* 過去の数字を見やすく並べるグリッド配置 */
        .history-grid { display: flex; flex-wrap: wrap; justify-content: center; gap: 8px; background: #2a2a2a; padding: 15px; border-radius: 5px; max-height: 120px; overflow-y: auto; }
        .history-num { background: #444; color: #fff; font-size: 16px; font-weight: bold; width: 35px; height: 35px; display: flex; align-items: center; justify-content: center; border-radius: 50%; }
        .history-latest { background: #ff3e3e !important; color: white; animation: blink 1s infinite; }
        
        .btn { display: inline-block; padding: 12px 30px; font-size: 18px; font-weight: bold; color: white; border: none; border-radius: 5px; cursor: pointer; text-decoration: none; margin: 5px; }
        .btn-create { background-color: #228be6; }
        .btn-draw { background-color: #fab005; color: #2c3e50; font-size: 24px; width: 85%; box-shadow: 0 4px 0 #cb9102; }
        .btn-draw:active { transform: translateY(4px); box-shadow: none; }
        .btn-reset { background-color: #fa5252; font-size: 14px; padding: 8px 16px; }
        .select-style { padding: 10px; font-size: 16px; border-radius: 5px; margin-right: 10px; }
        .info-text { font-size: 18px; color: #495057; line-height: 1.6; }
        .rank-list { text-align: left; background: #f8f9fa; padding: 15px; border-radius: 5px; margin-top: 20px; }
        
        @keyframes blink { 0% { opacity: 1; } 50% { opacity: 0.4; } 100% { opacity: 1; } }
    </style>

    <script>
        // 【F5キー＆リセット連動の魔法・完全版】
        function executeReset() {
            if (confirm("本当にゲームをリセットして、新しい部屋を作り直しますか？\n（現在のデータはすべて消去され、プレイヤーの同期も再開可能になります）")) {
                // サーブレットのリセットアクションへ正しく誘導
                window.location.href = "BingoServlet?action=reset";
            }
        }

        window.addEventListener('keydown', function(e) {
            if (e.key === 'F5') {
                e.preventDefault(); 
                executeReset();     
            }
        });
    </script>
</head>
<body>
<div class="container">
    <h1>👑 ビンゴ大会 司会者管理画面</h1>
    
    <% if (game != null) { %>
        <div class="status-container">
            <div class="counter-box">🎲 残り数字： <%= remainingCount %>個 / 75個</div>
            <div class="player-box">👥 現在の全参加者： <%= playerCount %> 名</div>
        </div>
    <% } %>
    
    <hr>

    <% if (game == null) { %>
        <div class="setup-box">
            <h3>🚀 新しいビンゴゲーム（部屋）を作成する</h3>
            <p class="info-text">有効期限（ロックタイマー）を選択してください。</p>
            <form action="BingoServlet" method="get">
                <input type="hidden" name="action" value="create">
                <select name="validDays" class="select-style">
                    <option value="3">お試しモード（3日間有効）</option>
                    <option value="8">通常製品版（8日間有効）</option>
                </select>
                <button type="submit" class="btn btn-create">部屋を新規作成する</button>
            </form>
        </div>
    <% } else { %>
        <div class="game-box">
            <span style="font-size: 20px; font-weight: bold; background: #ffec99; padding: 5px 10px; border-radius: 5px;">
                🔑 部屋番号 (ゲームID): <%= gameId %>
            </span>
            <p class="info-text" style="margin-top: 10px; font-size: 14px; color: #868e96;">
                ⏰ 有効期限: <%= game.getExpireTime() %> まで自動ロックタイマー作動中
            </p>
            
            <p style="margin-top: 20px; font-weight: bold; color: #495057;">👇 ボタンを押すとランダムに数字を1つ引きます</p>
            <form action="BingoServlet" method="get">
                <input type="hidden" name="action" value="draw">
                <button type="submit" class="btn btn-draw">🎲 次の数字を引く！</button>
            </form>
            
            <div style="text-align: right; margin-top: 25px;">
                <button type="button" class="btn btn-reset" onclick="executeReset()">⚠️ ゲームをリセット (F5)</button>
            </div>
        </div>

        <div class="screen-box">
            <div class="screen-title">📺 最新の当選番号 📺</div>
            <div class="big-number">
                <%= game.getDrawnNumbers().isEmpty() ? "READY" : game.getDrawnNumbers().get(game.getDrawnNumbers().size() - 1) %>
            </div>
            
            <div style="color: #ced4da; font-size: 14px; margin: 15px 0 5px 0; text-align: left; font-weight: bold;">📜 過去に出た数字（履歴）:</div>
            <div class="history-grid">
                <% if(game.getDrawnNumbers().isEmpty()) { %>
                    <span style="color:#868e96; font-size:14px;">まだ数字は引かれていません</span>
                <% } else { %>
                    <% 
                       // 最新が目立つように登録順に表示
                       for (int i = 0; i < game.getDrawnNumbers().size(); i++) { 
                        int num = game.getDrawnNumbers().get(i);
                        boolean isLatest = (i == game.getDrawnNumbers().size() - 1);
                    %>
                        <div class="history-num <%= isLatest ? "history-latest" : "" %>">
                            <%= num %>
                        </div>
                    <% } %>
                <% } %>
            </div>
        </div>

        <div class="rank-list">
            <h3>📊 プレイヤー側の状況確認</h3>
            <strong>🏆 ビンゴ達成者（最新が一番上）:</strong>
            <ul>
                <% for (PlayerResult p : game.getBingoPlayers()) { %>
                    <li style="font-size: 18px; margin-bottom: 5px;">
                        <strong><%= game.getPlayerRank(p.getPlayerName()) %>位</strong>: <%= p.getPlayerName() %> さん 
                        <span style="font-size: 14px; color: #2b8a3e; font-weight: bold;">（<%= p.getDrawnNumberAtBingo() == 0 ? "初期" : p.getDrawnNumberAtBingo() %>番でビンゴ!）</span>
                        <span style="font-size: 12px; color: #868e96;">(<%= p.getReachedTime() %> 確定)</span>
                    </li>
                <% } %>
            </ul>

            <strong>🔥 リーチしている人一覧:</strong>
            <p style="font-size: 16px; color: #e67e22;">
                <% for (PlayerResult p : game.getReachPlayers()) { %>
                    [<%= p.getPlayerName() %>さん] 
                <% } %>
            </p>
            
            <div style="text-align: center; margin-top: 20px;">
                <a href="BingoServlet?userType=admin" style="font-size: 14px; color: #228be6;">🔄 画面を手動更新して最新状態にする</a>
            </div>
        </div>
    <% } %>
</div>
</body>
</html>