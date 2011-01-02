package com.pilot51.cannon;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
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
import android.view.KeyEvent;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class GameField extends BaseGameActivity implements IOnSceneTouchListener {

	private static int cameraWidth;
	private static int cameraHeight;
	private final int RATIO = 32;
	private Texture mTexture;
	private TextureRegion mCircleTextureRegion;
	private FixedStepPhysicsWorld mPhysicsWorld;
	private SmoothCamera camera;
	private Timer timer = new Timer();
	private SharedPreferences prefs;
	private float angle, velocity, fuze, gravity, wind;
	private int targetD, targetH, targetRadius, gridx, gridy, colorbg, colorgrid, colorproj, colortarget;
	private boolean mRandom;
	private Ball target;
	private final float ballRadius = 3;
	private final float ballScale = ballRadius * 2f / RATIO;
	private Texture mFontTexture;
	private Font mFont;
	private ChangeableText aText, vText;
	private Random rand_gen = new Random();

	@Override
	public Engine onLoadEngine() {
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		cameraWidth = dm.widthPixels;
		cameraHeight = dm.heightPixels;
		camera = new SmoothCamera(0, -cameraHeight, cameraWidth, cameraHeight, 100, 100, 2.0f);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(cameraWidth, cameraHeight), camera));
		} else {
			return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(cameraWidth, cameraHeight), camera));
		}
	}

	@Override
	public void onLoadResources() {
		mRandom = getIntent().getBooleanExtra("random", false);
		mTexture = new Texture(64, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegionFactory.setAssetBasePath("gfx/");
		mCircleTextureRegion = TextureRegionFactory.createFromAsset(mTexture, this, "circle_white.png", 0, 32);
		mEngine.getTextureManager().loadTexture(mTexture);
		SharedPreferences values = getSharedPreferences("valuePref", 0);
		if (mRandom == false) {
			angle = values.getFloat("prefAngle", 0);
			velocity = values.getFloat("prefVelocity", 0);
			fuze = values.getFloat("prefFuze", 0) * 1000;
			gravity = values.getFloat("prefGravity", 0) / RATIO;
			wind = values.getFloat("prefWind", 0) / RATIO;
			targetRadius = values.getInt("prefTargetS", 0);
			targetD = values.getInt("prefTargetD", 0);
			targetH = values.getInt("prefTargetH", 0);
		} else {
			angle = 45;
			velocity = 50;
			fuze = 0;
			gravity = 1f / RATIO;
			wind = 0;
			//wind = (float)(rand_gen.nextInt(7) - 3) / RATIO;
			targetRadius = rand_gen.nextInt(50) + 3;
			targetD = rand_gen.nextInt(cameraWidth - targetRadius*2) + targetRadius;
			targetH = rand_gen.nextInt(cameraHeight - targetRadius*2) + targetRadius;
		}
		prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		colorbg = Color.parseColor(prefs.getString("prefColorBG", null));
		colorgrid = Color.parseColor(prefs.getString("prefColorGrid", null));
		colortarget = Color.parseColor(prefs.getString("prefColorTarget", null));
		colorproj = Color.parseColor(prefs.getString("prefColorProj", null));
		mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mFont = new Font(mFontTexture, Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 20, true, Color.WHITE);
		mEngine.getTextureManager().loadTexture(mFontTexture);
		mEngine.getFontManager().loadFont(mFont);
	}

	@Override
	public Scene onLoadScene() {
		final Scene scene = new Scene(2);
		scene.setBackground(new ColorBackground(Color.red(colorbg) / 255f, Color.green(colorbg) / 255f, Color.blue(colorbg) / 255f, Color.alpha(colorbg) / 255f));
		scene.setOnSceneTouchListener(this);
		mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(wind, SensorManager.GRAVITY_EARTH * gravity), false, 3, 2);
		drawGrid(scene);
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0, false);
		if (prefs.getBoolean("prefGround", false)) {
			final Shape ground = new Rectangle(0, -1, cameraWidth, 1);
			PhysicsFactory.createBoxBody(mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef); // Temporarily disabled for v1 accuracy
			scene.getBottomLayer().addEntity(ground);
		}
		if (prefs.getBoolean("prefRoof", false)) {
			final Shape roof = new Rectangle(0, -cameraHeight, cameraWidth, 1);
			PhysicsFactory.createBoxBody(mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
			scene.getBottomLayer().addEntity(roof);
		}
		if (prefs.getBoolean("prefLeftWall", false)) {
			final Shape left = new Rectangle(0, -cameraHeight, 1, cameraHeight);
			PhysicsFactory.createBoxBody(mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
			scene.getBottomLayer().addEntity(left);
		}
		if (prefs.getBoolean("prefRightWall", false)) {
			final Shape right = new Rectangle(cameraWidth - 1, -cameraHeight, 1, cameraHeight);
			PhysicsFactory.createBoxBody(mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
			scene.getBottomLayer().addEntity(right);
		}
		target = new Ball(targetD - targetRadius, -targetH - targetRadius, mCircleTextureRegion);
		target.setScaleCenter(targetRadius, targetRadius);
		target.setSize(targetRadius * 2, targetRadius * 2);
		target.setColor(Color.red(colortarget) / 255f, Color.green(colortarget) / 255f, Color.blue(colortarget) / 255f, Color.alpha(colortarget) / 255f);
		final FixtureDef targetFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0, false);
		if (!prefs.getBoolean("prefCollide", false)) {
			targetFixtureDef.isSensor = true;
		}
		final Body targetBody = PhysicsFactory.createCircleBody(mPhysicsWorld, target, BodyType.StaticBody, targetFixtureDef);
		//targetBody.setUserData(true);
		scene.getBottomLayer().addEntity(target);
		mPhysicsWorld.setContactListener(new ContactListener() {
			public void beginContact(final Contact pContact) {
				if (pContact.getFixtureA().getBody() == targetBody) {
					target.setColor(0, 0, 1);
					//target.setScale(target.getScaleX()*0.9f);
					((Ball) pContact.getFixtureB().getBody().getUserData()).setColor(1, 0, 0);
				}
			}

			public void endContact(final Contact pContact) {
				if (pContact.getFixtureA().getBody() == targetBody) {
					target.setColor(1, 0, 0);
					((Ball) pContact.getFixtureB().getBody().getUserData()).setColor(0, 1, 0);
					//targetBody.setUserData(false);
				}
			}
		});
		scene.registerUpdateHandler(mPhysicsWorld);
		/* // Remove target if body UserData indicates it should be removed
				scene.registerUpdateHandler(new IUpdateHandler() {

					@Override
					public void onUpdate(float pSecondsElapsed) {
						if (targetBody.getUserData().equals(false)) {
								mPhysicsWorld.destroyBody(targetBody);
								scene.getBottomLayer().removeEntity(target);
						}
					}

					@Override
					public void reset() {
					}
				});
		*/
		aText = new ChangeableText(10, -cameraHeight + 10, mFont, "Angle: " + angle, "Angle: XXXXX".length());
		scene.getBottomLayer().addEntity(aText);
		vText = new ChangeableText(10, -cameraHeight + 40, mFont, "Velocity: " + velocity, "Velocity: XXXXX".length());
		scene.getBottomLayer().addEntity(vText);
		return scene;
	}

	public void onLoadComplete() {}

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
	public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
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
					camera.setCenter(targetD, -targetH);
					camera.setZoomFactor(4.0f);
				}
			};
			timer.schedule(task, 1000);
			break;
		case TouchEvent.ACTION_UP:
			timer.cancel();
			timer = new Timer();
			camera.setCenter(cameraWidth / 2, -cameraHeight / 2);
			camera.setZoomFactor(1.0f);
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
			scene.getBottomLayer().addEntity(linex);
		} while (grid1 < cameraWidth - gridx && gridx != 0);
		// Draw horizontal lines within screen space
		do {
			grid2 += gridy;
			final Line liney = new Line(0, -grid2, cameraWidth, -grid2);
			liney.setColor(Color.red(colorgrid) / 255f, Color.green(colorgrid) / 255f, Color.blue(colorgrid) / 255f, Color.alpha(colorgrid) / 255f);
			PhysicsFactory.createBoxBody(mPhysicsWorld, liney, BodyType.StaticBody, gridFixtureDef);
			scene.getBottomLayer().addEntity(liney);
		} while (grid2 < cameraHeight - gridy && gridy != 0);
	}

	private void addBall(final float pX, final float pY) {
		final Scene scene = mEngine.getScene();
		final FixtureDef ballFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0, false);
		final Ball ball = new Ball(pX, pY, mCircleTextureRegion);
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {

				ball.setVelocity(velocity / RATIO * (float) Math.cos(Math.toRadians(angle)), -velocity / RATIO * (float) Math.sin(Math.toRadians(angle)));
				//ball.setScaleCenter(RATIO/2, RATIO/2);
				ball.setScale(ballScale);
				final Body body = PhysicsFactory.createCircleBody(mPhysicsWorld, ball, BodyType.DynamicBody, ballFixtureDef);
				ball.setColor(Color.red(colorproj) / 255f, Color.green(colorproj) / 255f, Color.blue(colorproj) / 255f, Color.alpha(colorproj) / 255f);
				ball.setUpdatePhysics(false);
				mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball, body, true, true, false, false));
				body.setUserData(ball);
				scene.getTopLayer().addEntity(ball);
			}
		});
		if (fuze == 0) {
			scene.registerUpdateHandler(new IUpdateHandler() {
				IUpdateHandler uh = this;
				DecimalFormat df2 = new DecimalFormat("0.##"); // Up to 2 decimal places
				double lastDistance = 0;

				@Override
				public void onUpdate(float pSecondsElapsed) {
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

				@Override
				public void reset() {
				}
			});
		} else {
			new CountDownTimer((int) fuze, (int) fuze) {
				final DecimalFormat df2 = new DecimalFormat("0.##");
				
				public void onTick(long millisUntilFinished) {}

				@Override
				public void onFinish() {
					runOnUpdateThread(new Runnable() {
						@Override
						public void run() {
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
							removeBall(ball);
						}
					});
				}
			}.start();
		}
	}

	private Double getDistance(Ball ball) {
		float ballX = ball.getX() + RATIO / 2;
		float ballY = ball.getY() + RATIO / 2;
		float targetX = target.getX() + targetRadius;
		float targetY = target.getY() + targetRadius;
		return Math.sqrt(Math.pow((ballX - targetX), 2) + Math.pow((ballY - targetY), 2));
	}

	private void removeBall(final Ball ball) {
		final Scene scene = mEngine.getScene();
		final PhysicsConnector ballPhysicsConnector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(ball);
		mPhysicsWorld.unregisterPhysicsConnector(ballPhysicsConnector);
		mPhysicsWorld.destroyBody(ballPhysicsConnector.getBody());
		scene.getTopLayer().removeEntity(ball);
	}

	/*
	private void removeTarget(final Ball ball) {
		final Scene scene = mEngine.getScene();
		final PhysicsConnector ballPhysicsConnector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(ball);
		mPhysicsWorld.unregisterPhysicsConnector(ballPhysicsConnector);
		mPhysicsWorld.destroyBody(ballPhysicsConnector.getBody());
		scene.getBottomLayer().removeEntity(ball);
	}
	*/
	
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