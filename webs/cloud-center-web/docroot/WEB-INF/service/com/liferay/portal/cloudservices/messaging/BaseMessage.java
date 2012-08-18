package com.liferay.portal.cloudservices.messaging;

import java.util.Map;

/**
 * Base class for all the messages. Marker class
 * 
 * @author Miguel Pastor
 * @author Ivica Cardic
 * @author Igor Beslic
 */
public abstract class BaseMessage implements Message {
	public BaseMessage() {
		this._created = System.currentTimeMillis();
	}

	public long getCreated() {
		return _created;
	}

    public String getOriginId() {
        return _originId;
    }

    public Map<String,String> getParms() {
        return _parms;
    }

	public String getType() {
		return this._type;
	}

    public void setCreated(long _created) {
        this._created = _created;
    }

    public void setOriginId(String originId) {
        this._originId = _originId;
    }

    public void setParms(Map<String,String> parms) {
        this._parms = parms;
    }

	public void setType(String name) {
		this._type = name;
	}

    private long _created;
    private String _originId;
    private String _type;
    private Map<String, String> _parms;
	
}
