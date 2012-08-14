package com.liferay.portal.cloudservices.messaging;


import com.liferay.portal.json.JSONFactoryImpl;
import com.liferay.portal.kernel.json.JSONFactory;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.apache.camel.support.TypeConverterSupport;

/**
 * @author Ivica Cardic
 */
public class MessageConverter extends TypeConverterSupport{

	@Override
	public <T> T convertTo(Class<T> tClass, Exchange exchange, Object o)
		throws TypeConversionException {

		if(tClass.equals(String.class)){
			return (T)_jsonFactory.serialize(o);
		}else{
			return (T)_jsonFactory.deserialize((String) o);
		}
	}

	
	private static final JSONFactory _jsonFactory = new JSONFactoryImpl();
}
