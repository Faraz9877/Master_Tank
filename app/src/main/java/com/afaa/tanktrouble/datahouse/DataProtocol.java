package com.afaa.tanktrouble.datahouse;

import android.util.Log;

import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.cannonball.Cannonball;

public class DataProtocol {

    public static String tokenizePosition(Position playerPosition){
        StringBuilder token = new StringBuilder();
        if (playerPosition != null) {
            Position stdPos = playerPosition.standardizePosition();
            token.append("P:");
            token.append(stdPos.x);
            token.append(",");
            token.append(stdPos.y);
            token.append(",");
            token.append(stdPos.deg);
            token.append(";");
            token.append("\n");
        }
        return token.toString();
    }

    public static String tokenizeCannonBall(Cannonball cannonball){
        StringBuilder token = new StringBuilder();
        if (cannonball != null){
            token.append("C:");
            Position stdPos = cannonball.getPosition().standardizePosition();
            token.append(stdPos.x);
            token.append(",");
            token.append(stdPos.y);
            token.append(",");
            token.append(stdPos.deg);
            token.append(",");
            token.append(cannonball.getUUID());
            token.append(",");
            token.append(cannonball.getShooterID());
            token.append(",");
            token.append(cannonball.getFiringTime());
            token.append(";");
            token.append("\n");
        }
        return token.toString();
    }

    public static void detokenizePosition(String token){
        int PsIndex = token.indexOf("P:");
        if (PsIndex != -1){
            Log.d("Receiver Position:", token);
            Position position;
            StringBuilder cursor = new StringBuilder();
            int xydegCounter = 0;
            int x = 300, y = 300;
            float deg = 0;
            for(int i = PsIndex + 2; i < token.indexOf(";", PsIndex) + 1; i++) {
                if(token.charAt(i) == ',' || token.charAt(i) == ';') {
                    if(xydegCounter == 0) {
                        x = Integer.parseInt(cursor.toString());
                        xydegCounter ++;
                    }
                    else if(xydegCounter == 1) {
                        y = Integer.parseInt(cursor.toString());
                        xydegCounter ++;
                    }
                    else if(xydegCounter == 2) {
                        deg = Float.parseFloat(cursor.toString());
                        xydegCounter = 0;
                    }
                    cursor = new StringBuilder();
                }
                else {
                    cursor.append(token.charAt(i));
                }
            }
            position = (new Position(x, y, deg)).scalePosition();
            GameData.getInstance().setOpponentPosition(position);
        }
    }

    public static void detokenizeCannonBall(String token){
        int CsIndex = token.indexOf("C:");
        if (CsIndex != -1){
            Log.d("Receiver CannonBall:", token);
            int maxCannonId = -1;
            StringBuilder cursor = new StringBuilder();
            int x = 10, y = 20, uuid = 0, shooterID = 0;
            long firingTime;
            float deg = 0;
            int varCounter = 0;
            for(int i = CsIndex + 2; i < token.indexOf(";", CsIndex) + 1; i++) {
                if(token.charAt(i) == ',' || token.charAt(i) == ';') {
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
                        if (uuid > maxCannonId) maxCannonId = uuid;
                        varCounter ++;
                    }
                    else if(varCounter == 4) {
                        shooterID = Integer.parseInt(cursor.toString());
                        varCounter ++;
                    }
                    else if(varCounter == 5){
                        firingTime = Long.parseLong(cursor.toString());
                        varCounter = 0;
                        if (uuid > GameData.getInstance().getLastOpponentCannonId()) {
                            Cannonball cannonball = new Cannonball(Math.round(x * Position.SCREEN_SCALE),
                                    Math.round(y * Position.SCREEN_SCALE), deg, uuid, shooterID);
                            cannonball.setFiringTime(firingTime);
                            GameData.getInstance().getCannonballSet().addCannonball(cannonball);
                        }
                    }
                    cursor = new StringBuilder();
                }
                else {
                    cursor.append(token.charAt(i));
                }
            }
            GameData.getInstance().setLastOpponentCannonId(maxCannonId);
        }
    }
}
