package servlet;

import java.io.Serializable;
import java.util.Date;

public class PlayerResult implements Serializable, Comparable<PlayerResult> {
    private static final long serialVersionUID = 1L;

    private String プレイヤー名;
    private Date 到達時刻;
    private int ビンゴで描かれた数字;

    public PlayerResult(String プレイヤー名, Date 到達時刻, int ビンゴで描かれた数字) {
        this.プレイヤー名 = プレイヤー名;
        this.到達時刻 = 到達時刻;
        this.ビンゴで描かれた数字 = ビンゴで描かれた数字;
    }

    public String getPlayerName() {
        return プレイヤー名;
    }

    public Date 到達時刻() {
        return 到達時刻;
    }

    public int getDrawnNumberAtBingo() {
        return ビンゴで描かれた数字;
    }

    @Override
    public int compareTo(PlayerResult 他の) {
        if (他の.到達時刻() == null || this.到達時刻 == null) {
            return 0;
        }
        return 他の.到達時刻().compareTo(this.到達時刻);
    }
}
