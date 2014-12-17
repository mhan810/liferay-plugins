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

/**
 * @author László Csontos
 * @author André de Oliveira
 */
public class KeyStoreLoader {

	public static KeyStore load(
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

	protected static void dumpKeyStore(KeyStore keyStore)
		throws KeyStoreException {

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

	protected static InputStream loadFile(String fileName)
		throws FileNotFoundException {

		if (_log.isDebugEnabled()) {
			_log.debug("Loading file: " + fileName);
		}

		InputStream inputStream = null;

		if (fileName.startsWith("classpath:")) {
			fileName = fileName.substring(10);

			Class<?> clazz = KeyStoreLoader.class;

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

	private KeyStoreLoader() {
	}

	private static Log _log = LogFactoryUtil.getLog(KeyStoreLoader.class);

}