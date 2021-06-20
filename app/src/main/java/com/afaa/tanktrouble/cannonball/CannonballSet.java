package com.afaa.tanktrouble.cannonball;

import android.graphics.Canvas;
import android.util.SparseArray;

import com.afaa.tanktrouble.datahouse.GameData;
import com.afaa.tanktrouble.tank.Tank;
import com.afaa.tanktrouble.tank.UserTank;

import java.util.ArrayList;

public class CannonballSet {

    private SparseArray<Cannonball> mCannonballSet;

    private static final long CANNONBALL_LIFESPAN = 10000;
    private static final String TAG = "WL/CannonballSet";


    public CannonballSet() {
        mCannonballSet = new SparseArray<>();
    }


    public void addCannonball(Cannonball cannonball) {
        if(mCannonballSet.get(cannonball.getUUID()) == null) {
            mCannonballSet.put(cannonball.getUUID(), cannonball);
            GameData.getInstance().playShootSound();
        }

    }

    public int getCannonballShooter(int uuid) {
        return mCannonballSet.get(uuid).getShooterID();
    }


    public void remove(int uuid) {
        mCannonballSet.remove(uuid);
    }


    public int size(){
        return mCannonballSet.size();
    }


    public boolean updateAndDetectUserCollision(Tank tank) {
        boolean collisionHappened = false;
        long nowTime = System.currentTimeMillis();
        ArrayList<Integer> keysToRemove =  new ArrayList<>();

        for(int i = 0; i < mCannonballSet.size(); i++) {
            int key = mCannonballSet.keyAt(i);

            Cannonball cannonball = mCannonballSet.get(key);
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
            mCannonballSet.remove(keysToRemove.get(i));
        }

        return collisionHappened;
    }

    public void clear() {
        mCannonballSet.clear();
    }


    public void draw(Canvas canvas) {

        for (int i = 0; i < mCannonballSet.size(); i++) {
            mCannonballSet.valueAt(i).draw(canvas);
        }
    }
}
