package it.ing.unipi.anaws.reverseproxy;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.coap.CoAP.ResponseCode;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.eclipse.californium.core.server.resources.ConcurrentCoapResource;

import it.ing.unipi.anaws.devices.AccelerometerDevice;
import it.ing.unipi.anaws.devices.Device;
import it.ing.unipi.anaws.devices.DeviceComparatorByCharge;
import it.ing.unipi.anaws.devices.LedsDevice;
import it.ing.unipi.anaws.devices.TemperatureDevice;
import it.ing.unipi.anaws.devices.ToggleDevice;

public class ExampleReverseProxy extends CoapServer {

	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
	private static final int THREADS_NUMBER = 4;//number of threads that can handle request on a single resource
	
	//motes 
	public static ArrayList<AccelerometerDevice> acc_dev;
	public static ArrayList<TemperatureDevice> temp_dev;
	public static ArrayList<ToggleDevice> tog_dev;
	public static ArrayList<LedsDevice> led_dev;
	
	//number of remaining requests for each device
	private static Vector<Integer> acc_req;
	
	//total number of served requests for device type at the moment
	private static int tot_acc_req;
	
	//total number of possible requests that devices can handle in a cycle
	private static int acc_cycle;
	
	//addresses
	public static ArrayList<String> addr;
	
    /*
     * Application entry point.
     */
    public static void main(String[] args) {
        
        try {
        
        	//initialize variables
        	acc_req = new Vector<Integer>();
        	
        	//create lists of devices
        	acc_dev = new ArrayList<AccelerometerDevice>();
        	temp_dev = new ArrayList<TemperatureDevice>();
        	tog_dev = new ArrayList<ToggleDevice>();
        	led_dev = new ArrayList<LedsDevice>();
        	
        	//create list of addresses
        	addr = new ArrayList<String>();
        	
            // create server
            ExampleReverseProxy server = new ExampleReverseProxy();
            
            // add endpoints on all IP addresses
            server.addEndpoints();
            server.start();
            
            /*  ADDRESSING PHASE 
                 
                 Find address of servers
            */
            
            /* add addresses in a static manner */
            addr.add("coap://[aaaa::c30c:0:0:2]:5683");
            addr.add("coap://[aaaa::c30c:0:0:3]:5683");
            addr.add("coap://[aaaa::c30c:0:0:4]:5683");
            addr.add("coap://[aaaa::c30c:0:0:9]:5683");
            /*
            addr.add("coap://[aaaa::c30c:0:0:5]:5683");
            addr.add("coap://[aaaa::c30c:0:0:6]:5683");
            addr.add("coap://[aaaa::c30c:0:0:7]:5683");
            addr.add("coap://[aaaa::c30c:0:0:8]:5683");
            
            addr.add("coap://[aaaa::c30c:0:0:a]:5683");
            addr.add("coap://[aaaa::c30c:0:0:b]:5683");
            addr.add("coap://[aaaa::c30c:0:0:c]:5683");
            addr.add("coap://[aaaa::c30c:0:0:d]:5683");
            */
            server.discoverResources();
            
            checkBatteryStatus(acc_dev);
            orderDevices(acc_dev, acc_req);
            acc_cycle = computeCycle(acc_req);
            System.out.println("Acc cycle : " + acc_cycle);
            
            /* check and order for all the others devices */
          
            System.out.println("--- PROXY READY ---");
            
        } catch (SocketException e) {
            System.err.println("Failed to initialize server: " + e.getMessage());
        }
    }

    private static int computeCycle(Vector<Integer> req){
    	
    	int cycle = 0;
    	for(Integer aux : req){
    		cycle += aux;
    	}
    	
    	return cycle;
    }
    
