package it.ing.unipi.anaws.reverseproxy;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.OptionSet;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;

import it.ing.unipi.anaws.devices.AccelerometerDevice;
import it.ing.unipi.anaws.devices.LedsDevice;
import it.ing.unipi.anaws.devices.TemperatureDevice;
import it.ing.unipi.anaws.devices.ToggleDevice;

public class ExampleReverseProxy extends CoapServer {
	
	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
	
	//motes 
	public static ArrayList<AccelerometerDevice> acc_dev;
	public static ArrayList<TemperatureDevice> temp_dev;
	public static ArrayList<ToggleDevice> tog_dev;
	public static ArrayList<LedsDevice> led_dev;
	
    /*
     * Application entry point.
     */
    public static void main(String[] args) {
        
        try {
        	
        	//create list of devices
        	acc_dev = new ArrayList<AccelerometerDevice>();
        	temp_dev = new ArrayList<TemperatureDevice>();
        	tog_dev = new ArrayList<ToggleDevice>();
        	led_dev = new ArrayList<LedsDevice>();
        	
            // create server
            ExampleReverseProxy server = new ExampleReverseProxy();
            
            // add endpoints on all IP addresses
            server.addEndpoints();
            server.start();
            
            /* DISCOVERY PHASE 
                 
                 Find address of servers
                 Discover resources of each server
                 For each type of servers create a device
            */
            
            /* example servers */
            acc_dev.add(new AccelerometerDevice("coap://[aaaa::c30c:0:0:2]:5683"));
            temp_dev.add(new TemperatureDevice("coap://[aaaa::c30c:0:0:4]:5683"));
            tog_dev.add(new ToggleDevice("coap://[aaaa::c30c:0:0:5]:5683"));
            led_dev.add(new LedsDevice("coap://[aaaa::c30c:0:0:3]:5683"));

        } catch (SocketException e) {
            System.err.println("Failed to initialize server: " + e.getMessage());
        }
    }

    /**
     * Add individual endpoints listening on default CoAP port on all IPv4 addresses of all network interfaces.
     */
    private void addEndpoints() {
    	for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
    		// only binds to localhost
			if (/*addr instanceof Inet6Address ||  */addr.isLoopbackAddress()) {
				InetSocketAddress bindToAddress = new InetSocketAddress(addr, COAP_PORT);
				addEndpoint(new CoapEndpoint(bindToAddress));
			}
		}
    }

    /*
     * Constructor for a new ExampleReverseProxy server. Here, the resources
     * of the server are initialized.
     */
    public ExampleReverseProxy() throws SocketException {
        
        // provide an instance of resources
    	AccelerometerResource acc_res = new AccelerometerResource();
    	TemperatureResource temp_res = new TemperatureResource();
    	ToggleResource tog_res = new ToggleResource();
    	LedsResource led_res = new LedsResource();
    		
    	add(acc_res);
        add(temp_res);
        add(tog_res);
        add(led_res);
    }

    /*
     * Definition of the Accelerometer Resource
     */
    class AccelerometerResource extends CoapResource {
        
        public AccelerometerResource() {
            
            // set resource identifier
            super("accelerometer");
            
            // set display name
            getAttributes().setTitle("Accelerometer Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            
        	String res = acc_dev.get(0).AccGet();
        	if(!res.equals("")){
        		// respond to the request
        		exchange.respond(res);
        	}
        	else{
        		exchange.respond("No response received");
        	}
        }
    }
    
    /*
     * Definition of the Temperature Resource
     */
    class TemperatureResource extends CoapResource {
        
        public TemperatureResource() {
            
            // set resource identifier
            super("temperature");
            
            // set display name
            getAttributes().setTitle("Temperature Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
            
        	String res = temp_dev.get(0).TempGet();
        	if(!res.equals("")){
        		// respond to the request
        		exchange.respond(res);
        	}
        	else{
        		exchange.respond("No response received");
        	}
        }
    }
    
    /*
     * Definition of the Toggle Resource
     */
    class ToggleResource extends CoapResource {
        
        public ToggleResource() {
            
            // set resource identifier
            super("toggle");
            
            // set display name
            getAttributes().setTitle("Toggle Resource");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {
            		
        	int tmp = tog_dev.get(0).TogglePost();
        		
        	if(tmp == 1){
        		exchange.respond(ResponseCode.CHANGED);
        	}
        	else{
        		//TODO è possibile settare un codice in caso di risposta non ricevuta?
        		exchange.respond("No response received");
        	}
        }
        
        @Override
        public void handlePUT(CoapExchange exchange) {
        	   
        	int tmp = tog_dev.get(0).TogglePut();
        	
        	if(tmp == 1){
        		exchange.respond(ResponseCode.CHANGED);
        	}
        	else{
        		exchange.respond("No response received");
        	}
        }
    }
    
    /*
     * Definition of the Leds Resource
     */
    class LedsResource extends CoapResource {
        
        public LedsResource() {
            
            // set resource identifier
            super("leds");
            
            // set display name
            getAttributes().setTitle("Leds Resource");
        }

        @Override
        public void handlePOST(CoapExchange exchange) {

            String opt = exchange.getRequestText();
        	String [] aux = opt.split(",");
   
        	int tmp = led_dev.get(0).LedsPost(aux[0],aux[1]);
        	
        	if(tmp == 1){
        		exchange.respond(ResponseCode.CHANGED);
        	}
        	else if (tmp == -1){
        		exchange.respond("No response received");
        	}
        	else{
        		exchange.respond(ResponseCode.BAD_REQUEST);
        	}
        }
        
        @Override
        public void handlePUT(CoapExchange exchange) {
        
        	String opt = exchange.getRequestText();
        	String [] aux = opt.split(",");
    
          	int tmp = led_dev.get(0).LedsPut(aux[0],aux[1]);
          	if(tmp == 1){
        		exchange.respond(ResponseCode.CHANGED);
        	}
        	else if (tmp == -1){
        		exchange.respond("No response received");
        	}
        	else{
        		exchange.respond(ResponseCode.BAD_REQUEST);
        	}
        }
    }
}
