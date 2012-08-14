package com.liferay.portal.cloudservices.csep.actor;

import akka.camel.CamelMessage;
import com.liferay.portal.cloudservices.akka.actor.CamelActor;
import com.liferay.portal.cloudservices.messaging.HandShakeMessage;

/**
 * This actor is responsible for handling the handshake requests
 * 
 * @author Miguel Pastor
 * @author Ivica Cardic
 * 
 */
public class HandshakeActor extends CamelActor {

	public void onReceive(Object message) {
		if (message instanceof CamelMessage) {
			HandShakeMessage m = getCamelMessageBody(
				(CamelMessage) message, HandShakeMessage.class);

            System.out.println(
				"Authenticate user!! Sending response to " + getSender() );

		    getSender().tell("ok");
		}else {
            throw new IllegalStateException("Unable to handle " + message);
        }
	}


}
