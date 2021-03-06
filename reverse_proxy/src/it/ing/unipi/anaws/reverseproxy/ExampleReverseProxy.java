package it.ing.unipi.anaws.reverseproxy;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import org.eclipse.californium.core.CoapClient;
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

public class ExampleReverseProxy extends CoapServer {

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
	
    /*
     * Application entry point.
     */
    public static void main(String[] args) {
        
        try {
        	
        	//create lists of devices
        	acc_dev = new ArrayList<AccelerometerDevice>();
        	temp_dev = new ArrayList<TemperatureDevice>();
        	tog_dev = new ArrayList<ToggleDevice>();
        	led_dev = new ArrayList<LedsDevice>();
        	
        	//create list of addresses
        	//addr = new ArrayList<String>();
        	addr = findNeighbors("http://[fd00::c30c:0:0:1]");
        	
            
            
            
            /*  ADDRESSING PHASE 
                 
                 Find address of servers
            */
            
            /* add addresses in a static manner */
            /*addr.add("coap://[fd00::c30c:0:0:2]:5683");
            addr.add("coap://[fd00::c30c:0:0:3]:5683");
            addr.add("coap://[fd00::c30c:0:0:4]:5683");
            addr.add("coap://[fd00::c30c:0:0:9]:5683");
            
            addr.add("coap://[fd00::c30c:0:0:5]:5683");
            addr.add("coap://[fd00::c30c:0:0:6]:5683");
            addr.add("coap://[fd00::c30c:0:0:7]:5683");
            addr.add("coap://[fd00::c30c:0:0:8]:5683");
            
            addr.add("coap://[aaaa::c30c:0:0:a]:5683");
            addr.add("coap://[aaaa::c30c:0:0:b]:5683");
            addr.add("coap://[aaaa::c30c:0:0:c]:5683");
            /*addr.add("coap://[aaaa::c30c:0:0:d]:5683");
            */
            discoverResources();
            
            // create server
            ExampleReverseProxy server = new ExampleReverseProxy();
            
            server.acc_res.init();
            server.temp_res.init();
            server.tog_res.init();
            server.led_res.init();
            
            // add endpoints on all IP addresses
            server.addEndpoints();
            server.start();
            
            
            //System.out.println("Acc cycle : " + acc_cycle);
            
            /* check and order for all the others devices */
          
            System.out.println("--- PROXY READY ---");
            
        } catch (SocketException e) {
            System.err.println("Failed to initialize server: " + e.getMessage());
        } catch (SocketTimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private static void addMotes(String s, String addr){
    	
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
    
    private static void discoverResources(){
    	
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
    		} else {
    			System.out.println("Server address " + address.split("//")[1] + " : Resources not found");
    		}
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
    	acc_res = new VirtualAccelerometer(acc_dev);
    	temp_res = new VirtualTemperature(temp_dev);
    	tog_res = new VirtualToggle(tog_dev);
    	led_res = new VirtualLeds(led_dev);
    		
    	add(acc_res);
        add(temp_res);
        add(tog_res);
        add(led_res);
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
		//The following call is executed implicitly by getInputStream
		//connection.connect();
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
		/**
		 * The following are the strings that come before and after the list 
		 * of neighbors in the border router response. They are used by this 
		 * function to locate the node list.
		 */
		final String startTag = "Routes<pre>\n";
		final String endTag = "</pre></body>";
		/** this is the separator between addresses **/
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
