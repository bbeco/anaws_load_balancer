package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Battery {
	
	
	public int  	charge;//-1 no response
	CoapClient 			client;
	String 				myUri;
	CoapResponse		re;

	public Battery(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/batt";
		client.setURI(myUri);
		charge = -1;
	}
	
	public void Get()
	{
		re = client.get();
		if(re != null && re.getCode() == ResponseCode.CONTENT){				
			charge = Integer.parseInt(re.getResponseText());
		} else{
			charge = -1;
		}
	}
}
