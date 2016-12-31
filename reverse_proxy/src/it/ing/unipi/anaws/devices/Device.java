package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Battery;

public class Device{
	
	public Battery	battery;
	public String 	ID; //IPV6 address
	
	/* counter for request */
	public int		req;
	
	public Device (String ID, String uri){
		battery = new Battery(uri);
		this.ID = ID;
		req = 0;
	}
	
	public int BatteryGet()	{
		battery.Get();
		
		return battery.charge;
	}
}
