package it.ing.unipi.anaws.reverseproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.CoapServer;
import org.eclipse.californium.core.network.CoapEndpoint;
import org.eclipse.californium.core.network.EndpointManager;
import org.eclipse.californium.core.network.config.NetworkConfig;
import it.ing.unipi.anaws.devices.Device;
import it.ing.unipi.anaws.virtual_resources.VirtualAccelerometer;
import it.ing.unipi.anaws.virtual_resources.VirtualLeds;
import it.ing.unipi.anaws.virtual_resources.VirtualTemperature;
import it.ing.unipi.anaws.virtual_resources.VirtualToggle;

public class ReverseProxy extends CoapServer {

	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
	
	//motes 
	protected static ArrayList<Device> dev;
	
	/*
	 * This are updated every time a mote is added.
	 * They are used to choose what virtual resources the proxy
	 * has to create. And they the keep track of the number of
	 * devices for each resource
	 */
	protected static int 	accelerometerDevices;
	protected static int 	ledsDevices;
	protected static int 	toggleDevices;
	protected static int	temperatureDevices;
	
	/* virtual resources */
	protected VirtualAccelerometer acc_res;
	protected VirtualTemperature temp_res;
	protected VirtualToggle tog_res;
	protected VirtualLeds led_res;
	
	//addresses
	protected static String[] addr;
	
    public static void main(String[] args) {
        
        try {
        	/* no resources seem available at beginning */
        	accelerometerDevices = ledsDevices = toggleDevices = temperatureDevices = 0;
        	
        	//create lists of devices
        	dev = new ArrayList<Device>();
        	
        	System.out.println("--- Servers available ---");
        	/*
        	 * ADDRESSING PHASE
        	 * creating list of addresses
        	 */
        	addr = findNeighbors("http://[aaaa::201:1:1:1]");
       
        	if(addr == null){
        		System.out.println("No servers found");
        		return;
        	}
        	
        	System.out.println("--- Resource discovery ---");
            /*
             * RESOURCE DISCOVERY PHASE
             * The following call populate the device lists
            */
            discoverResources();
            
            // create server
            ReverseProxy server = new ReverseProxy();
            
            if (dev.size() > 0) {
            	/* XXX it does not matter which virtual resource type 
            	 * we use for initialization because checkBatteryStatus(), 
            	 * orderDevices() and computeCycle() act on the device list
            	 * (which is in common)
            	 */
            	server.acc_res.init();
            }
            
            // add endpoints
            server.addEndpoints();
            server.start();
          
            System.out.println("--- Proxy ready ---");
            
        } catch (SocketTimeoutException e) {
			System.err.println("Failed to initialize the server: " + e.getMessage());
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Failed to initialize the server: " +e.getMessage());
			e.printStackTrace();
		}
    }
    
    /**
     * This function fills the device list
     * 
     * @param s The response text of /.well-known/core for this mote
     * @param addr This mote IP address
     */
    private static void addMotes(String s, String addr){
    	
    	String id = addr.split("\\[")[1].split("\\]")[0];
    	
    	/* This indicates if this mote has any kind of known resource */
    	boolean ok = false;
    	boolean [] resourceFound = {false, false, false, false};//[0] accelerometer, [1] temperature, [2] leds, [3] toggle
  
    	if(s.contains("rt=\"Acc\"")){
    		System.out.println("Server id " + id + " : Accelerometer added");
    		accelerometerDevices++;
    		resourceFound[0] = true;
    		ok = true;
    	}
    	
    	if(s.contains("rt=\"Temp\"")){
    		System.out.println("Server id " + id + " : Temperature added");
    		temperatureDevices++;
    		resourceFound[1] = true;
    		ok = true;
    	}
    	
    	if(s.contains("rt=\"Led\"")){
    		System.out.println("Server id " + id + " : Leds added");
    		ledsDevices++;
    		resourceFound[2] = true;
    		ok = true;
    	}
    	
    	if(s.contains("rt=\"Togg\"")){
    		System.out.println("Server id " + id + " : Toggle added");
    		toggleDevices++;
    		resourceFound[3] = true;
    		ok = true;
    	}
    	
    	if (!ok) { // no known resource found for this mote
    		System.out.println("Server id " + id + " : No known resources");
    	}
    	else{
    		dev.add(new Device(id, addr, resourceFound));
    	}
    }
    
