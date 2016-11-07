package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Temperature {
	
	public String 		temp;
	CoapClient 			client;
	String 				myUri;
	

	CoapHandler ch = new CoapHandler(){  /* GET Handler */
		@Override
		public void onLoad(CoapResponse re) 
		{
		
			if (re.getCode() == ResponseCode.CONTENT)
			{				
				temp = re.getResponseText();
			}
		}

		@Override
		public void onError() 
		{
			System.out.println("Temperature GET failed");
		}
	};
	
	public Temperature(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/temperature";
		client.setURI(myUri);
	}
	
	public void Get()
	{
		client.get(ch);
	}
}
