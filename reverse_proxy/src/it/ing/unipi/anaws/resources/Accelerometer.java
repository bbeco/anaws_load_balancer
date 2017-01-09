package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

/**
 * The classes that belong to this package describe some specific
 * resources. Each class provides methods to submit requests correctly.
 */
public class Accelerometer {
	
	/** This is the last value read for this accelerometer.
	 * If it is equal to "" after the Get has been called,
	 * then an error occurred.
	 */
	public String 		acc;//"" no response
	CoapClient 			client;
	String 				myUri;
	CoapResponse		re;
	
	public Accelerometer(String uri) {
		this.client = new CoapClient();
		myUri = uri + "/acc";
		client.setURI(myUri);
		client.setTimeout(10000);
	}
	
	/**
	 * This method performs a GET request on the accelerometer located at 
	 * the stored URI and update the acc attributes with the response.
	 * If some errors have occurred, acc is set to ""
	 */
	public void Get() {		
		re = client.get();
		if (re != null) {
			if (re.getCode() == ResponseCode.CONTENT){				
				acc = re.getResponseText();
			}
		} else {
			acc = ""; //no response received
		}
	}
}

