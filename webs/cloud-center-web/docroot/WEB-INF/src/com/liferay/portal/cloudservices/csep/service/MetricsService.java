package com.liferay.portal.cloudservices.csep.service;


import com.liferay.portal.cloudservices.messaging.Message;
import com.liferay.portal.cloudservices.messaging.MetricsMessage;

/**
 * @author Ivica Cardic
 */
public interface MetricsService {
	void saveMessage(MetricsMessage message);
}
