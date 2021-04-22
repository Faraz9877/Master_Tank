package com.wztlei.tanktrouble.datahouse;

import com.wztlei.tanktrouble.battle.Position;
import com.wztlei.tanktrouble.cannonball.CannonballSet;

import java.util.ArrayList;

public class GameData {
    private static GameData instance = null;

    ArrayList<Integer> playerIDs;
    ArrayList<String> playerUsernames;
    ArrayList<Position> playerPositions;
    CannonballSet cannonballSet;
    String gamePin;
    int thisPlayer;
    int status; // 0: wait to join, 1: playing, -1: cancelled

    private GameData() {
        playerIDs = new ArrayList<>();
        playerUsernames = new ArrayList<>();
        playerPositions = new ArrayList<>();
        status = 0;
        thisPlayer = -1;
    }

    public void sync()
    {

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

    public int getThisPlayer() {
        return thisPlayer;
    }

    public ArrayList<String> getPlayerUsernames() {
        return playerUsernames;
    }

    public int getStatus() {
        return status;
    }

    public String getGamePin() {
        return gamePin;
    }

    public Position getPlayerPosition(int playerID) {
        return playerPositions.get(playerIDs.indexOf(thisPlayer));
    }

    public void setPosition(Position position) {
        playerPositions.set(thisPlayer, position);
    }

    public ArrayList<Integer> getPlayerIDs() {
        return playerIDs;
    }

    public ArrayList<Position> getPlayerCoordinates() {
        return playerPositions;
    }

    public void addPlayer(String username) {
        int newPlayerID = playerIDs.size() + 1;
        playerIDs.add(newPlayerID);
        playerUsernames.add(username);
        playerPositions.add(new Position(10, 10, 0));
        thisPlayer = newPlayerID;
    }

    public void removePlayer() {
        playerUsernames.remove(playerIDs.indexOf(thisPlayer));
        playerPositions.remove(playerIDs.indexOf(thisPlayer));
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
        status = 1;
        thisPlayer = -1;
    }
}
