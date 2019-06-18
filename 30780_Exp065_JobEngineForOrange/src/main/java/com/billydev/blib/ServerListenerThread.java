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

public class ServerListenerThread extends Thread {

	String threadGivenName; 
	CommonMessageQueue mainMessageQueue;
	ObjectMapper mapper = new ObjectMapper();
	
	public ServerListenerThread(String inputName, CommonMessageQueue mainMessageQueue) {
		threadGivenName=inputName; 
		this.mainMessageQueue=mainMessageQueue; 
	}
	
	public void run() {
		ServerSocket server;
		
		
		class RequestHandlerThread extends Thread{

			String threadGivenName; 
			CommonMessageQueue mainMessageQueueInRequestHandlerThread;
			BufferedReader in ; 
			PrintWriter out; 
			
			private Socket handlerSocket; 
			
			
			public RequestHandlerThread(Socket s, String inputName, CommonMessageQueue mainMessageQueue) {
				handlerSocket=s; 
				threadGivenName=inputName; 
				this.mainMessageQueueInRequestHandlerThread=mainMessageQueue; 
			}
			


			
			@Override
			public void run() {
				
				try {						
					
					/*
					 * todo: shall we read from the client side first, then do the enque?
					 */
					synchronized(mainMessageQueueInRequestHandlerThread) {
					System.out.println("RequestHandlerThread is adding  message to MMQ");
					String httpResponse = "HTTP/1.1 200 OK\r\n\r\n"+new Date(); //+"with multiple threads handler";
					
					/*
					 * 
					 * We need read from the client and then decode and directly enqueue
					 * to the main message queue as the format of message in queue should be the same
					 * from client and server 
					 *  
					 */
					in = new BufferedReader( new InputStreamReader(handlerSocket.getInputStream()));
					//out = new PrintWriter(handlerSocket.getOutputStream(), true);
					
					CommonMsgInQueue msgInQueue=null; 
					String input=""; 
					if((input=in.readLine())!=null&&(!input.isEmpty())) {
						msgInQueue= mapper.readValue(input, CommonMsgInQueue.class);
					}
					
					//just temporarily hardcode a message and add to the MMQ 
					//comment out the following 2 lines as http might send more than one message to the server !!
					//mainMessageQueueInHttpWorkerThread.getMessageQueue().add(Configuration.MMQ_MSG_CREATE_A_NEW_AGENT_PREFIX); 
					mainMessageQueueInRequestHandlerThread.getMessageQueue().add(msgInQueue); 
					System.out.println("RequestHandlerThread  add a message to MMQ, MMQ is: "
							+ CommonUtils.getMMQInfo(mainMessageQueueInRequestHandlerThread));	
					
					//this line only will not work in thread!!
					//workerSocket.getOutputStream().write(httpResponse.getBytes("UTF-8"));
					
					/**
					 * the following lines worked as the output stream is closed in thread !!
					 */
					
					/*
					 * todo: need to tell the difference between OutputStream and PrintWriter wrapper 
					 */
					OutputStream ostream =handlerSocket.getOutputStream();
					ostream.write(httpResponse.getBytes("UTF-8"));
					ostream.flush();
					ostream.close();   //This very important here outputstream need a close or
					mainMessageQueueInRequestHandlerThread.notifyAll();
					System.out.println("RequestHandlerThread is notifyingAll other threads");
				}
					

				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}				
				
			}
			
		}
		
		
		
		try {
			//todo: add to the configuration file of the port
			server = new ServerSocket(CommonConfiguration.SERVER_LISTEN_PORT);
			System.out.println("ServerListenerThread: Listening for connection on port: "+CommonConfiguration.SERVER_LISTEN_PORT); 
			
			Socket socket=null; 
			while (true) { 	
				 socket = server.accept(); 
				 System.out.println("ServerListenerThread: waiting for the request from client...");
				 Thread  requestHandlerThread = new RequestHandlerThread(socket,"RequestHandlerThread",mainMessageQueue);
				 requestHandlerThread.start();
	 		}
		} catch (IOException e) {
			e.printStackTrace();
		} 

	}
	
}
