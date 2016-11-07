package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Battery;
import it.ing.unipi.anaws.resources.Temperature;

public class TemperatureDevice {
	
	Temperature temp;
	Battery		battery;
	
	public TemperatureDevice(String uri){
		temp = new Temperature(uri);
		battery = new Battery(uri);
	}
	
	public String TempGet()	{
		temp.Get();
		return temp.temp;
	}
	
	public String BatteryGet()	{
		battery.Get();
		return battery.charge;
	}
}
