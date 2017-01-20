package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

/**
 * The classes that belong to this package describe some specific
 * resources. Each class provides methods to submit requests correctly.
 */
public class Accelerometer extends BaseResource{
	
	/** This is the path this class is looking for when checking the accelerometer resource */
	private static final String accelerometerResourcePath = "/acc";
	
	protected String 		acc; //"" no response

	
	public Accelerometer(String uri) {
		super(uri + accelerometerResourcePath);
	}
	
	/**
	 * This method performs a GET request on the accelerometer located at 
	 * the stored URI and update the acc attributes with the response.
	 * If some errors have occurred, acc is set to ""
	 */
	public String Get() {		
		re = client.get();
		if (re != null) {
			if (re.getCode() == ResponseCode.CONTENT){				
				acc = re.getResponseText();
			}
		} else {
			acc = ""; //no response received
		}
		
		return acc;
	}
}

