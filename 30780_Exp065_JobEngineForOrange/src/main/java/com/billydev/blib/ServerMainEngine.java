package com.billydev.blib;

import com.billydev.blib.common.CommonMessageQueue;

public class ServerMainEngine {
	
	public static void main(String []args) {
		
       final CommonMessageQueue mainMessageQueue = new CommonMessageQueue();		
       /*
        * todo:use thread pool in future
        */
       new ServerMaintenanceThread("Maintenance Thread", mainMessageQueue).start();
       new ServerSchedulerMainThread("Scheduler Main Thread", mainMessageQueue).start(); 
       new ServerDequeThread("Main Thread", mainMessageQueue).start(); 	
       new ServerListenerThread("Http Main Thread", mainMessageQueue).start();			

	}
}
