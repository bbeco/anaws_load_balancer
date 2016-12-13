package it.ing.unipi.anaws.virtual_resources;

import java.util.ArrayList;

import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.server.resources.CoapExchange;
import it.ing.unipi.anaws.devices.AccelerometerDevice;
import it.ing.unipi.anaws.devices.Device;

/*
 * Definition of the Accelerometer Resource
 */
public class VirtualAccelerometer extends VirtualResource<AccelerometerDevice> {
    
    public VirtualAccelerometer(ArrayList<AccelerometerDevice> acc) {
        
        // set resource identifier, set pool of threads
        super("accelerometer", "Accelerometer Resource", acc);
       
    }
    
    @Override
    public void handleGET(CoapExchange exchange) {
    	AccelerometerDevice acc_dev = (AccelerometerDevice) chooseDevice();
    	String res = acc_dev.AccGet();
    	if(!res.equals("")){
    		// respond to the request
    		exchange.respond(res);
    	} else {
    		//System.out.println("Server id " + acc_dev.get(i).ID + " : Gateway timeout");
    		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
    	}
    }
}
