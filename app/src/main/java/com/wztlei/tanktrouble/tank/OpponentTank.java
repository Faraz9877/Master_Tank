package com.wztlei.tanktrouble.tank;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.Position;
import com.wztlei.tanktrouble.datahouse.GameData;

public class OpponentTank extends Tank {

    private static final String TAG = "WL/OpponentTank";
    private int opponentID;

    /**
     * @param activity      the activity in which the opponent tank is initialized
     * @param _opponentId    the Firebase ID of the opponent
     */
    public OpponentTank(Activity activity, int _opponentId, TankColor tankColor) {
        mWidth = Math.max(UserUtils.scaleGraphicsInt(TANK_WIDTH_CONST), 1);
        mHeight = Math.max(UserUtils.scaleGraphicsInt(TANK_HEIGHT_CONST), 1);

        opponentID = _opponentId;
        // Get the tank bitmap
        mBitmap = tankColor.getTankBitmap(activity);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mWidth, mHeight, false);
        mColorIndex = tankColor.getIndex();
        mScore = 0;
        mIsAlive = true;

        if (opponentID > -1) {
            addPosDataRefListeners();

            Log.d(TAG, "opponentId=" + opponentID);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }
    }

    /**
     * Add listeners for the position of the opponent tank.
     */
    // FARAZ: Used for updating opponent tank positions. Should be Transferred to GameData.sync()
    private void addPosDataRefListeners() {
        // Get position object and use the values to update the UI
        Position position = GameData.getInstance().getPlayerPosition(opponentID);

        if (position != null) {
            position.scalePosition();
            mX = (int) position.x;
            mY = (int) position.y;
            mDeg = (int) position.deg;
        }
    }

    public void kill() {
        mIsAlive = false;
    }

    public void respawn() {
        mIsAlive = true;
    }
}
