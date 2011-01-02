package com.pilot51.cannon;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class GameType extends Activity implements OnClickListener {
	
	Button btnCustom, btnRandom, btnMission, btnMulti;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gametype);
		new Common().ad(this);
		btnCustom = (Button) findViewById(R.id.btnCustom);
		btnCustom.setOnClickListener(this);
		btnRandom = (Button) findViewById(R.id.btnRandom);
		btnRandom.setOnClickListener(this);
		btnMission = (Button) findViewById(R.id.btnMission);
		btnMission.setOnClickListener(this);
		btnMission.setVisibility(Button.GONE); // Disabled until operational
		btnMulti = (Button) findViewById(R.id.btnMulti);
		btnMulti.setOnClickListener(this);
		btnMulti.setVisibility(Button.GONE); // Disabled until operational
	}

	@Override
	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.btnCustom:
			startActivity(new Intent(this, CustomGame.class));
			break;
		case R.id.btnRandom:
			Intent i = new Intent(this, GameField.class);
			i.putExtra("random", true);
			startActivity(i);
			break;
		case R.id.btnMission:
			break;
		case R.id.btnMulti:
			break;
		}
	}
}