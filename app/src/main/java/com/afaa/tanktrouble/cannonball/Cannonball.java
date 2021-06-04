package com.afaa.tanktrouble.cannonball;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.map.MapUtils;

import java.util.ArrayList;

public class Cannonball {

    private ArrayList<Coordinate> mPath;
    private int mPrevPathIndex;
    private float mX, mY;
    private float mDeg;
    private long mFiringTime, mLastTime;
    private int mUUID, mShooterID;


    private static final float SPEED =
            UserUtils.scaleGraphicsFloat(Constants.CANNONBALL_SPEED_CONST);
    private static final int RADIUS =
            UserUtils.scaleGraphicsInt(Constants.CANNONBALL_RADIUS_CONST);
    private static final int CANNONBALL_LIFESPAN = Constants.CANNONBALL_LIFESPAN;
    private static final int START_FADING_AGE = 9800;
    private static final int BUFFER = 1000;
    private static final float CANNONBALL_DISTANCE = SPEED * CANNONBALL_LIFESPAN + BUFFER;


    public Cannonball(int x, int y, float deg, int uuid, int shooterID) {
        mPath = generatePath(x, y, deg);
        mPrevPathIndex = 0;
        mX = x;
        mY = y;
        mDeg = deg;
        mFiringTime = System.currentTimeMillis();
        mLastTime = mFiringTime;
        mUUID = uuid;
        mShooterID = shooterID;
    }


    private ArrayList<Coordinate> generatePath(int startX, int startY, float deg) {
        ArrayList<Coordinate> path = new ArrayList<>();
        float x = startX;
        float y = startY;
        float dx = (float) (RADIUS * Math.cos(Math.toRadians(deg)));
        float dy = (float) (RADIUS * Math.sin(Math.toRadians(deg)));
        float travelDistance = 0;


        if (!MapUtils.cannonballWallCollision(x, y, RADIUS)) {
            path.add(new Coordinate(x, y));
        }


        while (travelDistance < CANNONBALL_DISTANCE) {

            if (MapUtils.cannonballWallCollision(x, y, RADIUS)) {

                boolean collisionRetreatX = MapUtils.cannonballWallCollision(x-dx, y, RADIUS);
                boolean collisionRetreatY = MapUtils.cannonballWallCollision(x, y-dy, RADIUS);
                boolean horizontalWallCollision = collisionRetreatX || !collisionRetreatY;
                boolean verticalWallCollision = collisionRetreatY || !collisionRetreatX;


                float xyDist = calcDistance(dx, dy);
                float testX = x, testY = y, testDist = 1;


                while (MapUtils.cannonballWallCollision(testX, testY, RADIUS)) {
                    testX = x - (testDist * dx/xyDist);
                    testY = y - (testDist * dy/xyDist);
                    testDist++;
                }


                x = testX;
                y = testY;
                travelDistance += calcDistance(dx, dy);
                path.add(new Coordinate(x, y));


                if (horizontalWallCollision) {
                    dy = -dy;
                }


                if (verticalWallCollision) {
                    dx = -dx;
                }
            } else {
                x += dx;
                y += dy;
                travelDistance += calcDistance(dx, dy);
            }
        }

        path.add(new Coordinate(x, y));
        return path;
    }


    public void update() {

        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - mLastTime;
        mLastTime = nowTime;


        float movementDist = deltaTime * SPEED;


        while (movementDist > 0) {

            Coordinate prevPathCoord = mPath.get(mPrevPathIndex);
            Coordinate nextPathCoord = mPath.get(mPrevPathIndex + 1);


            float pathDx = nextPathCoord.x - prevPathCoord.x;
            float pathDy = nextPathCoord.y - prevPathCoord.y;
            float pathDist = calcDistance(pathDx, pathDy);


            float dx = movementDist * pathDx / pathDist;
            float dy = movementDist * pathDy / pathDist;
            float prevDist = calcDistance(prevPathCoord.x, prevPathCoord.y, mX+dx, mY+dy);


            if (prevDist > pathDist) {

                mPrevPathIndex++;
                mX = nextPathCoord.x;
                mY = nextPathCoord.y;
                movementDist -= calcDistance(mX, mY, nextPathCoord.x, nextPathCoord.y);
            } else {

                mX += dx;
                mY += dy;
                movementDist = 0;
            }
        }
    }


    public void draw(Canvas canvas) {
        long nowTime = System.currentTimeMillis();
        long ageTime = nowTime - mFiringTime;

//            for (int i = 0; i < mPath.size(); i++) {
//                Paint paint = new Paint();
//                paint.setARGB(255, 255, 0, 0);
//                canvas.drawCircle(mPath.get(i).x, mPath.get(i).y, RADIUS, paint);
//            }
//        }


        if (ageTime <= 9800) {

            Paint paint = new Paint();
            paint.setARGB(255, 0, 0, 0);
            canvas.drawCircle(mX, mY, RADIUS, paint);
        } else {

            int tint = (int) Math.max(255 * (CANNONBALL_LIFESPAN - ageTime)/
                    (CANNONBALL_LIFESPAN-START_FADING_AGE), 0);
            Paint paint = new Paint();
            paint.setARGB(tint, 0, 0, 0);
            canvas.drawCircle(mX, mY, RADIUS, paint);
        }
    }

    public Path getStandardizedPath() {
        ArrayList<Coordinate> standardizedCoords = new ArrayList<>();

        for (Coordinate coordinate : mPath) {
            standardizedCoords.add(coordinate.standardized());
        }

        return new Path(standardizedCoords, mUUID);
    }


    private float calcDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }


    private float calcDistance(float dx, float dy) {
        return (float) Math.sqrt(dx*dx + dy*dy);
    }

    public int getX() {
        return (int) mX;
    }

    public int getY() {
        return (int) mY;
    }

    public float getDeg() {
        return mDeg;
    }

    public Position getPosition() {
        return new Position((int) mX, (int) mY, mDeg);
    }

    public int getRadius() {
        return RADIUS;
    }

    public long getFiringTime() {
        return mFiringTime;
    }

    public int getUUID() {
        return mUUID;
    }

    public int getShooterID() {
        return mShooterID;
    }
}
