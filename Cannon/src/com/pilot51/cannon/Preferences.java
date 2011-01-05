package com.pilot51.cannon;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {

	SharedPreferences prefs;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		((Preference)findPreference("prefResetPrefs")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference p) {
				prefs.edit().clear().commit();
				PreferenceManager.setDefaultValues(Preferences.this, R.xml.preferences, true);
				finish();
				startActivity(getIntent());
				Toast.makeText(Preferences.this, "Preferences reset", Toast.LENGTH_LONG).show();
				return true;
			}
		});

		((Preference)findPreference("prefResetValues")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference p) {
				getSharedPreferences("valuePref", MODE_PRIVATE).edit().clear().commit();
				setResult(1);
				Toast.makeText(Preferences.this, "Values reset", Toast.LENGTH_LONG).show();
				return true;
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}

	public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
		if (key.equals("prefColorBG")) {
			try {
				Color.parseColor(sp.getString("prefColorBG", null));
			} catch (Exception e) {
				sp.edit().putString("prefColorBG", "black").commit();
				((EditTextPreference)findPreference("prefColorBG")).setText("black");
				Toast.makeText(this, "Invalid color. Background reset to black.", Toast.LENGTH_SHORT).show();
			}
		}
		if (key.equals("prefColorGrid")) {
			try {
				Color.parseColor(sp.getString("prefColorGrid", null));
			} catch (Exception e) {
				sp.edit().putString("prefColorGrid", "green").commit();
				((EditTextPreference)findPreference("prefColorGrid")).setText("green");
				Toast.makeText(this, "Invalid color. Grid reset to green.", Toast.LENGTH_SHORT).show();
			}
		}
		if (key.equals("prefColorTarget")) {
			try {
				Color.parseColor(sp.getString("prefColorTarget", null));
			} catch (Exception e) {
				sp.edit().putString("prefColorTarget", "red").commit();
				((EditTextPreference)findPreference("prefColorTarget")).setText("red");
				Toast.makeText(this, "Invalid color. Target reset to red.", Toast.LENGTH_SHORT).show();
			}
		}
		if (key.equals("prefColorProj")) {
			try {
				Color.parseColor(sp.getString("prefColorProj", null));
			} catch (Exception e) {
				sp.edit().putString("prefColorProj", "yellow").commit();
				((EditTextPreference)findPreference("prefColorProj")).setText("yellow");
				Toast.makeText(this, "Invalid color. Projectile reset to yellow.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}