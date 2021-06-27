package com.afaa.tanktrouble.tank;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.cannonball.Cannonball;
import com.afaa.tanktrouble.datahouse.GameData;
import com.afaa.tanktrouble.map.MapUtils;

public class UserTank extends Tank {

    private static final String TAG = "WL/UserTank";
    private static final float SPEED_CONST = UserUtils.scaleGraphicsFloat(40/1080f)/100f;

    private int cannonCounter;
    private final int userId;

    public UserTank(Activity activity, TankColor tankColor, int userId) {
        width = Math.max(UserUtils.scaleGraphicsInt(TANK_WIDTH_CONST), 1);
        height = Math.max(UserUtils.scaleGraphicsInt(TANK_HEIGHT_CONST), 1);

        bitmap = tankColor.getTankBitmap(activity);
        bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
        colorIndex = tankColor.getIndex();
        score = INITIAL_SCORE;
        isAlive = true;

        x = Math.round(GameData.getInstance().getUserPosition().x);
        x = Math.round(GameData.getInstance().getUserPosition().y);
        deg = GameData.getInstance().getUserPosition().deg;
        cannonCounter = 0;
        this.userId = userId;

        do {
            x = UserUtils.randomInt(50, UserUtils.getScreenWidth());
            y = UserUtils.randomInt(UserUtils.scaleGraphicsInt(1.1f * Constants.MAP_TOP_Y_CONST),
                    UserUtils.scaleGraphicsInt(0.9f * Constants.MAP_TOP_Y_CONST + 1));
            deg = UserUtils.randomInt(-180, 180);
        } while (MapUtils.tankWallCollision(x, y, deg, width, height));
        GameData.getInstance().setUserPosition(new Position(x, y, deg));

    }


    public void update(float velocityX, float velocityY, float angle) {
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - lastTime;

        int maxDeltaX = Math.round(velocityX*deltaTime*SPEED_CONST);
        int maxDeltaY = Math.round(velocityY*deltaTime*SPEED_CONST);

        int deltaX = 0, deltaY = 0;
        int unitX = (int) Math.signum(maxDeltaX), unitY = (int) Math.signum(maxDeltaY);
        boolean reachedMaxX = false, reachedMaxY = false;

        while (!reachedMaxX || !reachedMaxY) {
            if (!reachedMaxX) {
                deltaX += unitX;
                if (MapUtils.tankWallCollision(x + deltaX, y, angle, width, height)) {
                    reachedMaxX = true;
                    deltaX -= unitX;
                }
                if (deltaX == maxDeltaX) {
                    reachedMaxX = true;
                }
            }

            if (!reachedMaxY) {
                deltaY += unitY;
                if (MapUtils.tankWallCollision(x, y + deltaY, angle, width, height)) {
                    reachedMaxY = true;
                    deltaY -= unitY;
                }
                if (deltaY == maxDeltaY) {
                    reachedMaxY = true;
                }
            }
        }

        if (deltaX != 0 || deltaY != 0 || (velocityX == 0 && velocityY == 0 &&
                !MapUtils.tankWallCollision(x, y, angle, width, height))) {
            x += deltaX;
            y += deltaY;
            deg = angle;
        } else {

            PointF[] oldPolygon = Tank.tankPolygon(x, y, deg, width, height);
            PointF oldFrontCenter = oldPolygon[0];
            PointF oldMidCenter = oldPolygon[1];
            PointF[] newPolygon = Tank.tankPolygon(x, y, angle, width, height);
            PointF newFrontCenter = newPolygon[0];
            PointF newMidCenter = newPolygon[1];

            int testX1 = Math.round(x + oldMidCenter.x - newMidCenter.x);
            int testY1 = Math.round(y + oldMidCenter.y - newMidCenter.y);
            int testX2 = Math.round(x + oldFrontCenter.x - newFrontCenter.x);
            int testY2 = Math.round(y + oldFrontCenter.y - newFrontCenter.y);
            if (!MapUtils.tankWallCollision(testX1, testY1, angle, width, height)) {
                x = testX1;
                y = testY1;
                deg = angle;
            } else if (!MapUtils.tankWallCollision(testX2, testY2, angle, width, height)) {
                x = testX2;
                y = testY2;
                deg = angle;
            }
        }

        Position position = new Position(x, y, deg);
//        position = position.standardizePosition();
        GameData.getInstance().setUserPosition(position);
        lastTime = nowTime;
    }


    public Cannonball fire() {
        PointF[] tankPolygon = Tank.tankPolygon(x, y, deg, width, height);

        return new Cannonball((int) tankPolygon[0].x, (int) tankPolygon[0].y, deg,
                         userId + (cannonCounter++) * 2, GameData.getInstance().getUserId());

    }

    public void respawn() {
        isAlive = true;
    }
}
