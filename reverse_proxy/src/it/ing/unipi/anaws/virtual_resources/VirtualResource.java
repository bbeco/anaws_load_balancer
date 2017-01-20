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
	private static final int THREADS_NUMBER = 4;//number of threads that can handle request on a single resource
	
	/* pool of server for the specific resource T */
    private ArrayList<Device> dev_list;
    
    /* Number of served request (for every possible resource).
     * This was made static because it is common to every virtual resource.
     * XXX It must be updated in mutual exclusive access only!
     */
  	protected static int tot_req;
  	
  	/* total number of possible requests that can be handled in a cycle
  	 * The same constraints as tot_req apply for this.
  	 */
  	protected static int cycle;
    
  	/*specify the type of the resource*/
  	protected String type;
  	
  	/*
  	 * When this timer expires, the reverse proxy performs a battery status check.
  	 * TODO questo deve essere a comune tra le resources (come tot_req e cycle)
  	 */
  	private Timer mTimer;
  	private BatteryTimer mTimerTask;
  	
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
        
        dev_list = devs;
        
        mTimer = new Timer(true);
        mTimerTask = new BatteryTimer();
    }
    
    /**
     * Sort the devices pool accordingly to the devices battery status
     * and assign the number of request for each device.
     */
    private void orderDevices() {
    
    	Collections.sort(dev_list, new DeviceComparatorByCharge());
    	
    	if(dev_list.size() == 0){
    		return;
    	}
    	
    	System.out.print("Requests for devices in this cycle :\t");
    	for(int i = 0; i < dev_list.size(); i++){
    		if(dev_list.get(i).getBattery().charge > 10){
    			dev_list.get(i).req = dev_list.get(i).getBattery().charge/10;
    		} else {
    			/* We avoid to overload an almost discharged server without penalizing load balancing
    			 */
    			dev_list.get(i).req = 1;
    		}
    		System.out.print(dev_list.get(i).req + "\t");
    	}
    	System.out.println();
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
    
    /* FIXME penso si possa togliere la mutua esclusione qui
     * 
     * This method is explicitly synchronized because it can be 
     * called by the timerTask too (that is executed by a 
     * different thread).
     */
    public synchronized void init() {
    	System.out.println("\n--- New cycle starts for " + type + " devices ---");
    	checkBatteryStatus();
        orderDevices();
        computeCycle();
        tot_req = 0;
    }
    
    /* TODO deve essere sincronizzato sulla lista dei device
     * perchè accede a mTimerTask (che dovrebbe essere comune a tutte le risorse)
     */
    private void checkBatteryStatus() {
    	
    	//resetting timer
    	mTimerTask.cancel();
    	mTimerTask = new BatteryTimer();
    	mTimer.schedule(mTimerTask, 4*60*60*1000); //4 hours
    	
    	Iterator<Device> iter = dev_list.iterator();
    	while(iter.hasNext()) {
    		Device dev = iter.next();
    		int charge = -1;
    		charge = dev.getBattery().Get();
    		
    		System.out.println("Battery Status of server " + dev.ID + " : " + charge);
    		
    		/* We were unable to get the battery status,
    		 * assume that the device is disconnected.
    		 */
    		if(charge == -1) {
    			System.out.println("Server id " + dev.ID + " : Impossible to get charge, Server Disconnected");
    			iter.remove();//remove current device from the list
    			continue;
    		}
    		
    		/* Remove the devices whose charge is too low */
    		if(charge <= 5) {
    			System.out.println("Server id " + dev.ID + " : Server discharged");
    			iter.remove();
    		}
    	}
    }
    
    /*
     * XXX
     * No need for this to be synchronized because it is called
     * either by 1 thread (during reverse proxy initialization), or
     * by a single thread who has accessed the synchronized method 
     * chooseDevice()
     */
    private void computeCycle() {
    	cycle = 0;
    	for( int i = 0; i < dev_list.size(); i++) {
    		cycle += dev_list.get(i).req;
    	}
    }
    
    /*
     * This method is synchronized on the shared object dev_list.
     * Since this method updates cycle, tot_req and some other common values,
     * it can be accessed in mutual exclusion only and the shared object's lock
     * guarantees that.
     */
    protected Device chooseDevice() {
    	synchronized (dev_list) {
	    	int i;
	    	
	    	//no servers available
	    	if(cycle == 0){
	    		return null;
	    	}
	    	//requests cycle is over
	    	if(tot_req == cycle){
	    		System.out.println("\n--- Cycle is over for " + type + " devices ---");
	    		init();
	    	}
	
	    	/* Choose the server to send the request to.
	    	 * The first device in the list that still has remaining requests for the current cycle is chosen.
	    	 * The list is ordered by decreasing battery charge percentages.
	    	 */
	    	for(i = 0; i < dev_list.size(); i++){
	    		if(dev_list.get(i).req != 0){
	    			dev_list.get(i).req--;
	    			System.out.println("\n--- Selection of a " + type + " device ---");
	    			System.out.println("Number of current total requests : " + ++tot_req);
	    			System.out.println("Selected server id : " + dev_list.get(i).ID);
	    			System.out.print("Remaining requests for " + type + " devices in this cycle :\t");
	    			for(int j = 0; j < dev_list.size(); j++) {
	    				System.out.print(dev_list.get(j).req + "\t");
	    			}
	    			System.out.println("");
	    			
	    			return dev_list.get(i);
	    		}
	    	}
	
	    	//there are no available devices
	    	return null;
	    }
    }
}
