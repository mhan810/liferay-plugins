package com.liferay.portal.cloudservices.messaging;

/**
 * @author Ivica Cardic
 * @author Igor Beslic
 */
public class CommandMessage extends BaseMessage{

    public CommandMessage() {
        super();
    }

    public  CommandMessage(String command) {
        super();
        setType(command);
    }

    public String getCorrelationId() {
		return _correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this._correlationId = correlationId;
	}

	private String _correlationId;
}
