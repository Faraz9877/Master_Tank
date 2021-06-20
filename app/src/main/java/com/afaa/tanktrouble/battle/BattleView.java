package com.afaa.tanktrouble.battle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.R;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.cannonball.Cannonball;
import com.afaa.tanktrouble.datahouse.GameData;
import com.afaa.tanktrouble.map.MapUtils;
import com.afaa.tanktrouble.tank.OpponentTank;
import com.afaa.tanktrouble.tank.TankColor;
import com.afaa.tanktrouble.tank.UserTank;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

@SuppressLint("ViewConstructor")
public class BattleView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private Bitmap mFireBitmap, mFirePressedBitmap;
    private BattleThread mBattleThread;
    private UserTank mUserTank;
    private OpponentTank mOpponentTank;
    private HashSet<ExplosionAnimation> mExplosionAnimations;
    private Paint mJoystickColor;
//    private int mKillingCannonball;
    private int mJoystickBaseCenterX, mJoystickBaseCenterY;
    private int mFireButtonOffsetX, mFireButtonOffsetY;
    private int mJoystickX, mJoystickY;
    private int mJoystickPointerId, mFireButtonPointerId;
    private int mJoystickBaseRadius, mJoystickThresholdRadius, mJoystickMaxDisplacement;
    private int mFireButtonDiameter, mFireButtonPressedDiameter;
    private float mUserDeg;
    private boolean mFireButtonPressed;

    Activity mActivity;

    private static final String TAG = "WL/BattleView";
    private static final float TOP_Y_CONST = Constants.MAP_TOP_Y_CONST;
    private static final float JOYSTICK_BASE_RADIUS_CONST = (float) 165/543;
    private static final float JOYSTICK_THRESHOLD_RADIUS_CONST = (float) 220/543;
    private static final float JOYSTICK_DISPLACEMENT_CONST = 0.9f;
    private static final float FIRE_BUTTON_DIAMETER_CONST = (float) 200/543;
    private static final float FIRE_BUTTON_PRESSED_DIAMETER_CONST = (float) 150/543;
    private static final float CONTROL_MARGIN_X_CONST = (float) 125/1080;
    private static final float SCORE_MARGIN_Y_CONST = (float) 50/1080;
    private static final float SCORE_TEXT_MARGIN_Y_CONST = (float) 20/1080;
    private static final float SCORE_TEXT_SIZE_CONST = (float) 50/1080;
    private static final float NUM_TANK_COLORS = 4;
    private static final int MAX_USER_CANNONBALLS = 5;


    public BattleView(Activity activity) {
        super(activity);
        mActivity = activity;
        UserUtils.initialize(activity);
        ExplosionAnimation.initialize(activity);
        mJoystickColor = TankColor.BLUE.getPaint();
//        mKillingCannonball = 0;
        mExplosionAnimations = new HashSet<>();
        addEnteringTanks(activity);
        getHolder().addCallback(this);
        mBattleThread = new BattleThread(getHolder(), this);
        setFocusable(true);
        setControlGraphicsData(activity);
        setOnTouchListener(this);
        GameData.getInstance().createSoundPool(activity);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mBattleThread.getState() == Thread.State.NEW) {
            mBattleThread.setRunning(true);
            mBattleThread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        Log.d(TAG, "surfaceDestroyed");
        boolean retry = true;

        while (retry) {
            try {
                mBattleThread.setRunning(false);
                mBattleThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            retry = false;
        }


//        removeGame();
    }

    public void terminateShowResult(Boolean win){
        Intent intent = new Intent(mActivity, ResultActivity.class);
        if (win)
            intent.putExtra("win", 1);
        else
            intent.putExtra("win", 0);
        mActivity.startActivity(intent);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        MapUtils.drawMap(canvas);
        drawFireButton(canvas);
        drawJoystick(canvas);

        boolean userTankHit = GameData.getInstance().getCannonballSet().updateAndDetectUserCollision(mUserTank);
        boolean opponentTankHit = GameData.getInstance().getCannonballSet().updateAndDetectUserCollision(mOpponentTank);

        GameData.getInstance().getCannonballSet().draw(canvas);

        updateUserTank();
        GameData.getInstance().syncPosition();
        mUserTank.draw(canvas);

        if (!userTankHit) {
//            updateUserTank();
//            mUserTank.draw(canvas);
            mUserTank.respawn();
        }
        else {
            mUserTank.kill();
            mExplosionAnimations.add(new ExplosionAnimation(mUserTank));
            if (mUserTank.getScore() == 0){
                terminateShowResult(false);
            }
        }

        if (!opponentTankHit) {
            mOpponentTank.draw(canvas);
        }
        else {
            mOpponentTank.kill();
            mExplosionAnimations.add(new ExplosionAnimation(mOpponentTank));
            if (mOpponentTank.getScore() == 0){
                terminateShowResult(true);
            }
        }

//        if (mKillingCannonball == 0) {
//            if (mUserTank != null) {
//                updateUserTank();
//                mUserTank.draw(canvas);
//                mUserTank.respawn();
//            }
//
//            GameData.getInstance().getCannonballSet().draw(canvas);
//            mKillingCannonball = GameData.getInstance().getCannonballSet().updateAndDetectUserCollision(mUserTank);
//        }
//        else {
//            if (mUserTank != null && mUserTank.isAlive()) {
//                updateUserTank();
//                mUserTank.draw(canvas);
//                mUserTank.kill(mKillingCannonball);
//                mExplosionAnimations.add(new ExplosionAnimation(mUserTank));
//            }
//
//            GameData.getInstance().getCannonballSet().updateAndDetectUserCollision(mUserTank);
//            GameData.getInstance().getCannonballSet().draw(canvas);
//            mKillingCannonball = 0;
//        }

//        if (mOpponentTank != null && mOpponentTank.isAlive()) {
//            mOpponentTank.draw(canvas);
//        }

//        GameData.getInstance().sync(1111);
        drawExplosions(canvas);
        mOpponentTank.updatePosition(Math.round(GameData.getInstance().getOpponentPosition().x),
                Math.round(GameData.getInstance().getOpponentPosition().y),
                GameData.getInstance().getOpponentPosition().deg);
        mOpponentTank.draw(canvas);
        drawScores(canvas);
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int pointerIndex = motionEvent.getActionIndex();
        int pointerId = motionEvent.getPointerId(pointerIndex);

        int pointerX = (int) motionEvent.getX(pointerIndex);
        int pointerY = (int) motionEvent.getY(pointerIndex);

        int action = motionEvent.getActionMasked();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
                if (mJoystickPointerId == MotionEvent.INVALID_POINTER_ID) {
                    updateJoystickData(pointerX, pointerY, pointerId, action);
                }

                if (mFireButtonPointerId == MotionEvent.INVALID_POINTER_ID) {
                    updateFireButtonData(pointerX, pointerY, pointerId);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (int index = 0; index < motionEvent.getPointerCount(); index++) {
                    int id = motionEvent.getPointerId(index);
                    int x = (int) motionEvent.getX(index);
                    int y = (int) motionEvent.getY(index);

                    if (id == mJoystickPointerId) {
                        updateJoystickData(x, y, id, action);
                    } else if (mJoystickPointerId == MotionEvent.INVALID_POINTER_ID) {
                        updateJoystickData(x, y, id, action);
                    } else if (id == mFireButtonPointerId){
                        updateFireButtonData(x, y, id);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (pointerId == mJoystickPointerId) {
                    mJoystickX = mJoystickBaseCenterX;
                    mJoystickY = mJoystickBaseCenterY;
                    mJoystickPointerId = MotionEvent.INVALID_POINTER_ID;
                } else if (pointerId == mFireButtonPointerId){
                    mFireButtonPressed = false;
                    mFireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
                }
                break;

            default:
                break;
        }

        return true;
    }

    private void addEnteringTanks(final Activity activity) {
        final int BLUE_INDEX_JOYSTICK_COLOR = 0;
        mJoystickColor = TankColor.values()[BLUE_INDEX_JOYSTICK_COLOR].getPaint();
        if (mUserTank == null) {
            TankColor tankColor = TankColor.values()[GameData.getInstance().getUserId()];
            mUserTank = new UserTank(activity, tankColor, GameData.getInstance().getUserId());
            mUserDeg = mUserTank.getDegrees();

        }
        if (mOpponentTank == null) {
            TankColor tankColor = TankColor.values()[GameData.getInstance().getOpponentId()];
            mOpponentTank = new OpponentTank(activity,
                    GameData.getInstance().getOpponentId(), tankColor);
//            addDeathDataRefListener(GameData.getInstance().getOpponentId());
        }
    }

//    private void addDeathDataRefListener(final int opponentId) {
//        Integer killingCannonball = 0;
//        OpponentTank opponentTank = mOpponentTank;
//        if (killingCannonball != null && opponentTank != null) {
//            GameData.getInstance().getCannonballSet().remove(killingCannonball);
//            mExplosionAnimations.add(new ExplosionAnimation(opponentTank));
//            opponentTank.incrementScore();
//            opponentTank.kill();
//        } else if (killingCannonball == null && opponentTank != null) {
//            opponentTank.respawn();
//        }
//    }

    private void setControlGraphicsData(Activity activity) {
        float screenHeight = UserUtils.getScreenHeight();
        float screenWidth = UserUtils.getScreenWidth();
        float controlHeight = screenHeight - UserUtils.scaleGraphicsInt(TOP_Y_CONST) - screenWidth;
        mJoystickBaseRadius = Math.round(JOYSTICK_BASE_RADIUS_CONST * controlHeight);
        mJoystickThresholdRadius = Math.round(JOYSTICK_THRESHOLD_RADIUS_CONST * controlHeight);
        mJoystickMaxDisplacement = Math.round(JOYSTICK_DISPLACEMENT_CONST * mJoystickBaseRadius);
        mFireButtonDiameter = Math.round(FIRE_BUTTON_DIAMETER_CONST * controlHeight);
        mFireButtonPressedDiameter = Math.round(FIRE_BUTTON_PRESSED_DIAMETER_CONST * controlHeight);

        if (mFireButtonDiameter > 0) {
            mFireBitmap = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource (activity.getResources(), R.drawable.fire_button),
                    mFireButtonDiameter, mFireButtonDiameter, false);
            mFirePressedBitmap = Bitmap.createScaledBitmap(mFireBitmap,
                    mFireButtonPressedDiameter, mFireButtonPressedDiameter, false);
        }

        int controlXMargin = UserUtils.scaleGraphicsInt(CONTROL_MARGIN_X_CONST);
        int controlYMargin = (int) (controlHeight - 2*mJoystickBaseRadius) / 2;

        mJoystickBaseCenterX = controlXMargin + mJoystickBaseRadius;
        mJoystickBaseCenterY = (int) screenHeight - controlYMargin - mJoystickBaseRadius;
        mJoystickX = mJoystickBaseCenterX;
        mJoystickY = mJoystickBaseCenterY;
        mFireButtonOffsetX = (int)(screenWidth - 1.5*controlXMargin - mFireButtonDiameter);
        mFireButtonOffsetY =  mJoystickBaseCenterY - mFireButtonDiameter/2;
        mJoystickPointerId = MotionEvent.INVALID_POINTER_ID;
        mFireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void updateUserTank() {
        int deltaX = mJoystickX-mJoystickBaseCenterX;
        int deltaY = mJoystickY-mJoystickBaseCenterY;

        float velocityX = (float) (deltaX)/mJoystickThresholdRadius;
        float velocityY = (float) (deltaY)/mJoystickThresholdRadius;

        if (mUserTank == null) {
            return;
        }
        else if (velocityX == 0 && velocityY == 0) {
            mUserTank.updatePosition(Math.round(GameData.getInstance().getUserPosition().x),
                    Math.round(GameData.getInstance().getUserPosition().y),
                    GameData.getInstance().getUserPosition().deg);
        }
        else {
            mUserDeg = calcDegrees(deltaX, deltaY);
            mUserTank.update(velocityX, velocityY, mUserDeg);
        }
    }

    private void drawJoystick(Canvas canvas) {
        int controlRadius = mJoystickBaseRadius / 2;
        Paint colors = new Paint();
        colors.setARGB(50, 50, 50, 50);
        canvas.drawCircle(mJoystickBaseCenterX, mJoystickBaseCenterY,
                mJoystickBaseRadius, colors);


        canvas.drawCircle(mJoystickX, mJoystickY, controlRadius, mJoystickColor);
    }

    private void drawFireButton(Canvas canvas) {
        if (mFireButtonPressed) {
            float x = mFireButtonOffsetX + (mFireButtonDiameter-mFireButtonPressedDiameter)/2f;
            float y = mJoystickBaseCenterY - mFireButtonPressedDiameter/2f;
            canvas.drawBitmap(mFirePressedBitmap, (int) x, (int) y, null);
        } else {
            canvas.drawBitmap(mFireBitmap, mFireButtonOffsetX, mFireButtonOffsetY, null);
        }
    }

    private void drawExplosions(Canvas canvas) {
        Iterator<ExplosionAnimation> iterator = mExplosionAnimations.iterator();


        while (iterator.hasNext()) {
            ExplosionAnimation ExplosionAnimation = iterator.next();
            if (ExplosionAnimation.isRemovable()) {
                iterator.remove();
            } else {
                GameData.getInstance().playExplosionSound();
                ExplosionAnimation.draw(canvas);
            }
        }
    }

    private void drawScores(Canvas canvas) {
        if (mUserTank != null) {

            String score = Integer.toString(mUserTank.getScore());
            int screenWidth = UserUtils.getScreenWidth();
            int numTanks = GameData.getInstance().getPlayerIDs().size();
            int tankWidth = mUserTank.getWidth();            
            int marginX = (screenWidth - numTanks*tankWidth) / (numTanks + 1);
            int marginY = UserUtils.scaleGraphicsInt(SCORE_MARGIN_Y_CONST);
            int textMargin = UserUtils.scaleGraphicsInt(SCORE_TEXT_MARGIN_Y_CONST);
            int textY = marginY + mUserTank.getWidth() + textMargin;
            int textSize = UserUtils.scaleGraphicsInt(SCORE_TEXT_SIZE_CONST);
            int tankIndex = 1;

            Paint textPaint = new Paint();
            textPaint.setARGB(255, 0, 0, 0);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(textSize);

            canvas.drawBitmap(mUserTank.getBitmap(), marginX, marginY, null);
            canvas.drawText(score, marginX  + tankWidth / 2f, textY, textPaint);
            if (mOpponentTank != null) {
                if (tankIndex < NUM_TANK_COLORS) {

                    Bitmap bitmap = mOpponentTank.getBitmap();
                    int offsetX = marginX + tankIndex * (tankWidth + marginX);
                    score = Integer.toString(mOpponentTank.getScore());
                    tankIndex++;


                    canvas.drawBitmap(bitmap, offsetX, marginY, null);
                    canvas.drawText(score, offsetX + tankWidth / 2f, textY, textPaint);
                }
            }
        }
    }

    private void updateJoystickData(int pointerX, int pointerY, int pointerId, int action) {
        float deltaX = pointerX - mJoystickBaseCenterX;
        float deltaY = pointerY - mJoystickBaseCenterY;
        float displacement = calcDistance((int) deltaX, (int) deltaY);

        if (displacement <= mJoystickMaxDisplacement) {
            mJoystickX = pointerX;
            mJoystickY = pointerY;
            mJoystickPointerId = pointerId;
        } else if ((action == MotionEvent.ACTION_MOVE && pointerId == mJoystickPointerId)
                || (displacement <= mJoystickThresholdRadius)){
            float joystickScaleRatio = (float) (mJoystickMaxDisplacement) / displacement;
            mJoystickX = (int) ((float) (mJoystickBaseCenterX) + (deltaX*joystickScaleRatio));
            mJoystickY = (int) ((float) (mJoystickBaseCenterY) + (deltaY*joystickScaleRatio));
            mJoystickPointerId = pointerId;
        } else {
            mJoystickX = mJoystickBaseCenterX;
            mJoystickY = mJoystickBaseCenterY;
        }
    }

    private void updateFireButtonData(int pointerX, int pointerY, int pointerId) {
        int deltaX = pointerX - (mFireButtonOffsetX + mFireButtonDiameter/2);
        int deltaY = pointerY - (mFireButtonOffsetY + mFireButtonDiameter/2);
        float displacement = calcDistance(deltaX, deltaY);

        if (displacement <= mFireButtonDiameter) {
            if (!mFireButtonPressed && GameData.getInstance().getUserAliveBulletsCount() < MAX_USER_CANNONBALLS) {
                Cannonball cannonball = mUserTank.fire();
                GameData.getInstance().getCannonballSet().addCannonball(cannonball);
                GameData.getInstance().syncCannonBall(cannonball);
                GameData.getInstance().incrementUserAliveBulletsCount();
            }
            mFireButtonPressed = true;
            mFireButtonPointerId = pointerId;
        }
        else {
            mFireButtonPressed = false;
            mFireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
        }
    }

    private void removeGame() {
        if (GameData.getInstance().getPlayerIDs().size() == 0) {
            GameData.getInstance().reset();
        }
    }

    private float calcDegrees(int x, int y) {

        float displacement = calcDistance(x, y);
        float angle = (float) Math.acos(x/displacement);

        if (y >= 0) {
            return (float) Math.toDegrees(angle);
        }
        else {
            return (float) -Math.toDegrees(angle);
        }
    }

    private float calcDistance (int x, int y) {
        return (float) Math.sqrt(x*x + y*y);
    }
}
