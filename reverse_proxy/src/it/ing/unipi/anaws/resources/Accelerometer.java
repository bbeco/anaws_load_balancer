package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Accelerometer {
	
	public String 		acc;//"" no response
	CoapClient 			client;
	String 				myUri;
	CoapResponse		re;
	
	public Accelerometer(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/acc";
		client.setURI(myUri);
		client.setTimeout(10000);
	}
	
	public void Get()
	{		
		re = client.get();
		if(re != null){
			if (re.getCode() == ResponseCode.CONTENT){				
				acc = re.getResponseText();
			}
		}
		else{
			acc = ""; //no response received
		}
	}
}

