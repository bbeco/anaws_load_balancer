package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Leds {
	
	public int 		ok; /* 1 success, -1 no response, 0 bad options */
	CoapClient 		client;
	String 			myUri;
	CoapResponse	re;
	
	public Leds(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/led";
	}
	
	public void Post(String color, String mode)
	{
		client.setURI(myUri + "?color=" + color);
		re = client.post("mode=" + mode, MediaTypeRegistry.TEXT_PLAIN);
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
	}
	
	public void Put(String color, String mode)
	{
		client.setURI(myUri + "?color=" + color);
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
	}
}
