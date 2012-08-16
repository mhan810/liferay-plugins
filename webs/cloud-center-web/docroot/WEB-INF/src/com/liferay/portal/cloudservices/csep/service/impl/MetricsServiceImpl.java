package com.liferay.portal.cloudservices.csep.service.impl;


import com.liferay.portal.cloudservices.csep.service.MetricsService;
import com.liferay.portal.cloudservices.messaging.MetricsMessage;

/**
 * @author Ivica Cardic
 */
public class MetricsServiceImpl implements MetricsService{

	public void saveMessage(MetricsMessage message) {
		System.out.println(
			"Saving message: " + message.getType() + ", " +
				message.getCreated());
	}

	// TODO reference to the datastore service.
	// TODO Improvement We should build an abstraction in order to be able to swith the provider (Riak, Cassandra, Mongo, etc)

	// private DataStore _dataStore = null;
}
