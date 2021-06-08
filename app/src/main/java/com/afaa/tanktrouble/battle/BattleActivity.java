package com.afaa.tanktrouble.battle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.afaa.tanktrouble.MainActivity;
import com.afaa.tanktrouble.datahouse.BluetoothService;
import com.afaa.tanktrouble.datahouse.DataProtocol;
import com.afaa.tanktrouble.datahouse.GameData;
import com.afaa.tanktrouble.match.WaitActivity;

public class BattleActivity extends AppCompatActivity {

    private static final String TAG = "WL/BattleActivity";

    private BluetoothService btService;
    private BluetoothService.MessageChannel messageChannel;
    private View battleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        battleView = new BattleView(this);
        setContentView(battleView);

        bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);
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
//        Log.d(TAG, "onPause");
//        Log.d(TAG, "isFinishing()==" + isFinishing());
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (btService != null) {
            btService.unregisterActivity();
        }

//        Log.d(TAG, "onStop");
//        Log.d(TAG, "isFinishing()==" + isFinishing());
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

//        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        startActivity( new Intent(this, MainActivity.class));
    }

    private void validateGame() {
        if (GameData.getInstance().isPlayerInGame()) {
            return;
        }

        startActivity(new Intent(getApplicationContext(), MainActivity.class));
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(WaitActivity.class);
            GameData.getInstance().setBtService(btService);

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
