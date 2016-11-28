package it.ing.unipi.anaws.devices;

import it.ing.unipi.anaws.resources.Toggle;

public class ToggleDevice extends Device {
	
	Toggle 	toggle;
	
	public ToggleDevice(String ID, String uri){
		super(ID, uri);
		toggle = new Toggle(uri);
	}
	
	public int TogglePost()	{
		toggle.Post();
		return toggle.ok;
	}
	
	public int TogglePut()	{
		toggle.Put();
		return toggle.ok;
	}
}
