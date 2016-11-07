package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Battery {
	
	
	public String 		charge;
	CoapClient 			client;
	String 				myUri;
	

	CoapHandler ch = new CoapHandler(){  /* GET Handler */
		@Override
		public void onLoad(CoapResponse re) 
		{
			if (re.getCode() == ResponseCode.CONTENT)
			{				
				charge = re.getResponseText();
			}
		}

		@Override
		public void onError() 
		{
			System.out.println("Battery GET failed");
		}
	};
	
	public Battery(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/battery";
		client.setURI(myUri);
	}
	
	public void Get()
	{
		client.get(ch);
	}
}
