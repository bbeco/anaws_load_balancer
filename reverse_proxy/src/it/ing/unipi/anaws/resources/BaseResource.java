package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;

public class BaseResource {
	
	CoapClient 			client;
	String 				uri;
	CoapResponse		re;
	
	public BaseResource (String uri){
		this.uri = uri;
		this.client = new CoapClient();
		client.setURI(uri);
		client.setTimeout(8000);
	}
}
