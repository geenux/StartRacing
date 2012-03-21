package physics;

public class EnginePhysics {
	private CarProperties p;
	
	/**
	 * Current selected gear (vitesse)
	 * 0 : reverse
	 */
	private int gear=1;
	private double speed = 0;
	
	public EnginePhysics(CarProperties prop) {
		this.p = prop;
	}
	
	/**
	 * 
	 * @param d
	 * 		miles per hour
	 * @return
	 * 		rotation per minute
	 */
	public double getRpm(double d) {
		double rpm = p.getGearRatio(gear)*d*336*p.getTgr()/p.getTh();
		return (rpm<=p.getIdleRpm()) ? p.getIdleRpm() : rpm; 
	}
	
	/**
	 * 
	 * @param rpm
	 * 		rotation per minute
	 * @return
	 * 		miles per hour
	 */
	public double getMph(double rpm) {
		return p.getTh()*rpm/(p.getGearRatio(gear)*336*p.getTgr());
	}
	
	public void setGear(int gear) {
		if(gear>=0)	this.gear = gear;
	}
	
	public int getGear() {
		return gear;
	}

	public void incrementGear() {
		if(gear < p.getNbGears()) {
			this.gear++;
		}
	}
	
	public void decrementGear() {
		if(gear>1) {
			this.gear--;
		}
	}
	
	public void setSpeed(double speed) {
		this.speed = speed;
	}
	
	public double getEngineSpeed() {
		/**v =  2πrω/(G*gk) 
		 * w = v*G*gk/(2*pi*r)*/
		return speed*p.getTgr()*p.getGearRatio(gear)/(2*Math.PI*p.getTireRadius());
	}
	
	/**
	 * Force generated by the engine
	 * @return
	 * 		The force (in Newtons)
	 */
	public double getForce() {
		/**v = 2πrω/Ggk, where
v	=	velocity of the car (ms-1)
r	=	radius of tire (m)
ω	=	engine speed in rotations per second (s-1)
G	=	final drive ratio (no unit)
gk	=	k-th gear ratio (no unit) 

		Γ(ω)G*gk					1
F	=	--------		−	_ crrmg	−	--	cdAρv2,	where
		r					    2 
**/
		System.out.println("Engine speed: " + getEngineSpeed());
		return p.getTorque(getEngineSpeed())*p.getTgr()*p.getGearRatio(gear)/p.getTireRadius();
	}
}
