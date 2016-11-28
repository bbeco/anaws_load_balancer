package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Toggle {
	
	public int 		ok; /* 1 success, -1 no response*/
	CoapClient 		client;
	String 			myUri;
	CoapResponse 	re;
	
	public Toggle(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/togg";
		client.setURI(myUri);
	}
	
	public void Post()
	{
		re = client.post("", MediaTypeRegistry.TEXT_PLAIN);
		if(re != null)
			ok = 1;
		else{
			ok = -1;
		}
	}
	
	public void Put()
	{
		client.put("", MediaTypeRegistry.TEXT_PLAIN);
		if(re != null)
			ok = 1;
		else
			ok = -1;
	}
}
