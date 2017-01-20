package it.ing.unipi.anaws.virtual_resources;

import java.util.ArrayList;

import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import it.ing.unipi.anaws.devices.Device;

/*
 * Definition of the Temperature Resource
 */
public class VirtualTemperature extends VirtualResource{
    
    public VirtualTemperature(ArrayList<Device> dev) {
        // set resource identifier
        super("temperature", "Temperature Resource", dev);
        type = "Temperature";
    }

    @Override
    public void handleGET(CoapExchange exchange) {
    	
    	Device temp_dev = chooseDevice(1);
    	if(temp_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		System.out.println("\nThere are not available servers for the " + type + " resource");
    		System.out.println("\nGET on coap://localhost/temperature ends");
    		return;
    	}
    	String res = temp_dev.getTemperature().Get();
    	if(!res.equals("")) {
    		// respond to the request
    		exchange.respond(res);
    	} else {
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	}
    	System.out.println("\nGET on coap://localhost/temperature ends");
    }
    
    @Override
    public void handlePOST(CoapExchange exchange) {
    	
        String opt = exchange.getRequestText();
        
    	Device temp_dev = chooseDevice(1);
    	
    	/* No server available for this resource */
    	if(temp_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		System.out.println("\nThere are not available servers for the " + type + " resource");
    		System.out.println("\nPOST on coap://localhost/temperature ends");
    		return;
    	}
    	
    	int tmp = temp_dev.getTemperature().Post(opt);
 
    	/* Ok, the request can be satisfied */
    	if(tmp == 1){
    		exchange.respond(ResponseCode.CHANGED);
    	} else if (tmp == -1){ /* Some messages have been lost or the server ran out of battery */
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	} else{ /* The server experienced some kind of error */
    		exchange.respond(ResponseCode.BAD_REQUEST);
    	}
    	System.out.println("\nPOST on coap://localhost/temperature ends");
    }
    
    @Override
    public void handlePUT(CoapExchange exchange) {
   
    	String opt = exchange.getRequestText();
    	
    	Device temp_dev = chooseDevice(1);
    	
    	if(temp_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		System.out.println("\nThere are not available servers for the " + type + " resource");
    		System.out.println("\nPUT on coap://localhost/temperature ends");
    		return;
    	}
    	
      	int tmp = temp_dev.getTemperature().Put(opt);
      	if(tmp == 1) {
    		exchange.respond(ResponseCode.CHANGED);
    	} else if (tmp == -1) {
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	} else{
    		exchange.respond(ResponseCode.BAD_REQUEST);
    	}
      	System.out.println("\nPUT on coap://localhost/temperature ends");
    }
}
