package it.ing.unipi.anaws.virtual_resources;

import java.util.ArrayList;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;

import it.ing.unipi.anaws.devices.ToggleDevice;

/*
 * Definition of the Toggle Resource
 */
public class VirtualToggle extends VirtualResource<ToggleDevice> {
    
    public VirtualToggle(ArrayList<ToggleDevice> tog) {
        
        // set resource identifier
        super("toggle", "Toggle Resource", tog);
    }
    
    @Override
    public void handlePOST(CoapExchange exchange) {
    	ToggleDevice tog_dev = (ToggleDevice) chooseDevice();
    	if(tog_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		return;
    	}
    	int tmp = tog_dev.TogglePost();
    		
    	if(tmp == 1){
    		exchange.respond(ResponseCode.CHANGED);
    	} else {
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	}
    }
    
    @Override
    public void handlePUT(CoapExchange exchange) {
    	ToggleDevice tog_dev = (ToggleDevice) chooseDevice();
    	if(tog_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		return;
    	}
    	int tmp = tog_dev.TogglePut();
    	
    	if(tmp == 1){
    		exchange.respond(ResponseCode.CHANGED);
    	} else {
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	}
    }
}
