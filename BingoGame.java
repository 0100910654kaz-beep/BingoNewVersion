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
                return;
            }
        }
        // 新規登録（PlayerResultの引数構造 [名前, 到達時刻, ビンゴ番号] に合わせてダミーで0をセット）
        PlayerResult newReach = new PlayerResult(playerName, new Date(), 0);
        reachPlayers.add(newReach);
    }

    // 参加者からビンゴ達成情報を登録するメソッド
    public void registerBingoPlayer(String playerName) {
        // 大文字になっていた removeIF を小文字の removeIf に完全修正！
        reachPlayers.removeIf(p -> p.getPlayerName().equals(playerName));

        for (PlayerResult p : bingoPlayers) {
            if (p.getPlayerName().equals(playerName)) {
                return;
            }
        }
        
        // 抽選された最新の数字を取得（なければ0）
        int lastNum = drawnNumbers.isEmpty() ? 0 : drawnNumbers.get(drawnNumbers.size() - 1);
        
        // PlayerResultのコンストラクタ（引数3つ）に完全一致させて生成
        PlayerResult newBingo = new PlayerResult(playerName, new Date(), lastNum);
        bingoPlayers.add(newBingo);
    }

    // あと何番でビンゴかを返すメソッド
    public int getWaitNumbers(String playerName) {
        return 1; 
    }

    /**
     * 🏆 同着オリンピック方式の順位付きHTMLリストを生成するメソッド
     */
    public List<String> getRankedBingoListHTML() {
        List<String> htmlLines = new ArrayList<>();
        if (bingoPlayers.isEmpty()) {
            return htmlLines;
        }

        // 1. ビンゴ達成者をルール通りにソート
        // 判定①：先にリーチ・ビンゴボタンを押した（到達時刻が早い）方が上
        Collections.sort(bingoPlayers, (p1, p2) -> {
            if (p1.到達時刻() != null && p2.到達時刻() != null) {
                return p1.到達時刻().compareTo(p2.到達時刻());
            }
            return 0;
        });

        // 2. オリンピック方式（同着を考慮）で順位付けしてHTML化
        int rank = 1;
        for (int i = 0; i < bingoPlayers.size(); i++) {
            PlayerResult current = bingoPlayers.get(i);

            // 前の人とボタンを押した時刻がミリ秒まで完全に同じなら同順位にする
            if (i > 0) {
                PlayerResult previous = bingoPlayers.get(i - 1);
                boolean sameTime = false;

                if (current.到達時刻() != null && previous.到達時刻() != null) {
                    sameTime = current.到達時刻().equals(previous.到達時刻());
                }

                if (!sameTime) {
                    rank = i + 1;
                }
            }

            // 王冠やメダルの装飾付きでHTMLを生成
            String medal = "";
            if (rank == 1) medal = "🥇 ";
            else if (rank == 2) medal = "🥈 ";
            else if (rank == 3) medal = "🥉 ";
            else medal = "🔹 " + rank + "位 ";

            // getDrawnNumberAtBingo() を使ってビンゴした番号を表示
            htmlLines.add("<li><strong>" + medal + current.getPlayerName() + " さん</strong> " +
                    "<span style='font-size: 14px; color: #888;'>(当選番号: " + current.getDrawnNumberAtBingo() + ")</span></li>");
        }

        return htmlLines;
    }
}
