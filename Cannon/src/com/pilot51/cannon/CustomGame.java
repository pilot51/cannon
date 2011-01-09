/*
 * Immediately before release:
 * TODO Test on all API's and screen sizes, particularly 1.6 QVGA and 1.5 HVGA
 * TODO In all projects, set release date in strings and version in strings and manifest
 * TODO Clean before compiling
 * TODO Install old version from Market to make sure update from Market works properly
 * TODO Save sources along with binary for each release version
 * 
 * Problems:
 * FIXME Bottom of scrollview in custom screen doesn't lose fade when at bottom
 * FIXME Targets spawning completely inside other targets in random mode, impossible to hit with collision enabled
 * FIXME Fit borders and keep random targets within scaled screen, also scale targets relative to screen scale  
 * FIXME Handle orientation/keyboard changes
 * 
 * v2.0.0:
 * TODO [4 hours] Update icon
 * TODO [30 min] Update About dialog
 * TODO Usability - Tell players what the controls are [suggested by HotShots]
 * TODO Score combos when hitting targets with consecutive projectiles
 * TODO Display and saving of scores
 * 
 * Pro (first release):
 * TODO Look into the new copy protection
 * 
 * v2.0.1:
 * TODO Zoom & pan
 * TODO Option: Analytics
 * TODO Add some suggestions
 * TODO Prevent random targets from spawning too close to launcher
 * TODO Adjust minimum size of targets 
 * 
 * v2.1.0
 * TODO Sounds
 * TODO Textures, maybe themes
 * TODO Explosive projectile with shrapnel
 * TODO Moving targets [extstw]
 * TODO Missions: preset, random, or infinitely calculated targets that need to be hit to reach higher levels
 * TODO Basic multiplayer
 * 
 * v2.2.0:
 * TODO Multiplayer: Hotseat & direct IP
 * 			Simultaneous turn-based, allowing projectiles to collide
 * 			One player places target, other player tries hitting it (requires in-game angle/velocity controls)
 * 			Players try to hit randomly placed target [suggested by SuM_WuN]
 * 				After so many seconds, closest shot wins
 * 				First direct hit wins
 * TODO Gravity presets (earth, moon, mars, etc.)
 * TODO Different types of targets/projectiles with different collision physics
 * 			Projectiles: Bouncy, sticky, explosive
 * 
 * Suggested features (high priority):
 * TODO Preview before firing [Tyler]
 * TODO Pause, timestep forward/back, speed [+1 by extstw]
 * TODO Explosions [+1 by extstw]
 * TODO For missions: Multiple projectiles/targets, hit multiple targets with a single projectile (maybe in an order) [+1 by extstw]
 * TODO Fading tail [extstw]
 * TODO Moving targets [extstw]
 * TODO Obstacles [extstw]
 * TODO Cluster bomb [extstw]
 * TODO Return to main screen after shot [Bill Irwin] Self note: Make optional, maybe enabled by default
 * 
 * Bin:
 * TODO More realistic wind (limited speed, not squared)
 * TODO Air resistance/density (ballistic coefficient on Wikipedia)
 * TODO Solve feature (automatically enter most conservative values in null fields necessary to hit the target)
 * TODO Dynamic gravity (targets that push or pull)
 * TODO Option: Show overlay data for projectile X, Y, speed, angle of motion, and time
 */

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
			editTargetD, editTargetH, editTargetS;

	float angle, velocity, fuze, gravity, wind;

	int targetd, targeth, targets;

	SharedPreferences values;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.customgame);
		new Common().ad(this);
		editAngle = (EditText) findViewById(R.id.editAngle);
		editVelocity = (EditText) findViewById(R.id.editVelocity);
		editFuze = (EditText) findViewById(R.id.editFuze);
		editGravity = (EditText) findViewById(R.id.editGravity);
		editWind = (EditText) findViewById(R.id.editWind);
		editTargetD = (EditText) findViewById(R.id.editTargetD);
		editTargetH = (EditText) findViewById(R.id.editTargetH);
		editTargetS = (EditText) findViewById(R.id.editTargetS);

		btnFire = (Button) findViewById(R.id.buttonFire);
		btnFire.setOnClickListener(this);

		values = getSharedPreferences("valuePref", 0);
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
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			// Log.d(TAG, "About pressed");
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
			if(PreferenceManager.getDefaultSharedPreferences(getBaseContext()).getBoolean("prefClassic", false)) {
				startActivity(new Intent(this, Classic.class));
			} else startActivity(new Intent(this, GameField.class));
			break;
		}
	}
	
	void loadValues() {
		editAngle.setText(Float.toString(values.getFloat("prefAngle", 59)));
		editVelocity.setText(Float.toString(values.getFloat("prefVelocity",
				(float) 82.5)));
		editFuze
				.setText(Float.toString(values.getFloat("prefFuze", (float) 0)));
		editGravity.setText(Float.toString(values.getFloat("prefGravity", 1)));
		editWind.setText(Float.toString(values.getFloat("prefWind", -3)));
		editTargetD
				.setText(Integer.toString(values.getInt("prefTargetD", 250)));
		editTargetH
				.setText(Integer.toString(values.getInt("prefTargetH", 250)));
		editTargetS.setText(Integer.toString(values.getInt("prefTargetS", 10)));
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
			targetd = Integer.parseInt(editTargetD.getText().toString());
		} catch (NumberFormatException e) {
			targetd = 0;
		}
		try {
			targeth = Integer.parseInt(editTargetH.getText().toString());
		} catch (NumberFormatException e) {
			targeth = 0;
		}
		try {
			targets = Integer.parseInt(editTargetS.getText().toString());
		} catch (NumberFormatException e) {
			targets = 0;
		}
	}

	void saveValues() {
		SharedPreferences.Editor editor = values.edit();
		editor.putFloat("prefAngle", angle);
		editor.putFloat("prefVelocity", velocity);
		editor.putFloat("prefFuze", fuze);
		editor.putFloat("prefGravity", gravity);
		editor.putFloat("prefWind", wind);
		editor.putInt("prefTargetD", targetd);
		editor.putInt("prefTargetH", targeth);
		editor.putInt("prefTargetS", targets);
		editor.commit();
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == 1) loadValues();
	}
}