package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Battery;

/** Base class for every possible device
 * The set of classes AccelerometerDevice, LedsDevice, etc. represents 
 * all the possible server type for our network environment.
 * Each of them describes a different resource configuration.
 * Every device owns a battery resource and this base class provides the methods
 * to access this special resource.
 */
public class Device{
	
	public Battery	battery;
	public String 	ID; //IPV6 address
	
	/* request counter*/
	public int		req;
	
	public Device (String ID, String uri){
		battery = new Battery(uri);
		this.ID = ID;
		req = 0;
	}
	
	/**
	 * This method performs a GET request on the battery
	 * resource exposed by the Erbium server provided by the
	 * mote associated to this class instance. It then
	 * returns the updated battery status.
	 * @return The battery charge level or -1 if some error occurred.
	 */
	public int BatteryGet()	{
		battery.Get();
		
		return battery.charge;
	}
}
