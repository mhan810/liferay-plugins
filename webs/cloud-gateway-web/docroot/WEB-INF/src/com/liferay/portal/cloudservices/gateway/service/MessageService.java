package com.liferay.portal.cloudservices.gateway.service;


import com.liferay.portal.cloudservices.messaging.Message;
import com.liferay.portal.cloudservices.messaging.MetricsMessage;

import java.util.List;

/**
 * @author Ivica Cardic
 */
public interface MessageService {
	public List<MetricsMessage> collectMessages();
}
