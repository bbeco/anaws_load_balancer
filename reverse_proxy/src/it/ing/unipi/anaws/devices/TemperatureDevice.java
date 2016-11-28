package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Temperature;

public class TemperatureDevice extends Device{
	
	Temperature temp;
	
	public TemperatureDevice(String ID, String uri){
		super(ID, uri);
		temp = new Temperature(uri);
	}
	
	public String TempGet()	{
		temp.Get();
		return temp.temp;
	}
}
