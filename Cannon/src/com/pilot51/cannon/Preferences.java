package com.pilot51.cannon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.KeyEvent;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	SharedPreferences prefs;
	SharedPreferences.Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		Preference prefResetPrefs = (Preference) findPreference("prefResetPrefs");
		prefResetPrefs.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				Context context = getBaseContext();
				SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
				SharedPreferences.Editor preferencesEditor = preferences.edit();
				preferencesEditor.clear();
				PreferenceManager.setDefaultValues(context, R.xml.preferences, true);
				preferencesEditor.commit();
				finish();
				startActivity(getIntent());
				Toast.makeText(getApplicationContext(), "Preferences reset", Toast.LENGTH_LONG).show();
				return true;
			}
		});

		Preference prefResetValues = (Preference) findPreference("prefResetValues");
		prefResetValues.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference preference) {
				SharedPreferences values = getSharedPreferences("valuePref", 0);
				SharedPreferences.Editor editor = values.edit();
				editor.clear();
				editor.commit();
				finish();
				startActivity(getIntent());
				Toast.makeText(getApplicationContext(), "Values reset", Toast.LENGTH_LONG).show();
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		if (key.equals("prefColorBG")) {
			try {
				Color.parseColor(prefs.getString("prefColorBG", null));
			} catch (Exception e) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("prefColorBG", "black");
				editor.commit();
				finish();
				startActivity(getIntent());
				Toast.makeText(getApplicationContext(), "Invalid color. Reset to default.", Toast.LENGTH_SHORT).show();
			}
		}
		if (key.equals("prefColorGrid")) {
			try {
				Color.parseColor(prefs.getString("prefColorGrid", null));
			} catch (Exception e) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("prefColorGrid", "green");
				editor.commit();
				finish();
				startActivity(getIntent());
				Toast.makeText(getApplicationContext(), "Invalid color. Reset to default.", Toast.LENGTH_SHORT).show();
			}
		}
		if (key.equals("prefColorTarget")) {
			try {
				Color.parseColor(prefs.getString("prefColorTarget", null));
			} catch (Exception e) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("prefColorTarget", "red");
				editor.commit();
				finish();
				startActivity(getIntent());
				Toast.makeText(getApplicationContext(), "Invalid color. Reset to default.", Toast.LENGTH_SHORT).show();
			}
		}
		if (key.equals("prefColorProj")) {
			try {
				Color.parseColor(prefs.getString("prefColorProj", null));
			} catch (Exception e) {
				SharedPreferences.Editor editor = prefs.edit();
				editor.putString("prefColorProj", "yellow");
				editor.commit();
				finish();
				startActivity(getIntent());
				Toast.makeText(getApplicationContext(), "Invalid color. Reset to default.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
			Intent menu = new Intent(this, CustomGame.class);
			startActivity(menu);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
