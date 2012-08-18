package com.liferay.portal.cloudservices.akka.spring;


import akka.actor.ActorSystem;
import akka.camel.Camel;
import akka.camel.CamelExtension;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.TypeConverter;
import org.springframework.beans.factory.FactoryBean;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Ivica Cardic
 */
public class ActorSystemFactoryBean implements FactoryBean<ActorSystem>{
	public ActorSystem getObject() throws Exception {
		ActorSystem actorSystem = ActorSystem.create(_name);

		Camel camel = CamelExtension.get(actorSystem);
		CamelContext camelContext = camel.context();

		if(_camelComponents.size() > 0) {
			_log.debug("Installing Apache Camel components");

			for (String key : _camelComponents.keySet()) {
				camelContext.addComponent(key, _camelComponents.get(key));
			}
		}

		_log.debug("Installing Apache Camel Type Converters");

		for(String key: _typeConverters.keySet()){
			camelContext.getTypeConverterRegistry().addTypeConverter(
				String.class, Class.forName(key), _typeConverters.get(key));

			camelContext.getTypeConverterRegistry().addTypeConverter(
				Class.forName(key), String.class, _typeConverters.get(key));

		}

		return actorSystem;
	}

	public Class<?> getObjectType() {
		return ActorSystem.class;
	}

	public boolean isSingleton() {
		return true;
	}

	public String getName() {
		return _name;
	}

	public void setName(String _name) {
		this._name = _name;
	}

	public Map<String, Component> getCamelComponents() {
		return _camelComponents;
	}

	public void setCamelComponents(Map<String, Component> _camelComponents) {
		this._camelComponents = _camelComponents;
	}

	public Map<String, TypeConverter> getTypeConverters() {
		return _typeConverters;
	}

	public void setTypeConverters(Map<String, TypeConverter> typeConverters) {
		this._typeConverters = typeConverters;
	}

	private static Log _log =
		LogFactoryUtil.getLog(ActorSystemFactoryBean.class);

	private String _name;
	private Map<String, Component> _camelComponents =
		new HashMap<String, Component>();

	private Map<String, TypeConverter> _typeConverters =
		new HashMap<String, TypeConverter>();
}
