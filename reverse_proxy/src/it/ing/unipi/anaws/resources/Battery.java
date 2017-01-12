package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Battery {
	/** This is the path this class is looking for when checking the battery resource */
	private static final String batteryResourcePath = "/batt";
	
	/** This is the battery charge value.
	 * If this value is equal to -1, then we encountered an error
	 * while contacting the battery resource.
	 */
	public int  		charge;
	CoapClient 			client;
	String 				myUri;
	CoapResponse		re;

	public Battery(String uri) {
		this.client = new CoapClient();
		myUri = uri + batteryResourcePath;
		client.setURI(myUri);
		client.setTimeout(16000);
		charge = -1;
	}
	
	/** This function request the battery status to the URI 
	 * that has been set with the constructor and put its 
	 * value in the charge variable. If some error occurred charge 
	 * is set to -1
	 */
	public void Get() {
		re = client.get();
		if(re != null && re.getCode() == ResponseCode.CONTENT){				
			charge = Integer.parseInt(re.getResponseText());
		} else{
			charge = -1;
		}
	}
}
