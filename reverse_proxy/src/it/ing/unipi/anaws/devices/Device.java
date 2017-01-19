package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Accelerometer;
import it.ing.unipi.anaws.resources.Battery;
import it.ing.unipi.anaws.resources.Leds;
import it.ing.unipi.anaws.resources.Temperature;
import it.ing.unipi.anaws.resources.Toggle;

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
	
	/* TODO sostituire questo con un array unica */
	protected Accelerometer acc;
	protected Leds led;
	protected Temperature temp;
	protected Toggle tog;
	
	/* request counter*/
	public int		req;
	
	/**
	 *
	 * @param ID IPv6 Address
	 * @param uri resource uri
	 */
	public Device (String ID, String uri){
		battery = new Battery(uri);
		this.ID = ID;
		req = 0;
		
		acc = null;
		led = null;
		temp = null;
		tog = null;
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
