package com.wztlei.tanktrouble;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;

public class BtGameConfigurationServerActivity extends AppCompatActivity {

    protected static final String TAG = "ServerConfiguration";

    public static final int COLOR_BLUE = 1;
    public static final int COLOR_RED = 2;
    public static final int COLOR_GREEN = 3;
    public static final int COLOR_ORANGE = 4;

    protected int color = COLOR_BLUE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bt_game_configuration_server);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_configuration, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
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
}
