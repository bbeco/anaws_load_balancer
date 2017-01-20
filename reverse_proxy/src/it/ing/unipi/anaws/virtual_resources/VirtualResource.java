package it.ing.unipi.anaws.virtual_resources;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.californium.core.network.Exchange;
import org.eclipse.californium.core.server.resources.ConcurrentCoapResource;

import it.ing.unipi.anaws.devices.Device;
import it.ing.unipi.anaws.devices.DeviceComparatorByCharge;

/**
 * Base class for every possible virtual resource exposed by the proxy.
 */
public abstract class VirtualResource extends ConcurrentCoapResource {
	
	protected static final int THREADS_NUMBER = 4;//number of threads that can handle request on a single resource
	
	/* pool of server for the specific resource T */
    protected static ArrayList<Device> dev_list;
    
    /* Number of served request (for every possible resource).
     * This was made static because it is common to every virtual resource.
     * XXX It must be updated in mutual exclusive access only!
     */
  	protected static int tot_req;
    
  	/*specify the type of the resource*/
  	protected String type;
  	
  	/*
  	 * When this timer expires, the reverse proxy performs a battery status check.
  	 * TODO questo deve essere a comune tra le resources (come tot_req e cycle)
  	 */
  	protected static Timer mTimer;
  	protected static BatteryTimer mTimerTask;
  	
  	private class BatteryTimer extends TimerTask {
		@Override
		public void run() {
			init();
		}	
  	}
  	
    public VirtualResource(String name, String title, ArrayList<Device> devs) {
        
        // set resource identifier, set pool of threads
        super(name, THREADS_NUMBER);
       
        // set display name
        getAttributes().setTitle(title);
        
        //only the first virtual resource created instantiates the timer and takes the devices 
        if(dev_list == null)
        	dev_list = devs;
        if(mTimer == null)
        	mTimer =  new Timer(true);
        if(mTimerTask == null)
        	mTimerTask = new BatteryTimer();
    }
    
    @Override
	public void handleRequest(final Exchange exchange) {
    		
    	/* We send an ACK back for each request. Some requests may not be fulfilled because of device availability */
    	exchange.sendAccept();
    	
    	synchronized(this){
    		System.out.println("\n" + exchange.getRequest().getCode() + " on " + exchange.getRequest().getURI() + " starts");
    		super.handleRequest(exchange);
    	}
	}
    
    /* 
     * This method is explicitly synchronized because it can be 
     * called by the timerTask too (that is executed by a 
     * different thread).
     * Called only at the beginning and when timer expires
     */
    public void init() {
    	synchronized(dev_list){
	    	System.out.println("\n--- Starting new session ---");
	    	checkAllBatteryStatus();
	    	orderDevices();
	    	System.out.print("Requests for devices :\t");
	    	for(int i = 0; i < dev_list.size(); i++){
	    		System.out.print(dev_list.get(i).getLeftRequests() + "\t");
	    	}
	    	System.out.println();
	    	tot_req = 0;
    	}
    }
    
    /**
     * Sort the devices pool accordingly to the devices battery status
     * and assign the number of request for each device.
     */
    private void orderDevices() {
    
    	Collections.sort(dev_list, new DeviceComparatorByCharge());
   	}
  
    /* check the battery status for a mote with a specified resource */
    private void checkBatteryStatus(int resType){
    	
    	Iterator<Device> iter = dev_list.iterator();
    	while(iter.hasNext()) {
    		Device dev = iter.next();
    		if(dev.deviceHasThisResource(resType)){
	    		int charge = -1;
	    		charge = dev.getBattery().Get();
	 
	    		/* We were unable to get the battery status,
	    		 * assume that the device is disconnected.
	    		 */
	    		if(charge == -1) {
	    			System.out.println("Server id " + dev.getID() + " : Impossible to get charge, Server Disconnected");
	    			iter.remove();//remove current device from the list
	    			continue;
	    		}
	    		
	    		/* Remove the devices whose charge is too low */
	    		if(charge <= 5) {
	    			System.out.println("Server id " + dev.getID() + " : Server discharged");
	    			iter.remove();
	    		}
	    		else{
	    			System.out.println("Battery Status of server " + dev.getID() + " : " + charge);
	    			//Update remaining requests 
	    			if(dev.getBattery().getCharge() > 10){
	        			dev.setLeftRequests(charge/10);
	        		} else {
	        			/* We avoid to overload an almost discharged server without penalizing load balancing
	        			 */
	        			dev.setLeftRequests(1);
	        		}
	    		}
    		}
    	}
    }
    
    /* TODO deve essere sincronizzato sulla lista dei device
     * perchè accede a mTimerTask (che dovrebbe essere comune a tutte le risorse)
     */
    private void checkAllBatteryStatus() {
    	
    	//resetting timer
    	mTimerTask.cancel();//TODO forse non serve perche quando entra qua il timer e scaduto
    	mTimerTask = new BatteryTimer();
    	mTimer.schedule(mTimerTask, 4*60*60*1000); //4 hours
    	
    	//check battery of all motes (TODO l unico problema e che se un mote ha due risorse viene controllato due volte)
    	checkBatteryStatus(0);
    	checkBatteryStatus(1);
    	checkBatteryStatus(2);
    	checkBatteryStatus(3);
    }
    
    /*
     * This method is synchronized on the shared object dev_list.
     * Since this method updates cycle, tot_req and some other common values,
     * it can be accessed in mutual exclusion only and the shared object's lock
     * guarantees that.
     */
    protected Device chooseDevice(int resType) { //0 accelerometer, 1 temperature, 2 leds, 3 toggle
    	synchronized (dev_list) {
	   	
    		int i;
    		
    		//search an available server for the requested resource
	    	boolean serverFound = false;
	    	for(i = 0; i < dev_list.size(); i++){
	    		if(dev_list.get(i).deviceHasThisResource(resType)){
	    			serverFound = true;
	    		}
	    	}
	    	
	    	if(serverFound == true){
		    	/* Choose the server to send the request to.
		    	 * The first device in the list that still has remaining requests for the current cycle is chosen.
		    	 * The list is ordered by decreasing battery charge percentages.
		    	 * We are sure that at least one device with the requested resource is available
		    	 */
		    	for(i = 0; i < dev_list.size(); i++){
		    		if(dev_list.get(i).getLeftRequests() != 0 && dev_list.get(i).deviceHasThisResource(resType)){
		    			dev_list.get(i).decrementLeftRequests();
		    			System.out.println("\n--- Selection of a " + type + " device ---");
		    			System.out.println("Number of current total requests : " + ++tot_req);
		    			System.out.println("Selected server id : " + dev_list.get(i).getID());
		    			System.out.print("Remaining requests for devices :\t");
		    			for(int j = 0; j < dev_list.size(); j++) {
		    				System.out.print(dev_list.get(j).getLeftRequests() + "\t");
		    			}
		    			System.out.println("");
		    			
		    			return dev_list.get(i);
		    		}
		    	}
	    	
		    	//All the available device for the requested resource finished the requests in the cycle
		    	if(i == dev_list.size()){
		    		checkBatteryStatus(resType);
		    		orderDevices();
		    		System.out.println("--- Updated " + type + " devices");
		    		System.out.print("Remaining requests for devices :\t");
	    			for(int j = 0; j < dev_list.size(); j++) {
	    				System.out.print(dev_list.get(j).getLeftRequests() + "\t");
	    			}
	    			System.out.println("");
	    			
	    			//call again chooseDevice since servers have been updated
	    			return chooseDevice(resType);
		    	}
	    	}
	    	
	    	return null;//no server available
	    }
    }
}
