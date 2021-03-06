package com.afaa.tanktrouble.battle;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import java.util.TimerTask;

public class BattleThread implements Runnable {

    private final BattleView battleView;
    private final SurfaceHolder surfaceHolder;

    BattleThread(SurfaceHolder surfaceHolder, BattleView battleView) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.battleView = battleView;
    }

    @Override
    public void run() {
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
