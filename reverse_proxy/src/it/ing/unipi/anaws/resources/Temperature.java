package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Temperature {
	
	public String 		temp; //"" no response
	CoapClient 			client;
	String 				myUri;
	CoapResponse		re;
	public int 			ok; /* 1 success, -1 no response, 0 bad options */
	
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
	
	public void Post(String value)
	{
		re = client.post("value=" + value, MediaTypeRegistry.TEXT_PLAIN);
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
	
	public void Put(String value)
	{
		client.put("value=" + value, MediaTypeRegistry.TEXT_PLAIN);
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
