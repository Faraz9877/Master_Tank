package com.afaa.tanktrouble.match;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.MainActivity;
import com.afaa.tanktrouble.R;
import com.afaa.tanktrouble.battle.BattleActivity;
import com.afaa.tanktrouble.datahouse.BluetoothService;
import com.afaa.tanktrouble.datahouse.DataProtocol;
import com.afaa.tanktrouble.datahouse.GameData;

public class WaitActivity extends AppCompatActivity {

    private BluetoothService btService;
    private BluetoothService.MessageChannel messageChannel;

    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private static final String TAG = "WL/WaitActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wait);

        bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);

        waitForGameToStart();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (btService != null) {
            btService.registerActivity(WaitActivity.class);
            GameData.getInstance().setBtService(btService);
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
        super.onDestroy();

        if (btService.getBluetoothAdapter() != null) {
            btService.getBluetoothAdapter().cancelDiscovery();
        }
//        if (btService != null   ) {
//            btService.stopSelf();
//            btService = null;
//        }
        unbindService(connection);
    }


    private void waitForGameToStart() {

        if (GameData.getInstance().getStatus() == -1) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        } else if (GameData.getInstance().getStatus() == 1) {
            onGameStarted();
        } else {

        }
    }


    private void onGameStarted() {
        Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
        startActivity(intent);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(WaitActivity.class);

            btService.setOnConnected(new BluetoothService.OnConnected() {
                @Override
                public void success() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            startActivity(new Intent(JoinActivity.this,
//                                    BtGameConfigurationClientActivity.class));
//                            JoinActivity.this.finish();
                        }
                    });
                }
            });

            messageChannel = btService.getChannel();
            messageChannel.setOnMessageReceivedListener(new BluetoothService.OnMessageReceivedListener() {
                @Override
                public void process(final byte[] buffer) {
                    WaitActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String data = new String(buffer);
//                            Log.d(TAG, "Wait Message Process: " + data);
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
