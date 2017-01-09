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
public abstract class VirtualResource<T extends Device> extends ConcurrentCoapResource {
	private static final int THREADS_NUMBER = 4;//number of threads that can handle request on a single resource
	
	/* pool of server for the specific resource T */
    private ArrayList<T> dev_list;
    
    //total number of served requests for  each virtual resource at the moment
  	private int tot_req;
  	
  	//total number of possible requests that this virtual resource can handle in a cycle
  	private int cycle;
    
  	/*
  	 * When this timer expires, the reverse proxy performs a battery status check.
  	 */
  	private Timer mTimer;
  	private BatteryTimer mTimerTask;
  	
  	private class BatteryTimer extends TimerTask {

		@Override
		public void run() {
			init();
		}
  		
  	}
  	
    public VirtualResource(String name, String title, ArrayList<T> devs) {
        
        // set resource identifier, set pool of threads
        super(name, THREADS_NUMBER);
       
        // set display name
        getAttributes().setTitle(title);
        
        dev_list = devs;
        tot_req = 0;
        cycle = 0;
        
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
    	System.out.println("--- SETTING PARAMETERS ---");
    	System.out.print("Remaining requests :	");
    	for(int i = 0; i < dev_list.size(); i++){
    		if(dev_list.get(i).battery.charge>10){
    			dev_list.get(i).req = dev_list.get(i).battery.charge/10;
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
    	
    	//System.out.println("Served by thread : " + Thread.currentThread().getName());
    	
    	/* We send an ACK back for each request. Some requests may not be fulfilled because of device availability */
    	exchange.sendAccept();
    	
    	synchronized(this){
    		super.handleRequest(exchange);
    	}
	}
    
    /* This method is explicitly synchronized because it can be 
     * called by the timerTask too (that is executed by a 
     * different thread).
     */
    public synchronized void init() {
    	checkBatteryStatus();
        orderDevices();
        computeCycle();
        tot_req = 0;
    }
    
    private void checkBatteryStatus() {
    	
    	//resetting timer
    	mTimerTask.cancel();
    	mTimerTask = new BatteryTimer();
    	mTimer.schedule(mTimerTask, 4*60*60*1000); //4 hours
    	
    	Iterator<T> iter = (Iterator<T>) dev_list.iterator();
    	while(iter.hasNext()) {
    		Device dev = iter.next();
    		int maxTry = 5;
    		int charge = -1;
    		// we try to obtain the battery status 5 times at most
    		while((charge == -1) && (maxTry > 0)){
    			maxTry--;
    			charge = dev.BatteryGet();
    			
  		 	}
    		System.out.println("Battery Status:" + charge + " max try:" + maxTry);
    		
    		/* We were unable to get the battery status,
    		 * assume that the device is disconnected.
    		 */
    		if((charge == -1) && maxTry == 0) {
    			System.out.println("Server id " + dev.ID + " : Impossible to get charge, Server Disconnected");
    			iter.remove();//remove current device from the list
    		}
    		
    		/* Remove the devices whose charge is too low */
    		if(charge <= 5) {
    			System.out.println("Server id " + dev.ID + " : Server discharged");
    			iter.remove();
    		}
    	}
    }
    
    private void computeCycle() {
    	
    	cycle = 0;
    	for( int i = 0; i < dev_list.size(); i++) {
    		cycle += dev_list.get(i).req;
    	}
    }
    
    protected Device chooseDevice() {
    	int i;

    	//requests cycle is over
    	if(cycle == 0){
    		return null;
    	}
    	if(tot_req == cycle){
    		System.out.println("Cycle is over");
    		init();
    	}

    	/* Choose the server to send the request to.
    	 * The first device in the list that still has remaining requests for the current cycle is chosen.
    	 * The list is ordered by decreasing battery charge percentages.
    	 */
    	for(i = 0; i < dev_list.size(); i++){
    		if(dev_list.get(i).req != 0){
    			dev_list.get(i).req--;
    			System.out.println("--- DEVICE SELECTION ---");
    			System.out.println("Number of total requests : " + ++tot_req);
    			System.out.println("Device selected : " + i);
    			System.out.println("Server id : " + dev_list.get(i).ID );
    			System.out.print("Remaining requests :	");
    			for(int j = 0; j < dev_list.size(); j++) {
    				System.out.print(dev_list.get(j).req);
    				System.out.print("	");
    			}
    			System.out.println();
    			
    			return dev_list.get(i);
    		}
    	}

    	//there are no available devices
    	return null;
    }
}
