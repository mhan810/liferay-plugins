package com.liferay.portal.cloudservices.akka.actor;


import akka.actor.ActorRef;
import akka.camel.CamelMessage;
import akka.camel.javaapi.UntypedConsumerActor;

/**
 * @author Ivica Cardic
 */
public class CamelConsumerActor  extends UntypedConsumerActor {

	@Override
	public String getEndpointUri() {
		return _endpointUri;
	}

	public void setEndpointUri(String endpointUri) {
		this._endpointUri = endpointUri;
	}

	@Override
	public void onReceive(Object message) {
		if (message instanceof CamelMessage) {
			_actor.forward(message, getContext());
		} else {
			throw new IllegalStateException(
				"Unable to handle this kind of message: " + message);
		}

	}



	public ActorRef getActor() {
		return _actor;
	}

	public void setActor(ActorRef actor) {
		this._actor = actor;
	}

	private ActorRef _actor;
	private String _endpointUri;
}
