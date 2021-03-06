package com.lsxy.framework.mq.api;


public interface MQEvent {
	
	public String getId();
	
	/**
	 * 事件名称
	 * 事件类名
	 * @return
	 */
	public String getEventName();
	
	/**
	 * 事件对应的topic
	 * @return
	 */
	public String getTopicName();
	
	/**
	 * 序列化本对象
	 * @return
	 */
	public String toJson();

	/**
	 * 消息生成发送时的时间戳
	 * @return
     */
	public long getTimestamp();

}
