package servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class BingoGame implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameId;
    private int validDays;
    private long createTime;
    private long lastBingoTime;

    private List<Integer> drawnNumbers = new CopyOnWriteArrayList<>();
    private List<PlayerResult> bingoPlayers = new CopyOnWriteArrayList<>();
    private List<PlayerResult> reachPlayers = new CopyOnWriteArrayList<>();

    private Map<String, List<List<String>>> playerCards = new ConcurrentHashMap<>();

    public BingoGame(String gameId, int validDays) {
        this.gameId = gameId;
        this.validDays = validDays;
        this.createTime = System.currentTimeMillis();
        this.lastBingoTime = 0;
    }

    public String getGameId() { return gameId; }
    public List<Integer> getDrawnNumbers() { return drawnNumbers; }
    public List<PlayerResult> getBingoPlayers() { return bingoPlayers; }
    public List<PlayerResult> getReachPlayers() { return reachPlayers; }
    public int getPlayerCount() { return playerCards.size(); }

    public boolean isExpired() {
        long duration = (long) validDays * 24 * 60 * 60 * 1000;
        return (System.currentTimeMillis() - createTime) > duration;
    }

    public boolean isPast2HoursFromLastBingo() {
        if (lastBingoTime == 0) return false;
        return (System.currentTimeMillis() - lastBingoTime) > (2 * 60 * 60 * 1000);
    }

    public synchronized String registerPlayer(String name) {
        if (name == null || name.trim().isEmpty()) {
            name = "ゲスト" + (getPlayerCount() + 1);
        }
        name = name.trim();
        
        String uniqueName = name;
        int count = 1;
        while (playerCards.containsKey(uniqueName)) {
            uniqueName = name + count;
            count++;
        }
        return uniqueName;
    }

    public synchronized void setPlayerCard(String playerName, List<List<String>> card) {
        playerCards.put(playerName, card);
        checkPlayerStatus(playerName, card);
    }

    public synchronized void checkAllPlayers() {
        for (Map.Entry<String, List<List<String>>> entry : playerCards.entrySet()) {
            checkPlayerStatus(entry.getKey(), entry.getValue());
        }
    }

    private void checkPlayerStatus(String playerName, List<List<String>> card) {
        boolean alreadyBingo = false;
        for (PlayerResult p : bingoPlayers) {
            if (p.getPlayerName().equals(playerName)) {
                alreadyBingo = true;
                break;
            }
        }

        int minMissing = 99;
        List<int[]> lines = getAllLines();

        for (int[] line : lines) {
            int missingCount = 0;
            for (int i = 0; i < 5; i++) {
                int r = line[i * 2];
                int c = line[i * 2 + 1];
                String numStr = card.get(r).get(c);
                int num = Integer.parseInt(numStr);

                if (num != 0 && !drawnNumbers.contains(num)) {
                    missingCount++;
                }
            }
            if (missingCount < minMissing) {
                minMissing = missingCount;
            }
        }

        if (minMissing == 0 && !alreadyBingo) {
            int lastNum = drawnNumbers.isEmpty() ? 0 : drawnNumbers.get(drawnNumbers.size() - 1);
            bingoPlayers.add(new PlayerResult(playerName, new Date(), lastNum));
            lastBingoTime = System.currentTimeMillis();
            
            reachPlayers.removeIf(p -> p.getPlayerName().equals(playerName));
        } 
        else if (minMissing == 1 && !alreadyBingo) {
            boolean alreadyReach = false;
            for (PlayerResult p : reachPlayers) {
                if (p.getPlayerName().equals(playerName)) {
                    alreadyReach = true;
                    break;
                }
            }
            if (!alreadyReach) {
                reachPlayers.add(new PlayerResult(playerName, new Date(), 0));
            }
        }
        else if (minMissing > 1) {
            reachPlayers.removeIf(p -> p.getPlayerName().equals(playerName));
        }
    }

    // 🚀【重要】同着を同じ着数（2位、2位、次は4位）にするオリンピック方式の計算
    public List<String> getRankedBingoListHTML() {
        List<String> htmlLines = new ArrayList<>();
        int currentRank = 1; // 表示する着数
        int skipped = 0;     // 同着によってスキップされる数

        for (int i = 0; i < bingoPlayers.size(); i++) {
            PlayerResult current = bingoPlayers.get(i);
            
            if (i > 0) {
                PlayerResult previous = bingoPlayers.get(i - 1);
                // 達成した日時（ミリ秒まで）が完全に同じか、または同じ「確定番号」で同時ビンゴした場合は同着
                if (current.getReachTime().equals(previous.getReachTime()) || 
                    current.getDrawnNumberAtBingo() == previous.getDrawnNumberAtBingo()) {
                    skipped++; // 同着カウントをためる
                } else {
                    currentRank += skipped + 1; // スキップ分を適用して着数を進める
                    skipped = 0;
                }
            }
            
            htmlLines.add("<li><strong>" + currentRank + "位</strong>: " + current.getPlayerName() + 
                          " さん <span style='color:#e63946; font-weight:bold;'>(🔑" + current.getDrawnNumberAtBingo() + "番でビンゴ!)</span></li>");
        }
        return htmlLines;
    }

    public int getWaitNumbers(String playerName) {
        List<List<String>> card = playerCards.get(playerName);
        if (card == null) return 0;

        List<int[]> lines = getAllLines();
        for (int[] line : lines) {
            int missingCount = 0;
            int missingNum = 0;
            for (int i = 0; i < 5; i++) {
                int r = line[i * 2];
                int c = line[i * 2 + 1];
                int num = Integer.parseInt(card.get(r).get(c));
                if (num != 0 && !drawnNumbers.contains(num)) {
                    missingCount++;
                    missingNum = num;
                }
            }
            if (missingCount == 1) {
                return missingNum; 
            }
        }
        return 0;
    }

    private List<int[]> getAllLines() {
        List<int[]> lines = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            lines.add(new int[]{i,0, i,1, i,2, i,3, i,4});
            lines.add(new int[]{0,i, 1,i, 2,i, 3,i, 4,i});
        }
        lines.add(new int[]{0,0, 1,1, 2,2, 3,3, 4,4});
        lines.add(new int[]{0,4, 1,3, 2,2, 3,1, 4,0});
        return lines;
    }
}
