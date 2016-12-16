package it.ing.unipi.anaws.virtual_resources;

import java.util.ArrayList;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import it.ing.unipi.anaws.devices.TemperatureDevice;

/*
 * Definition of the Temperature Resource
 */
public class VirtualTemperature extends VirtualResource<TemperatureDevice> {
    
    public VirtualTemperature(ArrayList<TemperatureDevice> temp) {
        // set resource identifier
        super("temperature", "Temperature Resource", temp);
    }

    @Override
    public void handleGET(CoapExchange exchange) {
    	TemperatureDevice temp_dev = (TemperatureDevice) chooseDevice();
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
}
