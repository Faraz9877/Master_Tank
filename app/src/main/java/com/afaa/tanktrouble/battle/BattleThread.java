package com.afaa.tanktrouble.battle;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class BattleThread extends Thread {

    private BattleView mBattleView;
    private final SurfaceHolder mSurfaceHolder;
    private boolean mRunning;


    BattleThread(SurfaceHolder surfaceHolder, BattleView battleView) {
        super();
        mSurfaceHolder = surfaceHolder;
        mBattleView = battleView;
    }


    public void setRunning(boolean isRunning) {
        mRunning = isRunning;
    }


    @Override
    public void run() {
        while (mRunning) {
            Canvas canvas = null;

            try {

                canvas = mSurfaceHolder.lockCanvas();


                synchronized (mSurfaceHolder) {
                    if (canvas != null && mBattleView != null) {
                        mBattleView.draw(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (canvas != null) {
                try {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