    private static void discoverResources(){
    	
    	CoapClient cl = new CoapClient();
    	//without this proxy cannot get resources often (wait for a response for 16s)
    	cl.setTimeout(16000);
    	CoapResponse res;
   		
    	for(String address : addr){
    		cl.setURI(address + "/.well-known/core");
    		res = cl.get();
    		if(res != null){ //erbium server found
    			addMotes(res.getResponseText(), address);
    		} else {
    			System.out.println("Server address " + address.split("//")[1] + " : No response");
    		}
    	}
    }
    
    /**
     * Add individual endpoints listening on default CoAP port on Ipv6 localhost address
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

    /**
     * Constructor for a new ExampleReverseProxy server. The resources
     * of the server are initialized here (including its CoAP core)
     */
    public ReverseProxy() {
    	if (accelerometerDevices > 0) {
    		acc_res = new VirtualAccelerometer(dev);
    		add(acc_res);
    	}
    	if (temperatureDevices > 0) {
    		temp_res = new VirtualTemperature(dev);
    		add(temp_res);
    	}
    	if (toggleDevices > 0) {
    		tog_res = new VirtualToggle(dev);
    		add(tog_res);
    	}
    	if (ledsDevices > 0) {
    		led_res = new VirtualLeds(dev);
    		add(led_res);
    	}
    	
    	/* Creating proxy's coap core */
        add(new CoapResource(".well-known/core"));
    }
    
    /**
	 * This function retrieves the list of border router neighbors.
	 * It performs an HTTP request to the border router and parse the response.
	 * 
	 * @param borderRouter a string with the border router IP address
	 * @return an array of string containing the mote's IP addresses
	 * @throws MalformedURLException when the border router IP is misspelled
	 * @throws IOException when there is a generic error while performing the 
	 * 		request.
	 * @throws SocketTimeoutException when the border router does not reply in 
	 * 		after a timeout has expired.
	 */
	public static String[] findNeighbors(String borderRouter) 
			throws MalformedURLException, IOException, SocketTimeoutException {
		URL url = new URL(borderRouter);
		URLConnection connection = url.openConnection();
		BufferedReader in = new BufferedReader(new InputStreamReader(
				connection.getInputStream()));
		
		StringBuilder builder = new StringBuilder();
		String response, s;
		while ((s = in.readLine()) != null) {
			builder.append(s + '\n');
		}
		response = builder.toString();
		
		return parseHtml(response);
	}

    /**
	 * This function parse a string in order to find the list of border router 
	 * neighbors.
	 * @param html The string to be parsed
	 * @return an array of string containing the mote's IP addresses
	 */
	protected static String[] parseHtml(String html) {
		/*
		 * The following are the strings that come before and after the list 
		 * of neighbors in the border router response. They are used by this 
		 * function to locate the node list.
		 */
		final String startTag = "Routes<pre>";
		final String endTag = "</pre></body>";
		/* this is the separator between addresses */
		final String separator = "/128";
		
		ArrayList<String> neighbors = new ArrayList<>();
		int index = html.indexOf(startTag);
		if (index < 0) {
			return null;
		}
		index += startTag.length();
		while (!html.substring(index, index + endTag.length()).equals(endTag)) {
			int endIndex = html.substring(index).indexOf(separator);
			neighbors.add("coap://[" + html.substring(index, index + endIndex) + "]:5683");
			endIndex = html.substring(index).indexOf("\n");
			index += endIndex + 1;
		}
		
		String[] result = new String[neighbors.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = neighbors.get(i);
		}
		
		for (String a : result) {
			System.out.println(a);
		}
		return result;
	}  
}
