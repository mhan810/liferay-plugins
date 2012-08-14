package com.liferay.portal.cloudservices.csep.actor;

import akka.camel.CamelMessage;
import com.liferay.portal.cloudservices.akka.actor.CamelActor;
import com.liferay.portal.cloudservices.csep.service.MetricsService;
import com.liferay.portal.cloudservices.messaging.MetricsMessage;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This actor is reponsible for handling the metrics messages from the gateway
 * system
 *
 * @author Miguel Pastor
 * @author Ivica Cardic
 *
 */
public class MetricsActor extends CamelActor {
	
	@Autowired
	MetricsService metricsService;

	public void onReceive(Object message) {
        if (message instanceof CamelMessage) {
			MetricsMessage metricsMessage = getCamelMessageBody(
				(CamelMessage) message, MetricsMessage.class);

            System.out.println(
				"I am " + getSelf() +
				": Persist message in the datastore: " +
					metricsMessage.getName());
			
			metricsService.saveMessage(metricsMessage);
        }else {
			unhandled(message);
		}
	}

}
