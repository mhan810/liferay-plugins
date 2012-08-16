package com.liferay.portal.cloudservices.gateway.service.impl;

import com.liferay.portal.cloudservices.gateway.service.CommandService;
import com.liferay.portal.cloudservices.messaging.CommandRequestMessage;
import com.liferay.portal.cloudservices.messaging.CommandResponseMessage;

/**
 * @author Ivica Cardic
 */
public class CommandServiceImpl implements CommandService{
	public CommandResponseMessage processCommandRequest(
		CommandRequestMessage requestMessage) {

		System.out.println(
			"Processing command request: " + requestMessage.getCorrelationId());

		CommandResponseMessage responseMessage = new CommandResponseMessage();
		responseMessage.setCorrelationId(requestMessage.getCorrelationId());
        responseMessage.setType("message response");

		return responseMessage;
	}
}
