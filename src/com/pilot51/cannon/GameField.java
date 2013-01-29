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

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.handler.IUpdateHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.particle.Particle;
import org.anddev.andengine.entity.particle.ParticleSystem;
import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;
import org.anddev.andengine.entity.particle.modifier.AlphaInitializer;
import org.anddev.andengine.entity.particle.modifier.AlphaModifier;
import org.anddev.andengine.entity.particle.modifier.ColorInitializer;
import org.anddev.andengine.entity.particle.modifier.ColorModifier;
import org.anddev.andengine.entity.particle.modifier.ExpireModifier;
import org.anddev.andengine.entity.particle.modifier.IParticleInitializer;
import org.anddev.andengine.entity.particle.modifier.ScaleModifier;
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
import android.util.DisplayMetrics;
import android.util.FloatMath;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class GameField extends BaseGameActivity implements IOnSceneTouchListener {
	static final String EXTRA_RANDOM = "random";
	private Texture mTexture, mFontTexture;
	private TextureRegion tCircle;
	private FixedStepPhysicsWorld mPhysicsWorld;
	private Camera camera;
	private SharedPreferences prefScores;
	private float ratio, speed, pxPerMeter, angle, velocity, gravity, wind, ballRadius, targetRadius, targetD, targetH, mLastTouchX, mLastTouchY;
	private byte fontSize = 13;
	private long fuze, nTargets, nShots, score;
	private int cameraWidth, cameraHeight, gridx, gridy, colorBG, colorGrid, colorProj, colorTarget, colorHitTarget, senseMove, sensePressure;
	private boolean mRandom, repeat, collide, expTarget, keepTargets, firing;
	private String scoreType;
	private Sprite target;
	private Body targetBody;
	private Font mFont;
	private ChangeableText aText, vText, sText, hText;
	private static final Random RANDOM_GEN = new Random();
	private final HUD HUD = new HUD();
	private TimerTask autofire;
	private static final DecimalFormat DF = new DecimalFormat("0.#");


	@Override
	public Engine onLoadEngine() {
		pxPerMeter = Float.parseFloat(Common.getPrefs().getString("meter", "1"));
		ratio = 32f;
		speed = Float.parseFloat(Common.getPrefs().getString("speed", "1"));
		DisplayMetrics dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);
		cameraWidth = dm.widthPixels;
		cameraHeight = dm.heightPixels;
		fontSize *= dm.densityDpi / 160f;
		camera = new Camera(0, -cameraHeight, cameraWidth, cameraHeight);
		camera.setHUD(HUD);
		if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
			return new Engine(new EngineOptions(true, ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(cameraWidth, cameraHeight), camera));
		} else {
			return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(cameraWidth, cameraHeight), camera));
		}
	}

	@Override
	public void onLoadResources() {
		mRandom = getIntent().getBooleanExtra(EXTRA_RANDOM, false);
		collide = Common.getPrefs().getBoolean("collide", false);
		expTarget = Common.getPrefs().getBoolean("expTarget", false);
		keepTargets = Common.getPrefs().getBoolean("keepTargets", false);
		repeat = Common.getPrefs().getBoolean("repeat", false);
		senseMove = Integer.parseInt(Common.getPrefs().getString("senseMove", null));
		sensePressure = Integer.parseInt(Common.getPrefs().getString("sensePressure", null));
		if(mRandom & collide & keepTargets) scoreType = "rMulti";
		else if(mRandom) scoreType = "rSingle";
		colorBG = Color.parseColor(Common.getPrefs().getString("colorBG", null));
		colorGrid = Color.parseColor(Common.getPrefs().getString("colorGrid", null));
		colorTarget = Color.parseColor(Common.getPrefs().getString("colorTarget", null));
		colorHitTarget = Color.parseColor(Common.getPrefs().getString("colorHitTarget", null));
		colorProj = Color.parseColor(Common.getPrefs().getString("colorProj", null));
		mTexture = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		TextureRegionFactory.setAssetBasePath("gfx/");
		tCircle = TextureRegionFactory.createFromAsset(mTexture, this, "circle_white.png", 0, 0);
		mEngine.getTextureManager().loadTexture(mTexture);
		if (mRandom) {
			angle = 45;
			velocity = 100;
			fuze = 0;
			gravity = 1f / ratio;
			wind = 0;
			ballRadius = 3;
		} else {
			angle = CustomGame.getCustomPrefs().getFloat("angle", 0);
			velocity = CustomGame.getCustomPrefs().getFloat("velocity", 0);
			fuze = (long) (CustomGame.getCustomPrefs().getFloat("fuze", 0) * 1000);
			gravity = CustomGame.getCustomPrefs().getFloat("gravity", 0) / ratio;
			wind = CustomGame.getCustomPrefs().getFloat("wind", 0) / ratio;
			ballRadius = CustomGame.getCustomPrefs().getInt("projS", 0);
		}
		mFontTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		mFont = new Font(mFontTexture, Typeface.create(Typeface.MONOSPACE, Typeface.BOLD), fontSize, true, Color.WHITE);
		mEngine.getTextureManager().loadTexture(mFontTexture);
		mEngine.getFontManager().loadFont(mFont);
		prefScores = getSharedPreferences("scores", MODE_PRIVATE);
	}

	@Override
	public Scene onLoadScene() {
		final Scene scene = new Scene(3);
		scene.setBackground(new ColorBackground(Color.red(colorBG) / 255f, Color.green(colorBG) / 255f, Color.blue(colorBG) / 255f, Color.alpha(colorBG) / 255f));
		scene.setOnSceneTouchListener(this);
		mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(wind * (float) Math.pow(speed, 2), SensorManager.GRAVITY_EARTH * gravity * (float) Math.pow(speed, 2)), false, 3, 2);
		drawGrid(scene);
		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0, false);
		if (Common.getPrefs().getBoolean("ground", false)) {
			final Shape ground = new Rectangle(0, -1 / pxPerMeter, cameraWidth / pxPerMeter, 1 / pxPerMeter);
			PhysicsFactory.createBoxBody(mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
			addEntity(ground, 0, scene);
		}
		if (Common.getPrefs().getBoolean("roof", false)) {
			final Shape roof = new Rectangle(0, -cameraHeight / pxPerMeter, cameraWidth / pxPerMeter, 1 / pxPerMeter);
			PhysicsFactory.createBoxBody(mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
			addEntity(roof, 0, scene);
		}
		if (Common.getPrefs().getBoolean("leftWall", false)) {
			final Shape left = new Rectangle(0, -cameraHeight / pxPerMeter, 1 / pxPerMeter, cameraHeight / pxPerMeter);
			PhysicsFactory.createBoxBody(mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
			addEntity(left, 0, scene);
		}
		if (Common.getPrefs().getBoolean("rightWall", false)) {
			final Shape right = new Rectangle((cameraWidth - 1) / pxPerMeter, -cameraHeight / pxPerMeter, 1 / pxPerMeter, cameraHeight / pxPerMeter);
			PhysicsFactory.createBoxBody(mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
			addEntity(right, 0, scene);
		}
		if (mRandom || CustomGame.getCustomPrefs().getInt("targetD", 0) > 0 | CustomGame.getCustomPrefs().getInt("targetH", 0) > 0) {
			addTarget();
			mPhysicsWorld.setContactListener(new ContactListener() {
				public void beginContact(final Contact pContact) {
					final Body bodyA = pContact.getFixtureA().getBody();
					final Body bodyB = pContact.getFixtureB().getBody();
					if ((bodyA == targetBody | bodyB == targetBody) & targetBody.getUserData().equals(true)) {
						targetBody.setUserData(false);
						TimerTask targetAction = new TimerTask() {
							@Override
							public void run() {
								if (expTarget) createFirework();
								if (keepTargets) {
									target.setColor(Color.red(colorHitTarget) / 255f, Color.green(colorHitTarget) / 255f, Color.blue(colorHitTarget) / 255f, Color.alpha(colorHitTarget) / 255f);
								} else {
									nTargets--;
									removeSprite(target, 1);
								}
								if (mRandom) addTarget();
						}};
						new Timer().schedule(targetAction, (long) (100 / speed));
						long hits;
						if (bodyA == targetBody) {
							@SuppressWarnings("unchecked")
							HashMap<String, Object> map = ((HashMap<String, Object>) bodyB.getUserData());
							hits = (Long)map.get("hits") + 1;
							map.put("hits", hits);
							bodyB.setUserData(map);
						} else {
							@SuppressWarnings("unchecked")
							HashMap<String, Object> map = ((HashMap<String, Object>) bodyA.getUserData());
							hits = (Long)map.get("hits") + 1;
							map.put("hits", hits);
							bodyA.setUserData(map);
						}
						if(mRandom) {
							if(collide & keepTargets) score += nTargets * hits;
							else score += hits * 20;
							if(score > prefScores.getLong(scoreType, 0)) {
								String txt = getString(R.string.hud_high, score);
								hText.setPosition(cameraWidth - 10 - txt.length() * fontSize * 0.6f, 40);
								hText.setText(txt);
								prefScores.edit().putLong(scoreType, score).commit();
							}
							String txt = getString(R.string.hud_score, score);
							sText.setPosition(cameraWidth - 10 - txt.length() * fontSize * 0.6f, 10);
							sText.setText(txt);
						}
					}
				}

				public void endContact(final Contact pContact) {}
			});
		}
		scene.registerUpdateHandler(mPhysicsWorld);
		aText = new ChangeableText(10, 10, mFont, getString(R.string.hud_angle, angle), getString(R.string.hud_angle, 11111).length());
		addEntity(aText, 0, HUD);
		vText = new ChangeableText(10, 40, mFont, getString(R.string.hud_velocity, velocity), getString(R.string.hud_velocity, 11111).length());
		addEntity(vText, 0, HUD);
		if(mRandom) {
			String txt = getString(R.string.hud_score, score);
			sText = new ChangeableText(cameraWidth - 10 - txt.length() * fontSize * 0.6f, 10, mFont, txt, 100);
			addEntity(sText, 0, HUD);
			txt = getString(R.string.hud_high, prefScores.getLong(scoreType, 0));
			hText = new ChangeableText(cameraWidth - 10 - txt.length() * fontSize * 0.6f, 40, mFont, txt, 100);
			addEntity(hText, 0, HUD);
		}
		return scene;
	}

	public void onLoadComplete() {}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if (event.getRepeatCount() == 0) {
				if(repeat & !firing) {
					autofire = new TimerTask() {
						@Override
						public void run() {
							addBall();
					}};
					firing = true;
					new Timer().schedule(autofire, 0, 250);
				} else addBall();
			}
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			angle = --angle < 0 ? 0 : angle--;
			aText.setText(getString(R.string.hud_angle, DF.format(angle)));
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			angle = ++angle > 90 ? 90 : angle++;
			aText.setText(getString(R.string.hud_angle, DF.format(angle)));
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			velocity = --velocity < 0 ? 0 : velocity--;
			vText.setText(getString(R.string.hud_velocity, DF.format(velocity)));
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			velocity++;
			vText.setText(getString(R.string.hud_velocity, DF.format(velocity)));
			break;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if(repeat) {
				autofire.cancel();
				firing = false;
			}
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onSceneTouchEvent(final Scene scene, final TouchEvent pSceneTouchEvent) {
		MotionEvent me = pSceneTouchEvent.getMotionEvent();
		final float x = me.getX();
		final float y = me.getY();
		final float p = me.getPressure();
		switch (pSceneTouchEvent.getAction()) {
		case TouchEvent.ACTION_DOWN:
			mLastTouchX = x;
			mLastTouchY = y;
			break;
		case TouchEvent.ACTION_UP:
			if (firing) {
				if(repeat) autofire.cancel();
				firing = false;
			}
			break;
		case TouchEvent.ACTION_MOVE:
			if (mPhysicsWorld != null & !firing & p > sensePressure / 100f) {
				firing = true;
				if(repeat) {
					autofire = new TimerTask() {
						@Override
						public void run() {
							addBall();
					}};
					new Timer().schedule(autofire, 0, 250);
				} else addBall();
			} else if (firing & p < sensePressure / 100f) {
				if(repeat) autofire.cancel();
				firing = false;
			}
			final float dx = x - mLastTouchX;
			final float dy = y - mLastTouchY;
			angle -= dx/(100/senseMove);
			if (angle < 0) angle = 0;
			else if (angle > 90) angle = 90;
			aText.setText(getString(R.string.hud_angle, DF.format(angle)));
			velocity -= dy/(100/senseMove);
			if(velocity < 0) velocity = 0;
			vText.setText(getString(R.string.hud_velocity, DF.format(velocity)));
			mLastTouchX = x;
			mLastTouchY = y;
			break;
		}
		return true;
	}

	private void drawGrid(Scene scene) {
		final FixtureDef gridFixtureDef = PhysicsFactory.createFixtureDef(0, 0, 0);
		gridFixtureDef.isSensor = true;
		gridx = Integer.parseInt(Common.getPrefs().getString("gridX", null));
		if (gridx > 0) {
			// Draw vertical lines within screen space
			int grid = 0;
			do {
				grid += gridx;
				final Line linex = new Line(grid, 0, grid, -cameraHeight);
				linex.setColor(Color.red(colorGrid) / 255f, Color.green(colorGrid) / 255f, Color.blue(colorGrid) / 255f, Color.alpha(colorGrid) / 255f);
				PhysicsFactory.createBoxBody(mPhysicsWorld, linex, BodyType.StaticBody, gridFixtureDef);
				addEntity(linex, 0, scene);
			} while (grid < cameraWidth - gridx && gridx != 0);
		}
		gridy = Integer.parseInt(Common.getPrefs().getString("gridY", null));
		if (gridy > 0) {
			// Draw horizontal lines within screen space
			int grid = 0;
			do {
				grid += gridy;
				final Line liney = new Line(0, -grid, cameraWidth, -grid);
				liney.setColor(Color.red(colorGrid) / 255f, Color.green(colorGrid) / 255f, Color.blue(colorGrid) / 255f, Color.alpha(colorGrid) / 255f);
				PhysicsFactory.createBoxBody(mPhysicsWorld, liney, BodyType.StaticBody, gridFixtureDef);
				addEntity(liney, 0, scene);
			} while (grid < cameraHeight - gridy && gridy != 0);
		}
	}

	private void addBall() {
		final Scene scene = mEngine.getScene();
		final FixtureDef ballFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0, false);
		final Sprite ball = new Sprite((-tCircle.getWidth() / 2) + 1, (-tCircle.getHeight() / 2) - 1, tCircle);
		ball.setVelocity(velocity * speed / ratio * (float) Math.cos(Math.toRadians(angle)), -velocity * speed / ratio * (float) Math.sin(Math.toRadians(angle)));
		ball.setScaleCenter(ball.getWidth() / 2, ball.getHeight() / 2);
		ball.setScale(ballRadius / (ball.getWidth() / 2));
		ball.setColor(Color.red(colorProj) / 255f, Color.green(colorProj) / 255f, Color.blue(colorProj) / 255f, Color.alpha(colorProj) / 255f);
		ball.setUpdatePhysics(false);
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				final Body body = PhysicsFactory.createCircleBody(mPhysicsWorld, ball, BodyType.DynamicBody, ballFixtureDef);
				body.setBullet(true);
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("sprite", ball);
				map.put("hits", (long)0);
				map.put("shot", nShots++);
				body.setUserData(map);
				mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ball, body, true, true, true, false));
		}});
		addEntity(ball, 2, null);
		if(mRandom) {
			score -= 1;
			String txt = getString(R.string.hud_score, score);
			sText.setPosition(cameraWidth - 10 - txt.length() * fontSize * 0.6f, 10);
			sText.setText(txt);
		}
		if (Common.getPrefs().getBoolean("trail", false)) {
			trail(ball);
		}
		if (fuze > 0) {
			TimerTask remove = new TimerTask() {
				@Override
				public void run() {
					removeSprite(ball, 2);
				}
			};
			new Timer().schedule(remove, (long) (fuze / speed));
		}
		scene.registerUpdateHandler(new IUpdateHandler() {
			@Override
			public void onUpdate(float pSecondsElapsed) {
				int ballX = (int) (ball.getX() + (ball.getWidth() / 2));
				int ballY = (int) (ball.getY() + (ball.getHeight() / 2));
				// Remove ball if it leaves screen and definitely won't come back
				if ((gravity >= 0 & ballY > 0) | (wind <= 0 & ballX < 0) | (gravity <= 0 & ballY < -cameraHeight) | (wind >= 0 & ballX > cameraWidth)) {
					scene.unregisterUpdateHandler(this);
					removeSprite(ball, 2);
				}
			}

			@Override
			public void reset() {}
		});
	}
	
	private void removeSprite(final Sprite sprite, final int layer) {
		final PhysicsConnector ballPhysicsConnector = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(sprite);
		mPhysicsWorld.unregisterPhysicsConnector(ballPhysicsConnector);
		mPhysicsWorld.destroyBody(ballPhysicsConnector.getBody());
		removeEntity(sprite, layer, null);
		sprite.setVisible(false);
	}

	private void addTarget() {
		if (mRandom) {
			targetRadius = (RANDOM_GEN.nextInt(50) + 6) / pxPerMeter;
			targetD = RANDOM_GEN.nextInt((int)(cameraWidth / pxPerMeter - targetRadius * 2)) + targetRadius;
			targetH = RANDOM_GEN.nextInt((int)(cameraHeight / pxPerMeter - targetRadius * 2)) + targetRadius;
		} else {
			targetRadius = CustomGame.getCustomPrefs().getInt("targetS", 0);
			targetD = CustomGame.getCustomPrefs().getInt("targetD", 0);
			targetH = CustomGame.getCustomPrefs().getInt("targetH", 0);
		}
		target = new Sprite(targetD - tCircle.getWidth() / 2, -targetH - tCircle.getHeight() / 2, tCircle);
		target.setScaleCenter(target.getWidth() / 2, target.getHeight() / 2);
		target.setScale(targetRadius / (target.getWidth() / 2));
		target.setColor(Color.red(colorTarget) / 255f, Color.green(colorTarget) / 255f, Color.blue(colorTarget) / 255f, Color.alpha(colorTarget) / 255f);
		target.setUpdatePhysics(false);
		final FixtureDef targetFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0, false);
		if (!collide) {
			targetFixtureDef.isSensor = true;
		}
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				targetBody = PhysicsFactory.createCircleBody(mPhysicsWorld, target, BodyType.StaticBody, targetFixtureDef);
				targetBody.setUserData(true);
				mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(target, targetBody, false, false, false, false));
			}
		});
		nTargets++;
		addEntity(target, 1, null);
	}

	private void createFirework() {
		final long expire = 10;
		final PointParticleEmitter particleEmitter = new PointParticleEmitter(target.getX(), target.getY());
		final ParticleSystem particleSystem = new ParticleSystem(particleEmitter, 1000, 1000, 200, tCircle);
		particleSystem.addParticleInitializer(new ColorInitializer(1, 1, 0));
		particleSystem.addParticleInitializer(new AlphaInitializer(1));
		particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		particleSystem.addParticleInitializer(new IParticleInitializer() {
			@Override
			public void onInitializeParticle(Particle pParticle) {
				int ang = RANDOM_GEN.nextInt(359);
				int vel = RANDOM_GEN.nextInt(100);
				float fVelocityX = FloatMath.cos((float) Math.toRadians(ang)) * vel;
				float fVelocityY = FloatMath.sin((float) Math.toRadians(ang)) * vel;
				pParticle.setVelocity(fVelocityX, fVelocityY);
				pParticle.setAcceleration((-fVelocityX / 6f) + wind * 30, (-fVelocityY / 6f) + SensorManager.GRAVITY_EARTH * gravity * 30);
			}
		});
		particleSystem.addParticleModifier(new ScaleModifier(0.05f, 0.01f, 0, 8));
		particleSystem.addParticleModifier(new ColorModifier(1, 1, 1, 0, 0, 0, 0, 0.1f));
		particleSystem.addParticleModifier(new ColorModifier(1, 0, 0, 1, 0, 0, 1, 3));
		particleSystem.addParticleModifier(new ColorModifier(0, 0, 1, 0, 0, 1, 3, 6));
		particleSystem.addParticleModifier(new AlphaModifier(1, 0, 7, 8));
		particleSystem.addParticleModifier(new ExpireModifier(expire, expire));
		addEntity(particleSystem, 0, null);
		TimerTask disable = new TimerTask() {
			@Override
			public void run() {
				particleSystem.setParticlesSpawnEnabled(false);
			}
		};
		new Timer().schedule(disable, 1000);
		final TimerTask remove = new TimerTask() {
			@Override
			public void run() {
				removeEntity(particleSystem, 0, null);
			}
		};
		new Timer().schedule(remove, expire * 1000);
	}

	private void addEntity(final IEntity entity, final int layer, final Scene scene) {
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				Scene s = mEngine.getScene();
				if (scene != null) s = scene;
				s.getLayer(layer).addEntity(entity);
			}
		});
	}

	private void removeEntity(final IEntity entity, final int layer, final Scene scene) {
		runOnUpdateThread(new Runnable() {
			@Override
			public void run() {
				Scene s = mEngine.getScene();
				if (scene != null) s = scene;
				s.getLayer(layer).removeEntity(entity);
			}
		});
	}

	private void trail(final Sprite ball) {
		final long expire = 5;
		final PointParticleEmitter particleEmitter = new PointParticleEmitter(0, 0);
		final ParticleSystem particleSystem = new ParticleSystem(particleEmitter, 20, 20, 400, tCircle);
		particleSystem.addParticleInitializer(new ColorInitializer(Color.red(colorProj) / 255f, Color.green(colorProj) / 255f, Color.blue(colorProj) / 255f));
		particleSystem.addParticleInitializer(new AlphaInitializer(1));
		particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE);
		particleSystem.addParticleModifier(new ScaleModifier(ballRadius / (tCircle.getWidth() / 2), ballRadius / (tCircle.getWidth() / 2), 0, 0));
		particleSystem.addParticleModifier(new AlphaModifier(1, 0, 0, 5));
		particleSystem.addParticleModifier(new ExpireModifier(expire, expire));
		addEntity(particleSystem, 0, null);
		new Timer().scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				if (ball.isVisible() & (Math.sqrt(Math.pow(Math.abs(ball.getVelocityX()),2) + Math.pow(Math.abs(ball.getVelocityY()),2))) / speed * ratio >= 5) {
					particleEmitter.setCenter(ball.getX(), ball.getY());
				} else {
					particleSystem.setParticlesSpawnEnabled(false);
					new Timer().schedule(new TimerTask() {
						@Override
						public void run() {
							removeEntity(particleSystem, 0, null);
						}
					}, expire * 1000);
					cancel();
				}
			}
		}, 0, 50);
	}
}
