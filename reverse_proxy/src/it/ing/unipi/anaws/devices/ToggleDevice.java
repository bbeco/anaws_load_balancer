package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Battery;
import it.ing.unipi.anaws.resources.Toggle;

public class ToggleDevice {
	
	Toggle 	toggle;
	Battery	battery;
	
	public ToggleDevice(String uri){
		toggle = new Toggle(uri);
		battery = new Battery(uri);
	}
	
	public int LedsPost()	{
		toggle.Post();
		return toggle.ok;
	}
	
	public int LedsPut()	{
		toggle.Put();
		return toggle.ok;
	}
	
	public String BatteryGet()	{
		battery.Get();
		return battery.charge;
	}
}
