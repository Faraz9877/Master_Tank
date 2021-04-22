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

    private String mGamePin;
    private String mUserId;

    private static final String TAG = "WL/WaitActivity";
    private static final String GAMES_KEY = Constants.GAMES_KEY;
    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private static final String STARTED_KEY = Constants.STARTED_KEY;
    private static final String OPPONENT_IDS_KEY = Constants.OPPONENT_IDS_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        Bundle intentBundle = getIntent().getExtras();
        mUserId = UserUtils.getUserId();

        if (intentBundle != null) {
            mGamePin = intentBundle.getString(GAME_PIN_KEY);
            if (mGamePin != null) {
                waitForGameToStart(mGamePin);
            }
        }
    }

    /**
     * Creates a listener to wait for the game to start.
     *
     * @param gamePin the random pin associated with the game
     */
    private void waitForGameToStart(String gamePin) {
        // Listen for new people joining the game
        GameData.getInstance().sync();
        GameData.getInstance().addPlayer(mUserId);
        if (GameData.getInstance().getStatus() == -1) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else if (GameData.getInstance().getStatus() == 1) {
            onGameStarted();
        }
    }

    /**
     * Called when the host presses the start game button.
     */
    private void onGameStarted() {
        GameData.getInstance().sync();
        ArrayList<Integer> opponentIDs = new ArrayList<>();

        for (Integer playerID : GameData.getInstance().getPlayerIDs()) {
            if (playerID != null && playerID != GameData.getInstance().getThisPlayer()) {
                opponentIDs.add(playerID);
            }
        }

        Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
        intent.putExtra(OPPONENT_IDS_KEY, opponentIDs);
        intent.putExtra(GAME_PIN_KEY, mGamePin);
        startActivity(intent);
    }
}
