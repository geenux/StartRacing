package physics;

/**
 * EnginePhysics is meant to simulate the workings of a gearbox. It can
 * calculate the force generated by an engine according to various parameters,
 * such as speed and rotations per minute.
 * 
 * @author TANGUY Arnaud
 * 
 */
public class EnginePhysics {
	private CarProperties p;

	/**
	 * Current selected gear (vitesse) 0 : reverse
	 */
	private int gear = 1;
	private double speed = 0;

	private long rpmTimer = 0;

	/**
	 * Used to know whether the rpm is over the redline.
	 */
	private boolean isBreaking = false;

	public EnginePhysics(CarProperties prop) {
		this.p = prop;
	}

	/**
	 * The speed must be up to date to get accurate value
	 * 
	 * @return rotation per minute
	 */
	public int getRpm() {
		int rpm = (int) (p.getGearRatio(gear) * speed * 336 * p.getTgr() / p
				.getTh());
		int redline = p.getRedline();
		if (rpm > redline) {
			isBreaking = true;
			/**
			 * When engine is breaking, oscillate rpm a little to simulate
			 * engine failure and get a nice sound ^^
			 */
			if (System.currentTimeMillis() - rpmTimer < 50) {
				rpm = redline - 100;
			} else if (System.currentTimeMillis() - rpmTimer < 100) {
				rpm = redline;
			} else {
				rpm = redline;
				rpmTimer = System.currentTimeMillis();
			}
		} else {
			isBreaking = false;
			if (rpm < p.getIdleRpm()) {
				return p.getIdleRpm();
			}
		}
		return rpm;
	}

	/**
	 * 
	 * @param rpm
	 *            rotation per minute
	 * @return miles per hour
	 */
	public double getMph(double rpm) {
		return p.getTh() * rpm / (p.getGearRatio(gear) * 336 * p.getTgr());
	}

	public void setGear(int gear) {
		if (gear >= 0)
			this.gear = gear;
	}

	public int getGear() {
		return gear;
	}

	/**
	 * Go to the next gear, if it exists, otherwise do nothing
	 */
	public void incrementGear() {
		if (gear < p.getNbGears()) {
			this.gear++;
		}
	}

	/**
	 * Go to the previous gear if it exists, otherwise do nothing
	 */
	public void decrementGear() {
		if (gear > 1) {
			this.gear--;
		}
	}

	/**
	 * Sets the vehicule speed.
	 * 
	 * @param speed
	 *            The speed in mph
	 */
	public void setSpeed(double speed) {
		this.speed = speed;
	}

	/**
	 * Gets the speed of the internal wheel of the engine
	 * 
	 * @return The engine speed
	 */
	public double getEngineSpeed() {
		/**
		 *  w = v*G*gk/(2*pi*r)
		 */
		return speed * p.getTgr() * p.getGearRatio(gear)
				/ (2 * Math.PI * p.getTireRadius());
	}

	/**
	 * Force generated by the engine at a given velocity with a given gear ratio
	 * To have an accurate result, the speed and gear ratio must be up to date,
	 * see the setSpeed and setGear function
	 * 
	 * @return The force generated (in Newtons)
	 */
	public double getForce() {
		if (!isBreaking)
			return p.getTorque(getRpm())
					* p.getTgr() * p.getGearRatio(gear) / p.getTireRadius();
		else
			return 0;
	}

	public CarProperties getCarProperties() {
		return p;
	}
}
