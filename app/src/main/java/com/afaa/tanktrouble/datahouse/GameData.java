package com.afaa.tanktrouble.datahouse;

import android.app.Activity;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.SoundPool;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.R;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.cannonball.Cannonball;
import com.afaa.tanktrouble.cannonball.CannonballSet;
import com.afaa.tanktrouble.tank.Tank;
import com.afaa.tanktrouble.tank.UserTank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class GameData {
    private static GameData instance = null;

    private static final int CLIENT_ID = 0;
    private static final int SERVER_ID = 1;

    public static String GAME_OVER_MESSAGE = "#Game Over!#\n";
    public static String TANK_FIRE = "#Tank Fire!#\n";

    private ArrayList<Integer> playerIDs;
    private String userUserName;
    private String opponentUserName;
    private Position userPosition;
    private Position opponentPosition;
    private Integer userAliveBulletsCount;
    private CannonballSet cannonballSet;
    int userId;
    int opponentId;
    int status;
    int lastOpponentCannonId;
    int opponentCannonCounter;
    boolean isServer;
    private BluetoothService btService;
    SoundPool shootSoundPool, explosionSoundPool;
    int shootSoundId, explosionSoundId;

    private GameData() {
        playerIDs = new ArrayList<>(Arrays.asList(SERVER_ID, CLIENT_ID));
        userUserName = "";
        opponentUserName = "";
        userPosition = Tank.getRandomInitialPosition();
        opponentPosition = Tank.getRandomInitialPosition();
        userAliveBulletsCount = 0;
        cannonballSet = new CannonballSet();
        status = 0;
        lastOpponentCannonId = -1;
        opponentCannonCounter = 0;
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

    public void signalEnd() {
        if (btService == null)
            return;

        btService.getChannel().send(GAME_OVER_MESSAGE.getBytes());
    }


    public void syncPosition(){
        if(btService == null || userPosition == null)
            return;
        String token = DataProtocol.tokenizePosition(userPosition);
        btService.getChannel().send(token.getBytes());
    }


    public void syncCannonBall(Cannonball cannonball) {
        if(btService == null || cannonball == null)
            return;
        String token = DataProtocol.tokenizeCannonBall(cannonball);
        btService.getChannel().send(token.getBytes());

    }

    public void signalTankFire() {
        btService.getChannel().send(TANK_FIRE.getBytes());
    }

    public String getOpponentUserName() {
        return opponentUserName;
    }

    public void setOpponentUserName(String opponentUserName) {
        this.opponentUserName = opponentUserName;
    }

    public void setServer(boolean server) {
        isServer = server;
        if (isServer){
            userId =  SERVER_ID;
            opponentId = CLIENT_ID;
        }
        else{
            userId =  CLIENT_ID;
            opponentId = SERVER_ID;
        }

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


    public int getStatus() {
        return status;
    }

    public Cannonball opponentTankFire(){
        PointF[] tankPolygon = Tank.tankPolygon(
                getOpponentPosition().x,
                getOpponentPosition().y,
                getOpponentPosition().deg,
                Math.max(UserUtils.scaleGraphicsInt(Constants.TANK_WIDTH_CONST), 1),
                Math.max(UserUtils.scaleGraphicsInt(Constants.TANK_HEIGHT_CONST), 1));
        Cannonball cannonball = new Cannonball((int) tankPolygon[0].x,
                (int) tankPolygon[0].y,
                getOpponentPosition().deg,
                getOpponentId() + opponentCannonCounter * 2, getOpponentId());
        opponentCannonCounter++;
        return cannonball;
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

    public void setUserPosition(Position position) {
        userPosition = position;
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

    public void resetUserAliveBulletsCount() {
        userAliveBulletsCount = 0;
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
