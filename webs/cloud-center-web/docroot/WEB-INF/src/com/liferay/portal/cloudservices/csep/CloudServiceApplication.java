package com.liferay.portal.cloudservices.csep;

import org.springframework.context.support.ClassPathXmlApplicationContext;


public class CloudServiceApplication{
	
	/**
	* TODO Acts as the deployed app. We should decide how to deploy it!
	*/
	public static void main(String[] args) {
		new ClassPathXmlApplicationContext("META-INF/akka-spring.xml");

		System.out.println("Started the system actor endpoint (over http). Waiting for gateway metrics.");
	}
}
