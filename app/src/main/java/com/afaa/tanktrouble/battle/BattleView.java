package com.afaa.tanktrouble.battle;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import com.afaa.tanktrouble.tank.Tank;
import com.afaa.tanktrouble.tank.TankColor;
import com.afaa.tanktrouble.tank.UserTank;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;

@SuppressLint("ViewConstructor")
public class BattleView extends SurfaceView implements SurfaceHolder.Callback, View.OnTouchListener {

    private Bitmap fireBitmap, firePressedBitmap;
    private final BattleThread battleThread;
    private UserTank userTank;
    private OpponentTank opponentTank;
    private final HashSet<ExplosionAnimation> explosionAnimations;
    private Paint joystickColor;
    private int joystickBaseCenterX, joystickBaseCenterY;
    private int fireButtonOffsetX, fireButtonOffsetY;
    private int joystickX, joystickY;
    private int joystickPointerId, fireButtonPointerId;
    private int joystickBaseRadius, joystickThresholdRadius, joystickMaxDisplacement;
    private int fireButtonDiameter, fireButtonPressedDiameter;
    private float userDeg;
    private boolean fireButtonPressed;
    private Timer battleTimer;
    private static long startTime;

    Activity activity;

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
        this.activity = activity;
        UserUtils.initialize(activity);
        ExplosionAnimation.initialize(activity);
        joystickColor = TankColor.BLUE.getPaint();
        explosionAnimations = new HashSet<>();
        addEnteringTanks(activity);
        getHolder().addCallback(this);

        battleTimer = new Timer("BattleTimer");
        battleThread = new BattleThread(getHolder(), this);

        setFocusable(true);
        setControlGraphicsData(activity);
        setOnTouchListener(this);
        GameData.getInstance().createSoundPool(activity);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        if (battleThread.getState() == Thread.State.NEW) {
//            battleThread.setRunning(true);
//            battleThread.start();
//        }
        startTime = System.currentTimeMillis();
        battleTimer.scheduleAtFixedRate(battleThread, 0, 30);

    }

    public void finishThread() {
//        try {
//            battleThread.setRunning(false);
//            battleThread.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        battleTimer.cancel();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        GameData.getInstance().signalEnd();
        GameData.getInstance().reset();
    }


    public void endGame(Boolean win){
        Intent intent = new Intent(activity, ResultActivity.class);
        if (win)
            intent.putExtra("win", 1);
        else
            intent.putExtra("win", 0);
        activity.startActivity(intent);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        canvas.drawColor(Color.WHITE);
        MapUtils.drawMap(canvas);
        drawFireButton(canvas);
        drawJoystick(canvas);

        boolean userTankHit = GameData.getInstance().getCannonballSet().updateAndDetectUserCollision(userTank);
        boolean opponentTankHit = GameData.getInstance().isOpponentTankHit();

        GameData.getInstance().getCannonballSet().draw(canvas);

        updateUserTank();
        GameData.getInstance().syncPosition();
        userTank.draw(canvas);

        opponentTank.updatePosition(Math.round(GameData.getInstance().getOpponentPosition().x),
                                     Math.round(GameData.getInstance().getOpponentPosition().y),
                                     GameData.getInstance().getOpponentPosition().deg);
        opponentTank.draw(canvas);


        if (!userTankHit) {
            userTank.respawn();
        }
        else {
            GameData.getInstance().signalDeath();
            userTank.kill();
            explosionAnimations.add(new ExplosionAnimation(userTank));
            if (userTank.getScore() == 0) {
                endGame(false);
            }
        }

        if (!opponentTankHit) {
            opponentTank.respawn();
        }
        else {
            opponentTank.kill();
            explosionAnimations.add(new ExplosionAnimation(opponentTank));
            if (opponentTank.getScore() == 0) {
                endGame(true);
            }
        }

        drawExplosions(canvas);
        drawScores(canvas);
        if (opponentTankHit || userTankHit) {
            resetTanks();
        }
    }

