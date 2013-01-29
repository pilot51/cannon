/*
 * Copyright 2013 Mark Injerd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pilot51.cannon;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CustomGame extends Activity implements OnClickListener {
	private Button btnFire;
	private EditText editAngle, editVelocity, editFuze, editGravity, editWind,
			editTargetD, editTargetH, editTargetS, editProjS;
	private float angle, velocity, fuze, gravity, wind;
	private int targetD, targetH, targetS, projS;
	private static SharedPreferences prefCustom;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.customgame);
		editAngle = (EditText) findViewById(R.id.editAngle);
		editVelocity = (EditText) findViewById(R.id.editVelocity);
		editFuze = (EditText) findViewById(R.id.editFuze);
		editGravity = (EditText) findViewById(R.id.editGravity);
		editWind = (EditText) findViewById(R.id.editWind);
		editTargetD = (EditText) findViewById(R.id.editTargetD);
		editTargetH = (EditText) findViewById(R.id.editTargetH);
		editTargetS = (EditText) findViewById(R.id.editTargetS);
		editProjS = (EditText) findViewById(R.id.editProjS);
		btnFire = (Button) findViewById(R.id.buttonFire);
		btnFire.setOnClickListener(this);
		prefCustom = getSharedPreferences("custom", MODE_PRIVATE);
		loadValues();
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			Common.menu(this);
			return true;
		case R.id.menu_prefs:
			startActivityForResult(new Intent(this, Preferences.class), 0);
			return true;
		}
		return false;
	}

	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.buttonFire:
			grabValues();
			saveValues();
			if(Common.getPrefs().getBoolean("classic", false)) {
				startActivity(new Intent(this, Classic.class));
			} else startActivity(new Intent(this, GameField.class));
			break;
		}
	}
	
	private void loadValues() {
		editAngle.setText(Float.toString(prefCustom.getFloat("angle", 59)));
		editVelocity.setText(Float.toString(prefCustom.getFloat("velocity", (float) 82.5)));
		editFuze.setText(Float.toString(prefCustom.getFloat("fuze", (float) 0)));
		editGravity.setText(Float.toString(prefCustom.getFloat("gravity", 1)));
		editWind.setText(Float.toString(prefCustom.getFloat("wind", -3)));
		editTargetD.setText(Integer.toString(prefCustom.getInt("targetD", 250)));
		editTargetH.setText(Integer.toString(prefCustom.getInt("targetH", 250)));
		editTargetS.setText(Integer.toString(prefCustom.getInt("targetS", 10)));
		editProjS.setText(Integer.toString(prefCustom.getInt("projS", 3)));
	}

	private void grabValues() {
		// Grab values from edit fields & use 0 for null fields
		try {
			angle = Float.parseFloat(editAngle.getText().toString());
		} catch (NumberFormatException e) {
			angle = 0;
		}
		try {
			velocity = Float.parseFloat(editVelocity.getText().toString());
		} catch (NumberFormatException e) {
			velocity = 0;
		}
		try {
			fuze = Float.parseFloat(editFuze.getText().toString());
		} catch (NumberFormatException e) {
			fuze = 0;
		}
		try {
			gravity = Float.parseFloat(editGravity.getText().toString());
		} catch (NumberFormatException e) {
			gravity = 0;
		}
		try {
			wind = Float.parseFloat(editWind.getText().toString());
		} catch (NumberFormatException e) {
			wind = 0;
		}
		try {
			targetD = Integer.parseInt(editTargetD.getText().toString());
		} catch (NumberFormatException e) {
			targetD = 0;
		}
		try {
			targetH = Integer.parseInt(editTargetH.getText().toString());
		} catch (NumberFormatException e) {
			targetH = 0;
		}
		try {
			targetS = Integer.parseInt(editTargetS.getText().toString());
		} catch (NumberFormatException e) {
			targetS = 0;
		}
		try {
			projS = Integer.parseInt(editProjS.getText().toString());
		} catch (NumberFormatException e) {
			projS = 0;
		}
	}

	private void saveValues() {
		prefCustom.edit()
		.putFloat("angle", angle)
		.putFloat("velocity", velocity)
		.putFloat("fuze", fuze)
		.putFloat("gravity", gravity)
		.putFloat("wind", wind)
		.putInt("targetD", targetD)
		.putInt("targetH", targetH)
		.putInt("targetS", targetS)
		.putInt("projS", projS)
		.commit();
	}
	
	static SharedPreferences getCustomPrefs() {
		return prefCustom;
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1) loadValues();
	}
}