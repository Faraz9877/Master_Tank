package com.wztlei.tanktrouble.match;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
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

import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.datahouse.BluetoothService;
import com.wztlei.tanktrouble.datahouse.DataProtocol;
import com.wztlei.tanktrouble.datahouse.DeviceChooserActivity;
import com.wztlei.tanktrouble.datahouse.GameData;

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

        // Log an error if there is no user ID
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
//        if (btService != null /* && shouldStop */) {
//            btService.stopSelf();
//            btService = null;
//        }
        unbindService(connection);
    }

    /**
     * Called whenever the user clicks the button to enter a game pin.
     * The method attempts to allow the user to join that game.
     *
     * @param view the button that is clicked
     */
    public void onClickEnterGamePin(View view) {
        // Get the game pin entered by the user
        EditText editGamePin = findViewById(R.id.edit_game_pin);
        mGamePin = editGamePin.getText().toString();

        if (mGamePin.length() > 0 && mGamePin.equals(GameData.getInstance().getGamePin())) {
            mWaitActivityStarting = true;
            joinGame();
        } else {
            createInvalidPinDialog();
        }
    }

    /**
     * Allows the user to join the game by adding the user's id to that game's database.
     * The method also starts the wait activity which functions as a waiting lobby.
     */
    private void joinGame() {
        GameData.getInstance().addPlayer(mUserId, 1);
        GameData.getInstance().sync(1100, true);
        Intent intent = new Intent(getApplicationContext(), WaitActivity.class);
        startActivity(intent);
    }

    /**
     *  Display an alert dialog with a single button saying Ok.
     *
     */
    private void createInvalidPinDialog() {

        // Build an alert dialog using the title and message
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("")
                .setMessage("We didn't recognize that game PIN. \nPlease try again.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {}
                });

        // Get the AlertDialog from create() and show it
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(JoinActivity.class);
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
