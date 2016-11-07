package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Accelerometer {
	
	public String 		acc;
	CoapClient 			client;
	String 				myUri;
	

	CoapHandler ch = new CoapHandler(){  /* GET Handler */
		@Override
		public void onLoad(CoapResponse re) 
		{
			
			if (re.getCode() == ResponseCode.CONTENT)
			{				
				acc = re.getResponseText();
			}
		}

		@Override
		public void onError() 
		{
			System.out.println("Accelerometer GET failed");
		}
	};
	
	public Accelerometer(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/accelerometer";
		client.setURI(myUri);
	}
	
	public void Get()
	{
		client.get(ch);
	}
}
