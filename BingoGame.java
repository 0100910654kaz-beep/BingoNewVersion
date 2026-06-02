package servlet;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class BingoGame implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameId;                     // 部屋番号（ゲームID）
    private List<Integer> drawnNumbers;        // 当選番号の履歴（登録順）
    private List<PlayerResult> bingoPlayers;   // ビンゴ達成者のリスト（超高速・安全な箱に変更）
    private List<PlayerResult> reachPlayers;   // リーチ達成者のリスト（超高速・安全な箱に変更）
    private List<String> allPlayers;           // 全参加者の名前リスト（人数カウント用）
    private Date expireTime;                   // この部屋の有効期限（3日または8日）
    private Date lastBingoTime;                // 最後にビンゴが出た時刻
    private int anonymousCount = 0;            // 名前空欄の人用のカウンター(A, B, C...)

    public BingoGame(String gameId, int validDays) {
        this.gameId = gameId;
        // 500人の同時アクセスで大渋滞（synchronizedによるフリーズ）を起こさないための特殊な箱を採用
        this.drawnNumbers = new CopyOnWriteArrayList<>();
        this.bingoPlayers = new CopyOnWriteArrayList<>();
        this.reachPlayers = new CopyOnWriteArrayList<>();
        this.allPlayers = new CopyOnWriteArrayList<>();
        this.lastBingoTime = new Date();
        
        // 部屋全体の有効期限（3日または8日）を計算
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_MONTH, validDays);
        this.expireTime = cal.getTime();
    }

    // プレイヤーをゲームに参加登録する（空欄なら自動でPlayer-A...を命名）
    public synchronized String registerPlayer(String name) {
        if (name == null || name.trim().isEmpty()) {
            char suffix = (char) ('A' + (anonymousCount % 26));
            name = "Player-" + suffix;
            if (anonymousCount >= 26) {
                name += (anonymousCount / 26 + 1);
            }
            anonymousCount++;
        }
        
        String trimmedName = name.trim();
        if (!allPlayers.contains(trimmedName)) {
            allPlayers.add(trimmedName);
        }
        return trimmedName;
    }

    // 部屋全体の有効期限（3日・8日）が切れたかチェック
    public boolean isExpired() {
        return new Date().after(this.expireTime);
    }

    // 最後のビンゴから2時間経過したかチェック
    public boolean isPast2HoursFromLastBingo() {
        if (bingoPlayers.isEmpty()) {
            return false;
        }
        long twoHoursInMilliseconds = 2L * 60 * 60 * 1000;
        long timePassed = new Date().getTime() - lastBingoTime.getTime();
        return timePassed > twoHoursInMilliseconds;
    }

    // 【同着対応】ビンゴ登録（どの数字でビンゴしたかも一緒に記録）
    public void addBingoPlayer(String name) {
        // すでに登録済みならスキップ
        for (PlayerResult p : bingoPlayers) {
            if (p.getPlayerName().equals(name)) return;
        }
        
        // 現在の最新の当選番号を取得（まだ数字が出ていない場合は0）
        int currentDrawnNumber = drawnNumbers.isEmpty() ? 0 : drawnNumbers.get(drawnNumbers.size() - 1);
        
        Date now = new Date();
        // 達成リストの「先頭（0番目）」に突っ込むことで、最新の達成者が一番上に表示される大山さんロジックを実現
        bingoPlayers.add(0, new PlayerResult(name, now, currentDrawnNumber));
        this.lastBingoTime = now;
        
        // ビンゴした人は、リーチリストからは自動削除（お祝い移動）
        removeReachPlayer(name);
    }

    // 【大山さんこだわりの同着タイ順位ロジック】
    // 500人規模でも「同じ番号なら同着」「5位が2人いたら次は7位」を完璧に自動計算します
    public int getPlayerRank(String name) {
        PlayerResult targetPlayer = null;
        for (PlayerResult p : bingoPlayers) {
            if (p.getPlayerName().equals(name)) {
                targetPlayer = p;
                break;
            }
        }
        if (targetPlayer == null) return 0;

        // 計算ルール：自分より「前に（＝早く）ビンゴ登録した人」の中で、
        // 「自分と違うビンゴ番号で上がった人」の人数をベースに順位を決めます。
        int rank = 1;
        
        // 登録が古い人から（リストの後ろから）順番にチェックしていく
        for (int i = bingoPlayers.size() - 1; i >= 0; i--) {
            PlayerResult other = bingoPlayers.get(i);
            
            // 自分自身の位置まで来たら計算終了
            if (other.getPlayerName().equals(name)) {
                break;
            }
            
            // 同着判定：もし相手が自分と「同じビンゴ番号」で上がっているなら、順位はスキップ（同着タイ）にする
            if (other.getDrawnNumberAtBingo() != targetPlayer.getDrawnNumberAtBingo()) {
                rank++;
            }
        }
        return rank;
    }

    // リーチ登録
    public void addReachPlayer(String name) {
        // すでにビンゴしてるか、すでにリーチ登録されてるならスキップ
        for (PlayerResult p : bingoPlayers) {
            if (p.getPlayerName().equals(name)) return;
        }
        for (PlayerResult p : reachPlayers) {
            if (p.getPlayerName().equals(name)) return;
        }
        
        // リーチは最新の人が一番上に降ってくるように先頭(0番目)に追加
        reachPlayers.add(0, new PlayerResult(name, new Date(), 0));
    }

    // リーチリストから削除する内部部品（ビンゴお祝い移動用）
    public void removeReachPlayer(String name) {
        reachPlayers.removeIf(p -> p.getPlayerName().equals(name));
    }

    // 各種データを受け渡すための部品（Getter）
    public String getGameId() { return gameId; }
    public List<Integer> getDrawnNumbers() { return drawnNumbers; }
    public List<PlayerResult> getBingoPlayers() { return bingoPlayers; }
    public List<PlayerResult> getReachPlayers() { return reachPlayers; }
    public List<String> getAllPlayers() { return allPlayers; }
    public int getPlayerCount() { return allPlayers.size(); }
    public Date getExpireTime() { return expireTime; }
}