package com.afaa.tanktrouble.battle;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class BattleThread extends Thread {

    private final BattleView battleView;
    private final SurfaceHolder surfaceHolder;
    private boolean mRunning;


    BattleThread(SurfaceHolder surfaceHolder, BattleView battleView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.battleView = battleView;
    }


    public void setRunning(boolean isRunning) {
        mRunning = isRunning;
    }


    @Override
    public void run() {
        while (mRunning) {
            Canvas canvas = null;

            try {
                canvas = surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    if (canvas != null && battleView != null) {
                        battleView.draw(canvas);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            if (canvas != null) {
                try {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
