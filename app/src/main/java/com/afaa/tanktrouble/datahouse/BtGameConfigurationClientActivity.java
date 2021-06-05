package com.afaa.tanktrouble.datahouse;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afaa.tanktrouble.R;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.battle.BattleActivity;

public class BtGameConfigurationClientActivity extends AppCompatActivity {

    private static final String TAG = "ConfigurationClient";

    String mUserId;
    String mGamePin;
    boolean mWaitActivityStarting;

    private BluetoothService btService;
    private BluetoothService.MessageChannel messageChannel;

    private boolean shouldStop = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_game_configuration_client);

        mUserId = UserUtils.getUserId();
        mWaitActivityStarting = false;

        Intent btServiceIntent = new Intent(this, BluetoothService.class);
        startService(btServiceIntent);
        bindService(btServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }


    private static long fromByteArray(byte[] array) {
        long result = 0;
        for (int i = 0; i < 8; ++i) {
            result <<= 8;
            result |= (array[i] & 0xFF);
        }
        return result;
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            GameData.getInstance().setBtService(btService);
            btService.registerActivity(BtGameConfigurationClientActivity.class);

            messageChannel = btService.getChannel();
            messageChannel.setOnMessageReceivedListener(new BluetoothService.OnMessageReceivedListener() {
                @Override
                public void process(final byte[] buffer) {
                    BtGameConfigurationClientActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String data = new String(buffer);
//                            Log.d(TAG, "BtClient Message Process: " + data);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.reject, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (btService != null) {
            btService.stopSelf();
            btService = null;
        }
        finish();
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (btService != null) {
            btService.registerActivity(BtGameConfigurationClientActivity.class);
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
        if(!mWaitActivityStarting) {
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

    public void onClickEnterGamePin(View view) {

        EditText editGamePin = findViewById(R.id.edit_game_pin2);
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
        GameData.getInstance().sync(1100, true);
        Intent intent = new Intent(getApplicationContext(), BattleActivity.class);
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
}