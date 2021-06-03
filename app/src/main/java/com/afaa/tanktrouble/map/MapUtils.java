package com.afaa.tanktrouble.map;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.tank.Tank;

import java.util.ArrayList;

public class MapUtils {

//    private static final String TRB = "TRB";
//    private static final String LTR = "LTR";
    private static final String TR = "TR";
    private static final String LTB = "LTB";
    private static final String TB = "TB";
    private static final String LT = "LT";
    private static final String T = "T";
    private static final String LRB = "LRB";
    private static final String RB = "RB";
    private static final String LR = "LR";
    private static final String R = "R";
    private static final String LB = "LB";
    private static final String B = "B";
    private static final String L = "L";
//    private static final String O = "";
    private static final String TAG = "WL/MapUtils";

    private static final int TOP_Y =
            UserUtils.scaleGraphicsInt(Constants.MAP_TOP_Y_CONST);
    private static final int CELL_WIDTH =
            UserUtils.scaleGraphicsInt(Constants.MAP_CELL_WIDTH_CONST);
    private static final int WALL_WIDTH =
            UserUtils.scaleGraphicsInt(Constants.MAP_WALL_WIDTH_CONST);

    private static final MapCell[][] DEFAULT_MAP_CELLS = new MapCell[][] {
            {new MapCell(LTB), new MapCell(T), new MapCell(T), new MapCell(T), new MapCell(TR)},
            {new MapCell(LT), new MapCell(R), new MapCell(LRB), new MapCell(LRB), new MapCell(LR)},
            {new MapCell(LR), new MapCell(L), new MapCell(T), new MapCell(T), new MapCell(R)},
            {new MapCell(LR), new MapCell(LB), new MapCell(R), new MapCell(LR), new MapCell(LR)},
            {new MapCell(LB), new MapCell(TB), new MapCell(B), new MapCell(RB), new MapCell(LRB)}};

    private static final ArrayList<RectF> DEFAULT_MAP_WALLS = cellsToWalls(DEFAULT_MAP_CELLS);
    private static final int NUM_CELL_ROWS = DEFAULT_MAP_CELLS.length;
    private static final int NUM_CELL_COLS = DEFAULT_MAP_CELLS[0].length;









    private static ArrayList<RectF> cellsToWalls(MapCell[][] cellGrid) {
        ArrayList<RectF> mapWalls = new ArrayList<>();

        if (TOP_Y == 0 || WALL_WIDTH == 0 || CELL_WIDTH == 0) {
            return null;
        }


        for (int row = 0; row < cellGrid.length; row++) {
            for (int col = 0; col < cellGrid[row].length; col++) {
                MapCell mapCell = cellGrid[row][col];


                if (mapCell.hasLeftWall() && col == 0) {
                    mapWalls.add(new RectF(CELL_WIDTH*col,
                            TOP_Y + CELL_WIDTH*row,
                            CELL_WIDTH*col + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH + WALL_WIDTH));
                }


                if (mapCell.hasTopWall() && row == 0) {
                    mapWalls.add(new RectF(CELL_WIDTH*col,
                            TOP_Y + CELL_WIDTH*row,
                            CELL_WIDTH*col + CELL_WIDTH + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + WALL_WIDTH));
                }


                if (mapCell.hasRightWall()) {
                    mapWalls.add(new RectF(CELL_WIDTH*col + CELL_WIDTH,
                            TOP_Y + CELL_WIDTH*row,
                            CELL_WIDTH*col + +CELL_WIDTH + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH + WALL_WIDTH));
                }


                if (mapCell.hasBottomWall()) {
                    mapWalls.add(new RectF(CELL_WIDTH*col,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH,
                            CELL_WIDTH*col + CELL_WIDTH + WALL_WIDTH,
                            TOP_Y + CELL_WIDTH*row + CELL_WIDTH + WALL_WIDTH));
                }
            }
        }

        return mapWalls;
    }


    public static void drawMap(Canvas canvas) {
        Paint paint = new Paint();
        paint.setARGB(255, 230, 230, 230);
        canvas.drawRect(0, TOP_Y, UserUtils.getScreenWidth(),
                TOP_Y+UserUtils.getScreenWidth(), paint);

        paint.setARGB(255, 120, 120, 120);

        if (DEFAULT_MAP_WALLS == null) {
            return;
        }


        for (RectF wall : DEFAULT_MAP_WALLS) {
            canvas.drawRect(wall, paint);
        }
    }


    public static boolean tankWallCollision(float x, float y, float deg, float w, float h) {
        RectF boundingRect = new RectF(x, y, x+w+h, y+w+h);
        PointF[] tankPolygon = Tank.tankPolygon(x, y, deg, w, h);

        if (DEFAULT_MAP_WALLS == null) {
            return false;
        }


        for (RectF wall : DEFAULT_MAP_WALLS) {

            if (RectF.intersects(boundingRect, wall)) {


                for (PointF pointF : tankPolygon) {

                    if (wall.contains(pointF.x, pointF.y)) {
                        return true;
                    }
                }
            }
        }


        return false;
    }

