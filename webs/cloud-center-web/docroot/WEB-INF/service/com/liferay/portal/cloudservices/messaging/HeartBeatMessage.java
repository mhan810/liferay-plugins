package com.liferay.portal.cloudservices.messaging;

import java.io.Serializable;

/**
 * This class represents the <code>heartbeat</code> used to control the status
 * of the gateways
 *
 * @author Miguel Pastor
 * @author Ivica Cardic
 * @author Igor Beslic
 */
public class HeartBeatMessage extends BaseMessage implements Serializable{

    public HeartBeatMessage() {
        super();
        setType(MessageDefinition.HEARTBEAT_MESSAGE);
    }

    public String getVersion() {
        return _version;
    }

    public void setVersion(String version) {
        this._version = version;
    }

    private String _version;
}
