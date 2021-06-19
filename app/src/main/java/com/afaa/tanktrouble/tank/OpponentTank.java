package com.afaa.tanktrouble.tank;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.datahouse.GameData;

public class OpponentTank extends Tank {

    private static final String TAG = "WL/OpponentTank";
    private int opponentID;

    public OpponentTank(Activity activity, int _opponentId, TankColor tankColor) {
        mWidth = Math.max(UserUtils.scaleGraphicsInt(TANK_WIDTH_CONST), 1);
        mHeight = Math.max(UserUtils.scaleGraphicsInt(TANK_HEIGHT_CONST), 1);
        opponentID = _opponentId;
        mBitmap = tankColor.getTankBitmap(activity);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mWidth, mHeight, false);
        mColorIndex = tankColor.getIndex();
        mScore = INITIAL_SCORE;
        mIsAlive = true;
        if (opponentID > -1) {
            addPosDataRefListeners();

            Log.d(TAG, "opponentId=" + opponentID);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }
    }

    private void addPosDataRefListeners() {
        Position position = GameData.getInstance().getOpponentPosition();
        if (position != null) {
            position = position.scalePosition();
            mX = (int) position.x;
            mY = (int) position.y;
            mDeg = position.deg;
        }
    }

//    public void kill() {
//        mIsAlive = false;
//    }

    public void respawn() {
        mIsAlive = true;
    }
}
