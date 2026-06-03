package servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BingoGame implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameId;
    private List<Integer> drawnNumbers;
    private List<Integer> lotteryMachine;
    private List<PlayerResult> reachPlayers;
    private List<PlayerResult> bingoPlayers;
    private Map<String, List<List<String>>> playerCards;

    public BingoGame(String gameId) {
        this.gameId = gameId;
        this.drawnNumbers = new ArrayList<>();
        this.lotteryMachine = new ArrayList<>();
        this.reachPlayers = new ArrayList<>();
        this.bingoPlayers = new ArrayList<>();
        this.playerCards = new HashMap<>();

        for (int i = 1; i <= 75; i++) {
            this.lotteryMachine.add(i);
        }
        Collections.shuffle(this.lotteryMachine);
    }

    public String getGameId() {
        return gameId;
    }

    public List<Integer> getDrawnNumbers() {
        return drawnNumbers;
    }

    public int getPlayerCount() {
        return this.playerCards.size();
    }

    public int drawNumber() {
        if (lotteryMachine.isEmpty()) {
            return -1;
        }
        int num = lotteryMachine.remove(0);
        drawnNumbers.add(num);
        return num;
    }

    public List<PlayerResult> getReachPlayers() {
        return reachPlayers;
    }

    public List<PlayerResult> getBingoPlayers() {
        return bingoPlayers;
    }

    public List<List<String>> getPlayerCard(String playerName) {
        if (playerCards.containsKey(playerName)) {
            return playerCards.get(playerName);
        }

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
        
        List<List<String>> card = new ArrayList<>();
        for(int row=0; row<5; row++) {
            List<String> rowList = new ArrayList<>();
            rowList.add(String.valueOf(b.get(row)));
            rowList.add(String.valueOf(iList.get(row)));
            if (row == 2) {
                rowList.add("0"); // FREE
            } else {
                rowList.add(String.valueOf(n.get(row)));
            }
            rowList.add(String.valueOf(g.get(row)));
            rowList.add(String.valueOf(o.get(row)));
            card.add(rowList);
        }
        
        playerCards.put(playerName, card);
        return card;
    }

    // 🎯【超重要】12ライン全自動スキャン頭脳
    public void updatePlayerStatus(String playerName) {
        List<List<String>> card = getPlayerCard(playerName);
        if (card == null) return;

        // すでにビンゴしているプレイヤーは判定をスキップ
        for (PlayerResult p : bingoPlayers) {
            if (p.getPlayerName().equals(playerName)) return;
        }

        int minHitRequiredToBingo = 5; 
        int maxHitsInLine = 0;

        // 全12ラインの確認用リストを準備
        List<List<String>> lines = new ArrayList<>();

        // 1. 横5本
        for (int r = 0; r < 5; r++) {
            lines.add(card.get(r));
        }
        // 2. 縦5本
        for (int c = 0; c < 5; c++) {
            List<String> col = new ArrayList<>();
            for (int r = 0; r < 5; r++) col.add(card.get(r).get(c));
            lines.add(col);
        }
        // 3. 斜め2本
        List<String> slash1 = new ArrayList<>();
        List<String> slash2 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            slash1.add(card.get(i).get(i));
            slash2.add(card.get(i).get(4 - i));
        }
        lines.add(slash1);
        lines.add(slash2);

        // 各ラインの「穴が空いている数（ヒット数）」を調べる
        boolean isBingo = false;
        boolean isReach = false;

        for (List<String> line : lines) {
            int hits = 0;
            for (String val : line) {
                if (val.equals("0") || drawnNumbers.contains(Integer.parseInt(val))) {
                    hits++;
                }
            }
            if (hits == 5) {
                isBingo = true;
            } else if (hits == 4) {
                isReach = true;
            }
        }

        if (isBingo) {
            // リーチリストから削除し、ビンゴリストの先頭に追加
            reachPlayers.removeIf(p -> p.getPlayerName().equals(playerName));
            boolean already = false;
            for (PlayerResult p : bingoPlayers) {
                if (p.getPlayerName().equals(playerName)) already = true;
            }
            if (!already) {
                int lastNum = drawnNumbers.isEmpty() ? 0 : drawnNumbers.get(drawnNumbers.size() - 1);
                // 先頭(0番目)に挿入することで、常に最新が「上」に来る
                bingoPlayers.add(0, new PlayerResult(playerName, new Date(), lastNum));
            }
        } else if (isReach) {
            boolean already = false;
            for (PlayerResult p : reachPlayers) {
                if (p.getPlayerName().equals(playerName)) already = true;
            }
            if (!already) {
                reachPlayers.add(new PlayerResult(playerName, new Date(), 0));
            }
        } else {
            // リーチでもビンゴでもなくなったらリストから外す（リセット対応など）
            reachPlayers.removeIf(p -> p.getPlayerName().equals(playerName));
        }
    }

    // 📊【新機能】JSPのエラーを防ぐ「待ち数字」自動計算部品
    public String getWaitNumbers(String playerName) {
        List<List<String>> card = getPlayerCard(playerName);
        if (card == null) return "なし";

        List<Integer> waitNums = new ArrayList<>();
        List<List<String>> lines = new ArrayList<>();

        // 横・縦・斜めのラインを全抽出
        for (int r = 0; r < 5; r++) lines.add(card.get(r));
        for (int c = 0; c < 5; c++) {
            List<String> col = new ArrayList<>();
            for (int r = 0; r < 5; r++) col.add(card.get(r).get(c));
            lines.add(col);
        }
        List<String> s1 = new ArrayList<>();
        List<String> s2 = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            s1.add(card.get(i).get(i));
            s2.add(card.get(i).get(4 - i));
        }
        lines.add(s1);
        lines.add(s2);

        // あと1マスのライン(hits==4)にある、まだ出ていない数字を特定
        for (List<String> line : lines) {
            int hits = 0;
            int missingNum = -1;
            for (String val : line) {
                if (val.equals("0") || drawnNumbers.contains(Integer.parseInt(val))) {
                    hits++;
                } else {
                    missingNum = Integer.parseInt(val);
                }
            }
            if (hits == 4 && missingNum != -1) {
                if (!waitNums.contains(missingNum)) {
                    waitNums.add(missingNum);
                }
            }
        }

        if (waitNums.isEmpty()) return "計算中";
        Collections.sort(waitNums);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < waitNums.size(); i++) {
            sb.append(waitNums.get(i));
            if (i < waitNums.size() - 1) sb.append(", ");
        }
        return sb.toString();
    }

    public int getPlayerRank(String playerName) {
        for (int i = 0; i < bingoPlayers.size(); i++) {
            if (bingoPlayers.get(i).getPlayerName().equals(playerName)) {
                return i + 1;
            }
        }
        return 0;
    }
}
