<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Collections" %>
<%
    String playerName = (String) session.getAttribute("playerName");
    if (playerName == null) {
        playerName = "ゲストプレイヤー";
    }

    int[][] card = (int[][]) session.getAttribute("bingoCard");
    if (card == null) {
        card = new int[5][5];
        List<Integer> b = new ArrayList<>();
        List<Integer> iList = new ArrayList<>();
        List<Integer> n = new ArrayList<>();
        List<Integer> g = new ArrayList<>();
        List<Integer> o = new ArrayList<>();
        
        for(int i=1; i<=15; i++) b.add(i);
        for(int i=16; i<=30; i++) iList.add(i);
        for(int i=31; i<=45; i++) n.add(i);
        for(int i=46; i<=60; i++) g.add(i);
        for(int i=61; i<=75; i++) o.add(i);
        
        Collections.shuffle(b);
        Collections.shuffle(iList);
        Collections.shuffle(n);
        Collections.shuffle(g);
        Collections.shuffle(o);
        
        for(int row=0; row<5; row++) {
            card[row][0] = b.get(row);
            card[row][1] = iList.get(row);
            card[row][2] = n.get(row);
            card[row][3] = g.get(row);
            card[row][4] = o.get(row);
        }
        card[2][2] = 0; // FREE
        session.setAttribute("bingoCard", card);
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>マイスマートビンゴカード</title>
    <style>
        body { font-family: Arial, sans-serif; background-color: #f7f9fa; padding: 15px; text-align: center; margin: 0; }
        .card-container { max-width: 450px; margin: 0 auto; background: white; padding: 20px; border-radius: 15px; box-shadow: 0 4px 10px rgba(0,0,0,0.1); }
        h2 { margin: 5px 0; color: #333; }
        .grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 8px; margin: 15px 0; }
        .cell { aspect-ratio: 1; background: #e9ecef; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-size: 22px; font-weight: bold; color: #333; cursor: pointer; border: 2px solid #dee2e6; }
        .cell.hit { background: #ff6b6b !important; color: white !important; border-color: #fa5252; text-shadow: 1px 1px 2px rgba(0,0,0,0.2); }
        .cell.free { background: #ffe3e3; color: #ff6b6b; font-size: 14px; }
        .btn-box { display: flex; justify-content: space-around; margin-top: 20px; gap: 10px; }
        .btn { flex: 1; padding: 12px; font-size: 18px; font-weight: bold; color: white; border: none; border-radius: 8px; cursor: pointer; }
        .btn-reach { background: #fcc419; color: #333; }
        .btn-bingo { background: #37b24d; }
    </style>
    <script>
        let hitNumbers = new Set([0]);

        function checkStatus() {
            fetch('BingoServlet?action=status')
                .then(res => res.json())
                .then(data => {
                    if (data.started && data.drawnNumbers) {
                        data.drawnNumbers.forEach(num => hitNumbers.add(num));
                        updateCardVisuals();
                    }
                });
        }

        function updateCardVisuals() {
            document.querySelectorAll('.cell').forEach(cell => {
                let num = parseInt(cell.getAttribute('data-num'));
                if (hitNumbers.has(num)) {
                    cell.classList.add('hit');
                }
            });
        }

        window.onload = () => {
            checkStatus();
            setInterval(checkStatus, 3000);
        };
    </script>
</head>
<body>
<div class="card-container">
    <h2><%= playerName %> さんのカード</h2>
    <div class="grid">
        <% for(int r=0; r<5; r++) {
            for(int c=0; c<5; c++) {
                int val = card[r][c];
                if (r==2 && c==2) { %>
                    <div class="cell free hit" data-num="0">FREE</div>
                <% } else { %>
                    <div class="cell" data-num="<%= val %>"><%= val %></div>
                <% }
            }
        } %>
    </div>
    <div class="btn-box">
        <form action="BingoServlet" method="post" style="flex:1;">
            <input type="hidden" name="action" value="reach">
            <input type="hidden" name="playerName" value="<%= playerName %>">
            <button type="submit" class="btn btn-reach">🔥 リーチ！</button>
        </form>
        <form action="BingoServlet" method="post" style="flex:1;">
            <input type="hidden" name="action" value="bingo">
            <input type="hidden" name="playerName" value="<%= playerName %>">
            <button type="submit" class="btn btn-bingo">🏆 ビンゴ！！</button>
        </form>
    </div>
</div>
</body>
</html>
