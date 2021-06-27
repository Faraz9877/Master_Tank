package com.afaa.tanktrouble.cannonball;

import java.util.ArrayList;

public class Path {
    private final ArrayList<Coordinate> coords;
    private final int uuid;

    Path(ArrayList<Coordinate> coords, int uuid) {
        this.uuid = uuid;
        this.coords = coords;
    }

    public ArrayList<Coordinate> getCoordinates() {
        return coords;
    }

    public Coordinate get(int index) {
        return coords.get(index);
    }

    public int size() {
        return coords.size();
    }

    public int getUUID() {
        return uuid;
    }


}
