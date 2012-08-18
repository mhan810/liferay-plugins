package com.liferay.portal.cloudservices.gateway.actor;

import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import com.liferay.portal.cloudservices.messaging.HeartBeatMessage;
import com.liferay.portal.cloudservices.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This actor is responsible for sending the initial <code>handshake</code> to
 * the cloud service.<br>
 * <p/>
 * Periodically (configurable) this actor will send a "keep alive" to the cloud service.
 *
 * @author Miguel Pastor
 * @author Ivica cardic
 */
public class HeartBeatActor extends UntypedActor {

	@Autowired
	ActorRef heartBeatProducerActor;

	public void onReceive(Object message) {
		if (Message.SCHEDULER_HEARTBEAT_MESSAGE.equals(message)) {
			heartBeatProducerActor.tell(new HeartBeatMessage());
		}else {
			unhandled(message);
		}
	}

}
