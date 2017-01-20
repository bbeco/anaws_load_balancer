package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Leds extends BaseResource {
	
	/** This is the path this class is looking for when checking the Leds resource */
	private static final String ledsResourcePath = "/led";
	
	public int 		ok; /* 1 success, -1 no response, 0 bad options */
	
	public Leds(String uri){
		super(uri + ledsResourcePath);
	}
	
	public int Post(String color, String mode)
	{
		client.setURI(uri + "?color=" + color);
		re = client.post("mode=" + mode, MediaTypeRegistry.TEXT_PLAIN);
		if(re != null){
			if (re.getCode() == ResponseCode.BAD_REQUEST){	
				ok = 0;
			} else {
				ok = 1;
			}
		} else {
			ok = -1;
		}
		
		return ok;
	}
	
	public int Put(String color, String mode)
	{
		client.setURI(uri + "?color=" + color);
		re = client.put("mode=" + mode, MediaTypeRegistry.TEXT_PLAIN);
		if(re != null){
			if (re.getCode() == ResponseCode.BAD_REQUEST){	
				ok = 0;
			}
			else{
				ok = 1;
			}
		}
		else{
			ok = -1;
		}
		
		return ok;
	}
}
