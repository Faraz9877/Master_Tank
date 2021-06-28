package com.afaa.tanktrouble.battle;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.afaa.tanktrouble.Constants;
import com.afaa.tanktrouble.MainActivity;
import com.afaa.tanktrouble.UserUtils;
import com.afaa.tanktrouble.cannonball.Cannonball;
import com.afaa.tanktrouble.datahouse.BluetoothService;
import com.afaa.tanktrouble.datahouse.DataProtocol;
import com.afaa.tanktrouble.datahouse.GameData;
import com.afaa.tanktrouble.tank.Tank;

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
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (btService != null) {
            btService.unregisterActivity();
        }
    }

    private void endGame() {
        ((BattleView)battleView).finishThread();
        finish();
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

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to leave the game?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        endGame();
                        dialog.cancel();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert = builder.create();
        alert.setTitle("Are You Sure?");
        alert.show();
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
                            Log.d("Receiver data", data);
                            if (data.contains(GameData.GAME_OVER_MESSAGE)) {
                                endGame();
                            }
                            else if(data.contains(GameData.TANK_FIRE)){
//                                Log.d("Shoot Time Recv:", "signalTankFire receive time: " +
//                                    GameData.getInstance().getElapsedTime());
                                Cannonball cannonball = GameData.getInstance().opponentTankFire();
                                GameData.getInstance().getCannonballSet().addCannonball(cannonball);
                            }
                            else if(data.contains(GameData.TANK_DIED)){
                                GameData.getInstance().setOpponentTankHit(true);
                            }
                            else {
                                DataProtocol.detokenizePosition(data);
//                                DataProtocol.detokenizeCannonBall(data);
                            }
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
