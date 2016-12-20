package it.ing.unipi.anaws.reverseproxy;

import java.util.Random;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class ReverseProxyTester {
	
	private final static int REQUESTS_NUMBER = 35;
	private static int requests;
	
	private static CoapHandler ch = new CoapHandler(){ /* Response Handler */
		@Override
		public void onLoad(CoapResponse re) {	
			// access advanced API with access to more details through .advanced()
			System.out.println(Utils.prettyPrint(re));				
			System.out.println("\n");
			synchronized(ReverseProxyTester.class){
				if((++requests) == REQUESTS_NUMBER)
					ReverseProxyTester.class.notify();
			}
		}
		
		@Override
		public void onError() {
			synchronized(ReverseProxyTester.class){
				if((++requests) == REQUESTS_NUMBER)
					ReverseProxyTester.class.notify();
			}
			System.out.println("No response received\n");
		}
	};

	public static void main(String args[]) {
		
		CoapClient client = new CoapClient();
		//client.useNONs();
		
		Random rand = new Random();
		int whichRequest;				
		
		int counter = 0;
		
		for (int i = 0; i < REQUESTS_NUMBER; i++){
				
			whichRequest = rand.nextInt(10)+1;
				
			switch(whichRequest){
				
				//get accelerometer
				case 1: System.out.println("GET ACCELEROMETER " + counter++ + "\n");
						client.setURI("coap://[::1]:5683/accelerometer");
						client.get(ch);
				break;
					
				//get temperature
				case 2: System.out.println("GET TEMPERATURE\n");
						client.setURI("coap://[::1]:5683/temperature");
						client.get(ch);
				break;

				//post toggle
				case 3: System.out.println("POST TOGGLE\n");
						client.setURI("coap://[::1]:5683/toggle");
						client.post(ch , "" , MediaTypeRegistry.TEXT_PLAIN);
				break;

				//put toggle
				case 4: System.out.println("PUT TOGGLE\n");
						client.setURI("coap://[::1]:5683/toggle");
						client.put(ch, "" , MediaTypeRegistry.TEXT_PLAIN);
				break;

				//post leds r on
				case 5: System.out.println("POST LEDS (r, on)\n");
						client.setURI("coap://[::1]:5683/leds");
						client.post(ch, "r,on" , MediaTypeRegistry.TEXT_PLAIN);
				break;

				//post leds r off
				case 6: System.out.println("POST LEDS (r, off)\n");
						client.setURI("coap://[::1]:5683/leds");
						client.post(ch, "r,off" , MediaTypeRegistry.TEXT_PLAIN);
				break;

				//put leds g on
				case 7: System.out.println("PUT LEDS (g, on)\n");
						client.setURI("coap://[::1]:5683/leds");
						client.put(ch, "g,on" , MediaTypeRegistry.TEXT_PLAIN);
				break;
					
				//put leds g off
				case 8: System.out.println("PUT LEDS (g, off)\n");
						client.setURI("coap://[::1]:5683/leds");
						client.put(ch, "g,off" , MediaTypeRegistry.TEXT_PLAIN);
				break;

				//post leds b on
				case 9: System.out.println("POST LEDS (b, on)\n");
						client.setURI("coap://[::1]:5683/leds");
						client.post(ch, "b,on" , MediaTypeRegistry.TEXT_PLAIN);	
				break;

				//post leds b off
				case 10:System.out.println("POST LEDS (b, off)\n");
						client.setURI("coap://[::1]:5683/leds");
						client.post(ch, "b,off" , MediaTypeRegistry.TEXT_PLAIN);
			}
		}
	
		synchronized(ReverseProxyTester.class){
			try {
				ReverseProxyTester.class.wait();
				System.out.println("Tester ends");
			}catch(InterruptedException ex) {
		    	Thread.currentThread().interrupt();
			}
		}
	}
}