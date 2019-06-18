package com.billydev.blib;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import com.billydev.blib.common.CommonConfiguration;
import com.billydev.blib.common.CommonMessageQueue;
import com.billydev.blib.common.CommonMsgInQueue;

public class ServerSchedulerMainThread extends Thread {
	   private String threadGivenName;
	   private CommonMessageQueue mainMessageQueue; 
	   private HashMap<String,String> scheduleEntries = new HashMap<>();
	   private final ScheduledExecutorService scheduler =
			     Executors.newScheduledThreadPool(1);
	   
	   public ServerSchedulerMainThread(String inputName, CommonMessageQueue mainMessageQueue) {
		   threadGivenName=inputName; 
		   this.mainMessageQueue=mainMessageQueue; 
	   }
	   
	   public void run() {		   
		   
		   
			class ServerSchedulerWorkerThread extends Thread{

				private String threadGivenName; 
				private CommonMessageQueue mainMessageQueueInScheduleWorker; 
				private HashMap<String,String> scheduleEntriesInWorkerThread;
				
				
				public ServerSchedulerWorkerThread(String inputName,CommonMessageQueue mainMessageQueueInput, HashMap<String,String> scheduleEntriesInput ) {
					threadGivenName=inputName; 
					mainMessageQueueInScheduleWorker=mainMessageQueueInput; 
					scheduleEntriesInWorkerThread=scheduleEntriesInput; 
				}
				
				public void run() {
					/*
					 * Once this workerThread start to run, a new application will be triggered
					 * the trigger procedure will put all the jobs of that application into DB
					 * then it will check which job will need to be triggered first !!
					 */
					synchronized(mainMessageQueueInScheduleWorker) {
					/*
					 * we need to sync the main queue first to avoid any deadlock
					 */
					
						synchronized(scheduleEntriesInWorkerThread){
							/*
							 * 	loop through all scheduled entries to see if current time meet the scheduled time 
							 */
							for(Iterator<String>  iter=scheduleEntriesInWorkerThread.keySet().iterator(); iter.hasNext();  ) {
								String scheduleString=iter.next(); 
								/*
								 * todo: we need to check the current time meet the schedule String
								 * when needed, we need put an entry in the main message queue
								 */
							
								CommonMsgInQueue commonMsgInQueue=new CommonMsgInQueue(); 
								mainMessageQueue.getMessageQueue().add(commonMsgInQueue);  
								System.out.println("ServerSchedulerWorkerThread is accessing the main message queue! MMQ is:"
									+mainMessageQueue.getMessageQueue());
								mainMessageQueueInScheduleWorker.notifyAll();							
							}//end of for
						}//end of innter sync
					}//end of outer sync
				}//end of run
			}//end of class
		   
		   
		   try {
			   /*
			    * todo:remove the following when we need the scheduler stuff
			    */
			   Thread.sleep(300000000);
		   } catch (InterruptedException e) {
			   e.printStackTrace();
		   }			

		   /*
			* First trigger a schedule worker thread 
			* the worker thread actually run every like 30 seconds, so we can do the comparison
			* give the scheduleEntries(HashMap of schedule) to the worker thread 
			* in a fixed internal, the worker thread will check if the current time match the 
			* schedule entry, if match, then trigger the event !! 
			*/
			ServerSchedulerWorkerThread workerThread = new ServerSchedulerWorkerThread("Schedule worker thread ", 
					mainMessageQueue,scheduleEntries);					 

			ScheduledFuture<?> workerThreadHandle =scheduler.scheduleAtFixedRate(workerThread, 1, 
					CommonConfiguration.SCHEDULER_WORKTHREAD_CHECK_TIME_INTERVAL_IN_SECONDS, SECONDS);		
		   

			/*
			 * ServerSchedulerMainThread is a dequeue thread which has a while loop to check if any new message
			 * come into the queue
			 */
	      
			   while(true) {
			
				   synchronized(mainMessageQueue) {  		
			   	  /*
				   * message was enqueued through web gui
				   * this message is dequeued here 
				   *   if it is a new scheduler msg: then added to the 	scheduleEntries					  
				   *   if it is cancel event message: then remove from the scheduleEntries !  
				   */		   
				   
				   System.out.println("In Server Scheduler main looping ...");
				   CommonMsgInQueue msgFromQue =mainMessageQueue.getMessageQueue().poll();				  

				   
				   if(msgFromQue!=null&&msgFromQue.getAction().startsWith(CommonConfiguration.MSG_EVENT_SCHEDULE_NEW_EVENT)) {
					   synchronized(scheduleEntries) {
						   scheduleEntries.put(msgFromQue.getEventName(), msgFromQue.getEventScheduleString()); 
						   scheduleEntries.notifyAll();
					   }
				   } else  if(msgFromQue!=null&&msgFromQue.getAction().startsWith(CommonConfiguration.MSG_EVENT_CANCEL_SCHEDULED_EVENT)){
					   synchronized(scheduleEntries) {
						   scheduleEntries.remove(msgFromQue.getEventName()); 
						   scheduleEntries.notifyAll();
					   }					   
					   mainMessageQueue.notifyAll();
				   }else  {
					   try {
							mainMessageQueue.wait();
  					   } catch (InterruptedException e) {
							e.printStackTrace();
  					   }
				   }
			   }//end of synch		   
			  
		   }//end of while	
	   }//end of run
}
