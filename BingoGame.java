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

    

    // プレイヤーごとの5x5カードデータを保持するマップ

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



    // プレイヤーの5x5カードを取得または新規生成するメソッド

    public List<List<String>> getPlayerCard(String playerName) {

        if (playerCards.containsKey(playerName)) {

            return playerCards.get(playerName);

        }



        // 新規カード生成 (B:1-15, I:16-30, N:31-45, G:46-60, O:61-75)

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

                rowList.add("0"); // 真ん中はFREE(0)

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

        Collections.sort(bingoPlayers); // 到達時刻順にソート

    }



    // 順位（インデックス+1）を返すメソッド

    public int getPlayerRank(String playerName) {

        for (int i = 0; i < bingoPlayers.size(); i++) {

            if (bingoPlayers.get(i).getPlayerName().equals(playerName)) {

                return i + 1;

            }

        }

        return 0;

    }

} 

