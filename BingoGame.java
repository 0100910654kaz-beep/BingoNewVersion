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

    // コンストラクタ
    public BingoGame(String gameId) {
        this.gameId = gameId;
        this.drawnNumbers = new ArrayList<>();
        this.lotteryMachine = new ArrayList<>();
        this.reachPlayers = new ArrayList<>();
        this.bingoPlayers = new ArrayList<>();

        // 1〜75の玉をマシンにセット
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
        // 簡易的にリーチとビンゴの合計、または参加者数を返す（必要に応じて調整してください）
        return this.reachPlayers.size() + this.bingoPlayers.size();
    }

    // 抽選するメソッド
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

    // 参加者からリーチ情報を登録・更新するメソッド
    public void registerReachPlayer(String playerName) {
        for (PlayerResult p : reachPlayers) {
            if (p.getPlayerName().equals(playerName)) {
                return; // 既に登録済みなら何もしない
            }
        }
        // 新規登録（到達時刻をセット）
        PlayerResult newReach = new PlayerResult(playerName);
        newReach.set到達時刻(new Date());
        reachPlayers.add(newReach);
    }

    // 参加者からビンゴ達成情報を登録するメソッド
    public void registerBingoPlayer(String playerName) {
        // リーチ一覧から削除
        reachPlayers.removeIF(p -> p.getPlayerName().equals(playerName));

        for (PlayerResult p : bingoPlayers) {
            if (p.getPlayerName().equals(playerName)) {
                return; // 既にビンゴ登録済みなら何もしない
            }
        }
        // 新規登録（達成時の現在の玉の数をセット）
        PlayerResult newBingo = new PlayerResult(playerName);
        newBingo.setビンゴ時排出数(drawnNumbers.size());
        newBingo.set到達時刻(new Date());
        bingoPlayers.add(newBingo);
    }

    // あと何番でビンゴかを返すダミーメソッド（必要に応じてロジックを実装してください）
    public int getWaitNumbers(String playerName) {
        return 1; 
    }

    /**
     * 🏆 同着オリンピック方式の順位付きHTMLリストを生成するメソッド
     * PlayerResultの日本語メソッド「ビンゴ時排出数()」と「到達時刻()」に完全対応
     */
    public List<String> getRankedBingoListHTML() {
        List<String> htmlLines = new ArrayList<>();
        if (bingoPlayers.isEmpty()) {
            return htmlLines;
        }

        // 1. ビンゴ達成者をルール通りにソート
        // 判定①：ビンゴした時の排出数が少ない方が上（少ない手数で上がった）
        // 判定②：排出数が同じなら、先にリーチ・ビンゴボタンを押した（到達時刻が早い）方が上
        Collections.sort(bingoPlayers, (p1, p2) -> {
            int numCompare = Integer.compare(p1.getビンゴ時排出数(), p2.getビンゴ時排出数());
            if (numCompare != 0) {
                return numCompare;
            }
            if (p1.到達時刻() != null && p2.到達時刻() != null) {
                return p1.到達時刻().compareTo(p2.到達時刻());
            }
            return 0;
        });

        // 2. オリンピック方式（同着を考慮）で順位付けしてHTML化
        int rank = 1;
        for (int i = 0; i < bingoPlayers.size(); i++) {
            PlayerResult current = bingoPlayers.get(i);

            // 前の人と「排出手数」も「ボタンを押した時刻」も完全に同じなら同順位にする
            if (i > 0) {
                PlayerResult previous = bingoPlayers.get(i - 1);
                boolean sameBalls = (current.getビンゴ時排出数() == previous.getビンゴ時排出数());
                boolean sameTime = false;

                if (current.到達時刻() != null && previous.到達時刻() != null) {
                    sameTime = current.到達時刻().equals(previous.到達時刻());
                }

                // 両方同じなら順位を据え置く（上げない）、違っていれば実際のインデックス+1にする
                if (!(sameBalls && sameTime)) {
                    rank = i + 1;
                }
            }

            // 王冠やメダルの装飾付きでHTMLを生成
            String medal = "";
            if (rank == 1) medal = "🥇 ";
            else if (rank == 2) medal = "🥈 ";
            else if (rank == 3) medal = "🥉 ";
            else medal = "🔹 " + rank + "位 ";

            htmlLines.add("<li><strong>" + medal + current.getPlayerName() + " さん</strong> " +
                    "<span style='font-size: 14px; color: #888;'>(" + current.getビンゴ時排出数() + "球目確定)</span></li>");
        }

        return htmlLines;
    }
}
