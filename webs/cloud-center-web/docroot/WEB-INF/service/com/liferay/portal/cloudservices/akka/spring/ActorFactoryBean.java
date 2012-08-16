package com.liferay.portal.cloudservices.akka.spring;


import akka.actor.*;
import akka.routing.RouterConfig;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.SmartFactoryBean;
import org.springframework.beans.factory.annotation.AutowiredAnnotationBeanPostProcessor;
import org.springframework.context.annotation.CommonAnnotationBeanPostProcessor;

/**
 * @author Ivica Cardic
 */
public class ActorFactoryBean implements
	SmartFactoryBean<ActorRef>, BeanFactoryAware {

	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		_autowiredPostProcessor.setBeanFactory(beanFactory);
		_resourcePostProcessor.setBeanFactory(beanFactory);
	}

	public boolean isPrototype() {
		return false;
	}

	public boolean isEagerInit() {
		return true;
	}

	public ActorRef getObject() throws Exception {

		Props props = new Props(new UntypedActorFactory() {
			public Actor create() {
				final Actor actorImplementation = createActor();

				_autowiredPostProcessor.processInjection(
					actorImplementation);
				_resourcePostProcessor.postProcessPropertyValues(
					null, null, actorImplementation, "dontcare");

				return actorImplementation;
			}
		});

		if(_routerConfig != null){
			props.withRouter(_routerConfig);
		}

		ActorRef actorRef = _actorSystem.actorOf(props, _actorName);

		return actorRef;
	}

	public Class<?> getObjectType() {
		return ActorRef.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getActorClassName() {
		return _actorClassName;
	}

	public void setActorClassName(String _actorClassName) {
		this._actorClassName = _actorClassName;
	}

	public ActorSystem getActorSystem() {
		return _actorSystem;
	}

	public void setActorSystem(ActorSystem _actorSystem) {
		this._actorSystem = _actorSystem;
	}

	public String getActorName() {
		return _actorName;
	}

	public void setActorName(String _actorName) {
		this._actorName = _actorName;
	}

	public RouterConfig getRouterConfig() {
		return _routerConfig;
	}

	public void setRouterConfig(RouterConfig _routerConfig) {
		this._routerConfig = _routerConfig;
	}

	protected Actor createActor(){
		try {
			return (Actor) Class.forName(_actorClassName).newInstance();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private String _actorClassName;
	private String _actorName;
	private ActorSystem _actorSystem;
	private RouterConfig _routerConfig;


	private AutowiredAnnotationBeanPostProcessor _autowiredPostProcessor =
		new AutowiredAnnotationBeanPostProcessor();
	private CommonAnnotationBeanPostProcessor _resourcePostProcessor =
		new CommonAnnotationBeanPostProcessor();

}