    private static <T extends Device> void checkBatteryStatus (ArrayList<T> devices){
    	//TODO is possible that a server is busy, handle this situation??
    	Iterator<T> iter = devices.iterator();
    	while(iter.hasNext()) {
    		T dev = iter.next();
    		int maxTry = 5;
    		int charge = -1;
    		while((charge == -1) && (maxTry > 0)){
    			maxTry--;
    			charge = dev.BatteryGet();
  		 	}
    		if((charge == -1) && maxTry == 0){//assume that the device is disconnected
    			System.out.println("Server id " + dev.ID + " : Impossible to get charge, Server Disconnetted");
    			iter.remove();//remove current device from the list
    		}
    		if(charge == 0){
    			System.out.println("Server id " + dev.ID + " : Server discharged");
    			iter.remove();
    		}
    	}
    }
    
    private static <T extends Device> void orderDevices(ArrayList<T> devices, Vector<Integer> req){
    	
    	/*
    	System.out.println("--- INITIAL ORDER ---");
    	for(T device : devices)
    		System.out.println(device.ID);
    	*/
    	
    	Collections.sort(devices, new DeviceComparatorByCharge());
    	
    	/*
    	System.out.println("--- FINAL ORDER ---");
    	for(T device : devices)
    		System.out.println(device.ID);
    	*/
    	
    	req.clear();
    	for(int i = 0; i < devices.size(); i++){
    		req.add(i, devices.get(i).battery.charge/10);
    	}
    	
    	System.out.println("--- SETTING PARAMETERS ---");
    	System.out.print("Remaining requests :	");
    	for(Integer aux : acc_req){
    		System.out.print(aux);
    		System.out.print("	");
    	}
    	System.out.println("");
   	}
    
    private void addMotes(String s, String addr){
    	
    	String id = addr.split("\\[")[1].split("\\]")[0];
  
    	if(s.contains("rt=\"Acc\"")){
    		acc_dev.add(new AccelerometerDevice(id, addr));
    		System.out.println("Server id " + id + " : Accelerometer added");
    	}
    	else if(s.contains("rt=\"Temp\"")){
    		temp_dev.add(new TemperatureDevice(id, addr));
    		System.out.println("Server id " + id + " : Temperature added");
    	}
    	else if(s.contains("rt=\"Led\"")){
    		led_dev.add(new LedsDevice(id , addr));
    		System.out.println("Server id " + id + " : Leds added");
    	}
    	else if(s.contains("rt=\"Togg\"")){
    		tog_dev.add(new ToggleDevice(id ,addr));
    		System.out.println("Server id " + id + " : Toggle added");
    	}
    	else
    		System.out.println("Server id " + id + " : No known resources");
    }
    
    private void discoverResources(){
    	
    	CoapClient cl = new CoapClient();
    	//without this often proxy cannot get resources (wait a response for 20s)
    	cl.setTimeout(30000);
    	CoapResponse res;
    	
    	for(String address : addr){
    		cl.setURI(address + "/.well-known/core");
    		res = cl.get();
    		if(res != null){
    			//System.out.println(res.getResponseText());
    			addMotes(res.getResponseText(), address);
    		}
    		else
    			System.out.println("Server address " + address.split("//")[1] + " : Resources not founded");
    	}
    	
    	/*
    	System.out.println("--- DEVICES NUMBER ---");
    	System.out.println("Accelerometer :	" + acc_dev.size());
    	System.out.println("Temperature :	" + temp_dev.size());
    	System.out.println("Toggle :	" + tog_dev.size());
    	System.out.println("Leds : 		" + led_dev.size());
    	*/
    }
    
