package com.liferay.portal.cloudservices.akka.spring;


import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import org.springframework.beans.factory.FactoryBean;
import scala.concurrent.util.Duration;

import java.util.concurrent.TimeUnit;

/**
 * @author Ivica Cardic
 */
public class SupervisorStrategyFactoryBean implements
	FactoryBean<SupervisorStrategy>{

	@Override
	public SupervisorStrategy getObject() throws Exception {
		return new OneForOneStrategy(
			_maxNrOfRetries,
			Duration.create(_withinTimeRange, _withinTimeRangeUnit), _trapExit);
	}

	@Override
	public Class<?> getObjectType() {
		return SupervisorStrategy.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public int getMaxNrOfRetries() {
		return _maxNrOfRetries;
	}

	public void setMaxNrOfRetries(int maxNrOfRetries) {
		this._maxNrOfRetries = maxNrOfRetries;
	}

	public TimeUnit getWithinTimeRangeUnit() {
		return _withinTimeRangeUnit;
	}

	public void setWithinTimeRangeUnit(TimeUnit withinTimeRangeUnit) {
		this._withinTimeRangeUnit = withinTimeRangeUnit;
	}

	public long getWithinTimeRange() {
		return _withinTimeRange;
	}

	public void setWithinTimeRange(long withinTimeRange) {
		this._withinTimeRange = withinTimeRange;
	}

	public Class<Object>[] getTrapExit() {
		return _trapExit;
	}

	public void setTrapExit(Class<Object>[] trapExit) {
		this._trapExit = trapExit;
	}

	private int _maxNrOfRetries;
	private long _withinTimeRange;
	private TimeUnit _withinTimeRangeUnit;
	private Class<Object>[] _trapExit;
}
