/**
 * Copyright (c) 2000-2013 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.search.solr.suggest;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.nio.charset.CharsetEncoderUtil;
import com.liferay.portal.kernel.search.Document;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.StringUtil;
import com.liferay.portal.search.solr.SolrIndexWriter;
import com.liferay.portal.util.PortletKeys;

import java.nio.CharBuffer;
import java.nio.charset.CharsetEncoder;
import java.security.MessageDigest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * @author Daniela Zapata Riesco
 */
public class SuggestionIndexer {

	public static final String FILTER_TYPE_SUGGESTION = "suggestion";

	public static final int UNICODE_BYTE_ORDER_MARK = 65279;

	public void indexSuggestions(
			SearchContext searchContext, long[] groupIds, Locale locale,
			InputStream inputStream)
		throws SearchException {

		Set<Document> documents = new HashSet<Document>();

		BufferedReader bufferedReader = null;

		try {
			InputStreamReader inputStreamReader = new InputStreamReader(
				inputStream, StringPool.UTF8);

			bufferedReader = new BufferedReader(inputStreamReader);

			String line = bufferedReader.readLine();

			if (line == null) {
				return;
			}

			if (line.charAt(0) == UNICODE_BYTE_ORDER_MARK) {
				line = line.substring(1);
			}

			int lineCounter = 0;

			do {
				lineCounter++;

				line = StringUtil.trim(line);

				if (line != null & line.equals(StringPool.BLANK)) {
					line = bufferedReader.readLine();
					continue;
				}

				documents.add(
					getSuggestionDocument(
						searchContext.getCompanyId(), groupIds, locale, line));

				line = bufferedReader.readLine();

				if ((lineCounter == _batchSize) || (line == null)) {
					_solrIndexWriter.addDocuments(searchContext, documents);

					documents.clear();

					lineCounter = 0;
				}
			}
			while (line != null);
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to index suggestions", e);
			}

			throw new SearchException(e.getMessage(), e);
		}
		finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				}
				catch (IOException ioe) {
					if (_log.isDebugEnabled()) {
						_log.debug("Unable to close suggestions file", ioe);
					}
				}
			}
		}
	}

	public void setBatchSize(int batchSize) {
		_batchSize = batchSize;
	}

	public void setDocument(Document document) {
		_document = document;
	}

	public void setMaxNGrams(int maxNGrams) {
		_maxNGrams = maxNGrams;
	}

	public void setSolrIndexWriter(SolrIndexWriter solrIndexWriter) {
		_solrIndexWriter = solrIndexWriter;
	}

	protected void addNGrams(Document document, String keywords) {
		String lowerCaseKeywords = keywords.toLowerCase();

		int maxNGrams = Math.min(_maxNGrams, lowerCaseKeywords.length());

		StringBundler nGram = new StringBundler(maxNGrams);
		String prefix = "start";

		for (int i = 0; i < maxNGrams; i++) {
			nGram.append(keywords.charAt(i));
			String field = prefix + (i + 1);
			document.addKeyword(field, nGram.toString());
		}
	}

	protected Document getSuggestionDocument(
			long companyId, long[] groupIds, Locale locale, String keywords)
		throws SearchException {

		Document document = (Document)_document.clone();

		document.addKeyword(
			Field.UID, getUID(companyId, groupIds, locale, keywords));

		document.addKeyword(Field.COMPANY_ID, companyId);
		document.addKeyword(Field.GROUP_ID, groupIds);
		document.addKeyword(Field.KEYWORD_SEARCH, keywords);
		document.addKeyword(Field.LANGUAGE_ID, locale.toString());
		document.addKeyword(Field.PORTLET_ID, PortletKeys.SEARCH);
		document.addKeyword(Field.TYPE, FILTER_TYPE_SUGGESTION);

		addNGrams(document, keywords);

		return document;
	}

	protected String getUID(
		long companyId, long[] groupIds, Locale locale, String keywords) {

		StringBundler sb = new StringBundler(groupIds.length + 4);

		sb.append(FILTER_TYPE_SUGGESTION);

		if (companyId > 0) {
			sb.append(String.valueOf(companyId));
		}

		sb.append(locale.toString());

		for (long groupId : groupIds) {
			sb.append(String.valueOf(groupId));
		}

		String lowerCaseKeywords = keywords.toLowerCase();
		sb.append(lowerCaseKeywords);

		String key = sb.toString();

		char[] encodeBuffer = new char[32];

		try {
			MessageDigest messageDigest = MessageDigest.getInstance(
				_ALGORITHM_MD5);

			CharsetEncoder charsetEncoder =
				CharsetEncoderUtil.getCharsetEncoder(StringPool.UTF8);

			messageDigest.update(charsetEncoder.encode(CharBuffer.wrap(key)));

			byte[] bytes = messageDigest.digest();

			for (int i = 0; i < bytes.length; i++) {
				int value = bytes[i] & 0xff;

				encodeBuffer[i * 2] = _HEX_CHARACTERS[value >> 4];
				encodeBuffer[i * 2 + 1] = _HEX_CHARACTERS[value & 0xf];
			}
		}
		catch (Exception e) {
			_log.error(e, e);
		}

		return new String(encodeBuffer);
	}

	private static final String _ALGORITHM_MD5 = "MD5";

	private static final int _DEFAULT_BATCH_SIZE = 1000;

	private static final int _DEFAULT_MAX_N_GRAMS = 50;

	private static final char[] _HEX_CHARACTERS = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd',
		'e', 'f'
	};

	private static Log _log = LogFactoryUtil.getLog(SuggestionIndexer.class);

	private int _batchSize = _DEFAULT_BATCH_SIZE;
	private Document _document;
	private int _maxNGrams =_DEFAULT_MAX_N_GRAMS;
	private SolrIndexWriter _solrIndexWriter;

}