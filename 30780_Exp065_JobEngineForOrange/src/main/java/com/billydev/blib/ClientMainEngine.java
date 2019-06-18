package com.billydev.blib;

import com.billydev.blib.common.CommonConfiguration;
import com.billydev.blib.common.CommonMessageQueue;

public class ClientMainEngine {


	
	public static void main(String[] args) {
		
		CommonMessageQueue messageQueue=new CommonMessageQueue(); 
		
	    new ClientHeartBeatThread(CommonConfiguration.CLIENT_HEART_BEAT_THREAD_NAME, messageQueue).start();
		
	    new ClientDequeThread(CommonConfiguration.CLIENT_DEQUEUE_THREAD_NAME, messageQueue).start();
	    
	    new ClientListenerThread(CommonConfiguration.CLIENT_LISTENER_THREAD_NAME, messageQueue).start();
	}
}
