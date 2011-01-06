package com.pilot51.cannon;

import java.text.DecimalFormat;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.SmoothCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.particle.Particle;
import org.anddev.andengine.entity.particle.ParticleSystem;
import org.anddev.andengine.entity.particle.emitter.CircleOutlineParticleEmitter;
import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;
import org.anddev.andengine.entity.particle.modifier.AlphaInitializer;
import org.anddev.andengine.entity.particle.modifier.AlphaModifier;
import org.anddev.andengine.entity.particle.modifier.ColorInitializer;
import org.anddev.andengine.entity.particle.modifier.ColorModifier;
import org.anddev.andengine.entity.particle.modifier.ExpireModifier;
import org.anddev.andengine.entity.particle.modifier.IParticleInitializer;
import org.anddev.andengine.entity.particle.modifier.RotationInitializer;
import org.anddev.andengine.entity.particle.modifier.ScaleModifier;
import org.anddev.andengine.entity.particle.modifier.VelocityInitializer;
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
import org.anddev.andengine.util.HorizontalAlign;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.SensorManager;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.FloatMath;
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
	private TextureRegion tCircle;
	private FixedStepPhysicsWorld mPhysicsWorld;
	private SmoothCamera camera;
	//private Timer timer = new Timer();
	private SharedPreferences prefs, values;
	private float ratio, speed, pxPerMeter, angle, velocity, gravity, wind, ballRadius;
	private long fuze;
	private int cameraWidth, cameraHeight, targetD, targetH, targetRadius, gridx, gridy, colorbg, colorgrid, colorproj, colortarget, score;
	private boolean mRandom;
	private Sprite target;
	private Body targetBody;
	private Font mFont;
	private ChangeableText aText, vText, sText;
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
		//ballScale = ballRadius * 0.5f / ratio;

		mTexture = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegionFactory.setAssetBasePath("gfx/");
		tCircle = TextureRegionFactory.createFromAsset(mTexture, this, "circle_white.png", 0, 0);
		mEngine.getTextureManager().loadTexture(mTexture);
		values = getSharedPreferences("valuePref", 0);
		if (mRandom) {
			angle = 45;
			velocity = 100;
			fuze = 0;
			gravity = 1f / ratio;
			wind = 0;
			//wind = (float)(rand_gen.nextInt(7) - 3) / RATIO;
		} else {
			angle = values.getFloat("prefAngle", 0);
			velocity = values.getFloat("prefVelocity", 0);
			fuze = (long) (values.getFloat("prefFuze", 0) * 1000);
			gravity = values.getFloat("prefGravity", 0) / ratio;
			wind = values.getFloat("prefWind", 0) / ratio;
		}
		mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
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
		if (mRandom) {
			targetRadius = rand_gen.nextInt(50) + 3;
			targetD = rand_gen.nextInt(cameraWidth - targetRadius * 2) + targetRadius;
			targetH = rand_gen.nextInt(cameraHeight - targetRadius * 2) + targetRadius;
		} else {
			targetRadius = values.getInt("prefTargetS", 0);
			targetD = values.getInt("prefTargetD", 0);
			targetH = values.getInt("prefTargetH", 0);
		}
		if (targetD > 0 | targetH > 0) {
			addTarget();
			mPhysicsWorld.setContactListener(new ContactListener() {
				public void beginContact(final Contact pContact) {
					Body bodyA = pContact.getFixtureA().getBody();
					Body bodyB = pContact.getFixtureB().getBody();
					/*
					if (bodyA == targetBody) {
						//target.setColor(0, 0, 1);
						//target.setScale(target.getScaleX()*0.9f);
						((Sprite) bodyB.getUserData()).setColor(1, 0, 0);
					} else if (bodyB == targetBody) {
						((Sprite) bodyA.getUserData()).setColor(1, 0, 0);
					}
					*/
				}

				public void endContact(final Contact pContact) {
					Body bodyA = pContact.getFixtureA().getBody();
					Body bodyB = pContact.getFixtureB().getBody();
					Boolean expTarget = prefs.getBoolean("prefExpTarget", false);
					Boolean keepTargets = prefs.getBoolean("prefKeep", false);
					if (bodyA == targetBody) {
						//target.setColor(1, 0, 0);
						//target.setPosition(100, -100);
						//targetBody.setType(BodyType.DynamicBody);
						//((Sprite) bodyB.getUserData()).setColor(0, 1, 0);
						if (expTarget)
							createFirework();
						if (!keepTargets)
							removeTarget();
						if (mRandom)
							addTarget();

					} else if (bodyB == targetBody) {
						//((Sprite) bodyA.getUserData()).setColor(0, 1, 0);
						if (expTarget)
							createFirework();
						if (!keepTargets)
							removeTarget();
						if (mRandom)
							addTarget();
					}
				}
			});
		}
		scene.registerUpdateHandler(mPhysicsWorld);
		aText = new ChangeableText(10, 10, mFont, "Angle: " + angle, "Angle: XXXXX".length());
		hud.getLayer(0).addEntity(aText);
		vText = new ChangeableText(10, 40, mFont, "Velocity: " + velocity, "Velocity: XXXXX".length());
		hud.getLayer(0).addEntity(vText);
		sText = new ChangeableText(cameraWidth - 100, 10, mFont, "Score: " + score, HorizontalAlign.RIGHT, "Score: XXXXXXX".length());
		hud.getLayer(0).addEntity(sText);
		return scene;
	}

	public void onLoadComplete() {
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER && event.getRepeatCount() == 0) {
			addBall();
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
				addBall();
				/* // Trail disabled until find a way to make it work right
				if (prefs.getBoolean("prefTrail", false)) {
					trail(pScene); // Disabled for now
				}
				*/
			}
			/* // Zoom disabled for v2.0.0 until something better is found
			TimerTask task = new TimerTask() {
				@Override
				public void run() {
					camera.setCenter(targetD, -targetH);
					camera.setZoomFactor(4f);
					camera.setZoomFactor(pxPerMeter * 4f);
				}
			};
			timer.schedule(task, 1000);
			*/
			break;
		/* // Other portion of zoom
		case TouchEvent.ACTION_UP:
		timer.cancel();
		timer = new Timer();
		camera.setCenter(cameraWidth / 2, -cameraHeight / 2);
		camera.setZoomFactor(1f);
		camera.setCenter(cameraWidth / 2 / pxPerMeter, -cameraHeight / 2 / pxPerMeter);
		camera.setZoomFactor(pxPerMeter);
		break;
		*/
		}
		return true;
	}

	void drawGrid(Scene scene) {
		final FixtureDef gridFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
		gridFixtureDef.isSensor = true;
		gridx = Integer.parseInt(prefs.getString("prefGridX", null));
		if (gridx > 0) {
			// Draw vertical lines within screen space
			int grid = 0;
			do {
				grid += gridx;
				final Line linex = new Line(grid, 0, grid, -cameraHeight);
				linex.setColor(Color.red(colorgrid) / 255f, Color.green(colorgrid) / 255f, Color.blue(colorgrid) / 255f, Color.alpha(colorgrid) / 255f);
				PhysicsFactory.createBoxBody(mPhysicsWorld, linex, BodyType.StaticBody, gridFixtureDef);
				scene.getLayer(0).addEntity(linex);
			} while (grid < cameraWidth - gridx && gridx != 0);
		}
		gridy = Integer.parseInt(prefs.getString("prefGridY", null));
		if (gridy > 0) {
			// Draw horizontal lines within screen space
			int grid = 0;
			do {
				grid += gridy;
				final Line liney = new Line(0, -grid, cameraWidth, -grid);
				liney.setColor(Color.red(colorgrid) / 255f, Color.green(colorgrid) / 255f, Color.blue(colorgrid) / 255f, Color.alpha(colorgrid) / 255f);
				PhysicsFactory.createBoxBody(mPhysicsWorld, liney, BodyType.StaticBody, gridFixtureDef);
				scene.getLayer(0).addEntity(liney);
			} while (grid < cameraHeight - gridy && gridy != 0);
		}
	}

	private void addBall() {
		final Scene scene = mEngine.getScene();
		final FixtureDef ballFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0, false);
		final Sprite ball = new Sprite((-tCircle.getWidth() / 2) + 1, (-tCircle.getHeight() / 2) - 1, tCircle);
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				ball.setVelocity(velocity * speed / ratio * (float) Math.cos(Math.toRadians(angle)), -velocity * speed / ratio * (float) Math.sin(Math.toRadians(angle)));
				ball.setScaleCenter(ball.getWidth() / 2, ball.getHeight() / 2);
				ball.setScale(ballRadius / (ball.getWidth() / 2));
				final Body body = PhysicsFactory.createCircleBody(mPhysicsWorld, ball, BodyType.DynamicBody, ballFixtureDef);
				ball.setColor(Color.red(colorproj) / 255f, Color.green(colorproj) / 255f, Color.blue(colorproj) / 255f, Color.alpha(colorproj) / 255f);
				body.setBullet(true);
				ball.setUpdatePhysics(false);
				mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball, body, true, true, false, false));
				body.setUserData(ball);
				scene.getLayer(2).addEntity(ball);
			}
		});
		if (targetD > 0 | targetH > 0) {
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
										//score = (int)lastDistance;
										String result = "Miss! - ";
										/*
										if (targetRadius >= lastDistance) {
											result = "Direct hit! - ";
											score /= 2;
										} else if (targetRadius + ballRadius >= lastDistance) {
											result = "Touched! - ";
											score /= 1.5;
										}
										*/
										if (targetRadius + ballRadius >= lastDistance) {
											result = "Hit! - ";
											score += 1;
										} else {
											score -= 1;
										}
										sText.setText("Score: " + score);
										//Toast.makeText(GameField.this, result + "Score: " + df2.format(score) + " - Distance: " + df2.format(lastDistance), Toast.LENGTH_SHORT).show();
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
									//score = (int)distance;
									String result = "Miss! - ";
									/*
									if (targetRadius >= distance) {
										result = "Direct hit! - ";
										score /= 2;
									} else if (targetRadius + ballRadius >= distance) {
										result = "Touched! - ";
										score /= 1.5;
									}
									*/
									if (targetRadius + ballRadius >= distance) {
										result = "Hit! - ";
										score += 2;
									} else {
										score -= 1;
									}
									sText.setText("Score: " + score);
									//Toast.makeText(GameField.this, result + "Score: " + df2.format(score) + " - Distance: " + df2.format(distance), Toast.LENGTH_SHORT).show();
								}
							});
						}
						removeBall(ball, 2);
					}
				}.start();
			}
		}
	}

	private Double getDistance(Sprite ball) {
		float ballX = ball.getX() + ratio / 2;
		float ballY = ball.getY() + ratio / 2;
		float targetX = target.getX() + targetRadius;
		float targetY = target.getY() + targetRadius;
		return Math.sqrt(Math.pow((ballX - targetX), 2) + Math.pow((ballY - targetY), 2));
	}

	private void removeBall(final Sprite ball, final int layer) {
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
				if (mRandom) {
					targetRadius = rand_gen.nextInt(50) + 3;
					targetD = rand_gen.nextInt(cameraWidth - targetRadius * 2) + targetRadius;
					targetH = rand_gen.nextInt(cameraHeight - targetRadius * 2) + targetRadius;
				} else {
					targetRadius = values.getInt("prefTargetS", 0);
					targetD = values.getInt("prefTargetD", 0);
					targetH = values.getInt("prefTargetH", 0);
				}
				final Scene scene = mEngine.getScene();
				target = new Sprite(targetD - tCircle.getWidth() / 2, -targetH - tCircle.getHeight() / 2, tCircle);
				//target.setSize(targetRadius * 2, targetRadius * 2);
				target.setScaleCenter(target.getWidth() / 2, target.getHeight() / 2);
				target.setScale(targetRadius / (target.getWidth() / 2));
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

	private void createFirework() {
		final Scene scene = mEngine.getScene();
		final PointParticleEmitter particleEmitter = new PointParticleEmitter(target.getX(), target.getY());
		final ParticleSystem particleSystem = new ParticleSystem(particleEmitter, 1000, 1000, 200, tCircle);
		particleSystem.addParticleInitializer(new ColorInitializer(1, 1, 0));
		particleSystem.addParticleInitializer(new AlphaInitializer(1));
		particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		//particleSystem.addParticleInitializer(new VelocityInitializer(0, 50, 0, 50));
		//particleSystem.addParticleInitializer(new RotationInitializer(0.0f, 360.0f));
		particleSystem.addParticleInitializer(new IParticleInitializer() {
			@Override
			public void onInitializeParticle(Particle pParticle) {
				int ang = rand_gen.nextInt(359);
				int vel = rand_gen.nextInt(100);
				float fVelocityX = FloatMath.cos((float) Math.toRadians(ang)) * vel;
				float fVelocityY = FloatMath.sin((float) Math.toRadians(ang)) * vel;
				pParticle.setVelocity(fVelocityX, fVelocityY);
				// calculate air resistance that acts opposite to particle
				// velocity
				// x% of deceleration is applied (that is opposite to velocity)
				//pParticle.setAcceleration(-fVelocityX / 10f, -fVelocityY / 10f);
				pParticle.setAcceleration((-fVelocityX / 6f) + wind * 30, (-fVelocityY / 6f) + SensorManager.GRAVITY_EARTH * gravity * 30);
			}
		});
		particleSystem.addParticleModifier(new ScaleModifier(0.05f, 0.01f, 0, 8));
		particleSystem.addParticleModifier(new ColorModifier(1, 1, 1, 0, 0, 0, 0, 0.1f));
		particleSystem.addParticleModifier(new ColorModifier(1, 0, 0, 1, 0, 0, 1, 3));
		particleSystem.addParticleModifier(new ColorModifier(0, 0, 1, 0, 0, 1, 3, 6));
		//particleSystem.addParticleModifier(new AlphaModifier(0, 1, 0, 1));
		particleSystem.addParticleModifier(new AlphaModifier(1, 0, 7, 8));
		particleSystem.addParticleModifier(new ExpireModifier(10, 10));

		scene.getLayer(0).addEntity(particleSystem);
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				particleSystem.setParticlesSpawnEnabled(false);
			}
		};
		new Timer().schedule(task, 1000);
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
	/*
		private static class Ball extends Sprite {
			public Ball(final float pX, final float pY, final TextureRegion pTextureRegion) {
				super(pX, pY, pTextureRegion);
			}
		}
		*/
}