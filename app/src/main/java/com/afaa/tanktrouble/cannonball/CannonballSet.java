package com.afaa.tanktrouble.cannonball;

import android.graphics.Canvas;
import android.util.SparseArray;

import com.afaa.tanktrouble.datahouse.GameData;
import com.afaa.tanktrouble.tank.Tank;

import java.util.ArrayList;

public class CannonballSet {

    private SparseArray<Cannonball> cannons;

    private static final long CANNONBALL_LIFESPAN = 10000;
    private static final String TAG = "WL/CannonballSet";


    public CannonballSet() {
        cannons = new SparseArray<>();
    }


    public void addCannonball(Cannonball cannonball) {
        if(cannons.get(cannonball.getUUID()) == null) {
            cannons.put(cannonball.getUUID(), cannonball);
            GameData.getInstance().playShootSound();
        }

    }

    public int getCannonballShooter(int uuid) {
        return cannons.get(uuid).getShooterID();
    }


    public void remove(int uuid) {
        cannons.remove(uuid);
    }


    public int size(){
        return cannons.size();
    }


    public boolean updateAndDetectUserCollision(Tank tank) {
        boolean collisionHappened = false;
        long nowTime = System.currentTimeMillis();
        ArrayList<Integer> keysToRemove =  new ArrayList<>();

        for(int i = 0; i < cannons.size(); i++) {
            int key = cannons.keyAt(i);

            Cannonball cannonball = cannons.get(key);
            long deltaTime = nowTime - cannonball.getFiringTime();


            if (deltaTime > CANNONBALL_LIFESPAN) {
                keysToRemove.add(key);
                if (cannonball.getShooterID() == GameData.getInstance().getUserId())
                    GameData.getInstance().decrementUserAliveBulletsCount();
            }
            else {
                cannonball.update();
                if (tank.detectCollision(cannonball)) {
                    collisionHappened = true;

                    keysToRemove.add(key);
                    if (cannonball.getShooterID() == GameData.getInstance().getUserId())
                        GameData.getInstance().decrementUserAliveBulletsCount();
                }
            }
        }

        for (int i = 0; i < keysToRemove.size(); i++) {
            cannons.remove(keysToRemove.get(i));
        }

        return collisionHappened;
    }

    public void clear() {
        cannons.clear();
    }


    public void draw(Canvas canvas) {

        for (int i = 0; i < cannons.size(); i++) {
            cannons.valueAt(i).draw(canvas);
        }
    }
}