    public static boolean cannonballWallCollision(float x, float y, int r) {
        return cannonballWallCollision((int) x, (int) y, r);
    }


    @SuppressWarnings({"SuspiciousNameCombination", "SimplifiableIfStatement"})
    private static boolean cannonballWallCollision(int x, int y, int r) {

        int cellRow = (y - TOP_Y) / CELL_WIDTH;
        int cellCol =  x / CELL_WIDTH;
        int cellX = x % CELL_WIDTH;
        int cellY = (y - TOP_Y) % CELL_WIDTH;


        if (cellRow >= NUM_CELL_ROWS || cellCol >= NUM_CELL_COLS) {
            return true;
        }


        MapCell mapCell = DEFAULT_MAP_CELLS[cellRow][cellCol];


        if (inRange(WALL_WIDTH+r, cellX, CELL_WIDTH-r)
                && inRange(WALL_WIDTH+r, cellY, CELL_WIDTH-r)) {

            return false;
        } else if (mapCell.hasLeftWall() && cellX < WALL_WIDTH+r) {

            return true;
        } else if (mapCell.hasTopWall() && cellY < WALL_WIDTH+r) {

            return true;
        } else if (mapCell.hasRightWall() && cellX > CELL_WIDTH-r) {

            return true;
        } else if (mapCell.hasBottomWall() && cellY > CELL_WIDTH-r) {

            return true;
        } else {

            return cannonballCornerCollision(cellRow, cellCol, cellX, cellY, r);
        }
    }


    @SuppressWarnings({"SuspiciousNameCombination", "RedundantIfStatement"})
    private static boolean cannonballCornerCollision
            (int cellRow, int cellCol, int cellX, int cellY, int r) {

        Rect boundingRect = new Rect(cellX-r, cellY-r, cellX+r, cellY+r);


        if ((cellRow > 0) && (cellCol > 0)) {
            MapCell topLeftCell = DEFAULT_MAP_CELLS[cellRow-1][cellCol-1];
            boolean tlCorner = topLeftCell.hasRightWall() || topLeftCell.hasBottomWall();


            if (tlCorner && (calcDistance(cellX, cellY, WALL_WIDTH, WALL_WIDTH) < r
                    || boundingRect.intersect(new Rect(0, 0, WALL_WIDTH, WALL_WIDTH)))) {
                return true;
            }
        }


        if ((cellRow > 0) && (cellCol < NUM_CELL_COLS-1)) {
            MapCell topRightCell = DEFAULT_MAP_CELLS[cellRow-1][cellCol+1];
            boolean trCorner = topRightCell.hasBottomWall() || topRightCell.hasLeftWall();


            if (trCorner && (calcDistance(cellX, cellY, CELL_WIDTH, WALL_WIDTH) < r
                    || boundingRect.intersect(new Rect(CELL_WIDTH, 0,
                    CELL_WIDTH+WALL_WIDTH, WALL_WIDTH)))) {
                return true;
            }
        }


        if ((cellRow < NUM_CELL_ROWS-1) && (cellCol < NUM_CELL_COLS-1)) {
            MapCell bottomRightCell = DEFAULT_MAP_CELLS[cellRow+1][cellCol+1];
            boolean brCorner = bottomRightCell.hasLeftWall() || bottomRightCell.hasTopWall();


            if (brCorner && (calcDistance(cellX, cellY, CELL_WIDTH, CELL_WIDTH) < r
                    || boundingRect.intersect(new Rect(CELL_WIDTH, CELL_WIDTH,
                    CELL_WIDTH+WALL_WIDTH, CELL_WIDTH+WALL_WIDTH)))) {
                return true;
            }
        }


        if ((cellRow < NUM_CELL_ROWS-1) && (cellCol > 0)) {
            MapCell bottomLeftCell = DEFAULT_MAP_CELLS[cellRow+1][cellCol-1];
            boolean blCorner = bottomLeftCell.hasTopWall() || bottomLeftCell.hasRightWall();


            if (blCorner && (calcDistance(cellX, cellY, WALL_WIDTH, CELL_WIDTH) < r
                    || boundingRect.intersect(new Rect(0, CELL_WIDTH, WALL_WIDTH,
                    CELL_WIDTH+WALL_WIDTH)))) {
                return true;
            }
        }


        return false;
    }


    private static float calcDistance (float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt(Math.pow(x2-x1, 2) + Math.pow(y2-y1, 2));
    }


    private static boolean inRange (float min, float num, float max) {
        return (min <= num && num <= max);
    }
}
