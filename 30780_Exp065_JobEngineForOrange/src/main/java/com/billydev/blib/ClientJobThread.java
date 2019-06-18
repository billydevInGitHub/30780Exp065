package com.billydev.blib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import com.billydev.blib.common.CommonConfiguration;
import com.billydev.blib.common.CommonMessageQueue;
import com.billydev.blib.common.CommonMsgInQueue;
import com.billydev.blib.common.CommonUtils;

public class ClientJobThread extends Thread {
	private String threadGivenName;
	CommonMessageQueue messageQueue;
	CommonMsgInQueue dequedMsg; 
	
	public ClientJobThread(String inputName, CommonMessageQueue messageQueueInput, CommonMsgInQueue messageInQueueInput) {
		threadGivenName=inputName; 
		this.dequedMsg=messageInQueueInput;
		this.messageQueue=messageQueueInput; 
	}
	
	public void run() {
		
		/*
		 * One Job thread
		 * 1. first  trigger the job
		 *    The job thread will get the result and output it;
		 *    
		 * 2. Trigger a standalone JobControlThread and check the MMQ, when get Cancel message, just cancel the job; 
		 *    lock the agentMessageQueue
		 *    dequeue the message when match this  job !! 
		 *        if need cancel the job, cancel is, add result to Agent message queue  
		 * 3. release lock and loop checking if there is output from the job ( means it is running)				 *  
		 *				 *       
		 * 4.  if job complete naturally, then JobThread add result to agent message queue as well !!
		 *     jobThread also need to interrupt the JobControlThread   
		 *      
		 * 
		 */	
		//first trigger the job
		try {
			/*
			 * first we need deque the message to get detailed job information
			 */
			
			
			final Process p = Runtime.getRuntime().exec(dequedMsg.getJobScript()+" "+dequedMsg.getJobArguments());
			//todo: once the job working, we can remove the following hard coded job script details
			//final Process p = Runtime.getRuntime().exec("/home/billy/billydev/tempdev/181123-1-30800Exp003_ControlLongRunningProcess/endlessloop.sh");
			
			class ClientJobControlThread extends Thread{						
				public void run() {						
					while(true) {
						synchronized(messageQueue) {
							String messageType="";
							long jobId=dequedMsg.getJobId(); 
							System.out.println("JobControlThread the MMQ is:"+CommonUtils.getMMQInfo(messageQueue));
							
							
							/*
							 * try dequeue the matched message and do the action
							 * matched message means: message type match, job id match !!
							 */
	        				try {
	        					if(messageQueue.getMessageQueue().isEmpty()||
		        					!((messageType=messageQueue.getMessageQueue().peek().getMsgType()).equals(CommonConfiguration.MSG_JOB_ACTION_CANCEL_A_JOB
        		    				))||
		        					!(jobId==messageQueue.getMessageQueue().peek().getJobId())
		        					){
	        						System.out.println("ClientJobControlThread is check the message in queue, type or JobId not matched ...expected messageType is:"
	        								+messageType+" MessageInQueue is:"+CommonUtils.getMsgInfo(messageQueue.getMessageQueue().peek()));
	        						messageQueue.wait();
	        					}else {
	        						System.out.println("ClientJobControlThread is dequing a  message with MsgType:"+CommonConfiguration.MSG_JOB_ACTION_CANCEL_A_JOB);
	        						CommonMsgInQueue msgDequed=messageQueue.getMessageQueue().poll();
	        						System.out.println("ClientJobControlThread is about to kill the long running process...");
	        						p.destroy();//todo: how to get the result of destroy 
	        						CommonMsgInQueue msgInQueue= new CommonMsgInQueue();
	        						msgInQueue.setMsgType(CommonConfiguration.MSG_JOB_CANCELLED);
	        						msgInQueue.setJobId(msgDequed.getJobId());
	        						messageQueue.getMessageQueue().add(msgInQueue);
	        						messageQueue.notifyAll();
	        						System.out.println("ClientJobControlThread  added the job cancelled message... MMQ is:"
	        								+CommonUtils.getMMQInfo(messageQueue)+" Message is:"+CommonUtils.getMsgInfo(msgInQueue));
	        						break; //should break the while loop
	        					}
		    				} catch (InterruptedException e) {
		    					break; 
							}
						}//end of synchronized								
					}//end of while
				}//end of run 
			}//end of class
				
			Thread jobControlThread = new ClientJobControlThread();
			jobControlThread.start();				

			BufferedReader reader = 
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            String line = "";			
            /*
             * If job not complete, then reader suppose block here 
             * so when readLine got null, stream suppose to be ended
             * todo: check some jobs without any output in spool 
             */
            while ((line = reader.readLine())!= null) {                	
            	System.out.println("ClientJobThread: job id:"+dequedMsg.getJobId()+" ++Job Spool++"+line);  //todo: so we can get the output in time, this will be written to spool folder
            }
            
            //when job completed normally, need to stop the jobControlThread as well 
            jobControlThread.interrupt(); 
            System.out.println("ClientJobThread:  JobControlThread is interrupted");
            //when Job completed, then send message to MessageQueue 
            //todo: we need to tell the difference
			synchronized(messageQueue) {
				CommonMsgInQueue msgInQueueJobCompleted = new CommonMsgInQueue();
				msgInQueueJobCompleted.setMsgType(CommonConfiguration.MSG_JOB_COMPLETED);
				msgInQueueJobCompleted.setJobId(dequedMsg.getJobId());
				messageQueue.getMessageQueue().add(msgInQueueJobCompleted);
				messageQueue.notifyAll();
			}					
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
}
