package com.wztlei.tanktrouble.battle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.MainActivity;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.datahouse.GameData;

import java.util.ArrayList;

public class BattleActivity extends AppCompatActivity {

    String mGamePin;

    private static final String TAG = "WL/BattleActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the graphics with battle view
        setContentView(new BattleView(this));

        // Grab the database reference for the game into which the user has possibly joined
        GameData.getInstance().sync();
        validateGame();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        Log.d(TAG, "isFinishing()==" + isFinishing());
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");
        Log.d(TAG, "isFinishing()==" + isFinishing());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        startActivity( new Intent(this, MainActivity.class));
    }

    /**
     * Determines if the user has actually joined the game,
     * and if not, return to the main activity.
     */
    private void validateGame() {
        // Determine if the user has actually joined the game

        // Determine if any of the children of the game has a key of the user id
        if (GameData.getInstance().isPlayerInGame()) {
            // Return if we have found a key matching the user id, since
            // this means that the user has actually joined the game
            return;
        }

        // The user has not actually joined the game, so return to the main activity
        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }
}
