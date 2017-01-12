package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Temperature {
	
	public String 		temp; //"" no response
	CoapClient 			client;
	String 				myUri;
	CoapResponse		re;
	
	public Temperature(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/temp";
		client.setURI(myUri);
		client.setTimeout(8000);
	}
	
	public void Get()
	{
		re = client.get();
		if(re != null){
			if (re.getCode() == ResponseCode.CONTENT){				
				temp = re.getResponseText();
			}
		}
		else{
			temp = "";
		}
	}
}
