package it.ing.unipi.anaws.virtual_resources;

import java.util.ArrayList;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import it.ing.unipi.anaws.devices.Device;

/*
 * Definition of the Leds Resource
 */
public class VirtualLeds extends VirtualResource {
    
    public VirtualLeds(ArrayList<Device> dev) {
        
        // set resource identifier
        super("leds", "Leds Resource", dev);
        type = "Leds";
    }
    
    @Override
    public void handlePOST(CoapExchange exchange) {
    	
        String opt = exchange.getRequestText();
    	String [] aux = opt.split(",");
    	Device led_dev = chooseDevice(2);
    	/* No server available for this resource */
    	if(led_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		System.out.println("\nThere are not available servers for the " + type + " resource");
    		System.out.println("\nPOST on coap://localhost/leds ends");
    		return;
    	}
    	int tmp = led_dev.getLeds().Post(aux[0],aux[1]);
    	
    	/* Ok, the request can be satisfied */
    	if(tmp == 1){
    		exchange.respond(ResponseCode.CHANGED);
    	} else if (tmp == -1){ /* Some messages have been lost or the server ran out of battery */
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	} else{ /* The server experienced some kind of error */
    		exchange.respond(ResponseCode.BAD_REQUEST);
    	}
    	System.out.println("\nPOST on coap://localhost/leds ends");
    }
    
    @Override
    public void handlePUT(CoapExchange exchange) {
    
    	String opt = exchange.getRequestText();
    	String [] aux = opt.split(",");
    	
    	Device led_dev = chooseDevice(2);
    	if(led_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		System.out.println("\nThere are not available servers for the " + type + " resource");
    		System.out.println("\nPUT on coap://localhost/leds ends");
    		return;
    	}
      	int tmp = led_dev.getLeds().Put(aux[0],aux[1]);
      	if(tmp == 1) {
    		exchange.respond(ResponseCode.CHANGED);
    	} else if (tmp == -1) {
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	} else{
    		exchange.respond(ResponseCode.BAD_REQUEST);
    	}
      	System.out.println("\nPUT on coap://localhost/leds ends");
    }
}
