package com.wztlei.tanktrouble.match;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.wztlei.tanktrouble.Constants;
import com.wztlei.tanktrouble.R;
import com.wztlei.tanktrouble.UserUtils;
import com.wztlei.tanktrouble.datahouse.GameData;

public class JoinActivity extends AppCompatActivity {

    String mUserId;
    String mGamePin;
    boolean mWaitActivityStarting;

    private static final String GAME_PIN_KEY = Constants.GAME_PIN_KEY;
    private static final String TAG = "WL/JoinActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join);

        mUserId = UserUtils.getUserId();
        mWaitActivityStarting = false;

        // Log an error if there is no user ID
        if (mUserId != null && mUserId.length() == 0) {
            Log.e(TAG, "No user ID set");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mWaitActivityStarting = false;
    }

    @Override
    protected void onStop() {
        super.onStop();
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

        GameData.getInstance().sync();

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
        GameData.getInstance().addPlayer(mUserId);
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
}
