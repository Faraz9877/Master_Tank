package com.afaa.tanktrouble.cannonball;

import android.graphics.Canvas;
import android.util.SparseArray;

import com.afaa.tanktrouble.datahouse.GameData;
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
        if(mCannonballSet.get(cannonball.getUUID()) == null)
            mCannonballSet.put(cannonball.getUUID(), cannonball);
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


    public int updateAndDetectUserCollision(UserTank userTank) {
        int detectedUserCollision = 0;
        long nowTime = System.currentTimeMillis();
        ArrayList<Integer> keysToRemove =  new ArrayList<>();


        for(int i = 0; i < mCannonballSet.size(); i++) {
            int key = mCannonballSet.keyAt(i);

            Cannonball cannonball = mCannonballSet.get(key);
            long deltaTime = nowTime - cannonball.getFiringTime();


            if (deltaTime > CANNONBALL_LIFESPAN) {
                keysToRemove.add(key);
            } else {
                cannonball.update();

                if (userTank.detectCollision(cannonball)) {
                    detectedUserCollision = key;
                    keysToRemove.add(key);
                }
            }
        }

        for (int i = 0; i < keysToRemove.size(); i++) {
            GameData.getInstance().decrementUserAliveBullets();
            mCannonballSet.remove(keysToRemove.get(i));
        }

        return detectedUserCollision;
    }


    public void draw(Canvas canvas) {

        for (int i = 0; i < mCannonballSet.size(); i++) {
            mCannonballSet.valueAt(i).draw(canvas);
        }
    }
}
