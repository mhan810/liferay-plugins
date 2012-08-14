package com.liferay.portal.cloudservices.gateway.actor;

import akka.actor.ActorRef;
import com.liferay.portal.cloudservices.akka.actor.CamelActor;
import com.liferay.portal.cloudservices.gateway.service.MessageService;
import com.liferay.portal.cloudservices.messaging.Message;
import org.springframework.beans.factory.annotation.Autowired;

/**
* This actor is responsible for sending the metrics to the cloud service.
* 
* Once this actor is started it will send periodically metrics
* 
*/

/**
 * @author Ivica Cardic
 */
public class MetricsActor extends CamelActor {

	@Autowired
	ActorRef metricsProducerActor;

	@Autowired
	MessageService messageService;

	public void onReceive(Object message) {
        if (Message.SCHEDULER_METRICS_MESSAGE.equals(message)) {
            System.out.println("Collecting metrics and sending metrics . . .");

			for(Message m : messageService.collectMessages()){
				metricsProducerActor.tell(createCamelMessage(m));
			}

        }else {
			unhandled(message);
		}
	}

}