    private void resetTanks() {
        GameData.getInstance().getCannonballSet().clear();
        GameData.getInstance().setOpponentTankHit(false);
        GameData.getInstance().resetUserAliveBulletsCount();
        GameData.getInstance().setUserPosition(Tank.getRandomInitialPosition());
        GameData.getInstance().setOpponentPosition(Tank.getRandomInitialPosition());
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
                if (joystickPointerId == MotionEvent.INVALID_POINTER_ID) {
                    updateJoystickData(pointerX, pointerY, pointerId, action);
                }

                if (fireButtonPointerId == MotionEvent.INVALID_POINTER_ID) {
                    updateFireButtonData(pointerX, pointerY, pointerId);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                for (int index = 0; index < motionEvent.getPointerCount(); index++) {
                    int id = motionEvent.getPointerId(index);
                    int x = (int) motionEvent.getX(index);
                    int y = (int) motionEvent.getY(index);

                    if (id == joystickPointerId) {
                        updateJoystickData(x, y, id, action);
                    } else if (joystickPointerId == MotionEvent.INVALID_POINTER_ID) {
                        updateJoystickData(x, y, id, action);
                    } else if (id == fireButtonPointerId){
                        updateFireButtonData(x, y, id);
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
                if (pointerId == joystickPointerId) {
                    joystickX = joystickBaseCenterX;
                    joystickY = joystickBaseCenterY;
                    joystickPointerId = MotionEvent.INVALID_POINTER_ID;
                } else if (pointerId == fireButtonPointerId){
                    fireButtonPressed = false;
                    fireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
                }
                break;

            default:
                break;
        }

        return true;
    }

    private void addEnteringTanks(final Activity activity) {
        final int BLUE_INDEX_JOYSTICK_COLOR = 0;
        joystickColor = TankColor.values()[GameData.getInstance().getUserId()].getPaint();
        if (userTank == null) {
            TankColor tankColor = TankColor.values()[GameData.getInstance().getUserId()];
            userTank = new UserTank(activity, tankColor, GameData.getInstance().getUserId());
            userDeg = userTank.getDegrees();
        }
        if (opponentTank == null) {
            TankColor tankColor = TankColor.values()[GameData.getInstance().getOpponentId()];
            opponentTank = new OpponentTank(activity, GameData.getInstance().getOpponentId(), tankColor);
        }
    }

    private void setControlGraphicsData(Activity activity) {
        float screenHeight = UserUtils.getScreenHeight();
        float screenWidth = UserUtils.getScreenWidth();
        float controlHeight = screenHeight - UserUtils.scaleGraphicsInt(TOP_Y_CONST) - screenWidth;
        joystickBaseRadius = Math.round(JOYSTICK_BASE_RADIUS_CONST * controlHeight);
        joystickThresholdRadius = Math.round(JOYSTICK_THRESHOLD_RADIUS_CONST * controlHeight);
        joystickMaxDisplacement = Math.round(JOYSTICK_DISPLACEMENT_CONST * joystickBaseRadius);
        fireButtonDiameter = Math.round(FIRE_BUTTON_DIAMETER_CONST * controlHeight);
        fireButtonPressedDiameter = Math.round(FIRE_BUTTON_PRESSED_DIAMETER_CONST * controlHeight);

        if (fireButtonDiameter > 0) {
            fireBitmap = Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource (activity.getResources(), R.drawable.fire_button),
                    fireButtonDiameter, fireButtonDiameter, false);
            firePressedBitmap = Bitmap.createScaledBitmap(fireBitmap,
                    fireButtonPressedDiameter, fireButtonPressedDiameter, false);
        }

        int controlXMargin = UserUtils.scaleGraphicsInt(CONTROL_MARGIN_X_CONST);
        int controlYMargin = (int) (controlHeight - 2* joystickBaseRadius) / 2;

        joystickBaseCenterX = controlXMargin + joystickBaseRadius;
        joystickBaseCenterY = (int) screenHeight - controlYMargin - joystickBaseRadius;
        joystickX = joystickBaseCenterX;
        joystickY = joystickBaseCenterY;
        fireButtonOffsetX = (int)(screenWidth - 1.5*controlXMargin - fireButtonDiameter);
        fireButtonOffsetY =  joystickBaseCenterY - fireButtonDiameter /2;
        joystickPointerId = MotionEvent.INVALID_POINTER_ID;
        fireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
    }

    @SuppressWarnings("UnnecessaryReturnStatement")
    private void updateUserTank() {
        int deltaX = joystickX - joystickBaseCenterX;
        int deltaY = joystickY - joystickBaseCenterY;

        float velocityX = (float) (deltaX)/ joystickThresholdRadius;
        float velocityY = (float) (deltaY)/ joystickThresholdRadius;

        if (userTank == null) {
            return;
        }
        else if (velocityX == 0 && velocityY == 0) {
            userTank.updatePosition(Math.round(GameData.getInstance().getUserPosition().x),
                    Math.round(GameData.getInstance().getUserPosition().y),
                    GameData.getInstance().getUserPosition().deg);
        }
        else {
            userDeg = calcDegrees(deltaX, deltaY);
            userTank.update(velocityX, velocityY, userDeg);
        }
    }

    private void drawJoystick(Canvas canvas) {
        int controlRadius = joystickBaseRadius / 2;
        Paint colors = new Paint();
        colors.setARGB(50, 50, 50, 50);
        canvas.drawCircle(joystickBaseCenterX, joystickBaseCenterY,
                joystickBaseRadius, colors);


        canvas.drawCircle(joystickX, joystickY, controlRadius, joystickColor);
    }

    private void drawFireButton(Canvas canvas) {
        if (fireButtonPressed) {
            float x = fireButtonOffsetX + (fireButtonDiameter - fireButtonPressedDiameter)/2f;
            float y = joystickBaseCenterY - fireButtonPressedDiameter /2f;
            canvas.drawBitmap(firePressedBitmap, (int) x, (int) y, null);
        } else {
            canvas.drawBitmap(fireBitmap, fireButtonOffsetX, fireButtonOffsetY, joystickColor);
        }
    }

    public static long getElapsedTime() {
        return System.currentTimeMillis() - startTime;
    }

    private void drawExplosions(Canvas canvas) {
        Iterator<ExplosionAnimation> iterator = explosionAnimations.iterator();


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
        if (userTank != null) {

            String score = Integer.toString(userTank.getScore());
            int screenWidth = UserUtils.getScreenWidth();
            int numTanks = GameData.getInstance().getPlayerIDs().size();
            int tankWidth = userTank.getWidth();
            int marginX = (screenWidth - numTanks*tankWidth) / (numTanks + 1);
            int marginY = UserUtils.scaleGraphicsInt(SCORE_MARGIN_Y_CONST);
            int textMargin = UserUtils.scaleGraphicsInt(SCORE_TEXT_MARGIN_Y_CONST);
            int textY = marginY + userTank.getWidth() + textMargin;
            int textSize = UserUtils.scaleGraphicsInt(SCORE_TEXT_SIZE_CONST);
            int tankIndex = 1;

            Paint textPaint = new Paint();
            textPaint.setARGB(255, 0, 0, 0);
            textPaint.setTextAlign(Paint.Align.CENTER);
            textPaint.setTextSize(textSize);

            canvas.drawBitmap(userTank.getBitmap(), marginX, marginY, null);
            canvas.drawText(score, marginX  + tankWidth / 2f, textY, textPaint);
            if (opponentTank != null) {
                if (tankIndex < NUM_TANK_COLORS) {

                    Bitmap bitmap = opponentTank.getBitmap();
                    int offsetX = marginX + tankIndex * (tankWidth + marginX);
                    score = Integer.toString(opponentTank.getScore());
                    tankIndex++;


                    canvas.drawBitmap(bitmap, offsetX, marginY, null);
                    canvas.drawText(score, offsetX + tankWidth / 2f, textY, textPaint);
                }
            }
        }
    }

    private void updateJoystickData(int pointerX, int pointerY, int pointerId, int action) {
        float deltaX = pointerX - joystickBaseCenterX;
        float deltaY = pointerY - joystickBaseCenterY;
        float displacement = calcDistance((int) deltaX, (int) deltaY);

        if (displacement <= joystickMaxDisplacement) {
            joystickX = pointerX;
            joystickY = pointerY;
            joystickPointerId = pointerId;
        } else if ((action == MotionEvent.ACTION_MOVE && pointerId == joystickPointerId)
                || (displacement <= joystickThresholdRadius)){
            float joystickScaleRatio = (float) (joystickMaxDisplacement) / displacement;
            joystickX = (int) ((float) (joystickBaseCenterX) + (deltaX*joystickScaleRatio));
            joystickY = (int) ((float) (joystickBaseCenterY) + (deltaY*joystickScaleRatio));
            joystickPointerId = pointerId;
        } else {
            joystickX = joystickBaseCenterX;
            joystickY = joystickBaseCenterY;
        }
    }

    private void updateFireButtonData(int pointerX, int pointerY, int pointerId) {
        int deltaX = pointerX - (fireButtonOffsetX + fireButtonDiameter /2);
        int deltaY = pointerY - (fireButtonOffsetY + fireButtonDiameter /2);
        float displacement = calcDistance(deltaX, deltaY);

        if (displacement <= fireButtonDiameter) {
            if (!fireButtonPressed && GameData.getInstance().getUserAliveBulletsCount() < MAX_USER_CANNONBALLS) {
                GameData.getInstance().signalTankFire();
                Cannonball cannonball = userTank.fire();
                GameData.getInstance().getCannonballSet().addCannonball(cannonball);
//                GameData.getInstance().syncCannonBall(cannonball);
                GameData.getInstance().incrementUserAliveBulletsCount();
            }
            fireButtonPressed = true;
            fireButtonPointerId = pointerId;
        }
        else {
            fireButtonPressed = false;
            fireButtonPointerId = MotionEvent.INVALID_POINTER_ID;
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
