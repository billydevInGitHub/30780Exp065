package com.billydev.blib.common;

import java.util.LinkedList;
import java.util.Queue;

public class CommonMessageQueue {

	private Queue<CommonMsgInQueue> messageQueue= new LinkedList<>();		
	public Queue<CommonMsgInQueue> getMessageQueue() {
		return messageQueue; 
	}	
	
}