    /**
     * Add individual endpoints listening on default CoAP port on all IPv4 addresses of all network interfaces.
     */
    private void addEndpoints() {
    	for (InetAddress addr : EndpointManager.getEndpointManager().getNetworkInterfaces()) {
    		// only binds to localhost
			if (addr.isLoopbackAddress()) {
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
    class AccelerometerResource extends ConcurrentCoapResource {
        
        public AccelerometerResource() {
            
            // set resource identifier, set pool of threads
            super("accelerometer", THREADS_NUMBER);
           
            // set display name
            getAttributes().setTitle("Accelerometer Resource");
        }

      
        @Override
        public void handleGET(CoapExchange exchange) {
            
            //send ack to the client
        	exchange.accept();//TODO check what is the behavior of the client when receive this 
        	
        	synchronized(this){
        		System.out.println("Served by thread : " + Thread.currentThread().getName());
        		
        		int i;
        		
        		//accelerometer requests cycle is over
        		if(tot_acc_req == acc_cycle){
        			System.out.println("Cycle is over");
        			checkBatteryStatus(acc_dev);
        			orderDevices(acc_dev, acc_req);
        			acc_cycle = computeCycle(acc_req);
        			//System.out.println("Acc cycle : " + acc_cycle);
        			tot_acc_req = 0;
        		}

        		/* Choose the server to whom send the request
        		 * It is chosen the first one in the ordered list (ordered 
        		 * in a decreasing percentage of battery) that is not busy
        		 * and has still remaining request in this cycle
        		 */
        		for(i = 0; i < acc_dev.size(); i++){//TODO add busy control
        			if(acc_req.get(i) != 0){
        				if(acc_dev.get(i).busy){
        					System.out.println("Server id " + acc_dev.get(i).ID + " : Server busy");
        					continue;
        				}
        				acc_req.set(i, acc_req.get(i) - 1);
        				System.out.println("--- DEVICE SELECTION ---");
        				System.out.println("Number of total requests : " + (tot_acc_req + 1));
        				System.out.println("Device selected : " + i);
        				System.out.println("Server id : " + acc_dev.get(i).ID );
        				System.out.print("Remaining requests :	");
        				for(int aux : acc_req){
        					System.out.print(aux);
        					System.out.print("	");
        				}
        				System.out.println("");
        				tot_acc_req++;
        				break;
        			}
        		}

        		//there are no available devices
        		if((acc_dev.size() == 0) || (i == acc_dev.size())){//TODO remove second part and handle the all busy servers situation
        			System.out.println("No servers available");
        			exchange.respond("Impossible to handle requests : No servers available");
        			return;
        		}

        		//get can be executed by more threads at time
        		acc_dev.get(i).busy = true;//TODO busy inutile se si sincronizza tutto
        		String res = acc_dev.get(i).AccGet();
        		acc_dev.get(i).busy = false;
        		if(!res.equals("")){
        			// respond to the request
        			exchange.respond(res);
        		}
        		else{
        			//System.out.println("Server id " + acc_dev.get(i).ID + " : Gateway timeout");
        			exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
        		}
        	}
        }
    }
    
    /*
     * Definition of the Temperature Resource
     */
    class TemperatureResource extends ConcurrentCoapResource {
        
        public TemperatureResource() {
            
            // set resource identifier
            super("temperature", 1);
            
            // set display name
            getAttributes().setTitle("Temperature Resource");
        }

        @Override
        public void handleGET(CoapExchange exchange) {
     
        	exchange.accept();
        	
        	String res = temp_dev.get(0).TempGet();
        	if(!res.equals("")){
        		// respond to the request
        		exchange.respond(res);
        	}
        	else{
        		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
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
            
        	exchange.accept();
        	
        	int tmp = tog_dev.get(0).TogglePost();
        		
        	if(tmp == 1){
        		exchange.respond(ResponseCode.CHANGED);
        	}
        	else{
        		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
        	}
        }
        
        @Override
        public void handlePUT(CoapExchange exchange) {
        	
        	exchange.accept();
        	
        	int tmp = tog_dev.get(0).TogglePut();
        	
        	if(tmp == 1){
        		exchange.respond(ResponseCode.CHANGED);
        	}
        	else{
        		exchange.respond(ResponseCode.GATEWAY_TIMEOUT);
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

        	exchange.accept();
        	
            String opt = exchange.getRequestText();
        	String [] aux = opt.split(",");
   
        	int tmp = led_dev.get(0).LedsPost(aux[0],aux[1]);
        	
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
        
        	exchange.accept();
        	
        	String opt = exchange.getRequestText();
        	String [] aux = opt.split(",");
        	
          	int tmp = led_dev.get(0).LedsPut(aux[0],aux[1]);
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
}
