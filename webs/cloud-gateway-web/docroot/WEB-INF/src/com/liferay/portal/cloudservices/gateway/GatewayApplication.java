package com.liferay.portal.cloudservices.gateway;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class GatewayApplication{
	/**
	* TODO For this prototype it acts as the gateway system. This should happen in the Liferay deployment
	*/
	public static void main(String[] args) throws InterruptedException {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("META-INF/akka-spring.xml");
	}

}
