package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Battery;

public class Device{
	
	public Battery	battery;
	public String 	ID; //IPV6 address
	public boolean 	busy;
	
	public Device (String ID, String uri){
		battery = new Battery(uri);
		this.ID = ID;
	}
	
	public int BatteryGet()	{
		battery.Get();
		return battery.charge;
	}
}
