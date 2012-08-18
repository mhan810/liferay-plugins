package com.liferay.portal.cloudservices.messaging;

/**
 * This class has the definitions for the messages.
 *
 * TODO Change this!! This is a stupid class just for gathering all
 * the strings used as messages in the prototype
 * 
 * @author Miguel Pastor
 */
public class MessageDefinition {

    public static final String COMMAND_INSTALL_PATCH_MESSAGE = "installPatch";
    public static final String COMMAND_CHECK_HEARTBEAT_MESSAGE = "checkHeartbeat";
	public static final String HANDSHAKE_MESSAGE = "handshake";
	public static final String HEARTBEAT_MESSAGE = "tick";
	public static final String METRIC_MESSAGE = "metric";

	public static final String POOL_MESSAGE = "isThereNewMessage";
    public static final String POOL_NO_DATA_MESSAGE = "noMessage";

}
