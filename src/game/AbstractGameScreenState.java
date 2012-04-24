package game;

import game.Car.CarType;
import physics.BMWM3Properties;
import physics.CarProperties;
import physics.EnginePhysics;
import physics.tools.Conversion;
import physics.tools.MathTools;
import audio.AudioRender;
import audio.SoundStore;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import com.jme3.asset.AssetManager;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.InputManager;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.AnalogListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Matrix3f;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.shadow.PssmShadowRenderer;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.SkyFactory;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.render.TextRenderer;
import de.lessvoid.nifty.screen.Screen;

public abstract class AbstractGameScreenState extends AbstractScreenController
		implements ActionListener, AnalogListener, PhysicsCollisionListener {

	private ViewPort viewPort;
	protected Node rootNode;
	protected AssetManager assetManager;
	private InputManager inputManager;

	private BulletAppState bulletAppState;

	protected SoundStore soundStore;

	protected Car player;
	protected CarProperties playerCarProperties;
	protected EnginePhysics playerEnginePhysics;

	protected boolean runIsOn;
	protected boolean runFinish;

	private ChaseCamera chaseCam;

	private TerrainQuad terrain;
	private Material mat_terrain;
	private RigidBodyControl terrainPhys;

	private PssmShadowRenderer pssmRenderer;

	protected long startTime = 0;
	protected long countDown = 0;

	protected boolean soudIsActive = true;

	protected AppStateManager stateManager;

	protected DigitalDisplay digitalTachometer;
	protected DigitalDisplay digitalSpeed;
	protected DigitalDisplay digitalGear;
	protected ShiftlightLed shiftlight;
	protected boolean isBreaking;
	protected long rpmTimer;

	protected boolean needReset;

	private long timerRedZone = 0;
	protected boolean playerFinish;
	protected long timerStopPlayer = 0;
	protected long timePlayer = 0;

	boolean zeroSec;
	boolean oneSec;
	boolean twoSec;
	boolean threeSec;

	protected AudioRender audioMotor;

	public AbstractGameScreenState() {
		super();
	}

	/***** Initialize Nifty gui ****/
	@Override
	public void stateAttached(AppStateManager stateManager) {
	}

	@Override
	public void stateDetached(AppStateManager stateManager) {

	}

	@Override
	public void bind(Nifty nifty, Screen screen) {
		super.bind(nifty, screen);
		// nifty.setDebugOptionPanelColors(true);
	}

	@Override
	public void onEndScreen() {
		stateManager.detach(this);
	}

	@Override
	public void onStartScreen() {
	}

	/******* Initialize game ******/
	@Override
	public void initialize(AppStateManager stateManager, Application a) {
		/** init the screen */
		super.initialize(stateManager, a);

		this.rootNode = app.getRootNode();
		this.viewPort = app.getViewPort();
		this.assetManager = app.getAssetManager();
		this.inputManager = app.getInputManager();
	}

	protected void initGame() {
		app.setDisplayStatView(false);

		bulletAppState = new BulletAppState();
		stateManager = app.getStateManager();
		stateManager.attach(bulletAppState);
		runIsOn = false;
		runFinish = false;
		this.isBreaking = false;
		this.needReset = false;
		zeroSec = false;
		oneSec = false;
		twoSec = false;
		threeSec = false;

		initAudio();
		initGround();
		buildPlayer();
		setupKeys();

		// Active skybox
		Spatial sky = SkyFactory.createSky(assetManager,
				"Textures/Skysphere.jpg", true);
		rootNode.attachChild(sky);

		// Enable a chase cam
		chaseCam = new ChaseCamera(app.getCamera(), player.getChassis(),
				inputManager);
		chaseCam.setSmoothMotion(true);

		// Set up light
		DirectionalLight dl = new DirectionalLight();
		dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
		rootNode.addLight(dl);

		AmbientLight al = new AmbientLight();
		al.setColor(ColorRGBA.White.mult(1.3f));
		rootNode.addLight(al);

		// Set up shadow
		pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 3);
		pssmRenderer.setDirection(new Vector3f(0.5f, -0.1f, 0.3f)
				.normalizeLocal()); // light direction
		viewPort.addProcessor(pssmRenderer);

		rootNode.setShadowMode(ShadowMode.Off); // reset all
		player.getNode().setShadowMode(ShadowMode.CastAndReceive); // normal

		// map.setShadowMode(ShadowMode.Receive);
		terrain.setShadowMode(ShadowMode.Receive);

		getPhysicsSpace().addCollisionListener(this);

		digitalTachometer = new DigitalDisplay(nifty, screen,
				"digital_tachometer", 80);
		digitalSpeed = new DigitalDisplay(nifty, screen, "digital_speed", 50);
		digitalGear = new DigitalDisplay(nifty, screen, "digital_gear", 50);
		shiftlight = new ShiftlightLed(nifty, screen, playerCarProperties,
				playerEnginePhysics);
	}

	private void initAudio() {

		// Init audio
		soundStore = SoundStore.getInstance();
		soundStore.setAssetManager(assetManager);

		soundStore.addEngineSound(1000, "Models/Default/1052_P.wav");
		// channels.put(1126, "Models/Default/1126_P.wav");
		// channels.put(1205, "Models/Default/1205_P.wav");
		// channels.put(1289, "Models/Default/1289_P.wav");
		// channels.put(1380, "Models/Default/1380_P.wav");
		// channels.put(1476, "Models/Default/1476_P.wav");
		// channels.put(1579, "Models/Default/1579_P.wav");
		// channels.put(1690, "Models/Default/1690_P.wav");
		// channels.put(1808, "Models/Default/1808_P.wav");
		// channels.put(1935, "Models/Default/1935_P.wav");
		// channels.put(2070, "Models/Default/2070_P.wav");
		// channels.put(2215, "Models/Default/2215_P.wav");
		// channels.put(2370, "Models/Default/2370_P.wav");
		// channels.put(2536, "Models/Default/2536_P.wav");
		soundStore.addEngineSound(2714, "Models/Default/2714_P.wav");
		// channels.put(2904, "Models/Default/2904_P.wav");
		// channels.put(3107, "Models/Default/3107_P.wav");
		// channels.put(3324, "Models/Default/3324_P.wav");
		// channels.put(3557, "Models/Default/3557_P.wav");
		// channels.put(3806, "Models/Default/3806_P.wav");
		// channels.put(4073, "Models/Default/4073_P.wav");
		soundStore.addEngineSound(4358, "Models/Default/4358_P.wav");
		// channels.put(4663, "Models/Default/4663_P.wav");
		// channels.put(4989, "Models/Default/4989_P.wav");
		// channels.put(5338, "Models/Default/5338_P.wav");
		// channels.put(5712, "Models/Default/5712_P.wav");
		// channels.put(6112, "Models/Default/6112_P.wav");
		soundStore.addEngineSound(8540, "Models/Default/6540_P.wav");

		soundStore.addExtraSound("start", "Models/Default/start.wav");
		soundStore.addExtraSound("up", "Models/Default/up.wav");
		soundStore.addExtraSound("lost", "Sound/lost.wav");
		soundStore.addExtraSound("win", "Sound/win.wav");
		soundStore.addExtraSound("start_low", "Sound/start_low.wav");
		soundStore.addExtraSound("start_high", "Sound/start_high.wav");
		soundStore.addExtraSound("burst", "Sound/explosion.wav");

		audioMotor = new AudioRender(rootNode, soundStore);
	}

	private void buildPlayer() {
		playerCarProperties = new BMWM3Properties();

		// Create a vehicle control
		player = new Car(assetManager, playerCarProperties);
		player.setType(CarType.PLAYER);
		player.setDriverName("Player");
		player.getNode().addControl(player);
		player.setPhysicsLocation(new Vector3f(0, 27, 700));

		playerCarProperties = player.getProperties();
		playerEnginePhysics = player.getEnginePhysics();

		rootNode.attachChild(player.getNode());

		getPhysicsSpace().add(player);

	}

	public void initGround() {
		/** 1. Create terrain material and load four textures into it. */
		mat_terrain = new Material(assetManager,
				"Common/MatDefs/Terrain/Terrain.j3md");

		/** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
		mat_terrain.setTexture("Alpha",
				assetManager.loadTexture("Textures/alphamap.png"));

		/** 1.2) Add GRASS texture into the red layer (Tex1). */
		Texture grass = assetManager
				.loadTexture("Textures/Terrain/splat/grass.jpg");
		grass.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex1", grass);
		mat_terrain.setFloat("Tex1Scale", 64f);

		/** 1.3) Add DIRT texture into the green layer (Tex2) */
		Texture dirt = assetManager
				.loadTexture("Textures/Terrain/splat/dirt.jpg");
		dirt.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex2", dirt);
		mat_terrain.setFloat("Tex2Scale", 32f);

		/** 1.4) Add ROAD texture into the blue layer (Tex3) */
		Texture rock = assetManager
				.loadTexture("Textures/Terrain/splat/road.jpg");
		rock.setWrap(WrapMode.Repeat);
		mat_terrain.setTexture("Tex3", rock);
		mat_terrain.setFloat("Tex3Scale", 128f);

		/** 2. Create the height map */
		AbstractHeightMap heightmap = null;
		Texture heightMapImage = assetManager
				.loadTexture("Textures/mountains512.png");

		heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
		heightmap.load();

		/**
		 * 3. We have prepared material and heightmap. Now we create the actual
		 * terrain: 3.1) Create a TerrainQuad and name it "my terrain". 3.2) A
		 * good value for terrain tiles is 64x64 -- so we supply 64+1=65. 3.3)
		 * We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
		 * 3.4) As LOD step scale we supply Vector3f(1,1,1). 3.5) We supply the
		 * prepared heightmap itself.
		 */
		int patchSize = 65;
		terrain = new TerrainQuad("my terrain", patchSize, 513,
				heightmap.getHeightMap());

		/**
		 * 4. We give the terrain its material, position & scale it, and attach
		 * it.
		 */
		terrain.setMaterial(mat_terrain);
		terrain.setLocalTranslation(0, -100, 0);
		terrain.setLocalScale(2f, 1f, 2f);
		rootNode.attachChild(terrain);

		/** 5. The LOD (level of detail) depends on were the camera is: */
		TerrainLodControl control = new TerrainLodControl(terrain,
				app.getCamera());
		terrain.addControl(control);

		// Rendre le terrain physique

		terrain.setLocalScale(3f, 2f, 4f);

		terrainPhys = new RigidBodyControl(0.0f);
		terrain.addControl(terrainPhys);
		bulletAppState.getPhysicsSpace().add(terrainPhys);

		bulletAppState.getPhysicsSpace()
				.setGravity(new Vector3f(0, -19.81f, 0));
		terrainPhys.setFriction(0.5f);

		bulletAppState.getPhysicsSpace().enableDebug(assetManager);
	}

	private void setupKeys() {
		inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_Q));
		inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("GearUp", new KeyTrigger(KeyInput.KEY_Z));
		inputManager.addMapping("GearDown", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("Space", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
		inputManager.addMapping("Mute", new KeyTrigger(KeyInput.KEY_M));
		inputManager.addMapping("GearUp", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("GearDown", new KeyTrigger(KeyInput.KEY_E));

		inputManager.addMapping("GearUp", new KeyTrigger(KeyInput.KEY_UP));
		inputManager.addMapping("GearDown", new KeyTrigger(KeyInput.KEY_DOWN));
		inputManager.addMapping("Throttle", new KeyTrigger(
				KeyInput.KEY_RCONTROL));
		inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_LEFT));
		inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_RIGHT));

		inputManager.addMapping("Menu", new KeyTrigger(KeyInput.KEY_ESCAPE));

		inputManager.addListener(this, "Lefts");
		inputManager.addListener(this, "Rights");
		inputManager.addListener(this, "Ups");
		inputManager.addListener(this, "Downs");
		inputManager.addListener(this, "Space");
		inputManager.addListener(this, "Reset");
		inputManager.addListener(this, "Mute");
		inputManager.addListener(this, "GearUp");
		inputManager.addListener(this, "GearDown");
		inputManager.addListener(this, "Throttle");
	}

	@Override
	public void update(float tpf) {
		int playerRpm = player.getEnginePhysics().getFreeRpm();
		int playerSpeed = (int) Math.abs(player.getCurrentVehicleSpeedKmHour());

		/** Stops 1 second after the finish line */
		if (playerFinish
				&& (System.currentTimeMillis() - timerStopPlayer > 1000)) {
			player.accelerate(0);
			player.setLinearVelocity(Vector3f.ZERO);
		}

		if (runIsOn) {
			playerRpm = player.getEnginePhysics().getRpm();

			playerEnginePhysics.setSpeed(Math.abs(Conversion
					.kmToMiles(playerSpeed)));
			float force = -(float) playerEnginePhysics.getForce() / 5;
			player.accelerate(2, force * 2);
			player.accelerate(3, force * 2);
		} else {
			if (!runFinish) {
				countDown();
			}

			// Baisser le régime moteur à l'arrêt
			playerEnginePhysics.setRpm(playerEnginePhysics.getFreeRpm() - 100);
		}

		// Traiter le cas du sur-régime
		if (playerRpm > (playerCarProperties.getRedline() - 500)) {
			if (!player.getBurstEnabled()) {
				// Déclencher le timer s'il n'est pas activé
				if (timerRedZone == 0) {
					timerRedZone = System.currentTimeMillis();
				} else {
					if (System.currentTimeMillis() - timerRedZone > 3000) {
						triggerBurst(player);
						player.explode();
					}
				}
			}
		} else {
			timerRedZone = 0;
		}

		// Update audio
		if (soudIsActive) {
			player.updateSound(playerRpm);
			app.getListener().setLocation(
					player.getNode().getWorldTranslation());
		}

		// particule_motor.controlBurst();

		digitalTachometer.setText(((Integer) playerRpm).toString());
		digitalSpeed.setText(((Integer) playerSpeed).toString());
		digitalGear.setText(((Integer) playerEnginePhysics.getGear())
				.toString());
		shiftlight.setRpm(playerRpm);
	}

	/**
	 * Displays a countdown
	 */
	private void countDown() {
		/*
		 * long ellapsedTime = System.currentTimeMillis() - countDown;
		 * 
		 * if (ellapsedTime < time) { screen.findElementByName("startTimer")
		 * .getRenderer(TextRenderer.class) .setText( ((Long) ((time -
		 * ellapsedTime + 1000) / 1000)) .toString()); } else if (ellapsedTime
		 * >= time && ellapsedTime < time + 500) {
		 * screen.findElementByName("startTimer")
		 * .getRenderer(TextRenderer.class).setText(""); runIsOn = true;
		 * audio_motor.playStartBeepHigh();
		 * playerEnginePhysics.setRpm(initialRev); startTime =
		 * System.currentTimeMillis(); countDown = 0; }
		 */
		if (countDown != 0) {
			long time = System.currentTimeMillis() - countDown;
			if (time > 5000) {
				if (!zeroSec) {
					audioMotor.playStartBeepHigh();
					zeroSec = true;
				}
				screen.findElementByName("startTimer")
						.getRenderer(TextRenderer.class).setText("");
				runIsOn = true;
				startTime = System.currentTimeMillis();
			} else if (time > 4000) {
				if (!oneSec) {
					audioMotor.playStartBeepLow();
					oneSec = true;
				}
				screen.findElementByName("startTimer")
						.getRenderer(TextRenderer.class).setText("1");
			} else if (time > 3000) {
				if (!twoSec) {
					audioMotor.playStartBeepLow();
					twoSec = true;
				}
				screen.findElementByName("startTimer")
						.getRenderer(TextRenderer.class).setText("2");
			} else if (time > 2000) {
				if (!threeSec) {
					audioMotor.playStartBeepLow();
					threeSec = true;
				}
				screen.findElementByName("startTimer")
						.getRenderer(TextRenderer.class).setText("3");
			}
		}
	}

	/**
	 * Triggers an explosion, occurs when you stay in redline for too long
	 * 
	 * @param vehicule
	 */
	public void triggerBurst(Car vehicule) {
		vehicule.explode();
		playerFinish = true;
		timePlayer = 0;
		timerStopPlayer = System.currentTimeMillis();
	}

	protected void reset() {
		player.setPhysicsLocation(new Vector3f(0, 27, 700));
		player.setPhysicsRotation(new Matrix3f());
		player.setLinearVelocity(Vector3f.ZERO);
		player.setAngularVelocity(Vector3f.ZERO);
		playerEnginePhysics.setGear(1);
		player.resetSuspension();
		player.steer(0);
		audioMotor.playStartSound();

		player.accelerate(0);
		playerEnginePhysics.setSpeed(0);
		playerEnginePhysics.setRpm(1000);

		if (player.getBurstEnabled()) {
			player.removeExplosion();
		}

		timerRedZone = 0;
		timerStopPlayer = 0;
		playerFinish = false;
		runIsOn = false;
		needReset = false;
		runFinish = false;
		startTime = 0;
		countDown = 0;

		threeSec = false;
		twoSec = false;
		oneSec = false;
		zeroSec = false;

		screen.findElementByName("startTimer").getRenderer(TextRenderer.class)
				.setText("Ready ?");
	}

	protected PhysicsSpace getPhysicsSpace() {
		return bulletAppState.getPhysicsSpace();
	}

	public void onAction(String binding, boolean value, float tpf) {
		if (binding.equals("Lefts")) {
			// XXX: Needs analog controller for releasing the wheels too!
			if (!value) {
				player.setSteeringValue(0.f);
				player.steer(player.getSteeringValue());
			}
		} else if (binding.equals("Rights")) {
			if (!value) {
				player.setSteeringValue(0);
				player.steer(0);
			}
		} else if (binding.equals("Space")) {
			if (value) {
				player.brake(700f);
			} else {
				player.brake(0f);
			}
		} else if (binding.equals("Reset")) {
			if (value) {
				System.out.println("Reset");
				needReset = true;
			}
		} else if (binding.equals("GearUp")) {
			if (value) {
				audioMotor.gearUp();
				playerEnginePhysics.incrementGear();
			}
		} else if (binding.equals("GearDown")) {
			if (value) {
				playerEnginePhysics.decrementGear();
			}
		} else if (binding.equals("Menu")) {
			app.gotoStart();
		}
	}

	@Override
	public void onAnalog(String binding, float value, float tpf) {
		if (binding.equals("Throttle")) {
			if (!player.getBurstEnabled()) {
				// Start countdown
				if (countDown == 0) {
					countDown = System.currentTimeMillis();
				}

				playerEnginePhysics
						.setRpm(playerEnginePhysics.getFreeRpm() + 400);
			}
		} else if (binding.equals("Rights")) {
			System.out.println("Value " + value + " tpf: " + tpf);
			float val = player.getSteeringValue();
			val = val - value;
			if (val < -0.5)
				val = -0.5f;
			player.setSteeringValue(val);
			System.out.println("New value " + player.getSteeringValue());
			player.steer(player.getSteeringValue());
		} else if (binding.equals("Lefts")) {
			float val = player.getSteeringValue();
			val = val + value;
			if (val > 0.5)
				val = 0.5f;
			player.setSteeringValue(val);
			player.steer(player.getSteeringValue());
		}
	}

	@Override
	public void collision(PhysicsCollisionEvent event) {
		Car car1 = null;
		Car car2 = null;
		if (event.getObjectA() instanceof Car) {
			car1 = (Car) event.getObjectA();
		}
		if (event.getObjectB() instanceof Car) {
			car2 = (Car) event.getObjectB();
		}

		// Two cars collide
		if (car1 != null && car2 != null) {
			float speed1 = Math.abs(car1.getCurrentVehicleSpeedKmHour());
			float speed2 = Math.abs(car2.getCurrentVehicleSpeedKmHour());
			float appliedImpulse = event.getAppliedImpulse();
			// Impact, reduce friction
			float damageForce = (appliedImpulse - event.getCombinedFriction() / 10) / 10000;

			/*
			 * System.out.println("Collision between " + car1.getType() + " " +
			 * car1.getDriverName() + " and " + car2.getType() + " " +
			 * car2.getDriverName()); System.out.println("Lateral 1 impulse " +
			 * event.getAppliedImpulseLateral1());
			 * System.out.println("Lateral 2 impulse " +
			 * event.getAppliedImpulseLateral2());
			 * System.out.println("Combined friction " +
			 * event.getCombinedFriction()); System.out.println("Force " +
			 * appliedImpulse);
			 */

			Vector3f forward1 = new Vector3f(0, 0, 0).subtract(
					car1.getForwardVector(null)).normalize();
			Vector3f forward2 = new Vector3f(0, 0, 0).subtract(
					car2.getForwardVector(null)).normalize();
			Vector2f f1 = new Vector2f(forward1.x, forward1.z);
			Vector2f f2 = new Vector2f(forward2.x, forward2.z);

			Vector3f position1 = event.getPositionWorldOnA();
			Vector3f position2 = event.getPositionWorldOnB();

			Vector2f pos1 = new Vector2f(position1.x, position1.z);
			Vector2f pos2 = new Vector2f(position2.x, position2.z);

			/*
			 * System.out.println("Position A: " + pos1);
			 * System.out.println("Position B: " + pos2);
			 * System.out.println("Forward " + f1 + " " + f2);
			 */

			float angle = Math.abs(MathTools.orientedAngle(f1, f2));
			// System.out.println("Angle " + angle);

			// Frontal collision
			if (angle >= Math.PI - Math.PI / 4
					&& angle <= Math.PI + Math.PI / 4) {
				System.out.println("Frontal collision " + speed1 + " " + speed2
						+ "  at force " + appliedImpulse);
				float speedPercent1 = speed1 / (speed1 + speed2);
				car1.decreaseLife(speedPercent1 * damageForce);
				car2.decreaseLife((1 - speedPercent1) * damageForce);
			} else {
				// back collision if (angle <= Math.PI / 4)
				// the car in front will have 75% of the damages
				// 25% for the car in back
				/*
				 * System.out.println("Back collision " + speed1 + " " + speed2
				 * + "  at force " + appliedImpulse);
				 * System.out.println("Distance 1" + event.getDistance1());
				 */
				double speedDifferenceDamage = Math.abs(speed2 - speed1)
						* damageForce / 2;
				if (car1.inFront(car2)) {
					car1.decreaseLife(0.75 * speedDifferenceDamage);
					car2.decreaseLife(0.25 * speedDifferenceDamage);
					System.out.println(car1.getType() + " In Front");
				} else {
					car1.decreaseLife(0.25 * speedDifferenceDamage);
					car2.decreaseLife(0.75 * speedDifferenceDamage);
					System.out.println(car2.getType() + " In Front");
				}
			}

			System.out.println(car1.getType() + " life " + car1.getLife());
			System.out.println(car2.getType() + " life " + car2.getLife());

		}
	}
}
