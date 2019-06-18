package com.billydev.blib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import com.billydev.blib.common.CommonConfiguration;
import com.billydev.blib.common.CommonMessageQueue;
import com.billydev.blib.common.CommonMsgInQueue;
import com.billydev.blib.common.CommonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientListenerThread extends Thread {

	
	private String threadGivenName;
	CommonMessageQueue messageQueue;
	ObjectMapper mapper = new ObjectMapper();
	
	ClientListenerThread(String inputName, CommonMessageQueue messageQueueInput){
		threadGivenName=inputName; 
		this.messageQueue=messageQueueInput; 
	}

	
	public void run() {
		ServerSocket server;
		
		
		class ClientRequestHandlerThread extends Thread{

			String threadGivenName; 
			CommonMessageQueue mainMessageQueueInClientRequestHandlerThread;
			BufferedReader in ; 
			PrintWriter out; 
			
			private Socket handlerSocket; 
			
			
			public ClientRequestHandlerThread(Socket s, String inputName, CommonMessageQueue mainMessageQueueInput) {
				handlerSocket=s; 
				threadGivenName=inputName; 
				this.mainMessageQueueInClientRequestHandlerThread=mainMessageQueueInput; 
			}
			


			
			@Override
		public void run() {
				
			OutputStream ostream=null;
			

				try {						
					/*
					 * We need read message from the client first and then decode and directly enqueue
					 * to the main message queue as the format of message in queue should be the same
					 * from client and server 
					 */
					in = new BufferedReader( new InputStreamReader(handlerSocket.getInputStream()));

					
					CommonMsgInQueue msgInQueue=null; 
					String input=""; 
					if((input=in.readLine())!=null&&(!input.isEmpty())) {
						msgInQueue= mapper.readValue(input, CommonMsgInQueue.class);
					}
					System.out.println("ClientRequestHandlerThread:===== received a new msg: "+ CommonUtils.getMsgInfo(msgInQueue)); 
				
					synchronized(mainMessageQueueInClientRequestHandlerThread) {
						mainMessageQueueInClientRequestHandlerThread.getMessageQueue().add(msgInQueue); 
						System.out.println("ClientRequestHandlerThread: add a new message to MMQ,  Msg is:"
								+ CommonUtils.getMsgInfo(msgInQueue)
								+" MMQ is"+ CommonUtils.getMMQInfo(mainMessageQueueInClientRequestHandlerThread)
								);	
						mainMessageQueueInClientRequestHandlerThread.notifyAll();
					} //end of sync						

					String httpResponse = "HTTP/1.1 200 OK\r\n\r\n"+new Date(); //+"with multiple threads handler";					
					/*
					 * todo: need to tell the difference between OutputStream and PrintWriter wrapper 
					 */
					//out = new PrintWriter(handlerSocket.getOutputStream(), true);
					ostream =handlerSocket.getOutputStream();
					ostream.write(httpResponse.getBytes("UTF-8"));					
					ostream.flush();				
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}	//end of try
				finally {
					 try {
						 handlerSocket.close();
		                } catch (IOException e) {
		                	System.out.println("ClientRequestHandlerThread: can not close socket Exception!!");
		                }
					 System.out.println("ClientRequestHandlerThread: handler socket closed"); 
				}
		}//end of run	
 	}//end of class
		
		
		/*
		 * the run method of ClientListenerThread
		 */
		try {
			server = new ServerSocket(CommonConfiguration.CLIENT_LISTEN_PORT);
			System.out.println("ClientListenerThread is Listening at port: "+CommonConfiguration.CLIENT_LISTEN_PORT); 
			
			Socket socket=null; 
			while (true) { 	
				 socket = server.accept(); 
				 Thread  requestHandlerThread = new ClientRequestHandlerThread(socket,"ClientRequestHandlerThread",messageQueue);
				 requestHandlerThread.start();
	 		}
		} catch (IOException e) {
			e.printStackTrace();
		} //end of try

	}//end of run
} //end of class


