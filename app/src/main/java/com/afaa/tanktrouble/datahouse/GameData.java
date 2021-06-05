package com.afaa.tanktrouble.datahouse;

import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.cannonball.Cannonball;
import com.afaa.tanktrouble.cannonball.CannonballSet;
import com.afaa.tanktrouble.tank.UserTank;

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
        playerUsernames = new ArrayList<>();
        playerPositions = new ArrayList<>();
        aliveBullets = new ArrayList<>();
        newCannonballs = new ArrayList<>();
        cannonballSet = new CannonballSet();
        status = 0;
        thisPlayer = -1;
    }

    public void sync(int syncCode, boolean isSolo)
    {
        if(btService == null)
            return;
//        Log.d("Sync", "sync: btService is found!");

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
                syncCode % 2 == 1 && newCannonballs.size() > 0 ? newCannonballs: null
            );

        btService.getChannel().send(token.getBytes());

        if(syncCode % 2 == 1)
            newCannonballs.clear();
    }

    public void setServer(boolean server) {
        isServer = server;
    }

    public boolean getServer() {
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
        return thisPlayer;
    }

    public ArrayList<String> getPlayerUsernames() {
        return playerUsernames;
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
        return playerPositions.get(playerID);
    }

    public int getPlayerAliveBullets() {
        return aliveBullets.get(thisPlayer);
    }

    public void incrementUserAliveBullets() {
        aliveBullets.set(thisPlayer, aliveBullets.get(thisPlayer) + 1);
    }

    public void decrementUserAliveBullets() {
        aliveBullets.set(thisPlayer, aliveBullets.get(thisPlayer) - 1);
    }

    public void setPosition(Position position) {
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
        playerIDs.add(userId);
        playerUsernames.add(username);
        playerPositions.add(UserTank.getRandomInitialPosition());
        aliveBullets.add(0);
        thisPlayer = userId;
    }

    public void removePlayer() {
        playerUsernames.remove(playerIDs.indexOf(thisPlayer));
        playerPositions.remove(playerIDs.indexOf(thisPlayer));
        aliveBullets.remove(playerIDs.indexOf(thisPlayer));
        playerIDs.remove(Integer.valueOf(thisPlayer));
        status = -1;
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
        this.playerIDs = playerIDs;
    }

    public void setPlayerUsernames(ArrayList<String> playerUsernames) {
        this.playerUsernames = playerUsernames;
    }

    public void setPlayerPositions(ArrayList<Position> playerPositions) {
        this.playerPositions = playerPositions;
    }

    public void addCannonball(Cannonball cannonball) {
        cannonballSet.addCannonball(cannonball);
    }

    public void addPlayerID(int playerID) {
        if(!playerIDs.contains(playerID)) {
            playerIDs.add(playerID);
        }
    }

    public void addPlayerUsername(int id, String username) {
        if(!playerUsernames.contains(username) && playerIDs.size() > playerUsernames.size()) {
            playerUsernames.add(username);
        }
    }

    public void setPlayerPosition(int playerID, Position position) {
        playerPositions.set(playerIDs.indexOf(playerID), position);
    }
}
