package com.billydev.blib.common;

public class CommonConfiguration {
	
	/*
	 * temp debug	  
	 */
	
	public static final String STOP_MESSAGE="Stop Signal"; 

	
	
	/*
	 * network setting for client 
	 */	
	//public final static int CLIENT_LISTEN_PORT=9898; //this port is for Ubuntu linux of Hyper-V
	//todo: SERVER_ADDRESS supposed to get from database, in message level it must be ip address
	//public final static String CLIENT_ADDRESS_01="172.17.188.140"; 
			
	/*
	 * network setting for server side 
	 */
	//public final static String SERVER_ADDRESS="172.17.188.129";      //job engine listener
//	public final static String SERVER_ADDRESS="192.168.1.65";     
//	public final static String SERVER_ADDRESS="localhost";     
	public final static String SERVER_ADDRESS="core-job-engine";
	public final static int SERVER_LISTEN_PORT=28080;     //listen to linux client to connect
	

//	public final static String WEB_SERVER_ADDRESS="192.168.1.65";
//	public final static String WEB_SERVER_ADDRESS="localhost"; //spring boot stuff
	public final static String WEB_SERVER_ADDRESS="spring-boot-50216exp008";
	public final static int WEB_SERVER_LISTEN_PORT=8180; //Spring boot listening port
	

	//public final static String CLIENT_MACHINE_ADDRESS="172.17.188.140"; 
//	public final static String CLIENT_MACHINE_ADDRESS="192.168.1.65";
//	public final static String CLIENT_MACHINE_ADDRESS="localhost";
	public final static String CLIENT_MACHINE_ADDRESS="job-agent-linux";
	public final static int CLIENT_LISTEN_PORT=9898;     //on linux machine
	

	public static String  WEB_SERVER_HTTP_URL="http://"+WEB_SERVER_ADDRESS+":"+WEB_SERVER_LISTEN_PORT; 
	
	/*
	 * network setting for server 
	 */
	
	
	
	/*
	 * message queue message format 
	 */


	public final static String LINUX_JOB_NAME_PREFIX="LINUX_JOB"; 

	//new message names
	public final static String MSG_JOB_ACTION_CANCEL_A_JOB="CANCEL_A_JOB";
	public final static String MSG_JOB_ACTION_CREATE_A_NEW_JOB="CREATE_A_NEW_JOB"; 
	public final static String MSG_EVENT_SCHEDULE_NEW_EVENT="SCHEDULE_NEW_EVENT";
	public final static String MSG_EVENT_CANCEL_SCHEDULED_EVENT="CANCEL_SCHEDULED_EVENT"; 	
	public final static String MSG_CLIENT_HEART_BEAT="CLIENT_HEART_BEAT"; 
	public final static String MSG_JOB_CANCELLED="JOB_CANCELLED"; 
	public final static String MSG_JOB_COMPLETED="JOB_COMPLETED"; 	
	
	//the following is temp msg -- for scheduling 
	public final static String MMQ_MSG_NEW_SCHEDULED_EVENT_PREFIX="NEW_SCHEDULE_EVENT_CREATED";	

	
	
	/*
	 * Scheduler setting 
	 */
	public final static int SCHEDULER_WORKTHREAD_CHECK_TIME_INTERVAL_IN_SECONDS=30; 
			
	//the follwing temp msg -- 
	
	/*
	 * server maintenance 
	 */
	public final static int CLIENT_HEART_BEAT_INTERVAL_IN_MILLISECONDS=100000000;
	public final static String CLIENT_HEART_BEAT_THREAD_NAME="HEART_BEAT_THREAD";
	public final static String CLIENT_DEQUEUE_THREAD_NAME="CLIENT_DEQUE_THREAD";
	public final static String CLIENT_LISTENER_THREAD_NAME="CLIENT_LISTENER_THREAD";	
	public final static int CLIENT_DETECT_LISTENER_UP_INTERVAL_IN_SECONDS=1;
	


	/*
	 * Job States
	 */
	public final static String JOB_STATE_WAIT="01";
	public final static String JOB_STATE_READY="06";
	public final static String JOB_STATE_RUNNING="10";
	public final static String JOB_STATE_FAILED="11";
	public final static String JOB_STATE_COMPLETED="20";
	public final static String JOB_STATE_BYPASSED="30";

	

	
	
	

}
