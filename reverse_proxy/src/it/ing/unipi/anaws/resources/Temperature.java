package it.ing.unipi.anaws.resources;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;

public class Temperature extends BaseResource {
	
	/** This is the path this class is looking for when checking the Temperature resource */
	private static final String temperatureResourcePath = "/temp";
	
	protected String 		temp; //"" no response
	protected int 			ok;  /* 1 success, -1 no response, 0 bad options */
	
	public Temperature(String uri){
		super(uri + temperatureResourcePath);
	}

	public String Get()
	{
		re = client.get();
		if(re != null){
			if (re.getCode() == ResponseCode.CONTENT){				
				temp = re.getResponseText();
			}
		}
		else{
			temp = "";
		}
		
		return temp;
	}
	
	public int Post(String value)
	{
		re = client.post("value=" + value, MediaTypeRegistry.TEXT_PLAIN);
		if(re != null){
			if (re.getCode() == ResponseCode.BAD_REQUEST){	
				ok = 0;
			}
			else{
				ok = 1;
			}
		}
		else{
			ok = -1;
		}
		
		return ok;
	}
	
	public int Put(String value)
	{
		client.put("value=" + value, MediaTypeRegistry.TEXT_PLAIN);
		if(re != null){
			if (re.getCode() == ResponseCode.BAD_REQUEST){	
				ok = 0;
			}
			else{
				ok = 1;
			}
		}
		else{
			ok = -1;
		}
		
		return ok;
	}
}
