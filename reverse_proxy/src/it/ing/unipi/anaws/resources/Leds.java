package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Leds {
	
	public int 		ok; /* 1 success, -1 failed, 0 bad options */
	CoapClient 		client;
	String 			myUri;
	

	CoapHandler ch = new CoapHandler(){  /* PUT/POST Handler */
		@Override
		public void onLoad(CoapResponse re) 
		{
			if (re.getCode() == ResponseCode.BAD_OPTION)
			{				
				ok = 0;
			}
			else{
				ok = 1;
			}
		}

		@Override
		public void onError() 
		{
			System.out.println("Leds PUT/POST failed");
			ok = -1;
		}
	};
	
	public Leds(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/leds";
	}
	
	public void Post(String color, String mode)
	{
		client.setURI(myUri + "?color=" + color);
		client.post(ch, "mode=" + mode, MediaTypeRegistry.TEXT_PLAIN);
	}
	
	public void Put(String color, String mode)
	{
		client.setURI(myUri + "?color=" + color);
		client.put(ch, "mode=" + mode, MediaTypeRegistry.TEXT_PLAIN);
	}
}
