package com.liferay.portal.cloudservices.messaging;

/**
 * @author Ivica Cardic
 */
public class CommandMessage extends BaseMessage{

	public String getCorrelationId() {
		return _correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this._correlationId = correlationId;
	}

	private String _correlationId;
}
