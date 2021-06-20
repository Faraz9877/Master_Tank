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

    private static Bitmap[] sExplosionBitmaps;
    private static int sExplosionWidth;
    private static int sExplosionHeight;

    private static final float EXPLOSION_WIDTH_CONST = (float) 205/1080;
    private static final float EXPLOSION_HEIGHT_CONST = (float) 139/1080;
    private static final int EXPLOSION_FRAME_DURATION = 80;
    

    public static void initialize(Activity activity) {

        sExplosionWidth = UserUtils.scaleGraphicsInt(EXPLOSION_WIDTH_CONST);
        sExplosionHeight = UserUtils.scaleGraphicsInt(EXPLOSION_HEIGHT_CONST);


        sExplosionBitmaps = new Bitmap[6];
        sExplosionBitmaps[0] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion0),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[1] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion1),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[2] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion2),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[3] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion3),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[4] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion4),
                sExplosionWidth, sExplosionHeight, false);
        sExplosionBitmaps[5] = Bitmap.createScaledBitmap(
                BitmapFactory.decodeResource(activity.getResources(), R.drawable.explosion5),
                sExplosionWidth, sExplosionHeight, false);
    }


    ExplosionAnimation(Tank tank) {
        PointF tankCenter = tank.getCenter();

        prevFrameTime = System.currentTimeMillis();
        frameIndex = 0;
        x = (int) tankCenter.x - sExplosionWidth/2;
        y = (int) tankCenter.y - sExplosionHeight/2;
    }


    public boolean isRemovable() {
        return frameIndex >= sExplosionBitmaps.length;
    }


    public void draw(Canvas canvas) {
        long nowTime = System.currentTimeMillis();
        long deltaTime = nowTime - prevFrameTime;


        if (deltaTime > EXPLOSION_FRAME_DURATION) {
            prevFrameTime = nowTime;
            frameIndex++;
        } 


        if (frameIndex < sExplosionBitmaps.length) {
            canvas.drawBitmap(sExplosionBitmaps[frameIndex], x, y, null);
        }
    }
}
