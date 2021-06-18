package com.afaa.tanktrouble.datahouse;

import android.util.Log;

import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.cannonball.Cannonball;

import java.util.ArrayList;

public class DataProtocol {

    public static String tokenizeGameData(String playerUsername, Position playerPosition,
										  ArrayList<Cannonball> cannonballs) {
        StringBuilder token = new StringBuilder();

        token.append("S:");

        if(playerUsername != null) {
            token.append("U:");
            token.append(playerUsername);
            token.append(";");
        }

        if(playerPosition != null) {
            Position stdPos = playerPosition.standardizePosition();
            token.append("P:");
            token.append(stdPos.x);
            token.append(",");
            token.append(stdPos.y);
            token.append(",");
            token.append(stdPos.deg);
            token.append(";");
        }

        if(cannonballs != null && cannonballs.size() > 0) {
            token.append("C:");
            for(int i = 0; i < cannonballs.size(); i++)
            {
                Position stdPos = cannonballs.get(i).getPosition().standardizePosition();
                if(i != 0)
                    token.append(",");
                token.append(stdPos.x);
                token.append(",");
                token.append(stdPos.y);
                token.append(",");
                token.append(stdPos.deg);
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
        int UsIndex = token.indexOf("U:");
        int PsIndex = token.indexOf("P:");
        int CsIndex = token.indexOf("C:");

        if(UsIndex != -1) {
            String username;
            StringBuilder cursor = new StringBuilder();
            for(int i = UsIndex + 2; i < token.indexOf(";", UsIndex); i++) {
                    cursor.append(token.charAt(i));
            }
            username = cursor.toString().trim();
            GameData.getInstance().setOpponentUserName(username);
        }

        if(PsIndex != -1) {
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
            System.out.println(position);
            GameData.getInstance().setOpponentPosition(position);
        }

        if(CsIndex != -1) {
            StringBuilder cursor = new StringBuilder();
            int x = 10, y = 20, uuid = 0, shooterID = 0;
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
                        varCounter ++;
                    }
                    else if(varCounter == 4) {
                        shooterID = Integer.parseInt(cursor.toString());
                        GameData.getInstance().getCannonballSet().addCannonball(
                                new Cannonball(Math.round(x * Position.SCREEN_SCALE),
                                Math.round(y * Position.SCREEN_SCALE), deg, uuid, shooterID)
                        );
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
