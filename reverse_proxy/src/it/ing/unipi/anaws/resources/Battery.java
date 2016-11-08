package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Battery {
	
	
	public String 		charge;//""no response
	CoapClient 			client;
	String 				myUri;
	CoapResponse		re;

	public Battery(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/battery";
		client.setURI(myUri);
	}
	
	public void Get()
	{
		re = client.get();
		if(re != null){
			if (re.getCode() == ResponseCode.CONTENT){				
				charge = re.getResponseText();
			}
		}
		else{
			charge = "";
		}
	}
}
