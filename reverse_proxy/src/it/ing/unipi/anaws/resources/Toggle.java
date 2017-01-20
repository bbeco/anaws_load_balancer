package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Toggle extends BaseResource {
	
	public int 		ok; /* 1 success, -1 no response*/
	
	public Toggle(String uri)
	{
		super(uri + "/togg");
	}
	
	public int Post()
	{
		re = client.post("", MediaTypeRegistry.TEXT_PLAIN);
		if(re != null)
			ok = 1;
		else
			ok = -1;
		
		return ok;
	}
	
	public int Put()
	{
		client.put("", MediaTypeRegistry.TEXT_PLAIN);
		if(re != null)
			ok = 1;
		else
			ok = -1;
		
		return ok;
	}
}
