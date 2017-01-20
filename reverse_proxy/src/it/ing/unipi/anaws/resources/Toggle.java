package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Toggle extends BaseResource {
	
	/** This is the path this class is looking for when checking the Temperature resource */
	private static final String toggleResourcePath = "/togg";
	
	protected int 		ok; /* 1 success, -1 no response*/
	
	public Toggle(String uri)
	{
		super(uri + toggleResourcePath);
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
