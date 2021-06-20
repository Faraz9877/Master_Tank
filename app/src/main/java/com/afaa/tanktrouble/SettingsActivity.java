package com.afaa.tanktrouble;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    private EditText editUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        String username = UserUtils.getUsername();
        editUsername = findViewById(R.id.edit_username);
        editUsername.setText(username);
    }


    public void onClickSaveButton(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void onClickCancelButton(View view) {
        startActivity(new Intent(this, MainActivity.class));
    }

    public void onClickRandomizeButton(View view) {
        String randomUsername = UserUtils.generateRandomUsername();
        UserUtils.setUsername(randomUsername);
        editUsername.setText(randomUsername);
    }

}
