package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Battery;
import it.ing.unipi.anaws.resources.Leds;

public class LedsDevice {
	
	Leds 	led;
	Battery	battery;
	
	public LedsDevice(String uri){
		led = new Leds(uri);
		battery = new Battery(uri);
	}
	
	public int LedsPost(String color, String mode)	{
		led.Post(color, mode);
		return led.ok;
	}
	
	public int LedsPut(String color, String mode)	{
		led.Put(color, mode);
		return led.ok;
	}
	
	public String BatteryGet()	{
		battery.Get();
		return battery.charge;
	}
}
