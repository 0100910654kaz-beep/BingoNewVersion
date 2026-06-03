package servlet;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PlayerResult implements Serializable, Comparable<PlayerResult> {
    private static final long serialVersionUID = 1L;

    private String playerName;
    private Date achievedTime;
    private int drawnNumberAtBingo;

    public PlayerResult(String playerName, Date achievedTime, int drawnNumberAtBingo) {
        this.playerName = playerName;
        this.achievedTime = achievedTime;
        this.drawnNumberAtBingo = drawnNumberAtBingo;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Date getAchievedTime() {
        return achievedTime;
    }

    public String getFormattedTime() {
        if (achievedTime == null) return "";
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(achievedTime);
    }

    public int getDrawnNumberAtBingo() {
        return drawnNumberAtBingo;
    }

    @Override
    public int compareTo(PlayerResult other) {
        if (this.achievedTime == null || other.getAchievedTime() == null) {
            return 0;
        }
        // 最新の達成者が上（降順）になるように比較
        return other.getAchievedTime().compareTo(this.achievedTime);
    }
}
