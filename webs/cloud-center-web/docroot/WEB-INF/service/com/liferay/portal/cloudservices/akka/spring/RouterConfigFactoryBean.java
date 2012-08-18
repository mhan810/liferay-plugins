package com.liferay.portal.cloudservices.akka.spring;


import akka.actor.SupervisorStrategy;
import akka.routing.RoundRobinRouter;
import akka.routing.RouterConfig;
import org.springframework.beans.factory.FactoryBean;

/**
 * @author Ivica Cardic
 */
public class RouterConfigFactoryBean implements FactoryBean<RouterConfig> {
	public RouterConfig getObject() throws Exception {
		RoundRobinRouter rc = new RoundRobinRouter(_nrOfInstance);

		if(_supervisorStrategy != null){
			rc.withSupervisorStrategy(_supervisorStrategy);
		}

		return rc;
	}

	public Class<?> getObjectType() {
		return RouterConfig.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public int getNrOfInstance() {
		return _nrOfInstance;
	}

	public void setNrOfInstance(int nrOfInstance) {
		this._nrOfInstance = nrOfInstance;
	}

	public SupervisorStrategy getSupervisorStrategy() {
		return _supervisorStrategy;
	}

	public void setSupervisorStrategy(SupervisorStrategy supervisorStrategy) {
		this._supervisorStrategy = supervisorStrategy;
	}

	private int _nrOfInstance;
	private SupervisorStrategy _supervisorStrategy;
}
