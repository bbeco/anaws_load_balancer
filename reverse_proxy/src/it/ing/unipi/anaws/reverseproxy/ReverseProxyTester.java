package it.ing.unipi.anaws.reverseproxy;

import java.util.Random;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class ReverseProxyTester {
	
	private final static int REQUESTS_NUMBER = 46;
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
		
		int temp = 25;
		
		for (int i = 0; i < REQUESTS_NUMBER; i++) {
				
			whichRequest = rand.nextInt(4) + 1;
			
			//For the third scenario
			/*
			if (whichRequest == 1) {
				whichRequest = 2;
			} else if (whichRequest == 3) {
				whichRequest = 4;
			}
			*/
			
			switch(whichRequest) {
				
				//accelerometer
				case 1:
					System.out.println("GET ACCELEROMETER " + counter++ + "\n");
					client.setURI("coap://[::1]:5683/accelerometer");
					client.get(ch);
				break;
					
				//temperature
				case 2:
					System.out.println("GET TEMPERATURE\n");
					client.setURI("coap://[::1]:5683/temperature");
					client.get(ch);
				break;

				//toggle
				case 3:
					if (rand.nextInt(2) == 0) {
						System.out.println("POST TOGGLE\n");
						client.setURI("coap://[::1]:5683/toggle");
						client.post(ch , "" , MediaTypeRegistry.TEXT_PLAIN);
					} else {
						System.out.println("PUT TOGGLE\n");
						client.setURI("coap://[::1]:5683/toggle");
						client.put(ch, "" , MediaTypeRegistry.TEXT_PLAIN);
					}
				break;

				//leds
				case 4: 
					String mode = (rand.nextInt(2) == 0) ? "off" : "on";
					String color = "";
					switch(rand.nextInt(3)) {
					case 0:
						color = "r";
						break;
					case 1:
						color = "g";
						break;
					case 2:
						color = "b";
						break;
					}
					if (rand.nextInt(2) == 0) {
						System.out.println("POST LEDS (" + color + ", " + mode + ")\n");
						client.setURI("coap://[::1]:5683/leds");
						client.post(ch, color + "," + mode , MediaTypeRegistry.TEXT_PLAIN);
					} else {
						System.out.println("PUT LEDS (" + color + ", " + mode + ")\n");
						client.setURI("coap://[::1]:5683/leds");
						client.put(ch, color + "," + mode , MediaTypeRegistry.TEXT_PLAIN);
						
					}
				break;
				
				case 5:
					System.out.println("GET TEMPERATURE\n");
					client.setURI("coap://[::1]:5683/temperature");
					client.get(ch);
					String temperature = Integer.toString(++temp);
					System.out.println("PUT TEMPERATURE\n");
					client.put(ch, temperature, MediaTypeRegistry.TEXT_PLAIN);
					System.out.println("GET TEMPERATURE\n");
					client.get(ch);
					temperature = Integer.toString(++temp);
					System.out.println("POST TEMPERATURE\n");
					client.post(ch, temperature, MediaTypeRegistry.TEXT_PLAIN);
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