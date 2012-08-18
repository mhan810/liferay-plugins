package com.liferay.portal.cloudservices.akka.spring;


import akka.actor.Actor;
import akka.actor.ActorRef;
import com.liferay.portal.cloudservices.akka.actor.CamelConsumerActor;
import com.liferay.portal.cloudservices.akka.actor.CamelProducerActor;

/**
 * @author Ivica Cardic
 */
public class CamelActorFactoryBean extends ActorFactoryBean{

	@Override
	protected Actor createActor() {
		Actor actor =  super.createActor();
		if(actor instanceof CamelProducerActor){
			CamelProducerActor camelProducerActor =  (CamelProducerActor)actor;
			camelProducerActor.setEndpointUri(_endpointUri);
			camelProducerActor.setIsOneWay(_isOneWay);
		}else{
			CamelConsumerActor camelConsumerActor =  (CamelConsumerActor)actor;
			camelConsumerActor.setEndpointUri(_endpointUri);
			camelConsumerActor.setActor(_actor);
		}

		return actor;
	}

	public String getEndpointUri() {
		return _endpointUri;
	}

	public void setEndpointUri(String endpointUri) {
		this._endpointUri = endpointUri;
	}

	public ActorRef getActor() {
		return _actor;
	}

	public void setActor(ActorRef actor) {
		this._actor = actor;
	}

	public boolean isIsOneWay() {
		return _isOneWay;
	}

	public void setIsOneWay(boolean isOneWay) {
		this._isOneWay = isOneWay;
	}

	private String _endpointUri;
	private ActorRef _actor;
	private boolean _isOneWay;
}
