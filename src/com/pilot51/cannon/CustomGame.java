package com.pilot51.cannon;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class CustomGame extends Activity implements OnClickListener {

	Button btnFire;

	EditText editAngle, editVelocity, editFuze, editGravity, editWind,
			editTargetD, editTargetH, editTargetS, editProjS;

	float angle, velocity, fuze, gravity, wind;

	int targetD, targetH, targetS, projS;

	SharedPreferences prefCustom;

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
		
		// Move old custom value preferences to new preferences (simpler naming)
		// To be removed at least 1 month after v2 release
		if(getSharedPreferences("valuePref", MODE_PRIVATE).contains("prefAngle")) movePref();
		
		loadValues();

		// Set the virtual keyboard to the numeric "phone" type without
		// overriding input type defined in the layout XML
		editAngle.setRawInputType(3);
		editVelocity.setRawInputType(3);
		editFuze.setRawInputType(3);
		editGravity.setRawInputType(3);
		editWind.setRawInputType(3);
		editTargetD.setRawInputType(3);
		editTargetH.setRawInputType(3);
		editTargetS.setRawInputType(3);
		editProjS.setRawInputType(3);
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			new Common().menu(this);
			return true;
		case R.id.menu_prefs:
			startActivityForResult(new Intent(getBaseContext(), Preferences.class), 0);
			return true;
		}
		return false;
	}

	public void onClick(View src) {
		switch (src.getId()) {
		case R.id.buttonFire:
			grabValues();
			saveValues();
			if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("classic", false)) {
				startActivity(new Intent(this, Classic.class));
			} else startActivity(new Intent(this, GameField.class));
			break;
		}
	}
	
	void loadValues() {
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

	void grabValues() {
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

	void saveValues() {
		SharedPreferences.Editor e = prefCustom.edit();
		e.putFloat("angle", angle);
		e.putFloat("velocity", velocity);
		e.putFloat("fuze", fuze);
		e.putFloat("gravity", gravity);
		e.putFloat("wind", wind);
		e.putInt("targetD", targetD);
		e.putInt("targetH", targetH);
		e.putInt("targetS", targetS);
		e.putInt("projS", projS);
		e.commit();
	}
	
	void movePref() {
		SharedPreferences p = getSharedPreferences("valuePref", 0);
		SharedPreferences.Editor e = prefCustom.edit();
		e.putFloat("angle", p.getFloat("prefAngle", 0));
		e.putFloat("velocity", p.getFloat("prefVelocity", 0));
		e.putFloat("fuze", p.getFloat("prefFuze", 0));
		e.putFloat("gravity", p.getFloat("prefGravity", 0));
		e.putFloat("wind", p.getFloat("prefWind", 0));
		e.putInt("targetD", p.getInt("prefTargetD", 0));
		e.putInt("targetH", p.getInt("prefTargetH", 0));
		e.putInt("targetS", p.getInt("prefTargetS", 0));
		e.commit();
		p.edit().clear().commit();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1) loadValues();
	}
}