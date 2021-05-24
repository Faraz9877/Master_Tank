package com.wztlei.tanktrouble.datahouse;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

import com.wztlei.tanktrouble.R;

public class BtGameConfigurationServerActivity extends AppCompatActivity {

    protected static final String TAG = "ServerConfiguration";

    private static final byte CHANNEL_ID = 2;

    public static final int COLOR_BLUE = 1;
    public static final int COLOR_RED = 2;
    public static final int COLOR_GREEN = 3;
    public static final int COLOR_ORANGE = 4;

    protected int color = COLOR_BLUE;

    private BluetoothService btService;
    private MenuItem acceptButton;
    private BluetoothService.MessageChannel messageChannel;
    private boolean shouldStop = true;

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            btService = ((BluetoothService.BtBinder) service).getService();
            btService.registerActivity(BtGameConfigurationServerActivity.class);

            messageChannel = btService.getChannel(CHANNEL_ID);

            if (acceptButton != null) {
                acceptButton.setEnabled(true);
            }
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
        if (btService != null) {
            btService.unregisterChannel(CHANNEL_ID);
        }
        if (btService != null && shouldStop) {
            btService.stopSelf();
        }
        unbindService(connection);
        super.onDestroy();
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
        // TODO  maybe something else inti gameData
        System.out.println("read to lunch");
    }
}
