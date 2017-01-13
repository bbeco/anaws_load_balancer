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
import it.ing.unipi.anaws.devices.AccelerometerDevice;
import it.ing.unipi.anaws.devices.LedsDevice;
import it.ing.unipi.anaws.devices.TemperatureDevice;
import it.ing.unipi.anaws.devices.ToggleDevice;
import it.ing.unipi.anaws.virtual_resources.VirtualAccelerometer;
import it.ing.unipi.anaws.virtual_resources.VirtualLeds;
import it.ing.unipi.anaws.virtual_resources.VirtualTemperature;
import it.ing.unipi.anaws.virtual_resources.VirtualToggle;

public class ReverseProxy extends CoapServer {

	private static final int COAP_PORT = NetworkConfig.getStandard().getInt(NetworkConfig.Keys.COAP_PORT);
	
	//motes 
	public static ArrayList<AccelerometerDevice> acc_dev;
	public static ArrayList<TemperatureDevice> temp_dev;
	public static ArrayList<ToggleDevice> tog_dev;
	public static ArrayList<LedsDevice> led_dev;
	
	/* virtual resources */
	VirtualAccelerometer acc_res;
	VirtualTemperature temp_res;
	VirtualToggle tog_res;
	VirtualLeds led_res;
	
	//addresses
	public static String[] addr;
	
    public static void main(String[] args) {
        
        try {
        	
        	//create lists of devices
        	acc_dev = new ArrayList<AccelerometerDevice>();
        	temp_dev = new ArrayList<TemperatureDevice>();
        	tog_dev = new ArrayList<ToggleDevice>();
        	led_dev = new ArrayList<LedsDevice>();
        	
        	/*
        	 * ADDRESSING PHASE
        	 * creating list of addresses
        	 */
        	addr = findNeighbors("http://[aaaa::201:1:1:1]");
       
        	if(addr == null){
        		System.out.println("No servers found");
        		return;
        	}
        	
            /*
             * RESOURCE DISCOVERY PHASE
             * The following call populate the device lists
            */
            discoverResources();
            
            // create server
            ReverseProxy server = new ReverseProxy();
            
            if (acc_dev.size() > 0) {
            	server.acc_res.init();
            }
            if (temp_dev.size() > 0) {
            	server.temp_res.init();
            }
            if (tog_dev.size() > 0) {
            	server.tog_res.init();
            }
            if (led_dev.size() > 0) {
            	server.led_res.init();
            }
            
            // add endpoints on all IP addresses
            server.addEndpoints();
            server.start();
          
            System.out.println("--- PROXY READY ---");
            
        } catch (SocketTimeoutException e) {
			System.err.println("Failed to initialize the server: " + e.getMessage());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Failed to initialize the server: " +e.getMessage());
			e.printStackTrace();
		}
    }
    
    /**
     * This function fills the device lists
     */
    private static void addMotes(String s, String addr){
    	
    	String id = addr.split("\\[")[1].split("\\]")[0];
  
    	if(s.contains("rt=\"Acc\"")){
    		acc_dev.add(new AccelerometerDevice(id, addr));
    		System.out.println("Server id " + id + " : Accelerometer added");
    	} else if(s.contains("rt=\"Temp\"")){
    		temp_dev.add(new TemperatureDevice(id, addr));
    		System.out.println("Server id " + id + " : Temperature added");
    	} else if(s.contains("rt=\"Led\"")){
    		led_dev.add(new LedsDevice(id , addr));
    		System.out.println("Server id " + id + " : Leds added");
    	} else if(s.contains("rt=\"Togg\"")){
    		tog_dev.add(new ToggleDevice(id ,addr));
    		System.out.println("Server id " + id + " : Toggle added");
    	} else {
    		System.out.println("Server id " + id + " : No known resources");
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
    public ReverseProxy() {
    	if (acc_dev.size() > 0) {
    		acc_res = new VirtualAccelerometer(acc_dev);
    		add(acc_res);
    	}
    	if (temp_dev.size() > 0) {
    		temp_res = new VirtualTemperature(temp_dev);
    		add(temp_res);
    	}
    	if (tog_dev.size() > 0) {
    		tog_res = new VirtualToggle(tog_dev);
    		add(tog_res);
    	}
    	if (led_dev.size() > 0) {
    		led_res = new VirtualLeds(led_dev);
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
		/* this is the separator between addresses **/
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
