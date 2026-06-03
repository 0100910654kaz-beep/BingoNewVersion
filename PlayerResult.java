package servlet;

import java.io.Serializable;
import java.util.Date;

public class PlayerResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private Date reachTime;
    private int drawnNumber;

    // コンストラクタ
    public PlayerResult(String playerName, Date reachTime, int drawnNumber) {
        this.playerName = playerName;
        this.reachTime = reachTime;
        this.drawnNumber = drawnNumber;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public Date getReachTime() {
        return this.reachTime;
    }

    public int getDrawnNumberAtBingo() {
        return this.drawnNumber;
    }
}
