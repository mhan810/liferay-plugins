package com.liferay.portal.cloudservices.messaging;

/**
 * Base class for all the metrics related messages.Every new message related to
 * metrics will need to extend that class.
 * 
 * @author Miguel Pastor
 * @author Ivica Cardic
 * @author Igor Beslic
 */
public class MetricsMessage extends BaseMessage {

    public MetricsMessage() {
        super();
        setType(MessageDefinition.METRIC_MESSAGE);
    }

    public String getChecksum() {
        return _checksum;
    }

    public String getSignature() {
        return _signature;
    }

    public String getTarget() {
        return _target;
    }

    public void setChecksum(String checksum) {
        this._checksum = checksum;
    }

    public void setSignature(String signature) {
        this._signature = signature;
    }

    public void setTarget(String target) {
        this._target = target;
    }

    private String _checksum;
    private String _signature;
    private String _target;
}
