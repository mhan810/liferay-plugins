package com.liferay.portal.cloudservices.gateway.service;

import com.liferay.portal.cloudservices.messaging.CommandRequestMessage;
import com.liferay.portal.cloudservices.messaging.CommandResponseMessage;

/**
 * @author Ivica Cardic
 */
public interface CommandService {
	public CommandResponseMessage processCommandRequest(
		CommandRequestMessage message);
}
