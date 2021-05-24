package com.wztlei.tanktrouble.match;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.MainActivity;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.battle.BattleActivity;
import com.wztlei.tanktrouble.datahouse.GameData;

public class HostActivity extends AppCompatActivity {

    private String mGamePin;
    private String mUserId;
    private boolean mBattleActivityStarting;

    private static final String TAG = "WL/HostActivity";
    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int MIN_GAME_PIN = 1000;
    private static final int MAX_GAME_PIN = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);

        // Get a reference to the games database
        UserUtils.initialize(this);

        // Get the user id
        mUserId = UserUtils.getUserId();

        // Set the random game pin
        hostGameWithRandomPin();

        mBattleActivityStarting = false;
    }

    @Override
    protected void onRestart(){
        super.onRestart();
        onUniqueRandomPinCreated();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (!mBattleActivityStarting) {
            // To destroy game
        }
    }

    /**
     * Creates a new game in firebase that is hosted by the current user.
     */
    private void hostGameWithRandomPin() {
        // Create a new random game pin and display it
        mGamePin = Integer.toString(UserUtils.randomInt(MIN_GAME_PIN, MAX_GAME_PIN));
        TextView textViewGamePin = findViewById(R.id.text_game_pin);
        String textGamePin = "PIN: " + mGamePin;
        textViewGamePin.setText(textGamePin);

        GameData.getInstance().setGamePin(mGamePin);
        GameData.getInstance().addPlayer(mUserId, 0);

        if (mUserId != null && mUserId.length() > 0) {
            // Process the game pin once it has been created
            onUniqueRandomPinCreated();

            // Automatically display that one player is ready (which is the current user)
            TextView textPlayersReady = findViewById(R.id.text_players_ready);
            String newPlayersReadyText = "1 Player Ready";
            textPlayersReady.setText(newPlayersReadyText);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }
    }

    /**
     * Displays the game pin on the screen and sets a listener to update a text view displaying
     * the number of people that are waiting to play the game.
     */
    private void onUniqueRandomPinCreated() {
        if (mUserId == null) {
            startActivity(new Intent(this, MainActivity.class));
        }

        // Listen for new people joining the game
        TextView textPlayersReady = findViewById(R.id.text_players_ready);
        int numPlayers = GameData.getInstance().getPlayerIDs().size();

        // Update the text displaying how many people have joined the game
        if (numPlayers == 1) {
            String newPlayersReadyText = "1 Player Ready";
            textPlayersReady.setText(newPlayersReadyText);
        } else {
            String newPlayersReadyText = numPlayers + " Players Ready";
            textPlayersReady.setText(newPlayersReadyText);
        }
    }

    /**
     * Starts the multiplayer game by starting the battle activity.
     *
     * @param view the button that is clicked
     */
    public void onClickStartGame(View view) {
        mBattleActivityStarting = true;
        GameData.getInstance().setStatus(1);
        Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
        intent.putExtra(GAME_PIN_KEY, mGamePin);
        startActivity(intent);
    }

    public void setBluetooth() {
        // Enable bluetooth
        if (!Bluetooth.getInstance().getBluetoothAdapter().isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        // Make device discoverable for 5 minutes
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        // Start accept thread to get a connection with the peer

    }
}
