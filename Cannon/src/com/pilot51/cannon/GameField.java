package com.pilot51.cannon;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.primitive.Line;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class GameField extends BaseGameActivity implements IOnSceneTouchListener {

	private Texture mTexture, mFontTexture;
	private TextureRegion mCircleTextureRegion;
	private FixedStepPhysicsWorld mPhysicsWorld;
	private SmoothCamera camera;
	private Timer timer = new Timer();
	private SharedPreferences prefs;
	private float ratio, speed, pxPerMeter, angle, velocity, gravity, wind, ballRadius, ballScale;
	private long fuze;
	private int cameraWidth, cameraHeight, targetD, targetH, targetRadius, gridx, gridy, colorbg, colorgrid, colorproj, colortarget;
	private boolean mRandom;
	private Ball target;
	private Body targetBody;
	private Font mFont;
	private ChangeableText aText, vText;
	private Random rand_gen = new Random();
	private final HUD hud = new HUD();

	@Override
	public Engine onLoadEngine() {
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		pxPerMeter = Float.parseFloat(prefs.getString("prefMeter", null));
		ratio = 32f;
		speed = Float.parseFloat(prefs.getString("prefSpeed", null));
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		cameraWidth = dm.widthPixels;
		cameraHeight = dm.heightPixels;
		//cameraWidth = (int)(dm.widthPixels / pxPerMeter);
		//cameraHeight = (int)(dm.heightPixels / pxPerMeter);
		camera = new SmoothCamera(0, -cameraHeight, cameraWidth, cameraHeight, 1000, 1000, 100f);
		//camera = new BoundCamera(0, -cameraHeight, cameraWidth, cameraHeight,0, cameraWidth / 2, 0, -cameraHeight);
		camera.setHUD(hud);
		camera.setCenter(cameraWidth / 2 / pxPerMeter, -cameraHeight / 2 / pxPerMeter);
		camera.setZoomFactor(pxPerMeter);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(cameraWidth, cameraHeight), camera));
		} else {
			return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(cameraWidth, cameraHeight), camera));
		}
	}

	@Override
	public void onLoadResources() {
		mRandom = getIntent().getBooleanExtra("random", false);

		colorbg = Color.parseColor(prefs.getString("prefColorBG", null));
		colorgrid = Color.parseColor(prefs.getString("prefColorGrid", null));
		colortarget = Color.parseColor(prefs.getString("prefColorTarget", null));
		colorproj = Color.parseColor(prefs.getString("prefColorProj", null));

		ballRadius = Float.parseFloat(prefs.getString("prefBallRadius", null));
		ballScale = ballRadius * 2f / ratio;

		mTexture = new Texture(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegionFactory.setAssetBasePath("gfx/");
		mCircleTextureRegion = TextureRegionFactory.createFromAsset(mTexture, this, "circle_white.png", 0, 32);
		mEngine.getTextureManager().loadTexture(mTexture);
		SharedPreferences values = getSharedPreferences("valuePref", 0);
		if (mRandom == false) {
			angle = values.getFloat("prefAngle", 0);
			velocity = values.getFloat("prefVelocity", 0);
			fuze = (long) (values.getFloat("prefFuze", 0) * 1000);
			gravity = values.getFloat("prefGravity", 0) / ratio;
			wind = values.getFloat("prefWind", 0) / ratio;
			targetRadius = values.getInt("prefTargetS", 0);
			targetD = values.getInt("prefTargetD", 0);
			targetH = values.getInt("prefTargetH", 0);
		} else {
			angle = 45;
			velocity = 50;
			fuze = 0;
			gravity = 1f / ratio;
			wind = 0;
			//wind = (float)(rand_gen.nextInt(7) - 3) / RATIO;
			targetRadius = rand_gen.nextInt(50) + 3;
			targetD = rand_gen.nextInt(cameraWidth - targetRadius * 2) + targetRadius;
			targetH = rand_gen.nextInt(cameraHeight - targetRadius * 2) + targetRadius;
		}
		mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		//mFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 20 / pxPerMeter, true, Color.WHITE);
		mFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 20, true, Color.WHITE);
		mEngine.getTextureManager().loadTexture(mFontTexture);
		mEngine.getFontManager().loadFont(mFont);
	}

	@Override
	public Scene onLoadScene() {
		final Scene scene = new Scene(3);
		scene.setBackground(new ColorBackground(Color.red(colorbg) / 255f, Color.green(colorbg) / 255f, Color.blue(colorbg) / 255f, Color.alpha(colorbg) / 255f));
		scene.setOnSceneTouchListener(this);
		mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(wind * (float) Math.pow(speed, 2), SensorManager.GRAVITY_EARTH * gravity * (float) Math.pow(speed, 2)), false, 3, 2);
		drawGrid(scene);
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0, false);
		if (prefs.getBoolean("prefGround", false)) {
			final Shape ground = new Rectangle(0, -1, cameraWidth, 1);
			PhysicsFactory.createBoxBody(mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef); // Temporarily disabled for v1 accuracy
			scene.getLayer(0).addEntity(ground);
		}
		if (prefs.getBoolean("prefRoof", false)) {
			final Shape roof = new Rectangle(0, -cameraHeight, cameraWidth, 1);
			PhysicsFactory.createBoxBody(mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
			scene.getLayer(0).addEntity(roof);
		}
		if (prefs.getBoolean("prefLeftWall", false)) {
			final Shape left = new Rectangle(0, -cameraHeight, 1, cameraHeight);
			PhysicsFactory.createBoxBody(mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
			scene.getLayer(0).addEntity(left);
		}
		if (prefs.getBoolean("prefRightWall", false)) {
			final Shape right = new Rectangle(cameraWidth - 1, -cameraHeight, 1, cameraHeight);
			PhysicsFactory.createBoxBody(mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
			scene.getLayer(0).addEntity(right);
		}
		target = new Ball(targetD - targetRadius, -targetH - targetRadius, mCircleTextureRegion);
		//target.setScaleCenter(targetRadius, targetRadius);
		//target.setScale(targetRadius * 2f / ratio);
		target.setSize(targetRadius * 2, targetRadius * 2);
		target.setColor(Color.red(colortarget) / 255f, Color.green(colortarget) / 255f, Color.blue(colortarget) / 255f, Color.alpha(colortarget) / 255f);
		target.setUpdatePhysics(false);
		final FixtureDef targetFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0, false);
		if (!prefs.getBoolean("prefCollide", false)) {
			targetFixtureDef.isSensor = true;
		}
		targetBody = PhysicsFactory.createCircleBody(mPhysicsWorld, target, BodyType.StaticBody, targetFixtureDef);
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(target, targetBody, false, false, false, false));
		scene.getLayer(1).addEntity(target);
		mPhysicsWorld.setContactListener(new ContactListener() {
			public void beginContact(final Contact pContact) {
				if (pContact.getFixtureA().getBody() == targetBody) {
					//target.setColor(0, 0, 1);
					//target.setScale(target.getScaleX()*0.9f);
					((Ball) pContact.getFixtureB().getBody().getUserData()).setColor(1, 0, 0);
				}
			}

			public void endContact(final Contact pContact) {
				if (pContact.getFixtureA().getBody() == targetBody) {
					//target.setColor(1, 0, 0);
					//target.setPosition(100, -100);
					//targetBody.setType(BodyType.DynamicBody);
					((Ball) pContact.getFixtureB().getBody().getUserData()).setColor(0, 1, 0);
					if (prefs.getBoolean("prefExpTarget", false)) {
						removeTarget();
						//addTarget();
					}
				}
			}
		});
		scene.registerUpdateHandler(mPhysicsWorld);
		/* // Remove target if body UserData indicates it should be removed
		scene.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if (targetBody.getUserData().equals(false)) {
					//removeTarget();
				}
			}

			@Override
			public void reset() {
			}
		});
		*/
		aText = new ChangeableText(10, 10, mFont, "Angle: " + angle, "Angle: XXXXX".length());
		//aText = new ChangeableText(10 / pxPerMeter, 10  / pxPerMeter, mFont, "Angle: " + angle, "Angle: XXXXX".length());
		hud.getLayer(0).addEntity(aText);
		vText = new ChangeableText(10, 40, mFont, "Velocity: " + velocity, "Velocity: XXXXX".length());
		//vText = new ChangeableText(10 / pxPerMeter, 40 / pxPerMeter, mFont, "Velocity: " + velocity, "Velocity: XXXXX".length());
		hud.getLayer(0).addEntity(vText);
		return scene;
	}

	public void onLoadComplete() {
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.getRepeatCount() == 0) {
			addBall(-16, -17);
			return true;
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			angle--;
			aText.setText("Angle: " + angle);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			angle++;
			aText.setText("Angle: " + angle);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			velocity--;
			vText.setText("Velocity: " + velocity);
		}
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			velocity++;
			vText.setText("Velocity: " + velocity);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onSceneTouchEvent(final Scene scene, final TouchEvent pSceneTouchEvent) {
		switch (pSceneTouchEvent.getAction()) {
		case TouchEvent.ACTION_DOWN:
			if (mPhysicsWorld != null) {
				addBall(-15, -17);
				if (prefs.getBoolean("prefTrail", false)) {
					//trail(pScene); // Disabled for now
				}
			}
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					//camera.setCenter(targetD, -targetH);
					//camera.setZoomFactor(4f);
					//camera.setZoomFactor(pxPerMeter * 4f);
				}
			};
			timer.schedule(task, 1000);
			break;
		case TouchEvent.ACTION_UP:
			timer.cancel();
			timer = new Timer();
			//camera.setCenter(cameraWidth / 2, -cameraHeight / 2);
			//camera.setZoomFactor(1f);
			//camera.setCenter(cameraWidth / 2 / pxPerMeter, -cameraHeight / 2 / pxPerMeter);
			//camera.setZoomFactor(pxPerMeter);
			break;
		}
		return true;
	}

	void drawGrid(Scene scene) {
		gridx = Integer.parseInt(prefs.getString("prefGridX", null));
		gridy = Integer.parseInt(prefs.getString("prefGridY", null));
		final FixtureDef gridFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
		gridFixtureDef.isSensor = true;
		int grid1 = 0, grid2 = 0;
		// Draw vertical lines within screen space
		do {
			grid1 += gridx;
			final Line linex = new Line(grid1, 0, grid1, -cameraHeight);
			linex.setColor(Color.red(colorgrid) / 255f, Color.green(colorgrid) / 255f, Color.blue(colorgrid) / 255f, Color.alpha(colorgrid) / 255f);
			PhysicsFactory.createBoxBody(mPhysicsWorld, linex, BodyType.StaticBody, gridFixtureDef);
			scene.getLayer(0).addEntity(linex);
		} while (grid1 < cameraWidth - gridx && gridx != 0);
		// Draw horizontal lines within screen space
		do {
			grid2 += gridy;
			final Line liney = new Line(0, -grid2, cameraWidth, -grid2);
			liney.setColor(Color.red(colorgrid) / 255f, Color.green(colorgrid) / 255f, Color.blue(colorgrid) / 255f, Color.alpha(colorgrid) / 255f);
			PhysicsFactory.createBoxBody(mPhysicsWorld, liney, BodyType.StaticBody, gridFixtureDef);
			scene.getLayer(0).addEntity(liney);
		} while (grid2 < cameraHeight - gridy && gridy != 0);
	}

	private void addBall(final float pX, final float pY) {
		final Scene scene = mEngine.getScene();
		final FixtureDef ballFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0, false);
		final Ball ball = new Ball(pX, pY, mCircleTextureRegion);
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				ball.setVelocity(velocity * speed / ratio * (float) Math.cos(Math.toRadians(angle)), -velocity * speed / ratio * (float) Math.sin(Math.toRadians(angle)));
				//ball.setScaleCenter(RATIO/2, RATIO/2);
				ball.setScale(ballScale);
				final Body body = PhysicsFactory.createCircleBody(mPhysicsWorld, ball, BodyType.DynamicBody, ballFixtureDef);
				ball.setColor(Color.red(colorproj) / 255f, Color.green(colorproj) / 255f, Color.blue(colorproj) / 255f, Color.alpha(colorproj) / 255f);
				ball.setUpdatePhysics(false);
				mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball, body, true, true, false, false));
				body.setUserData(ball);
				scene.getLayer(2).addEntity(ball);
			}
		});
		if (fuze == 0) {
			scene.registerUpdateHandler(new IUpdateHandler() {
				IUpdateHandler uh = this;
				DecimalFormat df2 = new DecimalFormat("0.##"); // Up to 2 decimal places
				double lastDistance = 0;

				@Override
				public void onUpdate(float pSecondsElapsed) {
					if (scene.getLayer(1).getEntityCount() != 0) {
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								double distance = getDistance(ball);
								if (distance > lastDistance & lastDistance > 0) {
									double score = lastDistance;
									String result = "Miss! - ";
									if (targetRadius >= lastDistance) {
										result = "Direct hit! - ";
										score /= 2;
									} else if (targetRadius + ballRadius >= lastDistance) {
										result = "Touched! - ";
										score /= 1.5;
									}
									Toast.makeText(GameField.this, result + "Score: " + df2.format(score) + " - Distance: " + df2.format(lastDistance), Toast.LENGTH_SHORT).show();
									lastDistance = -1;
									scene.unregisterUpdateHandler(uh);
								} else if (lastDistance == 0 | distance <= lastDistance) {
									lastDistance = distance;
								}
							}
						});
					}
				}

				@Override
				public void reset() {
				}
			});
		} else {
			new CountDownTimer((long) (fuze / speed), (long) (fuze / speed)) {
				final DecimalFormat df2 = new DecimalFormat("0.##");

				public void onTick(long millisUntilFinished) {
				}

				@Override
				public void onFinish() {
					if (scene.getLayer(1).getEntityCount() != 0) {
						// Scoring with fuze
						runOnUiThread(new Runnable() {
							@Override
							public void run() {
								double distance = getDistance(ball);
								double score = distance;
								String result = "Miss! - ";
								if (targetRadius >= distance) {
									result = "Direct hit! - ";
									score /= 2;
								} else if (targetRadius + ballRadius >= distance) {
									result = "Touched! - ";
									score /= 1.5;
								}
								Toast.makeText(GameField.this, result + "Score: " + df2.format(score) + " - Distance: " + df2.format(distance), Toast.LENGTH_SHORT).show();
							}
						});
					}
					removeBall(ball, 2);
				}
			}.start();
		}
	}

	private Double getDistance(Ball ball) {
		float ballX = ball.getX() + ratio / 2;
		float ballY = ball.getY() + ratio / 2;
		float targetX = target.getX() + targetRadius;
		float targetY = target.getY() + targetRadius;
		return Math.sqrt(Math.pow((ballX - targetX), 2) + Math.pow((ballY - targetY), 2));
	}

	private void removeBall(final Ball ball, final int layer) {
		try {
			final Scene scene = mEngine.getScene();
			final PhysicsConnector ballPhysicsConnector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(ball);
			runOnUpdateThread(new Runnable() {
				@Override
				public void run() {
					mPhysicsWorld.unregisterPhysicsConnector(ballPhysicsConnector);
					mPhysicsWorld.destroyBody(ballPhysicsConnector.getBody());
					scene.getLayer(layer).removeEntity(ball);
				}
			});
		} catch (NullPointerException e) {
			Log.e("Cannon", "Unable to remove ball, possibly already removed");
			e.printStackTrace();
		}
	}

	void addTarget() {
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				final Scene scene = mEngine.getScene();
				targetRadius = rand_gen.nextInt(50) + 3;
				targetD = rand_gen.nextInt(cameraWidth - targetRadius * 2) + targetRadius;
				targetH = rand_gen.nextInt(cameraHeight - targetRadius * 2) + targetRadius;
				target = new Ball(targetD - targetRadius, -targetH - targetRadius, mCircleTextureRegion);
				target.setSize(targetRadius * 2, targetRadius * 2);
				target.setColor(Color.red(colortarget) / 255f, Color.green(colortarget) / 255f, Color.blue(colortarget) / 255f, Color.alpha(colortarget) / 255f);
				target.setUpdatePhysics(false);
				final FixtureDef targetFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0, false);
				if (!prefs.getBoolean("prefCollide", false)) {
					targetFixtureDef.isSensor = true;
				}
				targetBody = PhysicsFactory.createCircleBody(mPhysicsWorld, target, BodyType.StaticBody, targetFixtureDef);
				mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(target, targetBody, false, false, false, false));
				scene.getLayer(1).addEntity(target);
			}
		});
	}

	private void removeTarget() {
		final Scene scene = mEngine.getScene();
		if (scene.getLayer(1).getEntityCount() != 0) {
			//Log.d("Cannon", "Layer 1 entity count is not 0");
					removeBall(target, 1);
		}
	}

	/*
	private void trail(final Scene scene) {
		final PointParticleEmitter particleEmitter = new PointParticleEmitter(0, 0);
		final ParticleSystem particleSystem = new ParticleSystem(particleEmitter, 5, 5, 100, mCircleTextureRegion);
		particleSystem.addParticleModifier(new ScaleModifier(6f / RATIO, 6f / RATIO, 0, 0));
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				scene.getBottomLayer().addEntity(particleSystem);
			}
		});
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				Log.d("Cannon", "TimerTask().run()");
				runOnUpdateThread(new Runnable() {
					@Override
					public void run() {
						Log.d("Cannon", "Runnable().run()");
						if (scene.getTopLayer().getEntityCount() > 0) {
							Log.d("Cannon", "Entity exist");
							final Ball ball = (Ball) scene.getTopLayer().getEntity(0);
							((BaseParticleEmitter) particleEmitter).setCenter(ball.getX(), ball.getY());
						} else {
							Log.d("Cannon", "Entity not exist");
							scene.getBottomLayer().removeEntity(particleSystem);
							cancel();
						}
					}
				});
			}
		};
		new Timer().scheduleAtFixedRate(task, 0, 50);
	}
	*/

	private static class Ball extends Sprite {
		public Ball(final float pX, final float pY, final TextureRegion pTextureRegion) {
			super(pX, pY, pTextureRegion);
		}
	}
}