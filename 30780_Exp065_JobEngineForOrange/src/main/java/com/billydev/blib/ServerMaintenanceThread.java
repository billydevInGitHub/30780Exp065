package com.billydev.blib;

import com.billydev.blib.common.CommonConfiguration;
import com.billydev.blib.common.CommonMessageQueue;
import com.billydev.blib.common.CommonMsgInQueue;
import com.billydev.blib.common.CommonUtils;

public class ServerMaintenanceThread extends Thread {
	String threadGivenName;		
	CommonMessageQueue mainMessageQueue; 
	
	public ServerMaintenanceThread(String inputName , CommonMessageQueue mainMessageQueue) {
		threadGivenName=inputName;
		this.mainMessageQueue=mainMessageQueue; 
	}				

	public void run() {				   
		    try {
		    	/*
		    	 * todo: we just do not use the maintenance thread for now
		    	 */
				Thread.sleep(800000000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized(mainMessageQueue) {	
				    /*
				     * adding a cancel job message through hard coding  
				     */
//				    CommonMsgInQueue msgInQueue=new CommonMsgInQueue();
//				    msgInQueue.setJobId(10);
//				    msgInQueue.setAction(CommonConfiguration.MSG_JOB_ACTION_CANCEL_A_JOB);
				    
				    /*
				     * hardcode a new job message and enque it 
				     * 
				     */
					CommonMsgInQueue msgInQueue=new CommonMsgInQueue();
					msgInQueue.setJobId(10);  //this is pure temp hardcoding 
					msgInQueue.setMsgType(CommonConfiguration.MSG_JOB_ACTION_CREATE_A_NEW_JOB);
					msgInQueue.setTarget(CommonConfiguration.CLIENT_MACHINE_ADDRESS);
					msgInQueue.setTargetPort(CommonConfiguration.CLIENT_LISTEN_PORT);
					msgInQueue.setJobScript("/home/billy/billydev/tempdev/181123-1-30800Exp003_ControlLongRunningProcess/endlessloop.sh");
		    
				    
				    
					mainMessageQueue.getMessageQueue().add(msgInQueue);
					System.out.println("MaintenanceThread is accessing the  main message queue!  MMQ is:"
							+CommonUtils.getMMQInfo(mainMessageQueue));
					mainMessageQueue.notifyAll();
			}//end of sync
			
		    try {
		    	/*
		    	 * now cancel the job 
		    	 */
				Thread.sleep(100000000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			synchronized(mainMessageQueue) {	
				    /*
				     * adding a cancel job message through hard coding  
				     */

				    /*
				     * hardcode a new job message and enque it 
				     * 
				     */
					CommonMsgInQueue msgInQueue=new CommonMsgInQueue();
					msgInQueue.setJobId(10);  //this is pure temp hardcoding 
					msgInQueue.setMsgType(CommonConfiguration.MSG_JOB_ACTION_CANCEL_A_JOB);
					msgInQueue.setTarget(CommonConfiguration.CLIENT_MACHINE_ADDRESS);
					msgInQueue.setTargetPort(CommonConfiguration.CLIENT_LISTEN_PORT);
	    
				    
				    
					mainMessageQueue.getMessageQueue().add(msgInQueue);
					System.out.println("MaintenanceThread is accessing the  main message queue!  MMQ is:"
							+CommonUtils.getMMQInfo(mainMessageQueue));
					mainMessageQueue.notifyAll();
			}//end of sync 
	}

}
