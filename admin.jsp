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
        .btn-screen { background-color: #4a90e2; font-size: 16px; padding: 10px 20px; border: none; }
        
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

        window.addEventListener("keydown", function(event) {
            if (event.key === "Enter") {
                let drawBtn = document.getElementById("drawButton");
                if (drawBtn) { event.preventDefault(); drawBtn.click(); }
            }
            if (event.key === "Escape") { event.preventDefault(); confirmReset(); }
        });

        function confirmReset() {
            if (confirm("⚠️ 本当にビンゴゲームをリセットしますか？")) {
                window.location.href = "BingoServlet?action=reset";
            }
        }

        // 🚀【大画面劇的リニューアル】左右分割で絶対にかぶらない配置構造
        function openProjectorScreen() {
            screenWindow = window.open("", "BingoProjector", "width=1280,height=800,top=50,left=50,resizable=yes");
            
            let htmlContent = '<html><head><title>ビンゴ中継大画面</title>' +
            '<style>' +
            'body { font-family: Arial, sans-serif; background-color: #111; color: white; padding: 20px; margin: 0; box-sizing: border-box; overflow:hidden; }' +
            '.title { font-size: 38px; color: #ff6b6b; font-weight: bold; text-align: center; margin-bottom: 15px; letter-spacing: 4px; height: 50px; }' +
            '.main-layout { display: flex; width: 100%; height: calc(100vh - 90px); gap: 20px; }' +
            '.left-side { flex: 1.3; display: flex; flex-direction: column; background: #1a1a1a; padding: 20px; border-radius: 15px; box-sizing: border-box; }' +
            '.right-side { flex: 1; background: #222; padding: 20px; border-radius: 15px; box-sizing: border-box; display: flex; flex-direction: column; gap: 20px; overflow-y: auto; }' +
            '.center-box { text-align: center; margin-bottom: 15px; }' +
            '.num-display { font-size: 150px; font-weight: bold; color: #fff; background: #ff6b6b; padding: 10px 80px; border-radius: 25px; display:inline-block; line-height:1.1; box-shadow: 0 0 25px rgba(255,107,107,0.5); }' +
            '.grid { display: grid; grid-template-columns: repeat(8, 1fr); gap: 10px; overflow-y: auto; flex: 1; padding-right: 5px; }' +
            '.cell { background: #333; padding: 12px 0; font-size: 24px; font-weight: bold; border-radius: 6px; color: #aaa; text-align:center; }' +
            '.cell.new { background: #ff6b6b; color: white; font-size: 32px; box-shadow: 0 0 20px #ff6b6b; animation: scaleUp 0.4s ease-out; }' +
            '.sub-title { font-size: 24px; font-weight: bold; color: #ffb74d; border-bottom: 2px solid #444; padding-bottom: 5px; margin: 0 0 10px 0; }' +
            '.winner-box { background: #2d2d2d; padding: 15px; border-radius: 10px; border-left: 6px solid #e63946; flex: 1; overflow-y: auto; }' +
            '.reach-box { background: #2d2d2d; padding: 15px; border-radius: 10px; border-left: 6px solid #ff9800; flex: 1; overflow-y: auto; }' +
            'ul { padding-left: 20px; margin: 0; font-size: 22px; line-height: 1.6; }' +
            'li { margin-bottom: 8px; }' +
            '@keyframes scaleUp { from { transform:scale(0.6); } to { transform:scale(1); } }' +
            '</style></head><body>' +
            '<div class="title">🎉 ビンゴ大会 抽選生中継 🎉</div>' +
            '<div class="main-layout">' +
            '    <div class="left-side">' +
            '        <div class="center-box">' +
            '            <div style="font-size:22px; color:#aaa; margin-bottom:5px;">現在の当選番号</div>' +
            '            <div class="num-display" id="p-num">---</div>' +
            '            <div id="p-ball" style="font-size:22px; margin-top:5px; color:#ffb74d; font-weight:bold;"></div>' +
            '        </div>' +
            '        <div style="font-size:22px; margin-bottom:10px; color:#aaa;">📊 出た数字の履歴</div>' +
            '        <div class="grid" id="p-grid"></div>' +
            '    </div>' +
            '    <div class="right-side">' +
            '        <div class="winner-box">' +
            '            <div class="sub-title">🏆 ビンゴ達成者一覧</div>' +
            '            <ul id="p-list"></ul>' +
            '        </div>' +
            '        <div class="reach-box">' +
            '            <div class="sub-title">🔥 リーチの人</div>' +
            '            <ul id="p-reach"></ul>' +
            '        </div>' +
            '    </div>' +
            '</div>' +
            '</body></html>';
            
            if (screenWindow.document.getElementById('p-num') === null) {
                screenWindow.document.open();
                screenWindow.document.write(htmlContent);
                screenWindow.document.close();
            }
            updateProjectorData(); 
        }

        function updateProjectorData() {
            if (screenWindow && !screenWindow.closed) {
                try {
                    let pNum = screenWindow.document.getElementById('p-num');
                    let pGrid = screenWindow.document.getElementById('p-grid');
                    let pList = screenWindow.document.getElementById('p-list');
                    let pReach = screenWindow.document.getElementById('p-reach');
                    let pBall = screenWindow.document.getElementById('p-ball');

                    if(pNum) pNum.innerText = document.querySelector('.big-number').innerText;
                    if(pBall) pBall.innerText = document.getElementById('adminBallCounter').innerText;
                    if(pGrid) pGrid.innerHTML = document.querySelector('.history-grid').innerHTML.replace(/history-cell/g, 'cell').replace(/newest/g, 'new');
                    if(pList) pList.innerHTML = document.getElementById('bingoList').innerHTML;
                    if(pReach) pReach.innerHTML = document.getElementById('adminReachList').innerHTML;
                } catch(e) {
                    console.log("同期待機中...");
                }
            }
        }

        setInterval(function() {
            fetch('BingoServlet?userType=admin')
                .then(response => response.text())
                .then(html => {
                    let parser = new DOMParser();
                    let
