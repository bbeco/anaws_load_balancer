package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Battery extends BaseResource {
	
	/** This is the path this class is looking for when checking the battery resource */
	private static final String batteryResourcePath = "/batt";
	
	/** This is the battery charge value.
	 * If this value is equal to -1, then we encountered an error
	 * while contacting the battery resource.
	 */
	protected int  		charge;
	
	public Battery(String uri) {
		super(uri + batteryResourcePath);
		charge = -1;
	}
	
	/** This function request the battery status to the URI 
	 * that has been set with the constructor and put its 
	 * value in the charge variable. If some error occurred charge 
	 * is set to -1
	 */
	public int Get() {
		re = client.get();
		if(re != null && re.getCode() == ResponseCode.CONTENT){				
			charge = Integer.parseInt(re.getResponseText());
		} else{
			charge = -1;
		}
		
		return charge;
	}
	
	public int getCharge(){
		return charge;
	}
}
