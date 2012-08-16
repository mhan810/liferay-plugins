package com.liferay.portal.cloudservices.akka.actor;

import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.camel.CamelExtension;
import akka.camel.CamelMessage;
import com.liferay.portal.cloudservices.messaging.CommandMessage;
import org.apache.camel.CamelContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivica Cardic
 */
public abstract class CamelActor extends UntypedActor{

	protected CamelContext camelContext;

	public CamelActor(){
		ActorSystem system = getContext().system();

		camelContext = CamelExtension.get(system).context();
	}

	protected CamelMessage createCamelMessage(Object body){
		return createCamelMessage(body, new HashMap<String, Object>());
	}

	protected CamelMessage createCamelMessage(
			Object body, Map<String, Object> headers){

		if(body instanceof CommandMessage){
			headers.put(
				CamelMessage.MessageExchangeId(),
				((CommandMessage)body).getCorrelationId());
		}

		//withBodyAs internally calls appropriate type converter

		CamelMessage message = new CamelMessage(
			body, headers).withBodyAs(String.class, camelContext);

		return message;
	}

	protected <T> T getCamelMessageBody(CamelMessage message, Class<T> clazz) {
		return message.getBodyAs(clazz, camelContext);
	}
}
