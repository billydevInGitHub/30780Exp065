package com.billydev.blib.common;

import java.util.ArrayList;

public class CommonUtils {
	
	public static String getMMQInfo(CommonMessageQueue messageQueue) {
		
		String returnString=""; 
		if(messageQueue==null) {
			returnString="MMQ is null"; 
		}else {
			if(messageQueue.getMessageQueue().peek()==null) {
				returnString="MMQ is empty"; 
			}else {
				returnString="First MsgType in Queue is:"+messageQueue.getMessageQueue().peek().getMsgType();
				returnString+=" Length of MMQ is:"+messageQueue.getMessageQueue().size();
			}
		}
		return "["+returnString+"]"; 
	}

	
	public static String getMsgInfo(CommonMsgInQueue msgInQueue) {
		if(msgInQueue==null) {
			return "Message is NULL!"; 
		}
		StringBuilder  retStrBuilder= new StringBuilder();
		String tmpStr=null; 
		retStrBuilder.append("[");
		if(isStringHasContent(tmpStr=msgInQueue.getMsgType())){
			retStrBuilder.append("MsgType:"+tmpStr+"; ");
		}
		if(isStringHasContent(tmpStr=String.valueOf(msgInQueue.getJobId()))){
			retStrBuilder.append("JobId:"+tmpStr+"; ");
		}
		if(isStringHasContent(tmpStr=msgInQueue.getJobName())){
			retStrBuilder.append("JobName:"+tmpStr+"; ");
		}
		if(isStringHasContent(tmpStr=msgInQueue.getRtApplicationName())){
			retStrBuilder.append("RtApplicationName:"+tmpStr+"; ");
		}
		if(isStringHasContent(tmpStr=msgInQueue.getDtApplicationName())){
			retStrBuilder.append("DtApplicationName:"+tmpStr+"; ");
		}
		if(isStringHasContent(tmpStr=msgInQueue.getEventName())){
			retStrBuilder.append("EventName:"+tmpStr+"; ");
		}
		if(isStringHasContent(tmpStr=msgInQueue.getTarget())){
			retStrBuilder.append("Target:"+tmpStr+"; ");
		}
		if(isStringHasContent(tmpStr=String.valueOf(msgInQueue.getTargetPort()))){
			retStrBuilder.append("TargetPort:"+tmpStr+"; ");
		}
		retStrBuilder.append("]");
		return retStrBuilder.toString(); 
	}
	
	public static boolean isStringHasContent(String input) {
		if(input==null) {
			return false ; 
		}else if(input.length()==0) {
			return false;
		}else {
			return true; 
		}
	}
	
	/*
	 * check the  server MMQ need to be dequed  
	 */
	public static boolean isServerMMQNeedToBeDequed(CommonMessageQueue messageQueue ) {
		if(messageQueue==null) {
			return false; 
		}
		if(messageQueue.getMessageQueue().isEmpty()) {
			return false; 
		}
		CommonMsgInQueue message=messageQueue.getMessageQueue().peek(); 
		if(isServerMsgNeedToBeDequed(message)) {
			return true; 
		}
		return false; 
	}
	
	/*
	 * check server Msg Types
	 */
	public static boolean isServerMsgNeedToBeDequed(CommonMsgInQueue  message) {
		/*
		 * 2 groups of msg, one send to web and one send to client 
		 */
		if(isServerMsgNeedToBeSentToWebServer(message)||isServerMsgNeedToBeSentToClient(message)) {
			return true;
		}else {
			return false; 
		}
	}
	
	public static boolean isServerMsgNeedToBeSentToWebServer(CommonMsgInQueue  message) {
		/*
		 * bottom implementation 
		 */
		if(message==null) {
			return false; 
		}
		String messageType=message.getMsgType();
		if(CommonConfiguration.MSG_JOB_CANCELLED.equals(messageType)||
				CommonConfiguration.MSG_JOB_COMPLETED.equals(messageType)) {
			return true; 
		}
			
		return false; 
	}
	
	public static boolean isServerMsgNeedToBeSentToClient(CommonMsgInQueue  message) {
		if(message==null) {
			return false; 
		}
		String messageType=message.getMsgType();
		if(CommonConfiguration.MSG_JOB_ACTION_CREATE_A_NEW_JOB.equals(messageType)||
			CommonConfiguration.MSG_JOB_ACTION_CANCEL_A_JOB.equals(messageType)) {
			return true; 
		}
		return false; 
	}
	
	/*
	 * check the client MMQ need to be dequed
	 */
	
	public static boolean isClientMsgNeedToBeDequed() {
	  return false; 	
	}
	
	/*
	 * check client Msg Types
	 */
	public static boolean isClientMsgNeedToCreateAJob() {
		return false; 
	}
	
	public static boolean isClientMsgNeedToBeSentToServer() {
	   return false; 	
	}
	
	public static String getMsgInfoArrayList(ArrayList<CommonMsgInQueue> messages) {
		StringBuilder strBuilder=new StringBuilder(); 
		for(CommonMsgInQueue msgInQueue:messages) {
			strBuilder.append(getMsgInfo(msgInQueue));
		}
		return strBuilder.toString();
	}

}
