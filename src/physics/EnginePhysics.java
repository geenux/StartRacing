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
	private int rpm;
	private double needBackward = 1;
	private boolean nosActif = false;

	private long rpmTimer = 0;

	/**
	 * Used to know whether the rpm is over the redline.
	 */
	private boolean isBreaking = false;

	private boolean engineBroken = false;

	public EnginePhysics(CarProperties prop) {
		this.p = prop;

		rpm = p.idleRpm;
	}

	/**
	 * The speed must be up to date to get accurate value
	 * 
	 * @return rotation per minute
	 */
	public int getRpm() {

		rpm = (rpm < p.getIdleRpm()) ? p.getIdleRpm() : (int) (p
				.getGearRatio(gear) * speed * 336 * p.getTgr() / p.getTh());
		;
		if (checkRedline()) {
			isBreaking = true;
		} else {
			isBreaking = false;
			if (rpm < p.getIdleRpm()) {
				return p.getIdleRpm();
			}
		}
		return (rpm >= p.getIdleRpm()) ? rpm : p.getIdleRpm();
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
	 * Set the vehicule rpm. Use it as a kick starting when the vehicule is not
	 * moving yet.
	 * 
	 * @param rpm
	 *            The rpm of the engine
	 */
	public void setRpm(int rpm) {
		if (rpm > p.getRedline())
			this.rpm = p.getRedline();
		else if (rpm < p.getIdleRpm())
			this.rpm = p.getIdleRpm();
		else
			this.rpm = rpm;
	}

	/**
	 * Get rpm when the clutch is disengaged
	 * 
	 * @return
	 */
	public int getFreeRpm() {
		/**
		 * When engine is breaking, oscillate rpm a little to simulate engine
		 * failure and get a nice sound ^^
		 */
		checkRedline();
		return rpm;
	}

	public void activeNos()	{
		nosActif =  true;
	}

	public void stopNos()	{
		nosActif = false;
	}
	private boolean checkRedline() {
		int redline = p.getRedline();
		if (rpm >= redline) {
			if (System.currentTimeMillis() - rpmTimer < 100) {
				rpm = redline - 200;
			} else if (System.currentTimeMillis() - rpmTimer < 200) {
				rpm = redline;
			} else {
				rpm = redline;
				rpmTimer = System.currentTimeMillis();
			}
			return true;
		}
		return false;
	}

	/**
	 * Gets the speed of the internal wheel of the engine
	 * 
	 * @return The engine speed
	 */
	public double getEngineSpeed() {
		/**
		 * w = v*G*gk/(2*pi*r)
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
		if (!isBreaking) {
			if (speed != 0) {
				rpm = getRpm();
			} else {
				rpm = getFreeRpm();
			}

			double force = needBackward * p.getTorque(rpm) * p.getTgr() * p.getGearRatio(gear) / p.getTireRadius();
			if (nosActif)	{
				force *= 1.4;
			}
			return force;
		} else {
			return 0;
		}
	}

	public CarProperties getCarProperties() {
		return p;
	}

	public void setBreaking(boolean b) {
		isBreaking = b;
	}

	public void setEngineBroken(boolean state) {
		engineBroken = state;
	}

	public boolean getEngineBroken() {
		return engineBroken;
	}

	public boolean getNosActivity()	{
		return nosActif;
	}

	/**
	 * Set if the car goes backward or forward
	 * @param backward : true if the car goes backward
	 */
	public void setBackward(boolean backward) {
		if (backward)	{
			needBackward = 1;
		}
		else	{
			needBackward = -1;
		}
		
	}
}
