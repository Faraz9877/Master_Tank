package com.wztlei.tanktrouble.cannonball;

import android.graphics.Canvas;
import android.util.SparseArray;

import com.wztlei.tanktrouble.datahouse.GameData;
import com.wztlei.tanktrouble.tank.UserTank;

import java.util.ArrayList;

public class CannonballSet {

    private SparseArray<Cannonball> mCannonballSet;

    private static final long CANNONBALL_LIFESPAN = 10000;
    private static final String TAG = "WL/CannonballSet";

    /**
     * Initializes the set of cannonballs as represented by a concurrent hash map.
     */
    public CannonballSet() {
        mCannonballSet = new SparseArray<>();
    }

    /**
     * Adds a new cannonball to the set.
     *
     * @param cannonball the cannonball to be added
     */
    public void addCannonball(Cannonball cannonball) {
        mCannonballSet.put(cannonball.getUUID(), cannonball);
    }

    public int getCannonballShooter(int uuid) {
        return mCannonballSet.get(uuid).getShooterID();
    }

    /**
     * Removes a cannonball from the set.
     *
     * @param uuid  the uuid as a string of the cannonball to be removed
     */
    public void remove(int uuid) {
        mCannonballSet.remove(uuid);
    }

    /**
     * Returns the number of cannonballs in the set.
     *
     * @return the number of cannonballs in the set.
     */
    public int size(){
        return mCannonballSet.size();
    }

    /**
     * Updates the location and direction of all the cannonballs,
     * removes cannonballs after they have exceeded their lifespan,
     * and detects if a collision has occurred with the user.
     *
     * @return the uuid string key if a cannonball collided with the user, and null otherwise
     */
    public int updateAndDetectUserCollision(UserTank userTank) {
        int detectedUserCollision = 0;
        long nowTime = System.currentTimeMillis();
        ArrayList<Integer> keysToRemove =  new ArrayList<>();

        // Iterate through all the cannonballs in the set
        for(int i = 0; i < mCannonballSet.size(); i++) {
            int key = mCannonballSet.keyAt(i);
            // get the object by the key.
            Cannonball cannonball = mCannonballSet.get(key);
            long deltaTime = nowTime - cannonball.getFiringTime();

            // Check whether the cannonball has exceeded its lifespan and remove if necessary
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

    /**
     * Draws all the cannonballs onto a canvas.
     *
     * @param canvas the canvas onto which the cannonballs are drawn
     */
    public void draw(Canvas canvas) {
        // Iterate through all the cannonballs in the set and draw them
        for (int i = 0; i < mCannonballSet.size(); i++) {
            mCannonballSet.valueAt(i).draw(canvas);
        }
    }
}
