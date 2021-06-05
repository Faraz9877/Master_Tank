package com.afaa.tanktrouble.battle;

import com.afaa.tanktrouble.UserUtils;

public class Position {

    public int x, y;
    public float deg;

    public static final float SCREEN_SCALE = UserUtils.getScreenScale();

    public Position(int x, int y, float deg) {
        this.x = x;
        this.y = y;
        this.deg = deg;
    }

    public Position(Position P) {
        this.x = P.x;
        this.y = P.y;
        this.deg = P.deg;
    }

    public Position standardizePosition() {
        return new Position( (int) (x / SCREEN_SCALE), (int) (y / SCREEN_SCALE), deg);
    }

    public Position scalePosition() {
        return new Position( (int) (x * SCREEN_SCALE), (int) (y * SCREEN_SCALE), deg);
    }
}
