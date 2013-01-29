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
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class GameType extends Activity implements OnClickListener {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Common.init(getApplicationContext());
		setContentView(R.layout.gametype);
		((Button)findViewById(R.id.btnCustom)).setOnClickListener(this);
		((Button)findViewById(R.id.btnRandom)).setOnClickListener(this);
		if(getLastNonConfigurationInstance() != (Object)true) {
			Toast.makeText(this, R.string.toast_menu, Toast.LENGTH_LONG).show();
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
			startActivity(new Intent(this, GameField.class).putExtra(GameField.EXTRA_RANDOM, true));
			break;
		}
	}
	
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_about:
			Common.menu(this);
			return true;
		case R.id.menu_prefs:
			startActivity(new Intent(this, Preferences.class));
			return true;
		}
		return false;
	}
}
