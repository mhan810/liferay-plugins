package com.liferay.portal.cloudservices.messaging;


/**
 * Ivica Cardic
 */
public interface Message {

	public static final String SCHEDULER_HEARTBEAT_MESSAGE = "scheduler_tick";
	public static final String SCHEDULER_METRICS_MESSAGE = "scheduler_metrics";

	public long getTimestamp();

}
