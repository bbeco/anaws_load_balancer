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
	/** IPV6 address */
	protected String 		ID;
	
	protected Battery		battery;
	protected Accelerometer acc;
	protected Leds 			led;
	protected Temperature 	temp;
	protected Toggle 		tog;
	
	/* request counter*/
	protected int			req;
	
	/**
	 * Constructor for the device objects. It needs the ipv6 of the device,
	 * the uri describing the base address of the resources and an array that
	 * describes the available resources.
	 * 
	 * @param ID IPv6 Address
	 * @param uri resource uri
	 * @param resources This is an array describing the recources available
	 * in this device. This array's elements are set if the corresponding 
	 * resource has been found (element from 0 to 3 represent accelerometer,
	 * temperature sensor, leds, and toggle respectively).
	 */
	public Device (String ID, String uri, boolean [] resources){
		battery = new Battery(uri);
		this.ID = ID;
		req = 0;
		
		if(resources[0]) {//true if accelerometer found
			acc = new Accelerometer(uri);
		} else {
			acc = null;
		}
		
		if(resources[1]) {//true if temperature found
			temp = new Temperature(uri);
		} else {
			temp = null;
		}
		
		if(resources[2]) {//true if leds found
			led = new Leds(uri);
		} else {
			led = null;
		}
		
		if(resources[3]) {//true if toggle found
			tog = new Toggle(uri);
		} else {
			tog = null;
		}
	}	
	
	public Battery getBattery(){
		return battery;
	}
	
	public Temperature getTemperature(){
		return temp;
	}
	
	public Toggle getToggle(){
		return tog;
	}
	
	public Leds getLeds(){
		return led;
	}
	
	public Accelerometer getAccelerometer(){
		return acc;
	}
	
	public String getID(){
		return ID;
	}
	
	/**
	 * This is a get method that returns the number of request that can still be
	 *  performed before needing to check the battery status again.
	 * @return The number of request left.
	 */
	public int getLeftRequests(){
		return req;
	}
	
	/**
	 * Decrease the number of request available for this device before a battery
	 *  status check. It is called when a request is sent to this device.
	 */
	public void decrementLeftRequests(){
		req--;
	}
	
	/**
	 * Setter method for the number of request left before checking the battery 
	 * again.
	 * @param r The number of request to be set.
	 */
	public void setLeftRequests(int r){
		req = r;
	}
	
	/**
	 * This method returns true if the resource is available, 
	 * false otherwise.
	 * 
	 * @param resource This string can be any among "Accelerometer", 
	 * "Temperature", "Leds" and "Toggle" 
	 * @return true if the given resource is available on this device
	 * false otherwise.
	 */
	public boolean deviceHasThisResource(String resource){
		/* In JDK 7 you can use switch for strings.
		 * (It uses string.equals() like in a if-else-if statement)
		 */
	    switch (resource){
	    	case "Accelerometer": return (acc != null) ? true : false;
	    	
	    	case "Temperature": return (temp != null) ? true : false;
	   
	    	case "Leds": return (led != null) ? true : false;
	    
	    	case "Toggle": return (tog != null) ? true : false;
	    
	    	default: return false;
	    }	
	}
}
