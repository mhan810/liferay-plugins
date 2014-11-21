/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.search.solr.http;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.pool.PoolStats;
import org.apache.solr.client.solrj.impl.HttpClientUtil;

/**
 * @author László Csontos
 */
public abstract class BasePoolingHttpClientFactory {

	public void afterPropertiesSet() throws Exception {
		if (_log.isDebugEnabled()) {
			_log.debug("afterPropertiesSet()");
		}

		SchemeRegistry schemeRegistry = getSchemeRegistry();

		if (schemeRegistry != null) {
			_poolingClientConnectionManager =
				new PoolingClientConnectionManager(schemeRegistry);
		}
		else {
			_poolingClientConnectionManager =
				new PoolingClientConnectionManager();
		}

		if (_defaultMaxConnectionsPerRoute != null) {
			_poolingClientConnectionManager.setDefaultMaxPerRoute(
				_defaultMaxConnectionsPerRoute);
		}

		if (_maxTotalConnections != null) {
			_poolingClientConnectionManager.setMaxTotal(_maxTotalConnections);
		}
	}

	public HttpClient createInstance() {
		if (_log.isDebugEnabled()) {
			_log.debug("createInstance()");
		}

		DefaultHttpClient httpClient = new DefaultHttpClient(
			_poolingClientConnectionManager);

		if (_allowCompression != null) {
			HttpClientUtil.setAllowCompression(httpClient, _allowCompression);
		}

		if (_connectionTimeout != null) {
			HttpClientUtil.setConnectionTimeout(httpClient, _connectionTimeout);
		}

		if (_followRedirects != null) {
			HttpClientUtil.setFollowRedirects(httpClient, _followRedirects);
		}

		if (_soTimeout != null) {
			HttpClientUtil.setSoTimeout(httpClient, _soTimeout);
		}

		Credentials credentials = getCredentials();

		if (credentials != null) {
			if (_authScope == null) {
				_authScope = AuthScope.ANY;
			}

			CredentialsProvider credentialsProvider =
				httpClient.getCredentialsProvider();

			credentialsProvider.setCredentials(_authScope, credentials);
		}

		for (HttpRequestInterceptor httpRequestInterceptor :
				_httpRequestInterceptors) {

			httpClient.addRequestInterceptor(httpRequestInterceptor, 0);
		}

		return httpClient;
	}

	public void destroyInstance() {
		if (_log.isDebugEnabled()) {
			_log.debug("destroyInstance()");
		}

		if (_poolingClientConnectionManager == null) {
			if (_log.isWarnEnabled()) {
				_log.warn("No instance has been created.");
			}

			return;
		}

		int retry = 0;

		while (retry < 10) {
			PoolStats poolStats =
				_poolingClientConnectionManager.getTotalStats();

			int availableConnections = poolStats.getAvailable();

			if (availableConnections <= 0) {
				break;
			}

			if (_log.isDebugEnabled()) {
				_log.debug(
					toString() + " waiting on " + availableConnections +
						" connections");
			}

			_poolingClientConnectionManager.closeIdleConnections(
				200, TimeUnit.MILLISECONDS);

			try {
				Thread.sleep(500);
			}
			catch (InterruptedException ie) {
			}

			retry++;
		}

		_poolingClientConnectionManager.shutdown();

		if (_log.isDebugEnabled()) {
			_log.debug(toString() + " is shutdown");
		}
	}

	public void setAllowCompression(boolean allowCompression) {
		_allowCompression = allowCompression;
	}

	public void setAuthScope(AuthScope authScope) {
		_authScope = authScope;
	}

	public void setConnectionTimeout(int connectionTimeout) {
		_connectionTimeout = connectionTimeout;
	}

	public void setDefaultMaxConnectionsPerRoute(
		int defaultMaxConnectionsPerRoute) {

		_defaultMaxConnectionsPerRoute = defaultMaxConnectionsPerRoute;
	}

	public void setFollowRedirects(boolean followRedirects) {
		_followRedirects = followRedirects;
	}

	public void setHttpRequestInterceptors(
		List<HttpRequestInterceptor> httpRequestInterceptors) {

		_httpRequestInterceptors = httpRequestInterceptors;
	}

	public void setMaxTotalConnections(int maxTotalConnections) {
		_maxTotalConnections = maxTotalConnections;
	}

	public void setSoTimeout(int soTimeout) {
		_soTimeout = soTimeout;
	}

	protected Credentials getCredentials() {
		return null;
	}

	protected SchemeRegistry getSchemeRegistry() {
		return null;
	}

	private static Log _log = LogFactoryUtil.getLog(
		BasePoolingHttpClientFactory.class);

	private Boolean _allowCompression;
	private AuthScope _authScope;
	private Integer _connectionTimeout;
	private Integer _defaultMaxConnectionsPerRoute;
	private Boolean _followRedirects;
	private List<HttpRequestInterceptor> _httpRequestInterceptors =
		Collections.emptyList();
	private Integer _maxTotalConnections;
	private PoolingClientConnectionManager _poolingClientConnectionManager;
	private Integer _soTimeout;

}