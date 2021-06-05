package com.afaa.tanktrouble.match;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.MainActivity;
import com.afaa.tanktrouble.R;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.battle.BattleActivity;
import com.afaa.tanktrouble.datahouse.BluetoothService;
import com.afaa.tanktrouble.datahouse.DataProtocol;
import com.afaa.tanktrouble.datahouse.GameData;

public class HostActivity extends AppCompatActivity {

    private String mGamePin;
    private String mUserId;
    private boolean mBattleActivityStarting;

    private BluetoothService btService;
    private BluetoothService.MessageChannel messageChannel;

    private static final String TAG = "WL/HostActivity";
    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int MIN_GAME_PIN = 1000;
    private static final int MAX_GAME_PIN = 9999;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_host);


        UserUtils.initialize(this);

        bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);


        mUserId = UserUtils.getUserId();


        hostGameWithRandomPin();

        mBattleActivityStarting = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (btService != null) {
            btService.registerActivity(HostActivity.class);
            GameData.getInstance().setBtService(btService);
        }
    }

    @Override
    protected void onRestart(){
        super.onRestart();

        onUniqueRandomPinCreated();
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
        super.onDestroy();

        if (!mBattleActivityStarting) {
            if (btService.getBluetoothAdapter() != null) {
                btService.getBluetoothAdapter().cancelDiscovery();
            }
//            if (btService != null   ) {
//                btService.stopSelf();
//                btService = null;
//            }
            unbindService(connection);
        }
    }


    private void hostGameWithRandomPin() {

        mGamePin = Integer.toString(UserUtils.randomInt(MIN_GAME_PIN, MAX_GAME_PIN));
        TextView textViewGamePin = findViewById(R.id.text_game_pin);
        String textGamePin = "PIN: " + mGamePin;
        textViewGamePin.setText(textGamePin);

        GameData.getInstance().setGamePin(mGamePin);
        GameData.getInstance().addPlayer(mUserId, 0);

        if (mUserId != null && mUserId.length() > 0) {

            onUniqueRandomPinCreated();


            TextView textPlayersReady = findViewById(R.id.text_players_ready);
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


        TextView textPlayersReady = findViewById(R.id.text_players_ready);
        int numPlayers = GameData.getInstance().getPlayerIDs().size();


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

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(HostActivity.class);

            btService.setOnConnected(new BluetoothService.OnConnected() {
                @Override
                public void success() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            startActivity(new Intent(HostActivity.this,
//                                    BtGameConfigurationClientActivity.class));
//                            HostActivity.this.finish();
                        }
                    });
                }
            });

            messageChannel = btService.getChannel();
            messageChannel.setOnMessageReceivedListener(new BluetoothService.OnMessageReceivedListener() {
                @Override
                public void process(final byte[] buffer) {
                    HostActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String data = new String(buffer);
                            Log.d(TAG, "Host Message Process: " + data);
                            DataProtocol.detokenizeGameData(data);
                        }
                    });
                }
            });

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            btService = null;
        }
    };
}
