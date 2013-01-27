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

	private SharedPreferences prefs;

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
				Toast.makeText(Preferences.this, R.string.toast_prefs_reset, Toast.LENGTH_LONG).show();
				return true;
			}
		});

		((Preference)findPreference("resetCustom")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference p) {
				getSharedPreferences("custom", MODE_PRIVATE).edit().clear().commit();
				setResult(1);
				Toast.makeText(Preferences.this, R.string.toast_custom_reset, Toast.LENGTH_LONG).show();
				return true;
			}
		});
		
		((Preference)findPreference("clearScores")).setOnPreferenceClickListener(new OnPreferenceClickListener() {
			public boolean onPreferenceClick(Preference p) {
				getSharedPreferences("scores", MODE_PRIVATE).edit().clear().commit();
				Toast.makeText(Preferences.this, R.string.toast_scores_cleared, Toast.LENGTH_LONG).show();
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
				Toast.makeText(this, getString(R.string.toast_gridx_disabled), Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("gridY")) {
			try {
				Integer.parseInt(sp.getString("gridY", null));
			} catch (Exception e) {
				sp.edit().putString("gridY", "0").commit();
				((EditTextPreference)findPreference("gridY")).setText("0");
				Toast.makeText(this, getString(R.string.toast_gridy_disabled), Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorBG")) {
			try {
				Color.parseColor(sp.getString("colorBG", null));
			} catch (Exception e) {
				sp.edit().putString("colorBG", "black").commit();
				((EditTextPreference)findPreference("colorBG")).setText("black");
				Toast.makeText(this, getString(R.string.toast_invalid_color_bg), Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorGrid")) {
			try {
				Color.parseColor(sp.getString("colorGrid", null));
			} catch (Exception e) {
				sp.edit().putString("colorGrid", "green").commit();
				((EditTextPreference)findPreference("colorGrid")).setText("green");
				Toast.makeText(this, getString(R.string.toast_invalid_color_grid), Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorTarget")) {
			try {
				Color.parseColor(sp.getString("colorTarget", null));
			} catch (Exception e) {
				sp.edit().putString("colorTarget", "red").commit();
				((EditTextPreference)findPreference("colorTarget")).setText("red");
				Toast.makeText(this, getString(R.string.toast_invalid_color_target), Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorHitTarget")) {
			try {
				Color.parseColor(sp.getString("colorHitTarget", null));
			} catch (Exception e) {
				sp.edit().putString("colorHitTarget", "blue").commit();
				((EditTextPreference)findPreference("colorHitTarget")).setText("blue");
				Toast.makeText(this, getString(R.string.toast_invalid_color_target_complete), Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("colorProj")) {
			try {
				Color.parseColor(sp.getString("colorProj", null));
			} catch (Exception e) {
				sp.edit().putString("colorProj", "yellow").commit();
				((EditTextPreference)findPreference("colorProj")).setText("yellow");
				Toast.makeText(this, getString(R.string.toast_invalid_color_projectile), Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("senseMove")) {
			try {
				if(Integer.parseInt(sp.getString("senseMove", null)) == 0) {
					sp.edit().putString("senseMove", "5").commit();
					((EditTextPreference)findPreference("senseMove")).setText("5");
					Toast.makeText(this, getString(R.string.toast_drag_min), Toast.LENGTH_SHORT).show();
				}
			} catch (NumberFormatException e) {
				sp.edit().putString("senseMove", "5").commit();
				((EditTextPreference)findPreference("senseMove")).setText("5");
				Toast.makeText(this, getString(R.string.toast_drag_blank), Toast.LENGTH_SHORT).show();
			}
		} else if (key.equals("sensePressure")) {
			try {
				Integer.parseInt(sp.getString("sensePressure", null));
			} catch (NumberFormatException e) {
				sp.edit().putString("sensePressure", "10").commit();
				((EditTextPreference)findPreference("sensePressure")).setText("10");
				Toast.makeText(this, getString(R.string.toast_pressure_blank), Toast.LENGTH_SHORT).show();
			}
		}
	}
}