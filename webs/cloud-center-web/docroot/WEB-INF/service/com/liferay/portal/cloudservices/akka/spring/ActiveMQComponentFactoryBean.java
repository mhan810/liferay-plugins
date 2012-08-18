package com.liferay.portal.cloudservices.akka.spring;

import org.apache.activemq.camel.component.ActiveMQComponent;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Ivica Cardic
 */
public class ActiveMQComponentFactoryBean implements
	FactoryBean<ActiveMQComponent> {

	public ActiveMQComponent getObject() throws Exception {
		return ActiveMQComponent.activeMQComponent(_brokerURL);
	}

	public Class<?> getObjectType() {
		return ActiveMQComponent.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getBrokerURL() {
		return _brokerURL;
	}

	public void setBrokerURL(String brokerURL) {
		this._brokerURL = brokerURL;
	}

	private String _brokerURL;
}

