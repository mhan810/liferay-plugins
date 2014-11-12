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

package com.liferay.portal.search.solr.server;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.search.solr.http.BasicAuthPoolingHttpClientFactory;
import com.liferay.portal.search.solr.http.HttpSolrServer;

import java.util.List;

import org.apache.http.HttpRequestInterceptor;
import org.apache.http.auth.AuthScope;
import org.apache.http.client.HttpClient;

/**
 * @author László Csontos
 */
@Deprecated
public class BasicAuthSolrServer extends HttpSolrServer {

	@Override
	public void afterPropertiesSet() {
		if (_log.isWarnEnabled()) {
			StringBundler sb = new StringBundler(4);

			sb.append(BasicAuthSolrServer.class);
			sb.append(" has been deprecated; please use ");
			sb.append(HttpSolrServer.class);
			sb.append(" instead.");

			_log.warn(sb.toString());
		}

		_httpClientFactory = new BasicAuthPoolingHttpClientFactory();

		_httpClientFactory.setAllowCompression(_allowCompression);
		_httpClientFactory.setAuthScope(_authScope);
		_httpClientFactory.setConnectionTimeout(_connectionTimeout);
		_httpClientFactory.setDefaultMaxConnectionsPerRoute(
			_defaultMaxConnectionsPerRoute);
		_httpClientFactory.setHttpRequestInterceptors(_httpRequestInterceptors);
		_httpClientFactory.setFollowRedirects(_followRedirects);
		_httpClientFactory.setMaxTotalConnections(_maxTotalConnections);
		_httpClientFactory.setPassword(_password);
		_httpClientFactory.setSoTimeout(_soTimeout);
		_httpClientFactory.setUsername(_username);

		HttpClient httpClient = _httpClientFactory.createInstance();

		setHttpClient(httpClient);

		super.afterPropertiesSet();
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

	public void setPassword(String password) {
		_password = password;
	}

	public void setSoTimeout(int soTimeout) {
		_soTimeout = soTimeout;
	}

	public void setUsername(String username) {
		_username = username;
	}

	@Override
	protected void doShutdown() {
		super.doShutdown();

		_httpClientFactory.destroyInstance();
	}

	private static Log _log = LogFactoryUtil.getLog(BasicAuthSolrServer.class);

	private Boolean _allowCompression;
	private AuthScope _authScope;
	private Integer _connectionTimeout;
	private Integer _defaultMaxConnectionsPerRoute;
	private Boolean _followRedirects;
	private BasicAuthPoolingHttpClientFactory _httpClientFactory;
	private List<HttpRequestInterceptor> _httpRequestInterceptors;
	private Integer _maxTotalConnections;
	private String _password;
	private Integer _soTimeout;
	private String _username;

}