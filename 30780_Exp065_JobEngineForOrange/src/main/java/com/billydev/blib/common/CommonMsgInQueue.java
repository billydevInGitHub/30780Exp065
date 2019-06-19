package com.billydev.blib.common;

public class CommonMsgInQueue {

	String msgType; //job, application, schedule, 
	String target;  //client destination ip address, 
	String source;   //ip address from which message comes
	int targetPort; 
	String content;
	String jobScript; 
	String jobArguments; 
	String jobName; 
	String rtApplicationName; 
	String dtApplicationName; 

	

	public String getRtApplicationName() {
		return rtApplicationName;
	}
	public void setRtApplicationName(String rtApplicationName) {
		this.rtApplicationName = rtApplicationName;
	}
	public String getDtApplicationName() {
		return dtApplicationName;
	}
	public void setDtApplicationName(String dtApplicationName) {
		this.dtApplicationName = dtApplicationName;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	public String getJobScript() {
		return jobScript;
	}
	public void setJobScript(String jobScript) {
		this.jobScript = jobScript;
	}
	public String getJobArguments() {
		return jobArguments;
	}
	public void setJobArguments(String jobArguments) {
		this.jobArguments = jobArguments;
	}
	long jobId; 
	public String getSource() {
		return source;
	}
	public void setSource(String source) {
		this.source = source;
	}
	String jobDetails; 
	
	public int getTargetPort() {
		return targetPort;
	}
	public void setTargetPort(int targetPort) {
		this.targetPort = targetPort;
	}
	String eventName; 
	String eventScheduleString; 	
	
	public String getEventName() {
		return eventName;
	}
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	
	public String getEventScheduleString() {
		return eventScheduleString;
	}
	public void setEventScheduleString(String eventScheduleString) {
		this.eventScheduleString = eventScheduleString;
	}
	public long getJobId() {
		return jobId;
	}
	public void setJobId(long jobId) {
		this.jobId = jobId;
	}
	public String getJobDetails() {
		return jobDetails;
	}
	public void setJobDetails(String jobDetails) {
		this.jobDetails = jobDetails;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	String action; //cancel, hold etc. 
 
	
	public String getMsgType() {
		return msgType;
	}
	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	} 
	
	
}
