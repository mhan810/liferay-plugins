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
import com.liferay.portal.kernel.log.SanitizerLogWrapper;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringBundler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;

import java.util.Enumeration;

import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.conn.ssl.X509HostnameVerifier;

/**
 * @author László Csontos
 */
public class CertAuthPoolingHttpClientFactory
	extends BasePoolingHttpClientFactory {

	@Override
	public void afterPropertiesSet() throws Exception {
		initSSLSocketFactory();

		super.afterPropertiesSet();
	}

	public void setKeyStoreLocation(String keyStoreLocation) {
		_keyStoreLocation = keyStoreLocation;
	}

	public void setKeyStorePassword(char[] keyStorePassword) {
		_keyStorePassword = keyStorePassword;
	}

	public void setKeyStoreType(String keyStoreType) {
		_keyStoreType = keyStoreType;
	}

	public void setTrustStoreLocation(String trustStoreLocation) {
		_trustStoreLocation = trustStoreLocation;
	}

	public void setTrustStorePassword(char[] trustStorePassword) {
		_trustStorePassword = trustStorePassword;
	}

	public void setTrustStoreType(String trustStoreType) {
		_trustStoreType = trustStoreType;
	}

	public void setVerifyServerCertificate(boolean verifyServerCertificate) {
		_verifyServerCertificate = verifyServerCertificate;
	}

	public void setVerifyServerHostname(boolean verifyServerHostname) {
		_verifyServerHostname = verifyServerHostname;
	}

	protected void dumpKeyStore(KeyStore keyStore) throws KeyStoreException {
		Enumeration<String> aliases = keyStore.aliases();

		Log log = SanitizerLogWrapper.allowCRLF(_log);

		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();

			boolean certificateEntry = keyStore.isCertificateEntry(alias);

			StringBundler sb = null;

			if (certificateEntry) {
				sb = new StringBundler(8);
			}
			else {
				sb = new StringBundler(6);
			}

			sb.append("alias=");
			sb.append(alias);
			sb.append(",isKeyEntry=");
			sb.append(keyStore.isKeyEntry(alias));
			sb.append(",isCertificateEntry=");
			sb.append(certificateEntry);

			if (certificateEntry) {
				Certificate certificate = keyStore.getCertificate(alias);

				sb.append(",certificate=");
				sb.append(certificate.toString());
			}

			log.debug(sb.toString());
		}
	}

	@Override
	protected SchemeRegistry getSchemeRegistry() {
		Scheme scheme = new Scheme(
			_DEFAULT_SCHEME_NAME, _DEFAULT_SCHEME_PORT, _socketFactory);

		SchemeRegistry schemeRegistry = new SchemeRegistry();

		schemeRegistry.register(scheme);

		return schemeRegistry;
	}

	protected KeyStore initKeyStore(
			String keyStoreType, String keyStoreLocation,
			char[] keyStorePassword)
		throws Exception {

		if (keyStoreLocation == null) {
			return null;
		}

		KeyStore keyStore = KeyStore.getInstance(keyStoreType);

		InputStream inputStream = loadFile(keyStoreLocation);

		try {
			keyStore.load(inputStream, keyStorePassword);

			if (_log.isDebugEnabled()) {
				dumpKeyStore(keyStore);
			}
		}
		finally {
			StreamUtil.cleanUp(inputStream);
		}

		return keyStore;
	}

	protected void initSSLSocketFactory() throws Exception {
		KeyStore keyStore = initKeyStore(
			_keyStoreType, _keyStoreLocation, _keyStorePassword);

		if (keyStore == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Custom keyStore has not been initialized, falling back " +
						"to system's defaults.");
			}

			_socketFactory = SSLSocketFactory.getSystemSocketFactory();

			return;
		}

		KeyStore trustStore = initKeyStore(
			_trustStoreType, _trustStoreLocation, _trustStorePassword);

		if (trustStore == null) {
			if (_log.isDebugEnabled()) {
				_log.debug(
					"Custom trustStore has not been initialized, falling " +
						"back to system's defaults.");
			}

			_socketFactory = SSLSocketFactory.getSystemSocketFactory();

			return;
		}

		TrustStrategy trustStrategy = null;

		if (!_verifyServerCertificate) {
			trustStrategy = new TrustSelfSignedStrategy();
		}

		X509HostnameVerifier hostnameVerifier =
			SSLSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER;

		if (!_verifyServerHostname) {
			hostnameVerifier = SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
		}

		try {
			_socketFactory = new SSLSocketFactory(
				_DEFAULT_ALGORITHM, keyStore, String.valueOf(_keyStorePassword),
				trustStore, null, trustStrategy, hostnameVerifier);
		}
		catch (Exception e) {
			if (_log.isWarnEnabled()) {
				_log.warn(
					"Custom SSLSocketFactory initialization has failed; " +
						"falling back to system's default.", e);
			}

			_socketFactory = SSLSocketFactory.getSystemSocketFactory();
		}
	}

	protected InputStream loadFile(String fileName)
		throws FileNotFoundException {

		if (_log.isDebugEnabled()) {
			_log.debug("Loading file: " + fileName);
		}

		InputStream inputStream = null;

		if (fileName.startsWith("classpath:")) {
			fileName = fileName.substring(10);

			Class<?> clazz = getClass();

			inputStream = clazz.getResourceAsStream(fileName);
		}

		if (inputStream != null) {
			return inputStream;
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				fileName + " hasn't been found on the classpath; trying to " +
					"load from the filesystem.");
		}

		return new FileInputStream(fileName);
	}

	private static final String _DEFAULT_ALGORITHM = SSLSocketFactory.TLS;

	private static final String _DEFAULT_KEYSTORE_TYPE =
		KeyStore.getDefaultType();

	private static final String _DEFAULT_SCHEME_NAME = "https";

	private static final int _DEFAULT_SCHEME_PORT = 443;

	private static Log _log = LogFactoryUtil.getLog(
		CertAuthPoolingHttpClientFactory.class);

	private String _keyStoreLocation;
	private char[] _keyStorePassword;
	private String _keyStoreType = _DEFAULT_KEYSTORE_TYPE;
	private SSLSocketFactory _socketFactory;
	private String _trustStoreLocation;
	private char[] _trustStorePassword;
	private String _trustStoreType = _DEFAULT_KEYSTORE_TYPE;
	private boolean _verifyServerCertificate = true;
	private boolean _verifyServerHostname = true;

}