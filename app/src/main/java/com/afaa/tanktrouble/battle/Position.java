package com.afaa.tanktrouble.battle;

import com.afaa.tanktrouble.UserUtils;

public class Position {

    public float x, y, deg;

    public static final float SCREEN_SCALE = UserUtils.getScreenScale();

    public Position(float x, float y, float deg) {
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
        return new Position(x / SCREEN_SCALE, y / SCREEN_SCALE, deg);
    }

    public Position scalePosition() {
        return new Position(x * SCREEN_SCALE, y * SCREEN_SCALE, deg);
    }
}