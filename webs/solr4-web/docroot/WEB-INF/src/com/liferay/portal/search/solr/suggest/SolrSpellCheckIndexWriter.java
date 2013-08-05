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

import com.liferay.portal.kernel.configuration.Filter;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.search.SpellCheckIndexWriter;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StreamUtil;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.model.Group;
import com.liferay.portal.search.solr.suggest.util.SuggestPropsKeys;
import com.liferay.portal.search.solr.suggest.util.SuggestPropsValues;
import com.liferay.portal.service.GroupLocalServiceUtil;
import com.liferay.portal.util.PortletKeys;
import com.liferay.util.portlet.PortletProps;

import java.io.InputStream;

import java.net.URL;

import org.apache.solr.client.solrj.SolrServer;

/**
 * @author Daniela Zapata Riesco
 * @author David Gonzalez
 * @author Michael C. Han
 */
public class SolrSpellCheckIndexWriter implements SpellCheckIndexWriter {

	public static final long _GLOBAL_GROUP_ID = 0L;

	@Override
	public void clearDictionaryIndexes(SearchContext searchContext)
		throws SearchException {

		StringBundler sb = new StringBundler(9);

		sb.append(StringPool.PLUS);
		sb.append(Field.COMPANY_ID);
		sb.append(StringPool.COLON);
		sb.append(searchContext.getCompanyId());
		sb.append(StringPool.SPACE);
		sb.append(StringPool.PLUS);
		sb.append(Field.PORTLET_ID);
		sb.append(StringPool.COLON);
		sb.append(PortletKeys.SEARCH);

		try {
			_solrServer.deleteByQuery(sb.toString());

			if (_commit) {
				_solrServer.commit();
			}
		}
		catch (Exception e) {
			_log.error(e, e);

			throw new SearchException(e.getMessage());
		}
	}

	@Override
	public void indexDictionaries(SearchContext searchContext)
		throws SearchException {

		for (String languageId :
				SuggestPropsValues.SOLR_SPELL_CHECKER_SUPPORTED_LOCALES) {

			try {
				if (_log.isInfoEnabled()) {
					_log.info("Start indexing dictionaries for " + languageId);
				}

				indexDefaultDictionary(languageId, searchContext);
				indexCustomDictionary(languageId, searchContext);

				if (_log.isInfoEnabled()) {
					_log.info("Finished indexing dictionaries for " +
						languageId);
				}
			}
			catch (Exception e) {
				throw new SearchException(e);
			}
		}
	}

	@Override
	public void indexDictionary(SearchContext searchContext)
		throws SearchException {
			indexDictionaries(searchContext);
	}

	public void setCommit(boolean commit) {
		_commit = commit;
	}

	public void setDictionaryIndexer(DictionaryIndexer dictionaryIndexer) {
		_dictionaryIndexer = dictionaryIndexer;
	}

	public void setSolrServer(SolrServer solrServer) {
		_solrServer = solrServer;
	}

	protected URL getResource(String name) {
		Class<?> clazz = getClass();

		ClassLoader classLoader = clazz.getClassLoader();

		return classLoader.getResource(name);
	}

	protected void indexCustomDictionary(
			String languageId, SearchContext searchContext)
		throws Exception {

		if (_log.isInfoEnabled()) {
			_log.info("Start indexing custom dictionaries");
		}

		String[] supportedGroupIds =
			SuggestPropsValues.SOLR_SPELL_CHECKER_GROUPS;

		for (String supportedGroupId : supportedGroupIds) {
			Filter languageGroupFilter = new Filter(
				languageId, supportedGroupId);

			String dictionaryFileName = PortletProps.get(
				SuggestPropsKeys.SOLR_SPELL_CHECKER_CUSTOM_DICTIONARY,
				languageGroupFilter);

			long resolvedGroupId = _resolveGroupId(
				supportedGroupId, searchContext.getCompanyId());

			if (_log.isInfoEnabled()) {
				_log.info("Indexing dictionary " + dictionaryFileName);
			}

			InputStream inputStream = null;

			try {
				URL url = getResource(dictionaryFileName);

				inputStream = url.openStream();

				if (inputStream == null) {
					if (_log.isWarnEnabled()) {
						_log.warn("Unable to read " + dictionaryFileName);
					}

					continue;
				}

				_dictionaryIndexer.indexDictionary(
					searchContext, new long[]{resolvedGroupId},
					LocaleUtil.fromLanguageId(languageId), inputStream);
			}
			finally {
				StreamUtil.cleanUp(inputStream);
			}
		}
	}

	protected void indexDefaultDictionary(
			String languageId, SearchContext searchContext)
		throws Exception {

		if (_log.isInfoEnabled()) {
			_log.info("Start indexing dictionaries for " + languageId);
		}

		String[] dictionaryFileNames =
			PortletProps.getArray(
				SuggestPropsKeys.SOLR_SPELL_CHECKER_DICTIONARY,
				new Filter(languageId));

		for (String dictionaryFileName : dictionaryFileNames) {
			if (_log.isInfoEnabled()) {
				_log.info("Indexing dictionary " + dictionaryFileName);
			}

			InputStream inputStream = null;

			try {
				URL url = getResource(dictionaryFileName);

				inputStream = url.openStream();

				if (inputStream == null) {
					if (_log.isWarnEnabled()) {
						_log.warn("Unable to read " + dictionaryFileName);
					}

					continue;
				}

				_dictionaryIndexer.indexDictionary(
					searchContext, new long[]{0},
					LocaleUtil.fromLanguageId(languageId), inputStream);
			}
			finally {
				StreamUtil.cleanUp(inputStream);
			}
		}
	}

	private long _resolveGroupId(String uuid, long companyId) {

		long resolvedGroupId = _INVALID_GROUP_ID;

		if (uuid.equals(String.valueOf( _GLOBAL_GROUP_ID))) {
			resolvedGroupId = _GLOBAL_GROUP_ID;

			if (_log.isInfoEnabled()) {
				_log.info("Global group with groupId= "
					+_GLOBAL_GROUP_ID + " will be used");
			}
		}

		try {
			Group resolvedGroup =
				GroupLocalServiceUtil.fetchGroupByUuidAndCompanyId(
					uuid, companyId);

			if (resolvedGroup != null) {
				resolvedGroupId = resolvedGroup.getGroupId();
			}
		}
		catch (SystemException e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to resolve group with uuid = " + uuid);

				return _INVALID_GROUP_ID;
			}
		}

		return resolvedGroupId;
	}

	private static final long _INVALID_GROUP_ID = -1L;

	private static Log _log = LogFactoryUtil.getLog(
		SolrSpellCheckIndexWriter.class);

	private boolean _commit;
	private DictionaryIndexer _dictionaryIndexer;
	private SolrServer _solrServer;

}