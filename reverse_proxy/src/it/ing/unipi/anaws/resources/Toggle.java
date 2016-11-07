package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class Toggle {
	
	public int 	ok; /* 1 success, -1 failed */
	CoapClient 	client;
	String 		myUri;
	

	CoapHandler ch = new CoapHandler(){  /* PUT/POST Handler */
		@Override
		public void onLoad(CoapResponse re) 
		{
			ok = 1;
		}

		@Override
		public void onError() 
		{
			System.out.println("Toggle PUT/POST failed");
			ok = -1;
		}
	};
	
	public Toggle(String uri)
	{
		this.client = new CoapClient();
		myUri = uri + "/toggle";
		client.setURI(myUri);
	}
	
	public void Post()
	{
		client.post(ch, "", MediaTypeRegistry.TEXT_PLAIN);
	}
	
	public void Put()
	{
		client.put(ch, "", MediaTypeRegistry.TEXT_PLAIN);
	}
}
