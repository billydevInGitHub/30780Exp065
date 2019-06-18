package com.billydev.blib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import com.billydev.blib.common.CommonConfiguration;
import com.billydev.blib.common.CommonMessageQueue;
import com.billydev.blib.common.CommonMsgInQueue;
import com.billydev.blib.common.CommonUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientDequeThread extends Thread {

	private String threadGivenName; 
	CommonMessageQueue messageQueue;
	CommonMsgInQueue msgInQueue; 
    Socket socket=null;	       
    BufferedReader in =null; 
    PrintWriter out=null; 
	ObjectMapper mapper = new ObjectMapper();

	
	public ClientDequeThread(String inputName, CommonMessageQueue messageQueueInput) {
		this.threadGivenName=inputName; 
		this.messageQueue=messageQueueInput; 
	}
	
	public void run() {
		
		while(true) {
			boolean isProcessingMessage=false; 
			/*
			 * first sync the message queue and deque the matched message pattern
			 * then create socket( connection) to server to send out the message
			 * or create a new job 
			 * todo: validation the input message
			 */
			String messageType="";			

		synchronized(messageQueue) {				        			
			/*
			 * dequeue only those matched message  
			 */


			System.out.println("ClientDequeThread:  accesses MMQ, current MMQ is:"+CommonUtils.getMMQInfo(messageQueue)); 
			/*
			 * todo: in client, deque need to deal with all types of message
			 */
			if(messageQueue.getMessageQueue().isEmpty()||
					(
					  !(
						 (messageType=messageQueue.getMessageQueue().peek().getMsgType()).equals(CommonConfiguration.MSG_JOB_CANCELLED) 
					  )
    				  &&!(messageType.equals(CommonConfiguration.MSG_JOB_COMPLETED))
    				  &&!(messageType.equals(CommonConfiguration.MSG_CLIENT_HEART_BEAT) )
    				  &&!(messageType.equals(CommonConfiguration.MSG_JOB_ACTION_CREATE_A_NEW_JOB) )
    				)
			   ){
				try {
					System.out.println("ClientDequeThread : does not get any message as MMQ is empty or MsgType not match, MMQ  is:"
							+CommonUtils.getMMQInfo(messageQueue) + " Expected MsgType is:"+messageType); 
					messageQueue.wait();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}else {
				/*
				 * this is temp solution before we use conditional lock
				 */
				msgInQueue=messageQueue.getMessageQueue().poll();
				messageQueue.notifyAll();
    			System.out.println("ClientDequeThread:  dequed the following message:"+CommonUtils.getMsgInfo(msgInQueue));   			
    			isProcessingMessage=true; 
    			
			}//end of if
		}//end of sync
    	
				/*
				 * only proceed ( process the message) when message is dequed
				 */
		     if(isProcessingMessage) {
    			/*
    			 * process dequed message
    			 */
    			
    			messageType=msgInQueue.getMsgType(); 
    			if(	messageType.equals(CommonConfiguration.MSG_JOB_ACTION_CREATE_A_NEW_JOB)) {
    				/*
    				 * messages are from server, we need create a new job thread
    				 */
    				new ClientJobThread(CommonConfiguration.LINUX_JOB_NAME_PREFIX, messageQueue, msgInQueue).start(); 
    				
    			}else if(messageType.equals(CommonConfiguration.MSG_JOB_CANCELLED)||
    						messageType.equals(CommonConfiguration.MSG_JOB_COMPLETED)||
    						  messageType.equals(CommonConfiguration.MSG_CLIENT_HEART_BEAT)) {
    				/*
    				 * message are from client and need to send to server using socket
    				 * the following is setup the socket to send the dequed message
    				 * 
    				 */	
    				System.out.println("ClientDequeThread: is setting up socket to the server...");
   				
					  do {
    					try { 
    						socket = new Socket(CommonConfiguration.SERVER_ADDRESS, CommonConfiguration.SERVER_LISTEN_PORT);
    						System.out.println("ClientDequeThread : socket connection to server: "+CommonConfiguration.SERVER_ADDRESS+":"+CommonConfiguration.SERVER_LISTEN_PORT+" ok");
    						
    						} catch (IOException e) {
    							System.out.println("Error create a connection to target:"
    									+CommonConfiguration.SERVER_ADDRESS+":"+CommonConfiguration.SERVER_LISTEN_PORT);
        						System.out.println("In ClientDequeThread : waiting until server listener up..."); 
            					try {
									Thread.sleep(CommonConfiguration.CLIENT_DETECT_LISTENER_UP_INTERVAL_IN_SECONDS);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
    						}    						
    						/*
    						 * todo: there is a risk here: if the server listener not up for long time, we might need 
    						 * throw runtime exception !! 
    						 */
    				  }while(socket==null);     

    				
    				try {
        				in = new BufferedReader( new InputStreamReader(socket.getInputStream()));
        				out = new PrintWriter(socket.getOutputStream(), true);
        				System.out.println("Client deque thread is sending message out to server");
    					out.println(mapper.writeValueAsString(msgInQueue));
    					socket.close();
    				} catch (IOException e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				} //let the server know			        			
    			}//end of if block
		     }//end of if (isProcessingMessage) 
	  }//end of while(true)
	}//end of run
}

	
	

