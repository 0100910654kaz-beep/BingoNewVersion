<script>
        let screenWindow = null;

        // 🚀【改善】「次の数字を引く」を押した瞬間に大画面を強制同期する仕掛け
        function triggerDraw(event) {
            // 通常の画面遷移（リロード）を一度止め、裏で確実に大画面を書き換えてから遷移させる
            if (screenWindow && !screenWindow.closed) {
                // ボタンが押された直後に、現在の大画面の「待機中」や「前の数字」をフェードアウトさせるなど、
                // 司会者画面が切り替わる一瞬の隙間も大画面を即時連動させる安全弁
                setTimeout(updateProjectorData, 50); 
            }
        }

        window.addEventListener("keydown", function(event) {
            if (event.key === "Enter") {
                let drawBtn = document.getElementById("drawButton");
                if (drawBtn) { 
                    event.preventDefault(); 
                    triggerDraw(); // 大画面連動のトリガーを引く
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

        // 📺 3列並び特大プロジェクター大画面の構築
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

        // 親から子画面にデータを送り込む（0秒即時連動対応）
        function updateProjectorData() {
            if (screenWindow && !screenWindow.closed) {
                let pNum = screenWindow.document.getElementById('p-num');
                let pGrid = screenWindow.document.getElementById('p-grid');
                let pList = screenWindow.document.getElementById('bingoList'); // IDのズレを完璧に修正
                let pReach = screenWindow.document.getElementById('adminReachList'); // IDのズレを完璧に修正
                let pBall = screenWindow.document.getElementById('p-ball');

                if(pNum && document.querySelector('.big-number')) pNum.innerText = document.querySelector('.big-number').innerText;
                if(pBall && document.getElementById('adminBallCounter')) pBall.innerText = document.getElementById('adminBallCounter').innerText;
                if(pGrid && document.querySelector('.history-grid')) pGrid.innerHTML = document.querySelector('.history-grid').innerHTML.replace(/history-cell/g, 'cell').replace(/newest/g, 'new');
                if(pList && document.getElementById('bingoList')) pList.innerHTML = document.getElementById('bingoList').innerHTML;
                if(pReach && document.getElementById('adminReachList')) pReach.innerHTML = document.getElementById('adminReachList').innerHTML;
            }
        }

        // 5秒に1回のバックグラウンド非同期同期（これでダブルセーフティ）
        setInterval(function() {
            fetch('BingoServlet?userType=admin')
                .then(response => response.text())
                .then(html => {
                    let parser = new DOMParser();
                    let doc = parser.parseFromString(html, 'text/html');
                    if(doc.querySelector('.admin-container')) {
                        document.querySelector('.admin-container').innerHTML = doc.querySelector('.admin-container').innerHTML;
                        updateProjectorData(); // データの書き換え直後に大画面へパス
                    }
                }).catch(e => console.log("同期エラー:", e));
        }, 5000);

        // 🚀【重要】JSPがロードされた瞬間（ボタンを押してリロードされた直後）に大画面を即座に最新にする
        window.addEventListener("DOMContentLoaded", function() {
            updateProjectorData();
        });
    </script>
