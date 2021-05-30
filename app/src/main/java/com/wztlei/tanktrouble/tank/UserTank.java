package com.wztlei.tanktrouble.tank;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.PointF;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.Position;
import com.wztlei.tanktrouble.cannonball.Cannonball;
import com.wztlei.tanktrouble.datahouse.GameData;
import com.wztlei.tanktrouble.map.MapUtils;

public class UserTank extends Tank {

    private long lastTime;

    private static final String TAG = "WL/UserTank";
    private static final float SPEED_CONST = UserUtils.scaleGraphicsFloat(40/1080f)/100f;

    /**
     * Constructor function for the User Tank class.
     *
     * @param activity      the activity in which the player tank is instantiated
     */
    public UserTank(Activity activity, TankColor tankColor) {
        mWidth = Math.max(UserUtils.scaleGraphicsInt(TANK_WIDTH_CONST), 1);
        mHeight = Math.max(UserUtils.scaleGraphicsInt(TANK_HEIGHT_CONST), 1);

        // Get the tank bitmap and color
        mBitmap = tankColor.getTankBitmap(activity);
        mBitmap = Bitmap.createScaledBitmap(mBitmap, mWidth, mHeight, false);
        mColorIndex = tankColor.getIndex();
        mScore = 0;
        mIsAlive = true;

        mX = Math.round(GameData.getInstance().getPlayerPositions().get(0).x);
        mX = Math.round(GameData.getInstance().getPlayerPositions().get(0).y);
        mDeg = GameData.getInstance().getPlayerPositions().get(0).deg;

        // Set the initial x and y coordinates for the tank
//        if(GameData.getInstance().getServer())
//        {
            do {
                mX = UserUtils.randomInt(50, UserUtils.getScreenWidth());
                mY = UserUtils.randomInt(UserUtils.scaleGraphicsInt(1.1f * Constants.MAP_TOP_Y_CONST),
                        UserUtils.scaleGraphicsInt(0.9f*Constants.MAP_TOP_Y_CONST + 1));
                mDeg = UserUtils.randomInt(-180, 180);
            } while (MapUtils.tankWallCollision(mX, mY, mDeg, mWidth, mHeight));
            GameData.getInstance().setPlayerPosition(0, new Position(mX, mY, mDeg));
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

    /**
     * Sets the new x and y coordinates and the new angle for the tank while ensuring that
     * the tank remains within the map boundaries.
     *
     * @param velocityX the velocity that the tank intends to move in the x direction
     * @param velocityY the velocity that the tank intends to move in the y direction
     * @param angle     the new angle of the tank
     */
    public void update(float velocityX, float velocityY, float angle) {
        // Get the difference in time between now and the last movement
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - lastTime;

        // Store the maximum displacement allowed in the x and y directions
        int maxDeltaX = Math.round(velocityX*deltaTime*SPEED_CONST);
        int maxDeltaY = Math.round(velocityY*deltaTime*SPEED_CONST);

        // Initialize variables for the while loop
        int deltaX = 0, deltaY = 0;
        int unitX = (int) Math.signum(maxDeltaX), unitY = (int) Math.signum(maxDeltaY);
        boolean reachedMaxX = false, reachedMaxY = false;

        // Loop until we have reached the max x and max y displacement
        while (!reachedMaxX || !reachedMaxY) {
            // Check whether we can still move in the x direction
            if (!reachedMaxX) {

                // Try increasing the movement in the x direction
                deltaX += unitX;

                // Check whether the new position collides with a wall
                if (MapUtils.tankWallCollision(mX + deltaX, mY, angle, mWidth, mHeight)) {
                    // Record that an invalid position has been reached and reset deltaX
                    reachedMaxX = true;
                    deltaX -= unitX;
                }

                // Check whether we have reached the maximum allowed value of deltaX
                if (deltaX == maxDeltaX) {
                    reachedMaxX = true;
                }
            }

            // Check whether we can still move in the y direction
            if (!reachedMaxY) {

                // Try increasing the movement in the y direction
                deltaY += unitY;

                // Check whether the new position collides with a wall
                if (MapUtils.tankWallCollision(mX, mY + deltaY, angle, mWidth, mHeight)) {
                    // Record that an invalid position has been reached and reset deltaY
                    reachedMaxY = true;
                    deltaY -= unitY;
                }

                // Check whether we have reached the maximum allowed value of deltaY
                if (deltaY == maxDeltaY) {
                    reachedMaxY = true;
                }
            }
        }

        // Check whether movement or rotation is possible with either deltaX, deltaY, or angle
        // and move accordingly
        if (deltaX != 0 || deltaY != 0 || (velocityX == 0 && velocityY == 0 &&
                !MapUtils.tankWallCollision(mX, mY, angle, mWidth, mHeight))) {
            mX += deltaX;
            mY += deltaY;
            mDeg = angle;
        } else {
            // Get the original polygon and get the two old centers of rotation
            PointF[] oldPolygon = Tank.tankPolygon(mX, mY, mDeg, mWidth, mHeight);
            PointF oldFrontCenter = oldPolygon[0];
            PointF oldMidCenter = oldPolygon[1];

            // Get the new polygon with rotation, but not movement,
            // and get the two new centers of rotation
            PointF[] newPolygon = Tank.tankPolygon(mX, mY, angle, mWidth, mHeight);
            PointF newFrontCenter = newPolygon[0];
            PointF newMidCenter = newPolygon[1];

            // Get test values of x and y where the rotation occurs,
            // but the rotational centers remain unmoved
            int testX1 = Math.round(mX + oldMidCenter.x - newMidCenter.x);
            int testY1 = Math.round(mY + oldMidCenter.y - newMidCenter.y);
            int testX2 = Math.round(mX + oldFrontCenter.x - newFrontCenter.x);
            int testY2 = Math.round(mY + oldFrontCenter.y - newFrontCenter.y);

            // Try rotating while keeping either rotational center unmoved
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
        GameData.getInstance().setPosition(position);
        lastTime = nowTime;
    }

    /**
     * Returns true if a cannonball hit the tank and false otherwise.
     *
     * @param   cannonball         the x coordinate of the cannonball
     * @return                      true if a cannonball hit the tank and false otherwise
     */
    public boolean detectCollision(Cannonball cannonball) {
        PointF[] hitbox = Tank.tankHitbox(mX, mY, mDeg, mWidth, mHeight);
        int cannonballX = cannonball.getX();
        int cannonballY = cannonball.getY();
        int cannonballRadius = cannonball.getRadius();

        // Iterate through all the points in the tank's hitbox
        for (PointF pointF : hitbox) {
            if (calcDistance(pointF.x, pointF.y, cannonballX, cannonballY) < cannonballRadius) {
                return true;
            }
        }

        return false;
    }

    /**
     * Calculates the Euclidean distance between two points, given a displacement in the x-axis
     * and a displacement in the y-axis using the Pythagorean theorem.
     *
     * @param x1    the x-coordinate of the first point
     * @param y1    the y-coordinate of the first point
     * @param x2    the x-coordinate of the second point
     * @param y2    the y-coordinate of the second point
     * @return      the distance between the two points
     */
    private static float calcDistance (float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }

    /**
     * Returns the position of the front of the gun when the tank fired and
     * sets the position value in Firebase to the location where the user last fired.
     */
    public Cannonball fire() {
        PointF[] tankPolygon = Tank.tankPolygon(mX, mY, mDeg, mWidth, mHeight);

        Cannonball c = new Cannonball((int) tankPolygon[0].x, (int) tankPolygon[0].y, mDeg,
                UserUtils.randomInt(1, Integer.MAX_VALUE-10), GameData.getInstance().getThisPlayer());

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
