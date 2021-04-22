package com.wztlei.tanktrouble.match;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.MainActivity;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.BattleActivity;
import com.wztlei.tanktrouble.datahouse.GameData;

import java.util.ArrayList;

public class WaitActivity extends AppCompatActivity {

    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        waitForGameToStart();
    }

    /**
     * Creates a listener to wait for the game to start.
     *
     */
    private void waitForGameToStart() {
        // Listen for new people joining the game
        GameData.getInstance().sync();
        if (GameData.getInstance().getStatus() == -1) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else if (GameData.getInstance().getStatus() == 1) {
            onGameStarted();
        } else {
            // Should keep waiting!
        }
    }

    /**
     * Called when the host presses the start game button.
     */
    private void onGameStarted() {
        GameData.getInstance().sync();
        Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
        startActivity(intent);
    }
}
