package com.liferay.portal.cloudservices.csep.service.impl;

import com.liferay.portal.cloudservices.csep.service.CommandService;
import com.liferay.portal.cloudservices.messaging.CommandResponseMessage;

/**
 * @author Ivica Cardic
 */
public class CommandServiceImpl implements CommandService {
	@Override
	public void processCommandResponse(CommandResponseMessage message) {
		System.out.println(
			"Processing command response: " + message.getCorrelationId());
	}
}
