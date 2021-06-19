package com.afaa.tanktrouble.datahouse;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;

import com.afaa.tanktrouble.R;
import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.cannonball.Cannonball;
import com.afaa.tanktrouble.cannonball.CannonballSet;
import com.afaa.tanktrouble.tank.UserTank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GameData {
    private static GameData instance = null;

    private static final int CLIENT_ID = 0;
    private static final int SERVER_ID = 1;


    private ArrayList<Integer> playerIDs;
    private String userUserName;
    private String opponentUserName;
    private Position userPosition;
    private Position opponentPosition;
    private Integer userAliveBulletsCount;
    private CannonballSet cannonballSet;
    private ArrayList<Cannonball> newCannonballs;
    int userId;
    int opponentId;
    int status;



    int lastOpponentCannonId;
    boolean isServer;
    private BluetoothService btService;
    SoundPool shootSoundPool, explosionSoundPool;
    int shootSoundId, explosionSoundId;

    private GameData() {
        playerIDs = new ArrayList<>(Arrays.asList(SERVER_ID, CLIENT_ID));
        userUserName = "";
        opponentUserName = "";
        userPosition = UserTank.getRandomInitialPosition();
        opponentPosition = UserTank.getRandomInitialPosition();
        userAliveBulletsCount = 0;
        newCannonballs = new ArrayList<>();
        cannonballSet = new CannonballSet();
        status = 0;
        lastOpponentCannonId = -1;
        isServer = false;
        userId = CLIENT_ID;
        opponentId = SERVER_ID;
    }

    public void createSoundPool(Activity activity){
        shootSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        shootSoundId = shootSoundPool.load(activity, R.raw.fire, 1);
        explosionSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        explosionSoundId = explosionSoundPool.load(activity, R.raw.explosion, 1);
    }

    public void playShootSound() {
        shootSoundPool.play(shootSoundId, 1, 1, 0, 0, 2);
    }

    public void playExplosionSound() {
        explosionSoundPool.play(explosionSoundId, 1, 1, 0, 0, 1);
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

    public void addCannonBallToNewCannonballs(Cannonball cannonball){
        newCannonballs.add(cannonball);
    }

    public int getStatus() {
        return status;
    }

    public int getLastOpponentCannonId() {
        return lastOpponentCannonId;
    }

    public void setLastOpponentCannonId(int cannonId) {
        lastOpponentCannonId = cannonId;
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

    public void incrementUserAliveBulletsCount() {
        userAliveBulletsCount += 1;
    }

    public void decrementUserAliveBulletsCount() {
        userAliveBulletsCount -= 1;
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
        status = 1;
        userId = -1;
        opponentId = -1;
    }

    public void addCannonball(Cannonball cannonball) {
        cannonballSet.addCannonball(cannonball);
    }

}
