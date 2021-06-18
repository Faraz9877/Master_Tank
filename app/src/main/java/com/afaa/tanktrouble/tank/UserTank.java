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

//    private long lastTime;

    private static final String TAG = "WL/UserTank";
    private static final float SPEED_CONST = UserUtils.scaleGraphicsFloat(40/1080f)/100f;


    public UserTank(Activity activity, TankColor tankColor) {
        mWidth = Math.max(UserUtils.scaleGraphicsInt(TANK_WIDTH_CONST), 1);
        mHeight = Math.max(UserUtils.scaleGraphicsInt(TANK_HEIGHT_CONST), 1);


        mBitmap = tankColor.getTankBitmap(activity);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mWidth, mHeight, false);
        mColorIndex = tankColor.getIndex();
        mScore = 0;
        mIsAlive = true;

        mX = Math.round(GameData.getInstance().getUserPosition().x);
        mX = Math.round(GameData.getInstance().getUserPosition().y);
        mDeg = GameData.getInstance().getUserPosition().deg;


//        {
            do {
                mX = UserUtils.randomInt(50, UserUtils.getScreenWidth());
                mY = UserUtils.randomInt(UserUtils.scaleGraphicsInt(1.1f * Constants.MAP_TOP_Y_CONST),
                        UserUtils.scaleGraphicsInt(0.9f*Constants.MAP_TOP_Y_CONST + 1));
                mDeg = UserUtils.randomInt(-180, 180);
            } while (MapUtils.tankWallCollision(mX, mY, mDeg, mWidth, mHeight));
            GameData.getInstance().setUserPosition(new Position(mX, mY, mDeg));
//        }
    }

    public static Position getRandomInitialPosition() {
        int x, y, deg;
        do {
            x = UserUtils.randomInt(50, UserUtils.getScreenWidth());
            y = UserUtils.randomInt(UserUtils.scaleGraphicsInt(1.1f * Constants.MAP_TOP_Y_CONST),
                    UserUtils.scaleGraphicsInt(0.9f*Constants.MAP_TOP_Y_CONST + 1));
            deg = UserUtils.randomInt(-180, 180);
        } while (MapUtils.tankWallCollision(x, y, deg,
                Math.max(UserUtils.scaleGraphicsInt(TANK_WIDTH_CONST), 1),
                Math.max(UserUtils.scaleGraphicsInt(TANK_HEIGHT_CONST), 1)));
        return new Position(x, y, deg);
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


                if (MapUtils.tankWallCollision(mX + deltaX, mY, angle, mWidth, mHeight)) {

                    reachedMaxX = true;
                    deltaX -= unitX;
                }


                if (deltaX == maxDeltaX) {
                    reachedMaxX = true;
                }
            }


            if (!reachedMaxY) {


                deltaY += unitY;


                if (MapUtils.tankWallCollision(mX, mY + deltaY, angle, mWidth, mHeight)) {

                    reachedMaxY = true;
                    deltaY -= unitY;
                }


                if (deltaY == maxDeltaY) {
                    reachedMaxY = true;
                }
            }
        }



        if (deltaX != 0 || deltaY != 0 || (velocityX == 0 && velocityY == 0 &&
                !MapUtils.tankWallCollision(mX, mY, angle, mWidth, mHeight))) {
            mX += deltaX;
            mY += deltaY;
            mDeg = angle;
        } else {

            PointF[] oldPolygon = Tank.tankPolygon(mX, mY, mDeg, mWidth, mHeight);
            PointF oldFrontCenter = oldPolygon[0];
            PointF oldMidCenter = oldPolygon[1];



            PointF[] newPolygon = Tank.tankPolygon(mX, mY, angle, mWidth, mHeight);
            PointF newFrontCenter = newPolygon[0];
            PointF newMidCenter = newPolygon[1];



            int testX1 = Math.round(mX + oldMidCenter.x - newMidCenter.x);
            int testY1 = Math.round(mY + oldMidCenter.y - newMidCenter.y);
            int testX2 = Math.round(mX + oldFrontCenter.x - newFrontCenter.x);
            int testY2 = Math.round(mY + oldFrontCenter.y - newFrontCenter.y);


            if (!MapUtils.tankWallCollision(testX1, testY1, angle, mWidth, mHeight)) {
                mX = testX1;
                mY = testY1;
                mDeg = angle;
            } else if (!MapUtils.tankWallCollision(testX2, testY2, angle, mWidth, mHeight)) {
                mX = testX2;
                mY = testY2;
                mDeg = angle;
            }
        }

        Position position = new Position(mX, mY, mDeg);
//        position = position.standardizePosition();
        GameData.getInstance().setUserPosition(position);
        lastTime = nowTime;
    }


    public boolean detectCollision(Cannonball cannonball) {
        PointF[] hitbox = Tank.tankHitbox(mX, mY, mDeg, mWidth, mHeight);
        int cannonballX = cannonball.getX();
        int cannonballY = cannonball.getY();
        int cannonballRadius = cannonball.getRadius();


        for (PointF pointF : hitbox) {
            if (calcDistance(pointF.x, pointF.y, cannonballX, cannonballY) < cannonballRadius) {
                return true;
            }
        }

        return false;
    }


    private static float calcDistance (float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }


    public Cannonball fire() {
        PointF[] tankPolygon = Tank.tankPolygon(mX, mY, mDeg, mWidth, mHeight);

        Cannonball c = new Cannonball((int) tankPolygon[0].x, (int) tankPolygon[0].y, mDeg,
                UserUtils.randomInt(1, Integer.MAX_VALUE-10), GameData.getInstance().getUserId());

        return c;
    }

    public void kill(int killingCannonball) {
//        if (GameData.getInstance().getCannonballSet().getCannonballShooter(killingCannonball)
//                == GameData.getInstance().getThisPlayer())
            incrementScore();
        mIsAlive = false;
    }

    public void respawn() {
        mIsAlive = true;
    }
}
