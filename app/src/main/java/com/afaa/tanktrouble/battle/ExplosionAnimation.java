package com.afaa.tanktrouble.battle;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;

import com.afaa.tanktrouble.R;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.tank.Tank;

public class ExplosionAnimation {
    
    private long prevFrameTime;
    private int frameIndex;
    private final int x;
    private final int y;

    private static Bitmap[] explosionBitmaps;
    private static int explosionWidth;
    private static int explosionHeight;

    private static final float EXPLOSION_WIDTH_CONST = (float) 205/1080;
    private static final float EXPLOSION_HEIGHT_CONST = (float) 139/1080;
    private static final int EXPLOSION_FRAME_DURATION = 80;
    

    public static void initialize(Activity activity) {

        explosionWidth = UserUtils.scaleGraphicsInt(EXPLOSION_WIDTH_CONST);
        explosionHeight = UserUtils.scaleGraphicsInt(EXPLOSION_HEIGHT_CONST);


        explosionBitmaps = new Bitmap[6];
        explosionBitmaps[0] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion0),
                explosionWidth, explosionHeight, false);
        explosionBitmaps[1] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion1),
                explosionWidth, explosionHeight, false);
        explosionBitmaps[2] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion2),
                explosionWidth, explosionHeight, false);
        explosionBitmaps[3] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion3),
                explosionWidth, explosionHeight, false);
        explosionBitmaps[4] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion4),
                explosionWidth, explosionHeight, false);
        explosionBitmaps[5] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion5),
                explosionWidth, explosionHeight, false);
    }


    ExplosionAnimation(Tank tank) {
        PointF tankCenter = tank.getCenter();

        prevFrameTime = System.currentTimeMillis();
        frameIndex = 0;
        x = (int) tankCenter.x - explosionWidth /2;
        y = (int) tankCenter.y - explosionHeight /2;
    }


    public boolean isRemovable() {
        return frameIndex >= explosionBitmaps.length;
    }


    public void draw(Canvas canvas) {
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - prevFrameTime;


        if (deltaTime > EXPLOSION_FRAME_DURATION) {
            prevFrameTime = nowTime;
            frameIndex++;
        } 


        if (frameIndex < explosionBitmaps.length) {
            canvas.drawBitmap(explosionBitmaps[frameIndex], x, y, null);
        }
    }
}
