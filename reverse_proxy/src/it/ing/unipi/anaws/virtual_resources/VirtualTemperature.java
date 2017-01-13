package it.ing.unipi.anaws.virtual_resources;

import java.util.ArrayList;

import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import it.ing.unipi.anaws.devices.LedsDevice;
import it.ing.unipi.anaws.devices.TemperatureDevice;

/*
 * Definition of the Temperature Resource
 */
public class VirtualTemperature extends VirtualResource<TemperatureDevice> {
    
    public VirtualTemperature(ArrayList<TemperatureDevice> temp) {
        // set resource identifier
        super("temperature", "Temperature Resource", temp);
        type = "Temperature";
    }

    @Override
    public void handleGET(CoapExchange exchange) {
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	System.out.println("Respond to a Temperature GET");
    	TemperatureDevice temp_dev = chooseDevice();
    	if(temp_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		return;
    	}
    	String res = temp_dev.TempGet();
    	if(!res.equals("")) {
    		// respond to the request
    		exchange.respond(res);
    	} else {
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	}
    }
    
    @Override
    public void handlePOST(CoapExchange exchange) {
    	System.out.println("Respond to a Temperature POST");
        String opt = exchange.getRequestText();
        
    	TemperatureDevice temp_dev = chooseDevice();
    	
    	/* No server available for this resource */
    	if(temp_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		return;
    	}
    	
    	int tmp = temp_dev.TempPost(opt);
 
    	/* Ok, the request can be satisfied */
    	if(tmp == 1){
    		exchange.respond(ResponseCode.CHANGED);
    	} else if (tmp == -1){ /* Some messages have been lost or the server ran out of battery */
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	} else{ /* The server experienced some kind of error */
    		exchange.respond(ResponseCode.BAD_REQUEST);
    	}
    }
    
    @Override
    public void handlePUT(CoapExchange exchange) {
    	System.out.println("Respond to a Temperature PUT");
    	String opt = exchange.getRequestText();
    	
    	TemperatureDevice temp_dev = chooseDevice();
    	
    	if(temp_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		return;
    	}
    	
      	int tmp = temp_dev.TempPut(opt);
      	if(tmp == 1) {
    		exchange.respond(ResponseCode.CHANGED);
    	} else if (tmp == -1) {
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	} else{
    		exchange.respond(ResponseCode.BAD_REQUEST);
    	}
    }
}
