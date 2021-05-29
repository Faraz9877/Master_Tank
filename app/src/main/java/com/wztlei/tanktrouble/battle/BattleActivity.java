package com.wztlei.tanktrouble.battle;

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
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.datahouse.BluetoothService;
import com.wztlei.tanktrouble.datahouse.DataProtocol;
import com.wztlei.tanktrouble.datahouse.DeviceChooserActivity;
import com.wztlei.tanktrouble.datahouse.GameData;
import com.wztlei.tanktrouble.match.WaitActivity;

import java.util.ArrayList;

public class BattleActivity extends AppCompatActivity {

    private static final String TAG = "WL/BattleActivity";

    private BluetoothService btService;
    private BluetoothService.MessageChannel messageChannel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the graphics with battle view
        setContentView(new BattleView(this));

        bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);

        // Grab the database reference for the game into which the user has possibly joined
        validateGame();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (btService != null) {
            btService.registerActivity(BattleActivity.class);
            GameData.getInstance().setBtService(btService);
        }
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

        if (btService != null) {
            btService.unregisterActivity();
        }

        Log.d(TAG, "onStop");
        Log.d(TAG, "isFinishing()==" + isFinishing());
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
                    BattleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String data = new String(buffer);
                            Log.d(TAG, "Battle Message Process: " + data);
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
