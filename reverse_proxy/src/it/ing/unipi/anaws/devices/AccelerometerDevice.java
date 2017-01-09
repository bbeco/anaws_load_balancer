package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Accelerometer;

/** This class describes an accelerometer device that is 
 * an Erbium real server with an exposed accelerometer resource.
 *
 */
public class AccelerometerDevice extends Device {
	
	public Accelerometer 	acc;

	public AccelerometerDevice(String ID, String uri){
		super(ID , uri);
		acc = new Accelerometer(uri);
	}
	
	/** Performs a new GET request on the accelerometer owned by 
	 * the device represented by this class instance.
	 * 
	 * @return a string that represents the acceleration vector
	 * or "" if some errors have occurred.
	 */
	public String AccGet()	{
		acc.Get();
		return acc.acc;
	}
}
