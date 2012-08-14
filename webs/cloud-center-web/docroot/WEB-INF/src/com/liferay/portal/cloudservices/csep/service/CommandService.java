package com.liferay.portal.cloudservices.csep.service;


import com.liferay.portal.cloudservices.messaging.CommandResponseMessage;

public interface CommandService {
	public void processCommandResponse(CommandResponseMessage message);
}
