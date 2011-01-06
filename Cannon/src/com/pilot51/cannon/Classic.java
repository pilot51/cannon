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
	//	String TAG = "Cannon";

	ClassicView cannonView;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set full screen view
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		SharedPreferences values = getSharedPreferences("valuePref", 0);

		cannonView = new ClassicView(this, dm.widthPixels, dm.heightPixels, values.getFloat("prefAngle", 0), values.getFloat("prefVelocity", 0), values.getFloat("prefFuze", 0), values.getFloat(
				"prefGravity",
				0), values.getFloat("prefWind", 0), values.getInt("prefTargetD", 0), values.getInt("prefTargetH", 0), values.getInt("prefTargetS", 0));

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
		//		String TAG = "Cannon";

		int screenx, screeny, targetd, targeth, targets, gridx, gridy, colorbg, colorgrid, colortarget, colorproj;

		List<Point> targetpoints = new ArrayList<Point>();
		List<Point> cannonpoints = new ArrayList<Point>();
		//		List<Point> particlepoints = new ArrayList<Point>(); // Experimental explosion -- Disabled
		Paint paintTarget = new Paint(), paintCannon = new Paint(), paintGrid = new Paint();
		float cannonx, cannony, angle, velocity, // ft/sec
				wind, gravity, // multiple of earth gravity
				time = 0, time2 = 0,
				//			time3 = 0, // Experimental explosion -- Disabled
				//			viewdist,
				timelimit // time in seconds until projectile stops
				;

		long mLastTime = 0, now = 0;

		double elapsed;

		boolean returncheck = false;

		//		int part1, part2, part3, part4, part5, part6; // Experimental explosion -- Disabled

		SharedPreferences prefs;

		Point cannon;

		public ClassicView(Context context, int screenw, int screenh, float angle, float velocity, float fuze, float gravity, float wind, int targetd, int targeth, int targets) {
			super(context);

			this.screenx = screenw;
			this.screeny = screenh;
			this.angle = angle;
			this.velocity = velocity;
			this.timelimit = fuze;
			this.gravity = gravity;
			this.wind = wind;
			this.targetd = targetd;
			this.targeth = targeth;
			this.targets = targets;

			prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

			colorbg = Color.parseColor(prefs.getString("prefColorBG", null));
			colorgrid = Color.parseColor(prefs.getString("prefColorGrid", null));
			colortarget = Color.parseColor(prefs.getString("prefColorTarget", null));
			colorproj = Color.parseColor(prefs.getString("prefColorProj", null));

			setBackgroundColor(colorbg);
			paintTarget.setAntiAlias(true);
			paintCannon.setAntiAlias(true);
			paintGrid.setColor(colorgrid);
			paintTarget.setColor(colortarget);
			paintCannon.setColor(colorproj);
			setFocusable(true);

			/* // Experimental explosion -- Disabled
			part1 = rand_particle();
			part2 = rand_particle();
			part3 = rand_particle();
			part4 = rand_particle();
			part5 = rand_particle();
			part6 = rand_particle();
			*/
		}

		@Override
		public void onDraw(Canvas canvas) {
			drawgrid(canvas);
			if (targetd > 0 | targeth > 0) {
				drawtarget(canvas);
			}
			mLastTime = now;
			now = System.currentTimeMillis();
			if (mLastTime != 0) {
				elapsed = (double) (now - mLastTime) / 1000;
			}
			if (time < timelimit || timelimit == 0) {
				drawcannon(canvas);
				time = time + (float) elapsed;
			} else if (prefs.getBoolean("prefRepeat", false)) {
				refire();
			} else {
				setFocusable(false);
				for (Point point : cannonpoints) {
					canvas.drawCircle(point.x, point.y, 3, paintCannon);
				}
				/* // Experimental explosion -- Disabled
				if (time3 < 4) {
					draw_explosion(canvas);
				}
				if (time3 > 4) {
					particlepoints.remove(0);
					if (particlepoints.size() == 0) {
						setFocusable(false);
					}
				}
				time3 = time3 + (float)elapsed;
				*/
			}
			if (targetd > 0 | targeth > 0) {
				// Detect collision
				if ((prefs.getBoolean("prefCollide", false) && targets >= Math.sqrt(Math.pow((cannon.x - targetd), 2) + Math.pow((screeny - cannon.y - targeth), 2)))) {
					if (prefs.getBoolean("prefRepeat", false)) {
						refire();
					} else {
						setFocusable(false);
					}
				}
			}
			// Reset returncheck when projectile returns to screen 
			if (returncheck & cannon.x > 0 & cannon.x < screenx & cannon.y > 0 & cannon.y < screeny) {
				//				Log.d(TAG, "Projectile has returned");
				returncheck = false;
			}
			// If projectile goes off screen & has not already been checked for return
			if (!returncheck & (cannon.x < 0 | cannon.x > screenx | cannon.y < 0 | cannon.y > screeny)) {
				//				Log.d(TAG, "Start return check");
				float timecheck = time, cannonxcheck = 0, cannonycheck = 0;
				// If there is a possibility of return based on basic knowledge, scan future for return
				if ((gravity < 0 & cannon.y > screeny) | (wind > 0 & cannon.x < 0) | (gravity > 0 & cannon.y < 0) | (wind < 0 & cannon.x > screenx)) {
					//					Log.d(TAG, "Scanning future for return");
					do {
						cannonxcheck = (float) (velocity * Math.cos(Math.toRadians(angle)) * timecheck + 0.5 * wind * (float) Math.pow(timecheck, 2));
						cannonycheck = (float) -(velocity * Math.sin(Math.toRadians(angle)) * timecheck - 0.5 * SensorManager.GRAVITY_EARTH * gravity * Math.pow(timecheck, 2) - screeny);
						timecheck = timecheck + (float) 0.1;
					}
					// Stop scan if return found
					while ((cannonxcheck < 0 | cannonxcheck > screenx | cannonycheck < 0 | cannonycheck > screeny)
					// Stop scan if return becomes impossible
							& !((gravity >= 0 & cannonycheck > screeny) | (wind <= 0 & cannonxcheck < 0) | (gravity <= 0 & cannonycheck < 0) | (wind >= 0 & cannonxcheck > screenx)));
				}
				// If return found
				if (cannonxcheck > 0 & cannonxcheck < screenx & cannonycheck > 0 & cannonycheck < screeny) {
					//					Log.d(TAG, "Projectile will return");
					returncheck = true;
				}
				// If return is impossible
				else {
					//					Log.d(TAG, "Projectile will not return");
					if (prefs.getBoolean("prefRepeat", false)) {
						refire();
					} else {
						setFocusable(false);
					}
					returncheck = false;
				}
			}

			if (isFocusable()) {
				invalidate();
			}
			if (onKeyDown(KeyEvent.KEYCODE_DPAD_CENTER, null)) {

			}
		}

		void drawgrid(Canvas canvas) {
			gridx = Integer.parseInt(prefs.getString("prefGridX", null));
			if (gridx > 0) {
				// Draw vertical lines within screen space
				int grid = 0;
				do {
					grid += gridx;
					canvas.drawLine(grid, 0, grid, screeny, paintGrid);
				} while (grid < screenx - gridx && gridx != 0);
			}
			gridy = Integer.parseInt(prefs.getString("prefGridY", null));
			if (gridy > 0) {
				// Draw horizontal lines within screen space
				int grid = screeny;
				do {
					grid -= gridy;
					canvas.drawLine(0, grid, screenx, grid, paintGrid);
				} while (grid > gridy && gridy != 0);
			}
		}

		void drawtarget(Canvas canvas) {
			if (time2 < 1) {
				do {
					Point target = new Point();
					target.x = (float) (targets * Math.sin(Math.toRadians(360) * time2) + targetd);
					target.y = (float) -(targets * Math.cos(Math.toRadians(360) * time2) + targeth - screeny);
					targetpoints.add(target);
					time2 = time2 + (float) 0.01;
				} while (time2 < 1);
			}
			for (Point point : targetpoints) {
				canvas.drawCircle(point.x, point.y, 1, paintTarget);
			}
		}

		void drawcannon(Canvas canvas) {
			cannon = new Point();
			cannon.x = (float) (velocity * Math.cos(Math.toRadians(angle)) * time + 0.5 * wind * (float) Math.pow(time, 2));
			cannon.y = (float) -(velocity * Math.sin(Math.toRadians(angle)) * time - 0.5 * 9.81 * gravity * Math.pow(time, 2) - screeny);
			if (prefs.getBoolean("prefTrail", false)) {
				cannonpoints.add(cannon);
				for (Point point : cannonpoints) {
					canvas.drawCircle(point.x, point.y, 3, paintCannon);
				}
			} else {
				canvas.drawCircle(cannon.x, cannon.y, 3, paintCannon);
			}
		}

		void refire() {
			cannonpoints.clear();
			time = 0;
			now = 0;
			setFocusable(true);
			invalidate();
		}

		/* // Experimental explosion -- Disabled
			void draw_explosion(Canvas canvas) {
				particle(canvas, part1);
				particle(canvas, part2);
				particle(canvas, part3);
				particle(canvas, part4);
				particle(canvas, part5);
				particle(canvas, part6);
			}
			
			void particle(Canvas canvas, int dir) {
				Point explode = new Point();
				explode.x = cannon.x + (float)((velocity / 4) * Math.cos(Math.toRadians(dir)) * time3 + 0.5 * wind * (float)Math.pow(time3,2));
				explode.y = cannon.y + (float)-((velocity / 4) * Math.sin(Math.toRadians(dir)) * time3 - 0.5 * 9.81 * gravity * Math.pow(time3,2));
		//			canvas.drawCircle(explode.x, explode.y, 1, paintCannon);
				if (particlepoints.size() == 50) {
					particlepoints.remove(0);
				}
				particlepoints.add(explode);
				for (Point point : particlepoints) {
					canvas.drawCircle(point.x, point.y, 1, paintCannon);
		        }
			}
			
			int rand_particle() {
				Random rand_gen = new Random();
				return rand_gen.nextInt(360)+1;
			}
		*/

		/*
			void cannon_original() {
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