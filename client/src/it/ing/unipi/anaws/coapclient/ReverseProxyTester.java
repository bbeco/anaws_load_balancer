package it.ing.unipi.anaws.coapclient;

import java.util.Random;

import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.Utils;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

public class ReverseProxyTester {
	

	private static void checkresponse(CoapResponse response){
		if(response != null){
			// access advanced API with access to more details through .advanced()
			System.out.println(Utils.prettyPrint(response));				
			System.out.println("\n");
		}
		else{
			System.out.println("No response received\n");
		}
	}

	public static void main(String args[]) {
			
		CoapClient client = new CoapClient();
		CoapResponse re;
		//client.useNONs();
		
		Random rand = new Random();
		int whichRequest;				
			
		for (int i = 0; i < 50; i++){
				
			whichRequest = rand.nextInt(10) + 1;
				
			switch(whichRequest){
					
				//get accelerometer
				case 1: System.out.println("GET ACCELEROMETER\n");
						client.setURI("coap://[::1]:5683/accelerometer");
						re = client.get();	
						checkresponse(re);
				break;
					
				//get temperature
				case 2: System.out.println("GET TEMPERATURE\n");
						client.setURI("coap://[::1]:5683/temperature");
						re = client.get();
						checkresponse(re);
				break;

				//post toggle
				case 3: System.out.println("POST TOGGLE\n");
						client.setURI("coap://[::1]:5683/toggle");
						re = client.post("", MediaTypeRegistry.TEXT_PLAIN);
						checkresponse(re);
				break;

				//put toggle
				case 4: System.out.println("PUT TOGGLE\n");
						client.setURI("coap://[::1]:5683/toggle");
						re = client.put("", MediaTypeRegistry.TEXT_PLAIN);
						checkresponse(re);
				break;

				//post leds r on
				case 5: System.out.println("POST LEDS (r, on)\n");
						client.setURI("coap://[::1]:5683/leds");
						re = client.post("r,on", MediaTypeRegistry.TEXT_PLAIN);
						checkresponse(re);
				break;

				//post leds r off
				case 6: System.out.println("POST LEDS (r, off)\n");
						client.setURI("coap://[::1]:5683/leds");
						re = client.post("r,off", MediaTypeRegistry.TEXT_PLAIN);
						checkresponse(re);
				break;

				//put leds g on
				case 7: System.out.println("PUT LEDS (g, on)\n");
						client.setURI("coap://[::1]:5683/leds");
						re = client.put("g,on", MediaTypeRegistry.TEXT_PLAIN);
						checkresponse(re);
				break;
					
				//put leds g off
				case 8: System.out.println("PUT LEDS (g, off)\n");
						client.setURI("coap://[::1]:5683/leds");
						re = client.put("g,off", MediaTypeRegistry.TEXT_PLAIN);
						checkresponse(re);
				break;

				//post leds b on
				case 9: System.out.println("POST LEDS (b, on)\n");
						client.setURI("coap://[::1]:5683/leds");
						re = client.post("b,on", MediaTypeRegistry.TEXT_PLAIN);
						checkresponse(re);		
				break;

				//post leds b off
				case 10:System.out.println("POST LEDS (b, off)\n");
						client.setURI("coap://[::1]:5683/leds");
						re = client.post("b,off", MediaTypeRegistry.TEXT_PLAIN);
						checkresponse(re);
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

