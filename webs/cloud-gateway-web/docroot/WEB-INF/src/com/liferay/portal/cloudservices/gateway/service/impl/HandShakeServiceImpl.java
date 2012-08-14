package com.liferay.portal.cloudservices.gateway.service.impl;


import akka.actor.ActorRef;
import akka.util.Timeout;
import com.liferay.portal.cloudservices.gateway.service.HandShakeService;
import com.liferay.portal.cloudservices.messaging.HandShakeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import scala.concurrent.Await;
import scala.concurrent.Future;
import scala.concurrent.util.Duration;

import java.util.concurrent.TimeUnit;

import static akka.pattern.Patterns.ask;

/**
 * @author Miguel Pastor
 * @author Ivica Cardic
 */
public class HandShakeServiceImpl implements HandShakeService {

	@Autowired
	ActorRef handShakeProducerActor;

	@Override
	public void init() {
		_doInitialHandshake();
	}

	private void _doInitialHandshake() {
		final Timeout timeout = new Timeout(
			Duration.create(10, TimeUnit.SECONDS));

		Future<Object> handshake = ask(
			handShakeProducerActor, new HandShakeMessage(), timeout);

		// synchronous waiting to establish the handshake

		try {
			// TODO handle different values for the handshake; right now assuming everything is fine
			Await.result(handshake, Duration.create(10, TimeUnit.SECONDS));

			System.out.println("User is authenticated");
		}
		catch(Exception ate) {
			// TODO Error in handshaking?? What to do?? Retry?? Exit??
			System.out.println("Error while handshaking!!!");
			ate.printStackTrace();
			System.exit(-1);
		}

	}
}
