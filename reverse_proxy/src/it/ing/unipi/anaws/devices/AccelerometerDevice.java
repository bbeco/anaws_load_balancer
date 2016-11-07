package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Accelerometer;
import it.ing.unipi.anaws.resources.Battery;

public class AccelerometerDevice {
	
	Accelerometer 	acc;
	Battery			battery;
	
	public AccelerometerDevice(String uri){
		acc = new Accelerometer(uri);
		battery = new Battery(uri);
	}
	
	public String TempGet()	{
		acc.Get();
		return acc.acc;
	}
	
	public String BatteryGet()	{
		battery.Get();
		return battery.charge;
	}
}
