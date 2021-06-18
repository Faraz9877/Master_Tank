package com.afaa.tanktrouble.datahouse;

import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.cannonball.Cannonball;
import com.afaa.tanktrouble.cannonball.CannonballSet;
import com.afaa.tanktrouble.tank.UserTank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GameData {
    private static GameData instance = null;

    private static final int SERVER_ID = 1;
    private static final int CLIENT_ID = 0;

    private ArrayList<Integer> playerIDs;
    private String userUserName;
    private String opponentUserName;
    private Position userPosition;
    private Position opponentPosition;
    private Integer userAliveBulletsCount;
    private Integer opponentAliveBulletsCount;
    private CannonballSet cannonballSet;
    private ArrayList<Cannonball> newCannonballs;
    int userId;
    int opponentId;
    int status;
    boolean isServer;
    private BluetoothService btService;

    private GameData() {
        playerIDs = new ArrayList<>(Arrays.asList(SERVER_ID, CLIENT_ID));
        userUserName = "";
        opponentUserName = "";
        userPosition = UserTank.getRandomInitialPosition();
        opponentPosition = UserTank.getRandomInitialPosition();
        userAliveBulletsCount = 0;
        opponentAliveBulletsCount = 0;
        newCannonballs = new ArrayList<>();
        cannonballSet = new CannonballSet();
        status = 0;
        isServer = false;
        userId = CLIENT_ID;
        opponentId = SERVER_ID;
    }

    public void sync(int syncCode)
    {
        if(btService == null)
            return;

        String token;
        token = DataProtocol.tokenizeGameData(
                (syncCode / 100) % 2 == 1 ? userUserName: null,
                (syncCode / 10) % 2 == 1 ? userPosition: null,
                syncCode % 2 == 1 && newCannonballs.size() > 0 ? newCannonballs: null
            );

        btService.getChannel().send(token.getBytes());

        if(syncCode % 2 == 1)
            newCannonballs.clear();
    }

    public String getOpponentUserName() {
        return opponentUserName;
    }

    public void setOpponentUserName(String opponentUserName) {
        this.opponentUserName = opponentUserName;
    }

    public void setServer(boolean server) {
        isServer = server;
        userId =  SERVER_ID;
        opponentId = CLIENT_ID;
    }

    public boolean getServer() {
        return  isServer;
    }

    public static GameData getInstance() {
        if(instance == null)
            instance = new GameData();
        return instance;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setBtService(BluetoothService btService) { this.btService = btService; }

    public int getUserId() {
        return userId;
    }

    public int getOpponentId() {
        return opponentId;
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

    public Position getUserPosition() {
        return userPosition;
    }

    public Position getOpponentPosition() {
        return opponentPosition;
    }

    public int getUserAliveBulletsCount() {
        return userAliveBulletsCount;
    }

    public int getOpponentAliveBulletsCount() {
        return opponentAliveBulletsCount;
    }

    public void incrementUserAliveBulletsCount() {
        userAliveBulletsCount += 1;
    }

    public void decrementUserAliveBulletsCount() {
        userAliveBulletsCount -= 1;
    }
    public void incrementOpponentAliveBulletsCount() {
        opponentAliveBulletsCount += 1;
    }

    public void decrementOpponentAliveBulletsCount() {
        opponentAliveBulletsCount -= 1;
    }

    public void setUserPosition(Position position) {
        userPosition = position;
    }
    public void setOpponentPosition(Position position) {
        opponentPosition = position;
    }

    public ArrayList<Integer> getPlayerIDs() {
        return playerIDs;
    }

    public boolean isPlayerInGame() {
        return playerIDs.contains(userId);
    }

    public void reset() {
        playerIDs = new ArrayList<>();
        userUserName = "";
        opponentUserName = "";
        userPosition = null;
        opponentPosition = null;
        userAliveBulletsCount = 0;
        opponentAliveBulletsCount = 0;
        status = 1;
        userId = -1;
        opponentId = -1;
    }

    public void addCannonball(Cannonball cannonball) {
        cannonballSet.addCannonball(cannonball);
    }

}
