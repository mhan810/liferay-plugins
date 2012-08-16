package com.liferay.portal.cloudservices.messaging;


import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import org.apache.camel.Exchange;
import org.apache.camel.TypeConversionException;
import org.apache.camel.support.TypeConverterSupport;
import org.jabsorb.JSONSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.UnmarshallException;

/**
 * @author Ivica Cardic
 */
public class MessageConverter extends TypeConverterSupport{
	public MessageConverter(){
		try {
			_jsonSerializer.registerDefaultSerializers();
		} catch (Exception e) {
			_log.error(e, e);
		}
	}

	public <T> T convertTo(Class<T> tClass, Exchange exchange, Object o)
		throws TypeConversionException {

		if(tClass.equals(String.class)){
			try {
				return (T)_jsonSerializer.toJSON(o);
			} catch (MarshallException e) {
				_log.error(e);

				throw new IllegalStateException("Unable to serialize oject", e);
			}
		}else{
			try {
				return (T)_jsonSerializer.fromJSON((String) o);
			} catch (UnmarshallException e) {
				_log.error(e);

				throw new IllegalStateException("Unable to deserialize oject", e);
			}
		}
	}

	private static Log _log = LogFactoryUtil.getLog(MessageConverter.class);


	private final JSONSerializer _jsonSerializer = new JSONSerializer();
}
