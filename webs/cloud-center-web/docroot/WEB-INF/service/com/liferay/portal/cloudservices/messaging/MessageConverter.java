package com.liferay.portal.cloudservices.messaging;


import com.liferay.portal.kernel.json.JSONFactoryUtil;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.apache.camel.support.TypeConverterSupport;

/**
 * @author Ivica Cardic
 */
public class MessageConverter extends TypeConverterSupport{

	public <T> T convertTo(Class<T> tClass, Exchange exchange, Object o)
		throws TypeConversionException {

		if(tClass.equals(String.class)){
			return (T)JSONFactoryUtil.serialize(o);
		}else{
			return (T)JSONFactoryUtil.deserialize((String) o);
		}
	}

}
