package com.afaa.tanktrouble.datahouse;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;
import android.widget.TextView;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.MainActivity;
import com.afaa.tanktrouble.R;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.battle.BattleActivity;

public class BtGameConfigurationServerActivity extends AppCompatActivity {

    private String mGamePin;
    private String mUserId;
    private boolean mBattleActivityStarting;

    protected static final String TAG = "ServerConfiguration";

    public static final int COLOR_BLUE = 1;
    public static final int COLOR_RED = 2;
    public static final int COLOR_GREEN = 3;
    public static final int COLOR_ORANGE = 4;
    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private static final int MIN_GAME_PIN = 1000;
    private static final int MAX_GAME_PIN = 9999;

    protected int color = COLOR_BLUE;

    private BluetoothService btService;
    private MenuItem acceptButton;
    private BluetoothService.MessageChannel messageChannel;
    private boolean shouldStop = true;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            GameData.getInstance().setBtService(btService);
            btService.registerActivity(BtGameConfigurationServerActivity.class);

            messageChannel = btService.getChannel();
            messageChannel.setOnMessageReceivedListener(new BluetoothService.OnMessageReceivedListener() {
                @Override
                public void process(final byte[] buffer) {
                    BtGameConfigurationServerActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String data = new String(buffer);
                            Log.d(TAG, "BtServer Message Process: " + data);
                            DataProtocol.detokenizeGameData(data);
                        }
                    });
                }
            });

            if (acceptButton != null) {
                acceptButton.setEnabled(true);
            }
            hostGameWithRandomPin();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_game_configuration_server);

        UserUtils.initialize(this);
        mUserId = UserUtils.getUserId();
        mBattleActivityStarting = false;

        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_configuration, menu);
        boolean result = super.onCreateOptionsMenu(menu);
        acceptButton = menu.findItem(R.id.accept);
        if (btService == null) {
            acceptButton.setEnabled(false);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        launchGame();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onRestart(){
        super.onRestart();

//        onUniqueRandomPinCreated();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (btService != null) {
            btService.registerActivity(BtGameConfigurationServerActivity.class);
        }
    }

    @Override
    protected void onStop() {
        if (btService != null) {
            btService.unregisterActivity();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (!mBattleActivityStarting) {
            if (btService != null) {
                btService.unregisterChannel();
            }
            if (btService != null && shouldStop) {
                btService.stopSelf();
            }
            unbindService(connection);
        }
        super.onDestroy();
    }

    private void hostGameWithRandomPin() {
        // Create a new random game pin and display it
        mGamePin = Integer.toString(UserUtils.randomInt(MIN_GAME_PIN, MAX_GAME_PIN));
        TextView textViewGamePin = findViewById(R.id.text_game_pin2);
        String textGamePin = "PIN: " + mGamePin;
        textViewGamePin.setText(textGamePin);

        GameData.getInstance().setGamePin(mGamePin);
        GameData.getInstance().addPlayer(mUserId, 0);
        GameData.getInstance().sync(11110, true);

        if (mUserId != null && mUserId.length() > 0) {
            // Process the game pin once it has been created
            onUniqueRandomPinCreated();

            // Automatically display that one player is ready (which is the current user)
            TextView textPlayersReady = findViewById(R.id.text_players_ready2);
            String newPlayersReadyText = "1 Player Ready";
            textPlayersReady.setText(newPlayersReadyText);
        } else {
            Log.e(TAG, "Warning: no user Id");
        }
    }

    private void onUniqueRandomPinCreated() {
        if (mUserId == null) {
            startActivity(new Intent(this, MainActivity.class));
        }

        // Listen for new people joining the game
        TextView textPlayersReady = findViewById(R.id.text_players_ready2);
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

    public void onClickStartGame(View view) {
        mBattleActivityStarting = true;
        GameData.getInstance().setStatus(1);
        Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
        intent.putExtra(GAME_PIN_KEY, mGamePin);
        startActivity(intent);
    }

    public void onColorSelected(View view) {
        if (!((RadioButton) view).isChecked()) {
            return;
        }

        switch (view.getId()) {
            case R.id.color_blue:
                color = COLOR_BLUE;
                break;

            case R.id.color_green:
                color = COLOR_GREEN;
                break;

            case R.id.color_orange:
                color = COLOR_ORANGE;
                break;

            case R.id.color_red:
                color = COLOR_RED;
                break;
        }
    }

    public void launchGame() {
        // TODO  maybe something else init gameData

        System.out.println("ready to launch");
    }
}
