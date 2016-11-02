package it.ing.unipi.anaws.coapclient;

import java.util.Random;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class ReverseProxyTester {
	  
	public static void checkResponse(CoapResponse response){
		
		if (response!=null) {
			// access advanced API with access to more details through .advanced()
			System.out.println(Utils.prettyPrint(response));
			System.out.println("\n");
			
		} else {
			System.out.println("No response received\n");
		}
	}
	
	public static void main(String args[]) {
			
		CoapClient client = new CoapClient();
		//client.useNONs();
		CoapResponse response;
		
		Random rand = new Random();
		int whichRequest;				
			
		for (int i = 0; i < 50; i++){
				
			whichRequest = rand.nextInt(11);
				
			switch(whichRequest){
					
				//get accelerometer
				case 1: System.out.println("GET ACCELEROMETER\n");
						client.setURI("coap://[aaaa::c30c:0:0:2]:5683/accelerometer");
						response = client.get();
						checkResponse(response);	
				break;
					
				//get temperature
				case 2: System.out.println("GET TEMPERATURE\n");
						client.setURI("coap://[aaaa::c30c:0:0:4]:5683/temperature");
						response = client.get();
						checkResponse(response);
				break;

				//post toggle
				case 3: System.out.println("POST TOGGLE\n");
						client.setURI("coap://[aaaa::c30c:0:0:5]:5683/toggle");
						response = client.post("", MediaTypeRegistry.TEXT_PLAIN);
						checkResponse(response);
				break;

				//put toggle
				case 4: System.out.println("PUT TOGGLE\n");
						client.setURI("coap://[aaaa::c30c:0:0:5]:5683/toggle");
						response = client.put("", MediaTypeRegistry.TEXT_PLAIN);
						checkResponse(response);
				break;

				//post leds r on
				case 5: System.out.println("POST LEDS (r, on)\n");
						client.setURI("coap://[aaaa::c30c:0:0:3]:5683/leds?color=r");
						response = client.post("mode=on", MediaTypeRegistry.TEXT_PLAIN);
						checkResponse(response);
				break;

				//post leds r off
				case 6: System.out.println("POST LEDS (r, off)\n");
						client.setURI("coap://[aaaa::c30c:0:0:3]:5683/leds?color=r");
						response = client.post("mode=off", MediaTypeRegistry.TEXT_PLAIN);
						checkResponse(response);
				break;

				//put leds g on
				case 7: System.out.println("PUT LEDS (g, on)\n");
						client.setURI("coap://[aaaa::c30c:0:0:3]:5683/leds?color=g");
						response = client.put("mode=on", MediaTypeRegistry.TEXT_PLAIN);
						checkResponse(response);
				break;
					
				//put leds g off
				case 8: System.out.println("PUT LEDS (g, off)\n");
						client.setURI("coap://[aaaa::c30c:0:0:3]:5683/leds?color=g");
						response = client.put("mode=off", MediaTypeRegistry.TEXT_PLAIN);
						checkResponse(response);
				break;

				//post leds b on
				case 9: System.out.println("POST LEDS (b, on)\n");
						client.setURI("coap://[aaaa::c30c:0:0:3]:5683/leds?color=b");
						response = client.post("mode=on", MediaTypeRegistry.TEXT_PLAIN);
						checkResponse(response);
				break;

				//post leds b off
				case 10:System.out.println("POST LEDS (b, off)\n");
						client.setURI("coap://[aaaa::c30c:0:0:3]:5683/leds?color=b");
						response = client.post("mode=off", MediaTypeRegistry.TEXT_PLAIN);
						checkResponse(response);
			}
			/*
			try {
			    Thread.sleep(2000);
			} catch(InterruptedException ex) {
			    Thread.currentThread().interrupt();
			}*/
		}
	}
}

