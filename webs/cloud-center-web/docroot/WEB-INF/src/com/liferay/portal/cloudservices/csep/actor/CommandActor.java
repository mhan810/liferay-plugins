package com.liferay.portal.cloudservices.csep.actor;

import akka.actor.ActorRef;
import akka.camel.CamelMessage;
import com.liferay.portal.cloudservices.akka.actor.CamelActor;
import com.liferay.portal.cloudservices.csep.service.CommandService;
import com.liferay.portal.cloudservices.messaging.CommandRequestMessage;
import com.liferay.portal.cloudservices.messaging.CommandResponseMessage;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Random;

/**
 * This actor is responsible of "messages cloud side".
 *
 * The "web application" will send a message to this actor with the desired
 * command that should be sent to the corresponding gateway.
 *
 * Since messages can not be sent from the "cloud services" to the gateway, the latter
 * will pull periodically for new messages in the command center queue.
 *
 * This actor can receive messages in two different ways:
 *
 * 1. Messages from the gateway will reach the endpoint (HTTP) and the latter one will
 * forward the original message to this actor.
 *
 * 2. Messages from the "web application" pushing the commands which will be stores
 * in the queue system. In this case we could use the Akka remoting facility instead
 * of using HTTP
 *
 * TODO Improve the queue system
 *
 * Right now the messages are stored "in-memory" using a queue (so messages would be lost
 * if this actor goes down). This storage should be an advanced system queue which is able
 * to handle the persistence of the messages. See amqp, sqs, etc
 *
 * @author Miguel Pastor
 */
public class CommandActor extends CamelActor {

	@Autowired
	ActorRef commandProducerActor;

	@Autowired
	CommandService commandService;

    @Override
    public void onReceive(Object message) {
        if (message instanceof CommandRequestMessage) {

			//For test porposes, in real system this will be set by outside system
			((CommandRequestMessage)message).setCorrelationId(
				new Random().nextLong() + ":COMMAND");

			commandProducerActor.tell(createCamelMessage(message), getSelf());
        }else if(message instanceof CamelMessage){
			CommandResponseMessage responseMessage = getCamelMessageBody(
				(CamelMessage) message, CommandResponseMessage.class);

			System.out.println(
				"Receiving command response: " +
					responseMessage.getCorrelationId());

			commandService.processCommandResponse(responseMessage);
		}else{
			unhandled(message);
		}
    }

}