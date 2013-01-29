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

import java.util.ArrayList;
import java.util.List;

import org.anddev.andengine.input.touch.TouchEvent;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Classic extends Activity {
	private ClassicView cannonView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set full screen view
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		cannonView = new ClassicView(this);
		setContentView(cannonView);
		cannonView.requestFocus();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.getRepeatCount() == 0) {
			cannonView.refire();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (event.getAction() == TouchEvent.ACTION_DOWN) {
			cannonView.refire();
			return true;
		}
		return super.onTouchEvent(event);
	}

	private class ClassicView extends View {
		private int
			screenX,
			screenY,
			targetD,
			targetH,
			targetS,
			projS,
			gridX,
			gridY,
			colorBg,
			colorGrid,
			colorTarget,
			colorProj;
		private List<Point>
			targetPoints = new ArrayList<Point>(),
			cannonPoints = new ArrayList<Point>();
		//List<Point> particlepoints = new ArrayList<Point>(); // Experimental explosion -- Disabled
		private Paint
			paintTarget = new Paint(),
			paintCannon = new Paint(),
			paintGrid = new Paint();
		private float
			angle,
			velocity, // ft/sec
			wind,
			gravity, // multiple of earth gravity
			time = 0,
			time2 = 0,
			//time3 = 0, // Experimental explosion -- Disabled
			timeLimit; // time in seconds until projectile stops
		private long mLastTime = 0, now = 0;
		private double elapsed;
		private boolean returncheck = false;

		// int part1, part2, part3, part4, part5, part6; // Experimental explosion -- Disabled

		private Point cannon;

		private ClassicView(Context context) {
			super(context);
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			screenX = dm.widthPixels;
			screenY = dm.heightPixels;
			angle = CustomGame.getCustomPrefs().getFloat("angle", 0);
			velocity = CustomGame.getCustomPrefs().getFloat("velocity", 0);
			timeLimit = CustomGame.getCustomPrefs().getFloat("fuze", 0);
			gravity = CustomGame.getCustomPrefs().getFloat("gravity", 0);
			wind = CustomGame.getCustomPrefs().getFloat("wind", 0);
			targetD = CustomGame.getCustomPrefs().getInt("targetD", 0);
			targetH = CustomGame.getCustomPrefs().getInt("targetH", 0);
			targetS = CustomGame.getCustomPrefs().getInt("targetS", 0);
			projS = CustomGame.getCustomPrefs().getInt("projS", 0);
			colorBg = Color.parseColor(Common.getPrefs().getString("colorBG", null));
			colorGrid = Color.parseColor(Common.getPrefs().getString("colorGrid", null));
			colorTarget = Color.parseColor(Common.getPrefs().getString("colorTarget", null));
			colorProj = Color.parseColor(Common.getPrefs().getString("colorProj", null));
			setBackgroundColor(colorBg);
			paintTarget.setAntiAlias(true);
			paintCannon.setAntiAlias(true);
			paintGrid.setColor(colorGrid);
			paintTarget.setColor(colorTarget);
			paintCannon.setColor(colorProj);
			setFocusable(true);
		}

		@Override
		public void onDraw(Canvas canvas) {
			drawGrid(canvas);
			if (targetD > 0 | targetH > 0) drawTarget(canvas);
			mLastTime = now;
			now = System.currentTimeMillis();
			if (mLastTime != 0) elapsed = (double) (now - mLastTime) / 1000;
			if (time < timeLimit || timeLimit == 0) {
				drawCannon(canvas);
				time = time + (float) elapsed;
			} else if (Common.getPrefs().getBoolean("repeat", false)) {
				refire();
			} else {
				setFocusable(false);
				for (Point point : cannonPoints) {
					canvas.drawCircle(point.x, point.y, projS, paintCannon);
				}
			}
			if (targetD > 0 | targetH > 0) {
				// Detect collision
				if ((Common.getPrefs().getBoolean("collide", false) && targetS >= Math.sqrt(Math.pow((cannon.x - targetD), 2) + Math.pow((screenY - cannon.y - targetH), 2)))) {
					if (Common.getPrefs().getBoolean("repeat", false)) refire();
					else setFocusable(false);
				}
			}
			// Reset returncheck when projectile returns to screen 
			if (returncheck & cannon.x > 0 & cannon.x < screenX & cannon.y > 0 & cannon.y < screenY) {
				// Projectile has returned
				returncheck = false;
			}
			// If projectile goes off screen & has not already been checked for return
			if (!returncheck & (cannon.x < 0 | cannon.x > screenX | cannon.y < 0 | cannon.y > screenY)) {
				// Start return check
				float timecheck = time, cannonxcheck = 0, cannonycheck = 0;
				// If there is a possibility of return based on basic knowledge, scan future for return
				if ((gravity < 0 & cannon.y > screenY) | (wind > 0 & cannon.x < 0) | (gravity > 0 & cannon.y < 0) | (wind < 0 & cannon.x > screenX)) {
					// Scanning future for return
					do {
						cannonxcheck = (float) (velocity * Math.cos(Math.toRadians(angle)) * timecheck + 0.5 * wind * (float) Math.pow(timecheck, 2));
						cannonycheck = (float) -(velocity * Math.sin(Math.toRadians(angle)) * timecheck - 0.5 * SensorManager.GRAVITY_EARTH * gravity * Math.pow(timecheck, 2) - screenY);
						timecheck = timecheck + (float) 0.1;
					}
					// Stop scan if return found
					while ((cannonxcheck < 0 | cannonxcheck > screenX | cannonycheck < 0 | cannonycheck > screenY)
					// Stop scan if return becomes impossible
							& !((gravity >= 0 & cannonycheck > screenY) | (wind <= 0 & cannonxcheck < 0) | (gravity <= 0 & cannonycheck < 0) | (wind >= 0 & cannonxcheck > screenX)));
				}
				// If return found
				if (cannonxcheck > 0 & cannonxcheck < screenX & cannonycheck > 0 & cannonycheck < screenY) {
					// Projectile will return
					returncheck = true;
				} else { // If return is impossible
					// Projectile will not return
					if (Common.getPrefs().getBoolean("repeat", false)) refire();
					else setFocusable(false);
					returncheck = false;
				}
			}
			if (isFocusable()) invalidate();
		}

		private void drawGrid(Canvas canvas) {
			gridX = Integer.parseInt(Common.getPrefs().getString("gridX", null));
			if (gridX > 0) {
				// Draw vertical lines within screen space
				int grid = 0;
				do {
					grid += gridX;
					canvas.drawLine(grid, 0, grid, screenY, paintGrid);
				} while (grid < screenX - gridX && gridX != 0);
			}
			gridY = Integer.parseInt(Common.getPrefs().getString("gridY", null));
			if (gridY > 0) {
				// Draw horizontal lines within screen space
				int grid = screenY;
				do {
					grid -= gridY;
					canvas.drawLine(0, grid, screenX, grid, paintGrid);
				} while (grid > gridY && gridY != 0);
			}
		}

		private void drawTarget(Canvas canvas) {
			if (time2 < 1) {
				do {
					Point target = new Point();
					target.x = (float) (targetS * Math.sin(Math.toRadians(360) * time2) + targetD);
					target.y = (float) -(targetS * Math.cos(Math.toRadians(360) * time2) + targetH - screenY);
					targetPoints.add(target);
					time2 = time2 + (float) 0.01;
				} while (time2 < 1);
			}
			for (Point point : targetPoints) {
				canvas.drawCircle(point.x, point.y, 1, paintTarget);
			}
		}

		private void drawCannon(Canvas canvas) {
			cannon = new Point();
			cannon.x = (float) (velocity * Math.cos(Math.toRadians(angle)) * time + 0.5 * wind * (float) Math.pow(time, 2));
			cannon.y = (float) -(velocity * Math.sin(Math.toRadians(angle)) * time - 0.5 * 9.81 * gravity * Math.pow(time, 2) - screenY);
			if (Common.getPrefs().getBoolean("trail", false)) {
				cannonPoints.add(cannon);
				for (Point point : cannonPoints) {
					canvas.drawCircle(point.x, point.y, projS, paintCannon);
				}
			} else canvas.drawCircle(cannon.x, cannon.y, projS, paintCannon);
		}

		private void refire() {
			cannonPoints.clear();
			time = 0;
			now = 0;
			setFocusable(true);
			invalidate();
		}
	}

	private class Point {
		private float x, y;
		
		@Override
		public String toString() {
			return x + ", " + y;
		}
	}
}