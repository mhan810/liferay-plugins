package com.liferay.portal.cloudservices.messaging;

/**
 * Base class for all the messages. Marker class
 * 
 * @author Miguel Pastor
 * @author Ivica Cardic
 */
public abstract class BaseMessage implements Message {
	public BaseMessage() {
		this._timestamp = System.currentTimeMillis();
	}

	public long getTimestamp() {
		return _timestamp;
	}

	public String getName() {
		return this._name;
	}

	public void setName(String name) {
		this._name = name;
	}

	private String _name;
	private long _timestamp;
}
