package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

/**
 * The classes that belong to this package describe some specific
 * resources. Each class provides methods to submit requests correctly.
 */
public class Accelerometer extends DeviceResource{
	
	public Accelerometer(String uri) {
		super(uri + "/acc");
		client.setURI(myUri);
		client.setTimeout(8000);
	}
	
	/**
	 * This method performs a GET request on the accelerometer located at 
	 * the stored URI and update the acc attributes with the response.
	 * If some errors have occurred, acc is set to ""
	 */
	@Override
	public CoapResponse get() {		
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

