package com.afaa.tanktrouble.map;

public class MapCell {

    private boolean topWall, rightWall, bottomWall, leftWall;

    MapCell(String code) {
        leftWall = code.contains("L");
        topWall = code.contains("T");
        rightWall = code.contains("R");
        bottomWall = code.contains("B");
    }

    public boolean hasLeftWall() {
        return leftWall;
    }

    public boolean hasTopWall() {
        return topWall;
    }

    public boolean hasRightWall() {
        return rightWall;
    }

    public boolean hasBottomWall() {
        return bottomWall;
    }
}
