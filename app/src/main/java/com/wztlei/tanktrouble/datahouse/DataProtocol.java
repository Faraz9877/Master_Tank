package com.wztlei.tanktrouble.datahouse;

import com.wztlei.tanktrouble.battle.Position;
import com.wztlei.tanktrouble.cannonball.Cannonball;
import com.wztlei.tanktrouble.cannonball.CannonballSet;

import java.util.ArrayList;

public class DataProtocol {
    private int state;

    // Example Token: I:01;U:King Killer,Sly Fox;P:2.354,5.46,120.2,55.4,15.6,-30.8;C:23,35,55.46,325,1
    public String tokenizeGameData(ArrayList<Integer> playerIDs, ArrayList<String> playerUsernames,
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

    public void detokenizeGameData(String token) {
        int IsIndex = token.indexOf("I:");
        int UsIndex = token.indexOf("U:");
        int PsIndex = token.indexOf("P:");
        int CsIndex = token.indexOf("C:");

        // Player IDs
        if(IsIndex != -1) {
            
            for(int i = IsIndex; i < token.indexOf(";", IsIndex); i++) {

            }
        }

        // Player Usernames
        if(UsIndex != -1) {
            for(int i = UsIndex; i < token.indexOf(";", UsIndex); i++) {

            }
        }

        // Player Positions
        if(PsIndex != -1) {
            for(int i = PsIndex; i < token.indexOf(";", PsIndex); i++) {

            }
        }

        // New Cannonballs
        if(CsIndex != -1) {
            for(int i = CsIndex; i < token.indexOf(";", CsIndex); i++) {

            }
        }

    }

}
