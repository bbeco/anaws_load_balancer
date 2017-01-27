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
	
	/* number of threads that can handle request on a single resource */
	protected static final int THREADS_NUMBER = 4;
	
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
  	 * When this timer expires, the reverse proxy performs a battery status 
  	 * check.
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
        
        /* only the first virtual resource created instantiates the timer and 
         * takes the devices.
         */
        /* TODO questo if per dev_list non dovrebbe servire. */
        //if(dev_list == null)
        dev_list = devs;
        
        if(mTimer == null)
        	mTimer =  new Timer(true);
        if(mTimerTask == null)
        	mTimerTask = new BatteryTimer();
        
        tot_req = 0;
    }
    
    @Override
	public void handleRequest(final Exchange exchange) {

    	/* We send an ACK back for each request. Some requests may not be 
    	 * fulfilled because of device availability.
    	 * Accordingly to the Californium documentation, this sends an ACK only
    	 * if the request was carried in a CON message.
    	 */
    	exchange.sendAccept();
    	
    	/* The following block is synchronized so that only one of the 
    	 * THREADS_NUMBER threads can access this resource at a time. By doing 
    	 * so we add a coherence mechanism for the resources status.
    	 */
    	synchronized(this){
    		System.out.println("\n" + exchange.getRequest().getCode() + " on " + exchange.getRequest().getURI() + " starts");
    		super.handleRequest(exchange);
    	}
	}
    
    /* 
     * This method is explicitly synchronized because it can be called by the 
     * timerTask too (that is executed by a different thread).
     * Called only at the beginning and when timer expires
     */
    public void init(){
    	synchronized(dev_list){
	    	System.out.println("\n--- Starting new session ---");
	    	checkAllBatteryStatus();
	    	orderDevices();
	    	printRequests();
	    	System.out.println();
    	}
    }
    
    /**
     * Sort the devices pool accordingly to the devices battery status
     * and assign the number of request for each device.
     * 
     * This method is called by {@link #init()} or {@link #chooseDevice()} which
     * are synchronized on the device list, so there is no nedd for it to be 
     * synchronized.
     */
    private void orderDevices() {
    
    	Collections.sort(dev_list, new DeviceComparatorByCharge());
   	}
  
    /* check the battery status for motes with a specified resource */
    /**
     * This method check the battery status of every mote that exposes the given
     *  resource.
     *  This method is called by {@link #chooseDevice()} or {@link #init()} 
     *  which are both synchronized on the shared device list, so it does not 
     *  need to be synchronized.
     *  
     * @param resType the resource type a device must expose in order to be 
     * updated by this method.
     */
    private void checkBatteryStatus(String resType){
    	
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
	    		} else {
	    			System.out.println("Battery Status of server " + dev.getID() + " : " + charge + " (" + resType + ")");
	    			//Update remaining requests 
	    			if(dev.getBattery().getCharge() > 10){
	        			dev.setLeftRequests(charge/10);
	        		} else {
	        			/* We avoid to overload an almost discharged server 
	        			 * without penalizing load balancing.
	        			 */
	        			dev.setLeftRequests(1);
	        		}
	    		}
    		}
    	}
    }
    
    /**
     * This method updated the battery of every mote.
     * This updated the timer status so that it this method is called 
     * periodically.
     * 
     * Even though this method modifies mTimerTask which is shared by many 
     * instances of this class, it does not need to be synchronized, since 
     * mTimerTask is updated only here and this method is always called by a 
     * single thread at a time. 
     */
    private void checkAllBatteryStatus() {
    	
    	//resetting timer
    	mTimerTask.cancel();
    	mTimerTask = new BatteryTimer();
    	mTimer.schedule(mTimerTask, 4*60*60*1000); //4 hours
    	
    	//check battery of all motes
    	checkBatteryStatus("Accelerometer");
    	checkBatteryStatus("Temperature");
    	checkBatteryStatus("Leds");
    	checkBatteryStatus("Toggle");
    }
    
    /*
     * This method is synchronized on the shared object dev_list.
     * Since this method updates cycle, tot_req and some other common values,
     * it can be accessed in mutual exclusion only and the shared object's lock
     * guarantees that.
     */
    protected Device chooseDevice() {
    	synchronized (dev_list) {
	   	
    		ArrayList<Device> targetServers = new ArrayList<Device>();
    		int i;
    		
    		/* TargetServers is filled with devices that own a resource the 
    		 * request can be forwarded to.
    		 */
    		for(i = 0; i < dev_list.size(); i++){
    			if(dev_list.get(i).deviceHasThisResource(type)){
    				targetServers.add(dev_list.get(i));
    			}
    		}
    		
	    	// if there is a server that can handle the request
	    	if(targetServers.size() > 0){
		    	/* Choose the server to send the request to.
		    	 * The first device in the list that still has some requests 
		    	 * left is chosen.
		    	 * The list is ordered by decreasing battery charge percentages.
		    	 * We are sure that at least one device with the requested 
		    	 * resource is available.
		    	 */
		    	for(i = 0; i < targetServers.size(); i++){
		    		Device aux = targetServers.get(i);		    		
		    		if(aux.getLeftRequests() != 0){
		    			tot_req++;
		    			aux.decrementLeftRequests();
		    			System.out.println("\n--- Selection of a " + type + " device ---");
		    			System.out.println("Number of current total requests : " + tot_req);
		    			System.out.println("Selected server id : " + aux.getID());
		    			printRequests();
		    			return aux;
		    		}
		    	}
	    	
		    	/* No device has been chosen in the previous loop. There are no 
		    	 * device with request left. It's time to update the request by 
		    	 * checking the battery for each device with the requested 
		    	 * resource.
		    	 */
		    	if(i == targetServers.size()){
		    		System.out.println("\n--- Update of " + type + " devices ---");
		    		checkBatteryStatus(type);
		    		orderDevices();
		    		printRequests();
	    			//call again chooseDevice since servers have been updated
	    			return chooseDevice();
		    	}
	    	}
	    	
	    	return null;//no server available
	    }
    }
    
    /**
     * This method prints the number of requests left for each resource.
     */
    private void printRequests(){
    	
    	ArrayList<Integer> acc_req 	= new ArrayList<Integer>();
    	ArrayList<Integer> temp_req = new ArrayList<Integer>();
    	ArrayList<Integer> led_req 	= new ArrayList<Integer>();
    	ArrayList<Integer> tog_req 	= new ArrayList<Integer>();
    	
    	for(int i = 0; i < dev_list.size(); i ++){
    		Device aux = dev_list.get(i);
    		if(aux.deviceHasThisResource("Accelerometer"))
    			acc_req.add(aux.getLeftRequests());
    		if(aux.deviceHasThisResource("Temperature"))
    			temp_req.add(aux.getLeftRequests());
    		if(aux.deviceHasThisResource("Leds"))
    			led_req.add(aux.getLeftRequests());
    		if(aux.deviceHasThisResource("Toggle"))
    			tog_req.add(aux.getLeftRequests());
    	}
    	
    	System.out.print("\nRemaining requests for accelerometer devices :\t");
    	for(int i = 0; i < acc_req.size(); i++) {
			System.out.print(acc_req.get(i) + "\t");
		}
		System.out.println("");
		
		System.out.print("Remaining requests for temperature devices :\t");
    	for(int i = 0; i < temp_req.size(); i++) {
			System.out.print(temp_req.get(i) + "\t");
		}
		System.out.println("");
		
		System.out.print("Remaining requests for leds devices :\t\t");
    	for(int i = 0; i < led_req.size(); i++) {
			System.out.print(led_req.get(i) + "\t");
		}
		System.out.println("");
		
		System.out.print("Remaining requests for toggle devices :\t\t");
    	for(int i = 0; i < tog_req.size(); i++) {
			System.out.print(tog_req.get(i) + "\t");
		}
		System.out.println("");
    }
}
