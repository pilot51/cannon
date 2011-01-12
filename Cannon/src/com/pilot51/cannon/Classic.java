package com.pilot51.cannon;

import java.util.ArrayList;
import java.util.List;

import org.anddev.andengine.input.touch.TouchEvent;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
//import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

public class Classic extends Activity {
	ClassicView cannonView;

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

	public class ClassicView extends View {
		int
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
		List<Point>
			targetPoints = new ArrayList<Point>(),
			cannonPoints = new ArrayList<Point>();
		//List<Point> particlepoints = new ArrayList<Point>(); // Experimental explosion -- Disabled
		Paint
			paintTarget = new Paint(),
			paintCannon = new Paint(),
			paintGrid = new Paint();
		float
			cannonx,
			cannony,
			angle,
			velocity, // ft/sec
			wind,
			gravity, // multiple of earth gravity
			time = 0,
			time2 = 0,
			//time3 = 0, // Experimental explosion -- Disabled
			//viewdist,
			timeLimit; // time in seconds until projectile stops
		long mLastTime = 0, now = 0;
		double elapsed;
		boolean returncheck = false;

		// int part1, part2, part3, part4, part5, part6; // Experimental explosion -- Disabled

		SharedPreferences prefs, prefCustom;

		Point cannon;

		public ClassicView(Context context) {
			super(context);
			DisplayMetrics dm = new DisplayMetrics();
			getWindowManager().getDefaultDisplay().getMetrics(dm);
			this.screenX = dm.widthPixels;
			this.screenY = dm.heightPixels;
			prefCustom = getSharedPreferences("custom", MODE_PRIVATE);
			this.angle = prefCustom.getFloat("angle", 0);
			this.velocity = prefCustom.getFloat("velocity", 0);
			this.timeLimit = prefCustom.getFloat("fuze", 0);
			this.gravity = prefCustom.getFloat("gravity", 0);
			this.wind = prefCustom.getFloat("wind", 0);
			this.targetD = prefCustom.getInt("targetD", 0);
			this.targetH = prefCustom.getInt("targetH", 0);
			this.targetS = prefCustom.getInt("targetS", 0);
			this.projS = prefCustom.getInt("projS", 0);
			prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
			colorBg = Color.parseColor(prefs.getString("colorBG", null));
			colorGrid = Color.parseColor(prefs.getString("colorGrid", null));
			colorTarget = Color.parseColor(prefs.getString("colorTarget", null));
			colorProj = Color.parseColor(prefs.getString("colorProj", null));
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
			}
			else if (prefs.getBoolean("repeat", false)) refire();
			else {
				setFocusable(false);
				for (Point point : cannonPoints) {
					canvas.drawCircle(point.x, point.y, projS, paintCannon);
				}
			}
			if (targetD > 0 | targetH > 0) {
				// Detect collision
				if ((prefs.getBoolean("collide", false) && targetS >= Math.sqrt(Math.pow((cannon.x - targetD), 2) + Math.pow((screenY - cannon.y - targetH), 2)))) {
					if (prefs.getBoolean("repeat", false)) refire();
					else setFocusable(false);
				}
			}
			// Reset returncheck when projectile returns to screen 
			if (returncheck & cannon.x > 0 & cannon.x < screenX & cannon.y > 0 & cannon.y < screenY) {
				// Log.d(TAG, "Projectile has returned");
				returncheck = false;
			}
			// If projectile goes off screen & has not already been checked for return
			if (!returncheck & (cannon.x < 0 | cannon.x > screenX | cannon.y < 0 | cannon.y > screenY)) {
				// Log.d(TAG, "Start return check");
				float timecheck = time, cannonxcheck = 0, cannonycheck = 0;
				// If there is a possibility of return based on basic knowledge, scan future for return
				if ((gravity < 0 & cannon.y > screenY) | (wind > 0 & cannon.x < 0) | (gravity > 0 & cannon.y < 0) | (wind < 0 & cannon.x > screenX)) {
					// Log.d(TAG, "Scanning future for return");
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
					// Log.d(TAG, "Projectile will return");
					returncheck = true;
				}
				// If return is impossible
				else {
					// Log.d(TAG, "Projectile will not return");
					if (prefs.getBoolean("repeat", false)) refire();
					else setFocusable(false);
					returncheck = false;
				}
			}

			if (isFocusable()) invalidate();
			if (onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null)) {}
		}

		void drawGrid(Canvas canvas) {
			gridX = Integer.parseInt(prefs.getString("gridX", null));
			if (gridX > 0) {
				// Draw vertical lines within screen space
				int grid = 0;
				do {
					grid += gridX;
					canvas.drawLine(grid, 0, grid, screenY, paintGrid);
				} while (grid < screenX - gridX && gridX != 0);
			}
			gridY = Integer.parseInt(prefs.getString("gridY", null));
			if (gridY > 0) {
				// Draw horizontal lines within screen space
				int grid = screenY;
				do {
					grid -= gridY;
					canvas.drawLine(0, grid, screenX, grid, paintGrid);
				} while (grid > gridY && gridY != 0);
			}
		}

		void drawTarget(Canvas canvas) {
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

		void drawCannon(Canvas canvas) {
			cannon = new Point();
			cannon.x = (float) (velocity * Math.cos(Math.toRadians(angle)) * time + 0.5 * wind * (float) Math.pow(time, 2));
			cannon.y = (float) -(velocity * Math.sin(Math.toRadians(angle)) * time - 0.5 * 9.81 * gravity * Math.pow(time, 2) - screenY);
			if (prefs.getBoolean("trail", false)) {
				cannonPoints.add(cannon);
				for (Point point : cannonPoints) {
					canvas.drawCircle(point.x, point.y, projS, paintCannon);
				}
			} else canvas.drawCircle(cannon.x, cannon.y, projS, paintCannon);
		}

		void refire() {
			cannonPoints.clear();
			time = 0;
			now = 0;
			setFocusable(true);
			invalidate();
		}

		/*
			void originalCannon() {
				x = targets * Math.sin(360÷timelimit÷2)time+targetd;
				y = targets * Math.cos(360÷timelimit÷2)time+targeth;
				(Ssin (360÷(L÷2))T+D,Scos (360÷(L÷2))T+H
				cannonx = velocity * Math.cos(angle) * T - 0.5 * wind * T²;
				cannony = velocity * Math.sin(angle) * T - 0.5 * 9.8 * gravity * T²;
				((Vcos A)T-(0.5)(W)T²,(Vsin A)T-(0.5)(9.8)(G)T²)
				velocity = velocity * 2.0835; // Converts to ft/sec on calculator
			}
		*/
	}

	class Point {
		float x, y;
		@Override
		public String toString() {
			return x + ", " + y;
		}
	}
}