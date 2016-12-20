package it.ing.unipi.anaws.virtual_resources;

import java.util.ArrayList;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import it.ing.unipi.anaws.devices.LedsDevice;

/*
 * Definition of the Leds Resource
 */
public class VirtualLeds extends VirtualResource<LedsDevice> {
    
    public VirtualLeds(ArrayList<LedsDevice> leds) {
        
        // set resource identifier
        super("leds", "Leds Resource", leds);
    }
    
    @Override
    public void handlePOST(CoapExchange exchange) {
    	
        String opt = exchange.getRequestText();
    	String [] aux = opt.split(",");
    	//System.out.println(aux[0]+" "+aux[1]);
    	LedsDevice led_dev = (LedsDevice) chooseDevice();
    	if(led_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		return;
    	}
    	int tmp = led_dev.LedsPost(aux[0],aux[1]);
    	
    	if(tmp == 1){
    		exchange.respond(ResponseCode.CHANGED);
    	}
    	else if (tmp == -1){
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	}
    	else{
    		exchange.respond(ResponseCode.BAD_REQUEST);
    	}
    }
    
    @Override
    public void handlePUT(CoapExchange exchange) {
    	
    	String opt = exchange.getRequestText();
    	String [] aux = opt.split(",");
    	
    	LedsDevice led_dev = (LedsDevice) chooseDevice();
    	if(led_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		return;
    	}
      	int tmp = led_dev.LedsPut(aux[0],aux[1]);
      	if(tmp == 1){
    		exchange.respond(ResponseCode.CHANGED);
    	}
    	else if (tmp == -1){
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	}
    	else{
    		exchange.respond(ResponseCode.BAD_REQUEST);
    	}
    }
}
