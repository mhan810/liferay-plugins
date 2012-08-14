package com.liferay.portal.cloudservices.gateway.service.impl;


import com.liferay.portal.cloudservices.gateway.service.MessageService;
import com.liferay.portal.cloudservices.messaging.MetricsMessage;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ivica Cardic
 */
public class MetricsServiceImpl implements MessageService {
	public List<MetricsMessage> collectMessages() {
		List<MetricsMessage> list = new ArrayList<MetricsMessage>();

		MetricsMessage portalMetricsMessage = new MetricsMessage();
		portalMetricsMessage.setName("portal");

		list.add(portalMetricsMessage);

		MetricsMessage jvmMetricsMessage = new MetricsMessage();
		jvmMetricsMessage.setName("jvm");

		list.add(jvmMetricsMessage);

		return list;
	}
}
