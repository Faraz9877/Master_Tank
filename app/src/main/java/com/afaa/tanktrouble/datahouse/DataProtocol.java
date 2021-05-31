package com.afaa.tanktrouble.datahouse;

import android.util.Log;

import com.afaa.tanktrouble.battle.Position;
import com.afaa.tanktrouble.cannonball.Cannonball;

import java.util.ArrayList;

public class DataProtocol {

    // Example Token: I:01;U:King Killer,Sly Fox;P:2.354,5.46,120.2,55.4,15.6,-30.8;C:23,35,55.46,325,1
    public static String tokenizeGameData(String gamePIN, ArrayList<Integer> playerIDs,
                                          ArrayList<String> playerUsernames,
                                          ArrayList<Position> playerPositions,
                                          ArrayList<Cannonball> cannonballs) {
        StringBuilder token = new StringBuilder();

        // Game PIN
        if(gamePIN != null) {
            token.append("G:");
            token.append(gamePIN);
            token.append(";");
        }

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
                Position stdPos = playerPositions.get(i).standardizePosition();
                if(i != 0)
                    token.append(",");
                token.append(stdPos.x);
                token.append(",");
                token.append(stdPos.y);
                token.append(",");
                token.append(stdPos.deg);
            }
            token.append(";");
        }

        // New Cannonballs
        if(cannonballs != null) {
            token.append("C:");
            for(int i = 0; i < cannonballs.size(); i++)
            {
                Position stdPos = cannonballs.get(i).getPosition().standardizePosition();
                if(i != 0) // int x, int y, float deg, int uuid, int shooterID
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

    // Example Token: SI:1;U:King Killer;P:2.354,5.46,120.2;C:23,35,55.46,325,1
    public static String tokenizeSoloGameData(String gamePIN, Integer playerID, String playerUsername,
                                          Position playerPosition, ArrayList<Cannonball> cannonballs) {
        StringBuilder token = new StringBuilder();

        token.append("S");

        if(gamePIN != null) {
            token.append("G:");
            token.append(gamePIN);
            token.append(";");
        }

        // Player ID
        if(playerID != null) {
            token.append("I:");
            token.append(playerID);
            token.append(";");
        }

        // Player Username
        if(playerUsername != null) {
            token.append("U:");
            token.append(playerUsername);
            token.append(";");
        }

        // Player Positions
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

        // New Cannonballs
        if(cannonballs != null) {
            token.append("C:");
            for(int i = 0; i < cannonballs.size(); i++)
            {
                Position stdPos = cannonballs.get(i).getPosition().standardizePosition();
                if(i != 0) // int x, int y, float deg, int uuid, int shooterID
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
        if(token.contains("S")) {
            Log.d("TOKEN DEBUG", "Solo detokenizer reached!");
            detokenizeSoloGameData(token);
            return;
        }
        Log.d("TOKEN DEBUG", "Detokenizer reached!");

        int GsIndex = token.indexOf("G:");
        int IsIndex = token.indexOf("I:");
        int UsIndex = token.indexOf("U:");
        int PsIndex = token.indexOf("P:");
        int CsIndex = token.indexOf("C:");

        // Game PIN
        if(GsIndex != -1) {
            String GamePIN;
            StringBuilder cursor = new StringBuilder();
            for(int i = GsIndex + 2; i < token.indexOf(";", GsIndex); i++) {
                cursor.append(token.charAt(i));
            }
            GamePIN = cursor.toString().trim();
            GameData.getInstance().setGamePin(GamePIN);
        }

        // Player IDs
        if(IsIndex != -1) {
            ArrayList<Integer> playerIDs = new ArrayList<>();
            for(int i = IsIndex + 2; i < token.indexOf(";", IsIndex); i++) {
                playerIDs.add(token.charAt(i) - '0');
//                Log.d("TOKEN DEBUG", "PlayerID: " + (token.charAt(i) - '0'));
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
//            Log.d("TOKEN DEBUG", "PlayerUsername: " + cursor.toString().trim());
            GameData.getInstance().setPlayerUsernames(usernames);
        }

        // Player Positions
        if(PsIndex != -1) {
            ArrayList<Position> positions = new ArrayList<>();
            StringBuilder cursor = new StringBuilder();
            int xydegCounter = 0;
            float x = 300, y = 300, deg = 0;
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
                        positions.add((new Position(x, y, deg)).scalePosition());
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
            int x = 300, y = 200, uuid = 0, shooterID = 0;
            float deg = 0;
            int varCounter = 0;
            for(int i = CsIndex + 2; i < token.indexOf(";", CsIndex); i++) {
                if(token.charAt(i) == ',') {
                    if(varCounter == 0) {
                        x = Math.round(Float.parseFloat(cursor.toString()));
                        varCounter ++;
                    }
                    else if(varCounter == 1) {
                        y = Math.round(Float.parseFloat(cursor.toString()));
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
                                Math.round(y * Position.SCREEN_SCALE), deg, uuid, shooterID));
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

    // Must have ID field
    public static void detokenizeSoloGameData(String token) {
        int GsIndex = token.indexOf("G:");
        int IsIndex = token.indexOf("I:");
        int UsIndex = token.indexOf("U:");
        int PsIndex = token.indexOf("P:");
        int CsIndex = token.indexOf("C:");

        // Game PIN
        if(GsIndex != -1) {
            String GamePIN;
            StringBuilder cursor = new StringBuilder();
            for(int i = GsIndex + 2; i < token.indexOf(";", GsIndex); i++) {
                cursor.append(token.charAt(i));
            }
            GamePIN = cursor.toString().trim();
            GameData.getInstance().setGamePin(GamePIN);
        }

        // Player IDs
        int playerID = token.charAt(IsIndex + 2) - '0';
//        Log.d("TOKEN DEBUG", "PlayerID: " + (token.charAt(IsIndex + 2) - '0'));
        GameData.getInstance().addPlayerID(playerID);

        // Player Usernames
        if(UsIndex != -1) {
            String username;
            StringBuilder cursor = new StringBuilder();
            for(int i = UsIndex + 2; i < token.indexOf(";", UsIndex); i++) {
                    cursor.append(token.charAt(i));
            }
            username = cursor.toString().trim();
//            Log.d("TOKEN DEBUG", "Player Username: " + cursor.toString().trim());
            GameData.getInstance().addPlayerUsername(playerID, username);
        }

        // Player Positions
        if(PsIndex != -1) {
            Position position;
            StringBuilder cursor = new StringBuilder();
            int xydegCounter = 0;
            float x = 300, y = 300, deg = 0;
            for(int i = PsIndex + 2; i < token.indexOf(";", PsIndex) + 1; i++) {
                if(token.charAt(i) == ',' || token.charAt(i) == ';') {
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
                        xydegCounter = 0;
                    }
                    cursor = new StringBuilder();
                }
                else {
                    cursor.append(token.charAt(i));
                }
            }
            position = (new Position(x, y, deg)).scalePosition();
//            Log.d("TOKEN DEBUG", "Player Position: " + x + " " + y + " " + deg);
//            Log.d("TOKEN DEBUG", "GameData Position: " +
//                    GameData.getInstance().playerPositions.get(0).x + " " +
//                    GameData.getInstance().playerPositions.get(0).y + " " +
//                    GameData.getInstance().playerPositions.get(0).deg);
            GameData.getInstance().setPlayerPosition(playerID, position);
        }

        // New Cannonballs
        if(CsIndex != -1) {
            StringBuilder cursor = new StringBuilder();
            int x = 10, y = 20, uuid = 0, shooterID = 0;
            float deg = 0;
            int varCounter = 0;
            for(int i = CsIndex + 2; i < token.indexOf(";", CsIndex); i++) {
                if(token.charAt(i) == ',') {
                    if(varCounter == 0) {
                        x = Math.round(Float.parseFloat(cursor.toString()));
                        varCounter ++;
                    }
                    else if(varCounter == 1) {
                        y = Math.round(Float.parseFloat(cursor.toString()));
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
