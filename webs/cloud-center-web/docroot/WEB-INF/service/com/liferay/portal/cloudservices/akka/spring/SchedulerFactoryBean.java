package com.liferay.portal.cloudservices.akka.spring;


import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Cancellable;
import org.springframework.beans.factory.SmartFactoryBean;
import scala.concurrent.util.Duration;

import java.util.concurrent.TimeUnit;

/**
 * @author Ivica Cardic
 */
public class SchedulerFactoryBean  implements
	SmartFactoryBean<Cancellable> {


	public boolean isPrototype() {
		return false;
	}

	public boolean isEagerInit() {
		return true;
	}

	public Cancellable getObject() throws Exception {
		return _actorSystem.scheduler().schedule(
			Duration.create(
				_delay, _delayUnit),
				Duration.create(_frequency, _frequencyUnit), _actor, _message);
	}

	public Class<?> getObjectType() {
		return Cancellable.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public ActorRef getActor() {
		return _actor;
	}

	public void setActor(ActorRef actor) {
		this._actor = actor;
	}

	public ActorSystem getActorSystem() {
		return _actorSystem;
	}

	public void setActorSystem(ActorSystem actorSystem) {
		this._actorSystem = actorSystem;
	}

	public int getDelay() {
		return _delay;
	}

	public void setDelay(int delay) {
		this._delay = delay;
	}

	public TimeUnit getDelayUnit() {
		return _delayUnit;
	}

	public void setDelayUnit(TimeUnit delayUnit) {
		this._delayUnit = delayUnit;
	}

	public int getFrequency() {
		return _frequency;
	}

	public void setFrequency(int frequency) {
		this._frequency = frequency;
	}

	public TimeUnit getFrequencyUnit() {
		return _frequencyUnit;
	}

	public void setFrequencyUnit(TimeUnit frequencyUnit) {
		this._frequencyUnit = frequencyUnit;
	}

	public Object getMessage() {
		return _message;
	}

	public void setMessage(Object message) {
		this._message = message;
	}

	private ActorRef _actor;
	private ActorSystem _actorSystem;
	private Object _message;
	private int _delay;
	private TimeUnit _delayUnit;
	private int _frequency;
	private TimeUnit _frequencyUnit;

}
