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
	
	protected String 		ID; //IPV6 address
	
	protected Battery		battery;
	protected Accelerometer acc;
	protected Leds 			led;
	protected Temperature 	temp;
	protected Toggle 		tog;
	
	/* request counter*/
	protected int			req;
	
	/**
	 *
	 * @param ID IPv6 Address
	 * @param uri resource uri
	 */
	public Device (String ID, String uri, boolean [] resources){
		battery = new Battery(uri);
		this.ID = ID;
		req = 0;
		
		if(resources[0])//true if accelerometer found
			acc = new Accelerometer(uri);
		
		if(resources[1])//true if temperature found
			temp = new Temperature(uri);
		
		if(resources[2])//true if leds found
			led = new Leds(uri);
		
		if(resources[3])//true if toggle found
			tog = new Toggle(uri);
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
	
	public int getLeftRequests(){
		return req;
	}
	
	public void decrementLeftRequests(){
		req--;
	}
	
	public void setLeftRequests(int r){
		req = r;
	}
	
	public boolean deviceHasThisResource(int resType){
	    switch (resType){
	    	case 0: return (acc != null) ? true : false;
	    	
	    	case 1: return (temp != null) ? true : false;
	   
	    	case 2: return (led != null) ? true : false;
	    
	    	case 3: return (tog != null) ? true : false;
	    
	    	default: return false;
	    }	
	}
}
