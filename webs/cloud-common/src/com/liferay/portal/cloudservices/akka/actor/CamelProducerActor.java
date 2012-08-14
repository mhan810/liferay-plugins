package com.liferay.portal.cloudservices.akka.actor;


import akka.camel.javaapi.UntypedProducerActor;

/**
 * @author Ivica Cardic
 */
public class CamelProducerActor extends UntypedProducerActor{

	@Override
	public String getEndpointUri() {
		return _endpointUri;
	}

	public void setEndpointUri(String endpointUri) {
		this._endpointUri = endpointUri;
	}

	@Override
	public boolean isOneway() {
		return _isOneWay;
	}

	public void setIsOneWay(boolean isOneWay) {
		this._isOneWay = isOneWay;
	}

	private String _endpointUri;
	private boolean _isOneWay;
}
