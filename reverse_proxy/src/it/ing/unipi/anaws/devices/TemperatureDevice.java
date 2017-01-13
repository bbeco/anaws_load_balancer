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
	
	public int TempPost(String value)	{
		temp.Post(value);
		return temp.ok;
	}
	
	public int TempPut(String value)	{
		temp.Put(value);
		return temp.ok;
	}
}
