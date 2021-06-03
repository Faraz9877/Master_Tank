package com.afaa.tanktrouble.match;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.afaa.tanktrouble.R;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.datahouse.BluetoothService;
import com.afaa.tanktrouble.datahouse.DataProtocol;
import com.afaa.tanktrouble.datahouse.GameData;

public class JoinActivity extends AppCompatActivity {

    String mUserId;
    String mGamePin;
    boolean mWaitActivityStarting;

    private BluetoothService btService;
    private BluetoothService.MessageChannel messageChannel;

    private final static int REQUEST_ENABLE_BT = 1;
    private static final String TAG = "WL/JoinActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mUserId = UserUtils.getUserId();
        mWaitActivityStarting = false;

        bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);


        if (mUserId != null && mUserId.length() == 0) {
            Log.e(TAG, "No user ID set");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (btService != null) {
            btService.registerActivity(JoinActivity.class);
            GameData.getInstance().setBtService(btService);
        }

        mWaitActivityStarting = false;
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


    public void onClickEnterGamePin(View view) {

        EditText editGamePin = findViewById(R.id.edit_game_pin);
        mGamePin = editGamePin.getText().toString();

        if (mGamePin.length() > 0 && mGamePin.equals(GameData.getInstance().getGamePin())) {
            mWaitActivityStarting = true;
            joinGame();
        } else {
            createInvalidPinDialog();
        }
    }


    private void joinGame() {
        GameData.getInstance().addPlayer(mUserId, 1);
        GameData.getInstance().sync(1110, true);
        Intent intent = new Intent(getApplicationContext(), WaitActivity.class);
        startActivity(intent);
    }


    private void createInvalidPinDialog() {


        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setMessage("We didn't recognize that game PIN. \nPlease try again.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });


        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(JoinActivity.class);

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
                    JoinActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String data = new String(buffer);
                            Log.d(TAG, "Join Message Process: " + data);
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
