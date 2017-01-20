package it.ing.unipi.anaws.virtual_resources;

import java.util.ArrayList;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import it.ing.unipi.anaws.devices.Device;

/**
 * Definition of the Accelerometer Resource.
 * This class represents a virtual resource (a resource that is exposed by the 
 * reverse proxy) that is bound to a pool of accelerometer devices (real Erbium 
 * servers that expose an accelerometer resource).
 * When a request is performed to this virtual resource, such request is 
 * forwarded to a real device of the pool.
 * The device the request is forwarded to is chosen by {@link #chooseDevice()}.
 */
public class VirtualAccelerometer extends VirtualResource {
    
    public VirtualAccelerometer(ArrayList<Device> dev) {
        
        // set resource identifier, set pool of threads
        super("accelerometer", "Accelerometer Resource", dev);
        type = "Accelerometer";
    }
    
    @Override
    public void handleGET(CoapExchange exchange) {
    	
    	Device acc_dev = chooseDevice(0);
    	if(acc_dev == null){
    		exchange.respond(ResponseCode.SERVICE_UNAVAILABLE);
    		System.out.println("\nThere are not available servers for the " + type + " resource");
    		System.out.println("\nGET on coap://localhost/accelerometer ends");
    		return;
    	}
    	String res = acc_dev.getAccelerometer().Get();
    	if(!res.equals("")){
    		// respond to the request
    		exchange.respond(res);
    	} else {
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	}
    	System.out.println("\nGET on coap://localhost/accelerometer ends");
    }
}
