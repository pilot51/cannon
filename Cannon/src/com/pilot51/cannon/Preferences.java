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

		((Preference)findPreference("resetPrefs")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference p) {
				prefs.edit().clear().commit();
				PreferenceManager.setDefaultValues(Preferences.this, R.xml.preferences, true);
				finish();
				startActivity(getIntent());
				Toast.makeText(Preferences.this, "Preferences reset", Toast.LENGTH_LONG).show();
				return true;
			}
		});

		((Preference)findPreference("resetCustom")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference p) {
				getSharedPreferences("custom", MODE_PRIVATE).edit().clear().commit();
				setResult(1);
				Toast.makeText(Preferences.this, "Custom game values reset", Toast.LENGTH_LONG).show();
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
		if (key.equals("gridX")) {
			try {
				Integer.parseInt(sp.getString("gridX", null));
			} catch (Exception e) {
				sp.edit().putString("gridX", "0").commit();
				((EditTextPreference)findPreference("gridX")).setText("0");
				Toast.makeText(this, "Grid X scale set to 0 (disabled)", Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("gridY")) {
			try {
				Integer.parseInt(sp.getString("gridY", null));
			} catch (Exception e) {
				sp.edit().putString("gridY", "0").commit();
				((EditTextPreference)findPreference("gridY")).setText("0");
				Toast.makeText(this, "Grid Y scale set to 0 (disabled)", Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorBG")) {
			try {
				Color.parseColor(sp.getString("colorBG", null));
			} catch (Exception e) {
				sp.edit().putString("colorBG", "black").commit();
				((EditTextPreference)findPreference("colorBG")).setText("black");
				Toast.makeText(this, "Invalid color. Background reset to black.", Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorGrid")) {
			try {
				Color.parseColor(sp.getString("colorGrid", null));
			} catch (Exception e) {
				sp.edit().putString("colorGrid", "green").commit();
				((EditTextPreference)findPreference("colorGrid")).setText("green");
				Toast.makeText(this, "Invalid color. Grid reset to green.", Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorTarget")) {
			try {
				Color.parseColor(sp.getString("colorTarget", null));
			} catch (Exception e) {
				sp.edit().putString("colorTarget", "red").commit();
				((EditTextPreference)findPreference("colorTarget")).setText("red");
				Toast.makeText(this, "Invalid color. Target reset to red.", Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorHitTarget")) {
			try {
				Color.parseColor(sp.getString("colorHitTarget", null));
			} catch (Exception e) {
				sp.edit().putString("colorHitTarget", "blue").commit();
				((EditTextPreference)findPreference("colorHitTarget")).setText("blue");
				Toast.makeText(this, "Invalid color. Completed Target reset to blue.", Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorProj")) {
			try {
				Color.parseColor(sp.getString("colorProj", null));
			} catch (Exception e) {
				sp.edit().putString("colorProj", "yellow").commit();
				((EditTextPreference)findPreference("colorProj")).setText("yellow");
				Toast.makeText(this, "Invalid color. Projectile reset to yellow.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}