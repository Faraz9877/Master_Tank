package com.afaa.tanktrouble.cannonball;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.map.MapUtils;

import java.util.ArrayList;

public class Cannonball {

    private final ArrayList<Coordinate> path;
    private int prevPathIndex;
    private float x, y;
    private final float deg;
    private long firingTime, lastTime;
    private final int UUID, shooterID;


    private static final float SPEED =
            UserUtils.scaleGraphicsFloat(Constants.CANNONBALL_SPEED_CONST);
    private static final int RADIUS =
            UserUtils.scaleGraphicsInt(Constants.CANNONBALL_RADIUS_CONST);
    private static final int CANNONBALL_LIFESPAN = Constants.CANNONBALL_LIFESPAN;
    private static final int START_FADING_AGE = 9800;
    private static final int BUFFER = 1000;
    private static final float CANNONBALL_DISTANCE = SPEED * CANNONBALL_LIFESPAN + BUFFER;


    public Cannonball(int x, int y, float deg, int uuid, int shooterID) {
        path = generatePath(x, y, deg);
        prevPathIndex = 0;
        this.x = x;
        this.y = y;
        this.deg = deg;
        firingTime = System.currentTimeMillis();
        lastTime = firingTime;
        UUID = uuid;
        this.shooterID = shooterID;
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
        long deltaTime = nowTime - lastTime;
        lastTime = nowTime;


        float movementDist = deltaTime * SPEED;


        while (movementDist > 0) {

            Coordinate prevPathCoord = path.get(prevPathIndex);
            Coordinate nextPathCoord = path.get(prevPathIndex + 1);


            float pathDx = nextPathCoord.x - prevPathCoord.x;
            float pathDy = nextPathCoord.y - prevPathCoord.y;
            float pathDist = calcDistance(pathDx, pathDy);


            float dx = movementDist * pathDx / pathDist;
            float dy = movementDist * pathDy / pathDist;
            float prevDist = calcDistance(prevPathCoord.x, prevPathCoord.y, x +dx, y +dy);


            if (prevDist > pathDist) {

                prevPathIndex++;
                x = nextPathCoord.x;
                y = nextPathCoord.y;
                movementDist -= calcDistance(x, y, nextPathCoord.x, nextPathCoord.y);
            } else {

                x += dx;
                y += dy;
                movementDist = 0;
            }
        }
    }


    public void draw(Canvas canvas) {
        long nowTime = System.currentTimeMillis();
        long ageTime = nowTime - firingTime;

//            for (int i = 0; i < mPath.size(); i++) {
//                Paint paint = new Paint();
//                paint.setARGB(255, 255, 0, 0);
//                canvas.drawCircle(mPath.get(i).x, mPath.get(i).y, RADIUS, paint);
//            }
//        }


        if (ageTime <= 9800) {

            Paint paint = new Paint();
            paint.setARGB(255, 0, 0, 0);
            canvas.drawCircle(x, y, RADIUS, paint);
        } else {

            int tint = (int) Math.max(255 * (CANNONBALL_LIFESPAN - ageTime)/
                    (CANNONBALL_LIFESPAN-START_FADING_AGE), 0);
            Paint paint = new Paint();
            paint.setARGB(tint, 0, 0, 0);
            canvas.drawCircle(x, y, RADIUS, paint);
        }
    }

    public Path getStandardizedPath() {
        ArrayList<Coordinate> standardizedCoords = new ArrayList<>();

        for (Coordinate coordinate : path) {
            standardizedCoords.add(coordinate.standardized());
        }

        return new Path(standardizedCoords, UUID);
    }


    private float calcDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
    }


    private float calcDistance(float dx, float dy) {
        return (float) Math.sqrt(dx*dx + dy*dy);
    }

    public int getX() {
        return (int) x;
    }

    public int getY() {
        return (int) y;
    }

    public float getDeg() {
        return deg;
    }

    public Position getPosition() {
        return new Position((int) x, (int) y, deg);
    }

    public int getRadius() {
        return RADIUS;
    }

    public long getFiringTime() {
        return firingTime;
    }

    public void setFiringTime(long firingTime) {
        this.firingTime = firingTime;
    }

    public int getUUID() {
        return UUID;
    }

    public int getShooterID() {
        return shooterID;
    }
}
