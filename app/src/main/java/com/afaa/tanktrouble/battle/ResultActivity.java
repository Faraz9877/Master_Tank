package com.afaa.tanktrouble.battle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.afaa.tanktrouble.MainActivity;
import com.afaa.tanktrouble.R;

public class ResultActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_result);
		ImageView imageView = findViewById(R.id.result_game);
		if (getIntent().getExtras().getInt("win") == 1)
			imageView.setImageResource(R.drawable.win);
		else
			imageView.setImageResource(R.drawable.loose);
	}

	public void onClickBackMenuButton (View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
	}
}