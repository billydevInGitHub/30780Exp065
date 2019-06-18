package com.billydev.blib;

import java.util.Date;

import com.billydev.blib.common.CommonConfiguration;
import com.billydev.blib.common.CommonMessageQueue;
import com.billydev.blib.common.CommonMsgInQueue;
import com.billydev.blib.common.CommonUtils;

public class ClientHeartBeatThread extends Thread {

	private String threadGivenName;
	CommonMessageQueue messageQueue;
	
	public ClientHeartBeatThread(String inputName, CommonMessageQueue messageQueueInput) {
		threadGivenName=inputName; 
		messageQueue=messageQueueInput; 			
	}
	
	public void run() {
	 while(true) {
		System.out.println("ClientHeartBeatThread is running...");
		try {
			Thread.sleep(CommonConfiguration.CLIENT_HEART_BEAT_INTERVAL_IN_MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} 
		//temp: just remove the enqueue of heart beat stuff 
		synchronized(messageQueue) {
			//heart beat thread only do the enqueue of AgentMessageQueue
			CommonMsgInQueue heartBeatMsg= new CommonMsgInQueue(); 
			heartBeatMsg.setMsgType(CommonConfiguration.MSG_CLIENT_HEART_BEAT);
			
			messageQueue.getMessageQueue().offer(heartBeatMsg); 
			System.out.println("ClientHeartBeatThread has enqued a Heatbeat message, MMQ peek()is:"
						+CommonUtils.getMMQInfo(messageQueue)+" timestamp:["+ new Date()+"]"); 
			messageQueue.notifyAll();
		}
	 }
	}
	
}
