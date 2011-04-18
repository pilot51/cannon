package com.pilot51.cannon;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Process;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class GameType extends Activity implements OnClickListener {
	
	Button btnCustom, btnRandom, btnMission, btnMulti;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gametype);
		
		if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).contains("prefCollide")) movePref();
		
		// Load default preferences from xml if not saved
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		
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
		
		if(getLastNonConfigurationInstance() != (Object)true) {
			Toast.makeText(this, "Press menu button for help & preferences.", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	public Object onRetainNonConfigurationInstance() {
	    return true;
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
			startActivity(new Intent(getBaseContext(), Preferences.class));
			return true;
		}
		return false;
	}
	
	void movePref() {
		SharedPreferences p = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		SharedPreferences.Editor e = p.edit();
		e.putBoolean("collide", p.getBoolean("prefCollide", false));
		e.putBoolean("repeat", p.getBoolean("prefRepeat", false));
		e.putString("gridX", p.getString("prefGridX", null));
		e.putString("gridY", p.getString("prefGridY", null));
		e.putBoolean("trail", p.getBoolean("prefCannonTrail", false));
		e.putString("colorBG", p.getString("prefColorBG", null));
		e.putString("colorGrid", p.getString("prefColorGrid", null));
		e.putString("colorTarget", p.getString("prefColorTarget", null));
		e.putString("colorProj", p.getString("prefColorProj", null));
		e.clear().commit();
	}
	
	public void finish() {
		super.finish();
		Process.killProcess(Process.myPid());
	}
}