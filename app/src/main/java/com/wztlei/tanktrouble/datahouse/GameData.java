package com.wztlei.tanktrouble.datahouse;

import android.util.Log;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.Position;
import com.wztlei.tanktrouble.cannonball.Cannonball;
import com.wztlei.tanktrouble.cannonball.CannonballSet;
import com.wztlei.tanktrouble.map.MapUtils;
import com.wztlei.tanktrouble.tank.UserTank;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class GameData {
    private static GameData instance = null;

    ArrayList<Integer> playerIDs;
    ArrayList<String> playerUsernames;
    ArrayList<Position> playerPositions;
    ArrayList<Integer> aliveBullets; // Does not sync
    CannonballSet cannonballSet;
    ArrayList<Cannonball> newCannonballs;
    String gamePin;
    int thisPlayer;
    int status; // 0: wait to join, 1: playing, -1: cancelled
    boolean isServer;

    private BluetoothService btService;

    private GameData() {
        playerIDs = new ArrayList<>();
        playerIDs.add(0);
        playerUsernames = new ArrayList<>();
        playerUsernames.add("King Slayer");
        playerPositions = new ArrayList<>();
        playerPositions.add(UserTank.getRandomInitialPosition());
        aliveBullets = new ArrayList<>();
        aliveBullets.add(0);
        newCannonballs = new ArrayList<>();
        cannonballSet = new CannonballSet();
        status = 0;
        isServer = false;
        thisPlayer = 0;
    }

    public void sync(int syncCode, boolean isSolo)
    {
        if(btService == null || ! isServer)
            return;
        Log.d("Sync", "sync: btService is found!");

        String token;
        token = isSolo ? DataProtocol.tokenizeSoloGameData(
                (syncCode / 10000) % 2 == 1 ? gamePin: null,
                playerIDs.get(thisPlayer),
                (syncCode / 100) % 2 == 1 ? playerUsernames.get(thisPlayer): null,
                (syncCode / 10) % 2 == 1 ? playerPositions.get(thisPlayer): null,
                syncCode % 2 == 1 ? newCannonballs: null
            ) : DataProtocol.tokenizeGameData(
                (syncCode / 10000) % 2 == 1 ? gamePin: null,
                (syncCode / 1000) % 2 == 1 ? playerIDs: null,
                (syncCode / 100) % 2 == 1 ? playerUsernames: null,
                (syncCode / 10) % 2 == 1 ? playerPositions: null,
                syncCode % 2 == 1 ? newCannonballs: null
            );

        btService.getChannel().send(token.getBytes(/*"UTF-8"*/));
        newCannonballs.clear();
    }

    public void setServer(boolean server) {
        isServer = server;
    }

    public boolean getServer(boolean server) {
        return  isServer;
    }

    public static GameData getInstance() {
        if(instance == null)
            instance = new GameData();

        return instance;
    }

    public void setGamePin(String gamePin) {
        this.gamePin = gamePin;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setBtService(BluetoothService btService) { this.btService = btService; }

    public int getThisPlayer() {
        return 0;
    }

    public CannonballSet getCannonballSet() {
        return cannonballSet;
    }

    public ArrayList<Cannonball> getNewCannonballs() {
        return newCannonballs;
    }

    public int getStatus() {
        return status;
    }

    public String getGamePin() {
        return gamePin;
    }

    public Position getPlayerPosition(int playerID) {
        return playerPositions.get(0);
    }

    public int getPlayerAliveBullets() {
        return aliveBullets.get(0);
    }

    public void incrementUserAliveBullets() {
        aliveBullets.set(thisPlayer, aliveBullets.get(0) + 1);
    }

    public void decrementUserAliveBullets() {
        aliveBullets.set(thisPlayer, aliveBullets.get(0) - 1);
    }

    public void setPosition(Position position) {
        if(thisPlayer != 0)
            return;
        playerPositions.set(thisPlayer, position);
    }

    public ArrayList<Integer> getPlayerIDs() {
        return playerIDs;
    }

    public ArrayList<Position> getPlayerPositions() {
        return playerPositions;
    }

    public void addPlayer(String username, int userId) {
//        int newPlayerID = playerIDs.size();
//        playerIDs.add(userId);
//        playerUsernames.add(username);
//        playerPositions.add(UserTank.getRandomInitialPosition());
//        aliveBullets.add(0);
//        thisPlayer = userId;
    }

    public boolean isPlayerInGame() {
        return playerIDs.contains(thisPlayer);
    }

    public void reset() {
        playerIDs = new ArrayList<>();
        playerUsernames = new ArrayList<>();
        playerPositions = new ArrayList<>();
        aliveBullets = new ArrayList<>();
        status = 1;
        thisPlayer = -1;
    }

    public void setPlayerIDs(ArrayList<Integer> playerIDs) {
//        this.playerIDs = playerIDs;
    }

    public void setPlayerUsernames(ArrayList<String> playerUsernames) {
//        this.playerUsernames = playerUsernames;
    }

    public void setPlayerPositions(ArrayList<Position> playerPositions) {
        this.playerPositions = playerPositions;
    }

    public void addCannonball(Cannonball cannonball) {
        cannonballSet.addCannonball(cannonball);
    }

    public void addPlayerID(int playerID) {
//        if(!playerIDs.contains(playerID)) {
//            playerIDs.add(playerID);
//        }
    }

    public void addPlayerUsername(int id, String username) {
//        if(!playerUsernames.contains(username) && playerIDs.size() > playerUsernames.size()) {
//            playerUsernames.add(username);
//        }
    }

    public void setPlayerPosition(int playerID, Position position) {
        playerPositions.get(0).x = position.x;
        playerPositions.get(0).y = position.y;
        playerPositions.get(0).deg = position.deg;
    }
}
