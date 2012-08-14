package com.liferay.portal.cloudservices.csep.actor;

import akka.camel.CamelMessage;
import com.liferay.portal.cloudservices.akka.actor.CamelActor;
import com.liferay.portal.cloudservices.messaging.HeartBeatMessage;

/**
 * @author Miguel Pastor
 * @author Ivica Cardic
 */
public class HeartBeatActor extends CamelActor {

	public void onReceive(Object message) {
		if (message instanceof CamelMessage) {
			HeartBeatMessage m = getCamelMessageBody(
				(CamelMessage) message, HeartBeatMessage.class);

			// TODO handle the heartbeat

			// 1. track the gateway
			System.out.println(
				"Received a heartbeat message from the system " +
					getSender() + " at " + m.getTimestamp());

			// 2. do we need to ack the heartbeat request??
		} else {
			throw new IllegalStateException(
				"Unable to handle this kind of message: " + message);
		}
	}

	//TODO
	// Keep track for all the active gateways
	// private Map _aliveGateways;
}
