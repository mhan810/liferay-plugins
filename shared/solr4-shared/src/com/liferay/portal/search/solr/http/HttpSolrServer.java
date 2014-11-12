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
import com.liferay.portal.kernel.util.Validator;

import java.io.IOException;

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.ResponseParser;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;

/**
 * @author Bruno Farache
 */
public class HttpSolrServer extends SolrServer {

	public void afterPropertiesSet() {
		_server = new org.apache.solr.client.solrj.impl.HttpSolrServer(
			_url, _httpClient);

		if (Validator.isNotNull(_baseURL)) {
			_server.setBaseURL(_baseURL);
		}

		if (_maxRetries != null) {
			_server.setMaxRetries(_maxRetries);
		}

		if (_responseParser != null) {
			_server.setParser(_responseParser);
		}
	}

	public String getBaseURL() {
		return _server.getBaseURL();
	}

	public HttpClient getHttpClient() {
		return _server.getHttpClient();
	}

	public ModifiableSolrParams getInvariantParams() {
		return _server.getInvariantParams();
	}

	public ResponseParser getParser() {
		return _server.getParser();
	}

	@Override
	public NamedList<Object> request(SolrRequest solrRequest)
		throws IOException, SolrServerException {

		if (_stopped.get()) {
			return null;
		}

		return _server.request(solrRequest);
	}

	public NamedList<Object> request(
			SolrRequest solrRequest, ResponseParser responseParser)
		throws IOException, SolrServerException {

		if (_stopped.get()) {
			return null;
		}

		return _server.request(solrRequest, responseParser);
	}

	public void setBaseURL(String baseURL) {
		_baseURL = baseURL;

		if (_server != null) {
			_server.setBaseURL(baseURL);
		}
	}

	public void setHttpClient(HttpClient httpClient) {
		_httpClient = httpClient;
	}

	public void setMaxRetries(int maxRetries) {
		_maxRetries = maxRetries;

		if (_server != null) {
			_server.setMaxRetries(maxRetries);
		}
	}

	public void setParser(ResponseParser responseParser) {
		_responseParser = responseParser;

		if (_server != null) {
			_server.setParser(responseParser);
		}
	}

	public void setUrl(String url) {
		_url = url;
	}

	@Override
	public void shutdown() {
		if (_stopped.compareAndSet(false, true)) {
			doShutdown();

			if (_log.isInfoEnabled()) {
				_log.info(toString() + " has been shut down.");
			}
		}
	}

	protected void doShutdown() {
		_server.shutdown();
	}

	private static Log _log = LogFactoryUtil.getLog(HttpSolrServer.class);

	private String _baseURL;
	private HttpClient _httpClient;
	private Integer _maxRetries;
	private ResponseParser _responseParser;
	private org.apache.solr.client.solrj.impl.HttpSolrServer _server;
	private AtomicBoolean _stopped = new AtomicBoolean(false);
	private String _url;

}