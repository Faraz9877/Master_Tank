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
        width = Math.max(UserUtils.scaleGraphicsInt(TANK_WIDTH_CONST), 1);
        height = Math.max(UserUtils.scaleGraphicsInt(TANK_HEIGHT_CONST), 1);
        opponentID = _opponentId;
        bitmap = tankColor.getTankBitmap(activity);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        colorIndex = tankColor.getIndex();
        score = INITIAL_SCORE;
        isAlive = true;
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
            x = (int) position.x;
            y = (int) position.y;
            deg = position.deg;
        }
    }

//    public void kill() {
//        mIsAlive = false;
//    }

    public void respawn() {
        isAlive = true;
    }
}
