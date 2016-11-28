package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Leds;

public class LedsDevice extends Device {
	
	Leds 	led;

	public LedsDevice(String ID, String uri){
		super(ID, uri);
		led = new Leds(uri);
	}
	
	public int LedsPost(String color, String mode)	{
		led.Post(color, mode);
		return led.ok;
	}
	
	public int LedsPut(String color, String mode)	{
		led.Put(color, mode);
		return led.ok;
	}
}
