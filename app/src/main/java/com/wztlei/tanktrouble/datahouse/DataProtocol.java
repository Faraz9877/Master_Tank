package com.wztlei.tanktrouble.datahouse;

import com.wztlei.tanktrouble.battle.Position;
import com.wztlei.tanktrouble.cannonball.Cannonball;
import com.wztlei.tanktrouble.cannonball.CannonballSet;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataProtocol {

    // Example Token: I:01;U:King Killer,Sly Fox;P:2.354,5.46,120.2,55.4,15.6,-30.8;C:23,35,55.46,325,1
    public static String tokenizeGameData(ArrayList<Integer> playerIDs, ArrayList<String> playerUsernames,
                                   ArrayList<Position> playerPositions, ArrayList<Cannonball> cannonballs) {
        StringBuilder token = new StringBuilder();

        // Player IDs
        if(playerIDs != null) {
            token.append("I:");
            for(int i = 0; i < playerIDs.size(); i++)
                token.append(playerIDs.get(i));
            token.append(";");
        }

        // Player Usernames
        if(playerUsernames != null) {
            token.append("U:");
            for(int i = 0; i < playerUsernames.size(); i++)
            {
                if(i != 0)
                    token.append(",");
                token.append(playerUsernames.get(i));
            }
            token.append(";");
        }

        // Player Positions
        if(playerPositions != null) {
            token.append("P:");
            for(int i = 0; i < playerPositions.size(); i++)
            {
                if(i != 0)
                    token.append(",");
                token.append(playerPositions.get(i).x);
                token.append(",");
                token.append(playerPositions.get(i).y);
                token.append(",");
                token.append(playerPositions.get(i).deg);
            }
            token.append(";");
        }

        // New Cannonballs
        if(cannonballs != null) {
            token.append("C:");
            for(int i = 0; i < cannonballs.size(); i++)
            {
                if(i != 0) // int x, int y, float deg, int uuid, int shooterID
                    token.append(",");
                token.append(cannonballs.get(i).getX());
                token.append(",");
                token.append(cannonballs.get(i).getY());
                token.append(",");
                token.append(cannonballs.get(i).getDeg());
                token.append(",");
                token.append(cannonballs.get(i).getUUID());
                token.append(",");
                token.append(cannonballs.get(i).getShooterID());
            }
            token.append(";");
        }

        return token.toString();
    }

    public static void detokenizeGameData(String token) {
        int IsIndex = token.indexOf("I:");
        int UsIndex = token.indexOf("U:");
        int PsIndex = token.indexOf("P:");
        int CsIndex = token.indexOf("C:");

        // Player IDs
        if(IsIndex != -1) {
            ArrayList<Integer> playerIDs = new ArrayList<>();
            for(int i = IsIndex; i < token.indexOf(";", IsIndex); i++) {
                playerIDs.add(token.charAt(i) - '0');
            }
            GameData.getInstance().setPlayerIDs(playerIDs);
        }

        // Player Usernames
        if(UsIndex != -1) {
            ArrayList<String> usernames = new ArrayList<>();
            StringBuilder cursor = new StringBuilder();
            for(int i = UsIndex + 2; i < token.indexOf(";", UsIndex); i++) {
                if(token.charAt(i) == ',') {
                    usernames.add(cursor.toString().trim());
                    cursor = new StringBuilder();
                }
                else {
                    cursor.append(token.charAt(i));
                }
            }
            usernames.add(cursor.toString().trim());
            GameData.getInstance().setPlayerUsernames(usernames);
        }

        // Player Positions
        if(PsIndex != -1) {
            ArrayList<Position> positions = new ArrayList<>();
            StringBuilder cursor = new StringBuilder();
            int xydegCounter = 0;
            float x = 10, y = 10, deg = 0;
            for(int i = PsIndex + 2; i < token.indexOf(";", PsIndex); i++) {
                if(token.charAt(i) == ',') {
                    if(xydegCounter == 0) {
                        x = Float.parseFloat(cursor.toString());
                        xydegCounter ++;
                    }
                    else if(xydegCounter == 1) {
                        y = Float.parseFloat(cursor.toString());
                        xydegCounter ++;
                    }
                    else if(xydegCounter == 2) {
                        deg = Float.parseFloat(cursor.toString());
                        positions.add(new Position(x, y, deg));
                        xydegCounter = 0;
                    }
                    cursor = new StringBuilder();
                }
                else {
                    cursor.append(token.charAt(i));
                }
            }
            GameData.getInstance().setPlayerPositions(positions);
        }

        // New Cannonballs
        if(CsIndex != -1) {
            StringBuilder cursor = new StringBuilder();
            int x = 10, y = 20, uuid = 0, shooterID = 0;
            float deg = 0;
            int varCounter = 0;
            for(int i = CsIndex; i < token.indexOf(";", CsIndex); i++) {
                if(token.charAt(i) == ',') {
                    if(varCounter == 0) {
                        x = Integer.parseInt(cursor.toString());
                        varCounter ++;
                    }
                    else if(varCounter == 1) {
                        y = Integer.parseInt(cursor.toString());
                        varCounter ++;
                    }
                    else if(varCounter == 2) {
                        deg = Float.parseFloat(cursor.toString());
                        varCounter ++;
                    }
                    else if(varCounter == 3) {
                        uuid = Integer.parseInt(cursor.toString());
                        varCounter ++;
                    }
                    else if(varCounter == 4) {
                        shooterID = Integer.parseInt(cursor.toString());
                        GameData.getInstance().addCannonball(new Cannonball(x, y, deg, uuid, shooterID));
                        varCounter = 0;
                    }
                    cursor = new StringBuilder();
                }
                else {
                    cursor.append(token.charAt(i));
                }
            }
        }
    }

}
