package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Accelerometer;

public class AccelerometerDevice extends Device{
	
	public Accelerometer 	acc;

	public AccelerometerDevice(String ID, String uri){
		super(ID , uri);
		acc = new Accelerometer(uri);
	}
			
	public String AccGet()	{
		//super.busy = true;
		acc.Get();
		//super.busy = false;
		return acc.acc;
	}
}
