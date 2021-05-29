package com.wztlei.tanktrouble.match;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.MainActivity;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.battle.BattleActivity;
import com.wztlei.tanktrouble.datahouse.BluetoothService;
import com.wztlei.tanktrouble.datahouse.DataProtocol;
import com.wztlei.tanktrouble.datahouse.GameData;

import java.util.ArrayList;

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
//        if (btService != null /* && shouldStop */) {
//            btService.stopSelf();
//            btService = null;
//        }
        unbindService(connection);
    }

    /**
     * Creates a listener to wait for the game to start.
     *
     */
    private void waitForGameToStart() {
        // Listen for new people joining the game
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
        Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
        startActivity(intent);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(WaitActivity.class);
            // TODO: manage this part
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
                            Log.d(TAG, "Wait Message Process: " + data);
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
