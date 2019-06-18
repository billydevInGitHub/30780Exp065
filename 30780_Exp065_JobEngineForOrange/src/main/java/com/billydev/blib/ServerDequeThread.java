package com.billydev.blib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.HashMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;

import com.billydev.blib.common.CommonConfiguration;
import com.billydev.blib.common.CommonMessageQueue;
import com.billydev.blib.common.CommonMsgInQueue;
import com.billydev.blib.common.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;


public class ServerDequeThread extends Thread{
	
	String threadGivenName; 
	CommonMessageQueue mainMessageQueue; 
	CommonMsgInQueue commonMsgInQueue; 

	Socket socket;
	ObjectMapper mapper = new ObjectMapper();

	
	public ServerDequeThread(String inputName, CommonMessageQueue mainMessageQueueInput) {
		this.threadGivenName=inputName; 
		this.mainMessageQueue=mainMessageQueueInput;

	}
	
	public void run() {			      		    		
		
		while(true) {
			System.out.println("ServerDequeThread is looping"); 
			
			boolean isProcessMessage=false; 
		/*
		 * this is a deque thread and it is big while loop
		 * when got new message from MMQ, we just create a new socket and send
		 * the message to the client machine or web server 
		 */
	
		/*
		 * deque the message from main message queue 
		 */
		String messageType="";
		synchronized(mainMessageQueue) {	
			System.out.println("ServerDequeThread  access MMQ, MMQ is:"+CommonUtils.getMMQInfo(mainMessageQueue));
			System.out.println("ServerDequeThread  check ~~mainMessageQueue.getMessageQueue().isEmpty()~~ method: "
					+mainMessageQueue.getMessageQueue().isEmpty());  
			
			if(!CommonUtils.isServerMMQNeedToBeDequed(mainMessageQueue)){
				try {
					/*
					 * if the message not match, just give up the monitor; 
					 */
					System.out.println("ServerDequeThread MMQ is empty or MsgType not match, thread will be waiting ..."); 
					mainMessageQueue.wait();
				} catch (InterruptedException e) {											
					e.printStackTrace();			    								
				}
			}else {
				/*
				 * as now we are not using the conditional check, we have to do the "busy check"
				 * only dequeue when the above if is false (not empty and message match), but the wait might not be useful
				 * in future if we use conditional check, no need to go to the big loop again
				 * anyway, if no message match, the thread just wait until other thread remove the first msg in the queue
				 */
				commonMsgInQueue=mainMessageQueue.getMessageQueue().poll();
				System.out.println("ServerDequeThread just deque a message, msg is:"+CommonUtils.getMsgInfo(commonMsgInQueue));
				mainMessageQueue.notifyAll();
				isProcessMessage=true; 
			}
		}//end of synchronized
		
		//todo: double check if monitor is released after sync block
    	
		if(isProcessMessage) {
			System.out.println("ServerDequeThread : dequeued the message type is:  "+commonMsgInQueue.getMsgType());


			

				/*
				 * check the dequed message, if it is from client side, like job cancelled, job completed, then
				 * we need crete http client to send messsage to web server 
				 * if it is from server side, like create a new job, cancel a job etc. then 
				 * we need to create a socket to the client
				 */
				messageType=commonMsgInQueue.getMsgType();
				if(CommonUtils.isServerMsgNeedToBeSentToWebServer(commonMsgInQueue)) {
					/*
					 * need to setup http client
					 */
					HttpClient client = new HttpClient();
					PostMethod method = new PostMethod(CommonConfiguration.WEB_SERVER_HTTP_URL+"/api/status_update/");
					String result=""; 
					try {
						/*
						 * the following is previous code using namepairs
						NameValuePair[] params = new NameValuePair[1];    			        
    			        params[0]=new NameValuePair(String.valueOf(commonMsgInQueue.getJobId()),messageType );
    			        method.addParameters(params);
    			        client.executeMethod(method);
    			        result = method.getResponseBodyAsString();
    			        System.out.println("ServerDequeThread: http request sent to the web server, Msg:"
    			        		+CommonUtils.getMsgInfo(commonMsgInQueue)+" result is:"+result);
    			        */
						String strJSONContent= mapper.writeValueAsString(commonMsgInQueue); 
						StringRequestEntity requestEntity = new StringRequestEntity(
								strJSONContent,
							    "application/json",
							    "UTF-8");
						System.out.println("ServerDequeThread: the JSON string sent to web site is:"+strJSONContent);


						method.setRequestEntity(requestEntity);
						client.executeMethod(method);
						result = method.getResponseBodyAsString();
    			        System.out.println("ServerDequeThread: http request sent to the web server, Msg:"
    			        		+CommonUtils.getMsgInfo(commonMsgInQueue)+" result is:"+result);						
						
    			    } catch (Exception e) {
    			    	System.out.println("ServerDequeThread: Exception in sending Msg to web server, Msg:"
    			    			+CommonUtils.getMsgInfo(commonMsgInQueue)+" Exception is:"+e.getMessage());
    			    	e.printStackTrace();
    			    } finally {
    			        method.releaseConnection();
    			    }    			
				}else if(CommonUtils.isServerMsgNeedToBeSentToClient(commonMsgInQueue)) {   			
    		  		/*
    		  		 * only when there is a message dequeued and need to send to the 
    		  		 * client, then  we check the socket connection ok  
    		  		 * and send out the message
    		  		 */
					/*
					 * first need check if socket is ready 
					 */   			
    			
					/*
					 * check if the key is already in the socket map, if not create a new socket  
					 * if key is already in the socket, then we will use the existing socket 
					 * with this, if the client is not up, then socket is not there , we will create a new one when
					 * the message require that ! 
					 */

						do {
							try {
									socket = new Socket(commonMsgInQueue.getTarget(), commonMsgInQueue.getTargetPort());
									System.out.println("ServerDequeThread : new socket created to the client: "+commonMsgInQueue.getTarget()); 
								}							
							catch (IOException   e) {
								System.out.println("ServerDequeThread: Error create a connection to target:"+commonMsgInQueue.getTarget());
								socket=null; 
								/*
								 * if client listener is not up 
								 */
								try {
									Thread.sleep(CommonConfiguration.CLIENT_DETECT_LISTENER_UP_INTERVAL_IN_SECONDS);
								} catch (InterruptedException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
							System.out.println("ServerDequeThread : waiting until target machine  listener is up..."); 
							/*
							 * todo: there is a risk here: if the client is not up, then the server deque thread might be stuck here !!
							 * then the message could not send out, we might need detect that if the client is not up
							 * we just ignore it and send alert out !! 
							 */
						}while(socket==null); 

    			
    			try {
    				BufferedReader in =null; 
    				PrintWriter out=null; 
    				in = new BufferedReader( new InputStreamReader(socket.getInputStream()));
    				out = new PrintWriter(socket.getOutputStream(), true);
    				System.out.println("ServerDequeue thread  connected to the target machine with address:"+commonMsgInQueue.getTarget());
    				/*
    				 * encode the Message to JSON and send to target
    				 */
    				out.println(mapper.writeValueAsString(commonMsgInQueue));
    				System.out.println("ServerDequeue :  after dequeue MMQ is :"+CommonUtils.getMMQInfo(mainMessageQueue)); //for debug only
    				//socket.close();
    			} catch (IOException  e) {
    				System.out.println("ServerDequeue:  Error create a stream for the socket or error write to socket, target machine:"+commonMsgInQueue.getTarget());
   			 	} //end of try
			}//end of if (message type check) 

		}//end of if ( isProcessMessage) 
		}//end of while(true)
	}//end of run	
	
}//end of class
