package com.liferay.portal.cloudservices.gateway.actor;

import akka.camel.CamelMessage;
import com.liferay.portal.cloudservices.akka.actor.CamelActor;
import com.liferay.portal.cloudservices.gateway.service.CommandService;
import com.liferay.portal.cloudservices.messaging.CommandRequestMessage;
import com.liferay.portal.cloudservices.messaging.CommandResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This actor is responsible of the pooling for new messages
 * in the "cloud messages store".
 *
 * In addition this actor will handle the responses of the
 * "cloud messages store"
 *
 * @author Miguel Pastor
 * @author Ivica Cardic
 * 
 */
public class CommandActor extends CamelActor {

	@Autowired
	CommandService commandService;

	public void onReceive(Object message) {
		if (message instanceof CamelMessage) {
			CommandRequestMessage requestMessage = getCamelMessageBody(
				(CamelMessage) message, CommandRequestMessage.class);

			CommandResponseMessage responseMessage =
				commandService.processCommandRequest(requestMessage);

			getSender().tell(createCamelMessage(responseMessage));
		}else {
			unhandled(message);
		}
	}

}
