package com.afaa.tanktrouble.datahouse;

import com.afaa.tanktrouble.UserUtils;
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
    int aliveBullets; // Does not sync
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
        aliveBullets = 0;
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
        if(syncCode == 2) { // Special for player register
            token = DataProtocol.tokenizeSoloGameData(null, thisPlayer,
                    playerUsernames.get(0), null, null);
            btService.getChannel().send(token.getBytes());
            return;
        }
        else {
            token = isSolo ? DataProtocol.tokenizeSoloGameData(
                    (syncCode / 10000) % 2 == 1 ? gamePin: null,
                    thisPlayer,
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

        }

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
        return aliveBullets;
    }

    public void incrementUserAliveBullets() {
        aliveBullets++;
    }

    public void decrementUserAliveBullets() {
        aliveBullets--;
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

    public void addPlayer(int userId, String username) {
        playerIDs.add(userId);
        playerUsernames.add(username);
        playerPositions.add(UserTank.getRandomInitialPosition());
        aliveBullets = 0;
        thisPlayer = userId;
    }

    public void addPeerPlayers() {
        if(playerIDs.size() < 2)
            return;
        while(playerUsernames.size() < playerIDs.size())
            playerUsernames.add(UserUtils.generateRandomUsername());
        while(playerPositions.size() < playerIDs.size())
            playerPositions.add(UserTank.getRandomInitialPosition());
    }

    public void removePlayer() {
        playerUsernames.remove(playerIDs.indexOf(thisPlayer));
        playerPositions.remove(playerIDs.indexOf(thisPlayer));
        aliveBullets = 0;
        playerIDs.remove(Integer.valueOf(thisPlayer));
        status = -1;
    }

    public void setThisPlayer(int playerId) {
        thisPlayer = playerId;
    }

    public boolean isPlayerInGame() {
        return playerIDs.contains(thisPlayer);
    }

    public void reset() {
        playerIDs = new ArrayList<>();
        playerUsernames = new ArrayList<>();
        playerPositions = new ArrayList<>();
        aliveBullets = 0;
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
