package servlet;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class BingoGame implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameId;
    private List<Integer> drawnNumbers;
    private List<Integer> lotteryMachine;
    private List<PlayerResult> reachPlayers;
    private List<PlayerResult> bingoPlayers;

    public BingoGame(String gameId) {
        this.gameId = gameId;
        this.drawnNumbers = new ArrayList<>();
        this.lotteryMachine = new ArrayList<>();
        this.reachPlayers = new ArrayList<>();
        this.bingoPlayers = new ArrayList<>();

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
        return this.reachPlayers.size() + this.bingoPlayers.size();
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

    public void registerReachPlayer(String playerName) {
        for (PlayerResult p : reachPlayers) {
            if (p.getPlayerName().equals(playerName)) {
                return;
            }
        }
        reachPlayers.add(new PlayerResult(playerName, new Date(), 0));
    }

    public void registerBingoPlayer(String playerName) {
        reachPlayers.removeIf(p -> p.getPlayerName().equals(playerName));

        for (PlayerResult p : bingoPlayers) {
            if (p.getPlayerName().equals(playerName)) {
                return;
            }
        }
        int lastNum = drawnNumbers.isEmpty() ? 0 : drawnNumbers.get(drawnNumbers.size() - 1);
        bingoPlayers.add(new PlayerResult(playerName, new Date(), lastNum));
    }

    public int getWaitNumbers(String playerName) {
        return 1; 
    }
}
