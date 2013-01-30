/**
 * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
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

package com.liferay.portal.search.solr;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.QuerySuggester;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.StringBundler;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.search.solr.spell.ScopedIndexReader;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.SpellCheckResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * @author Michael C. Han
 */
public class SolrQuerySuggesterImpl implements QuerySuggester {

	public void setSolrServer(SolrServer solrServer) {
		_solrServer = solrServer;
	}

	public String spellCheckKeywords(SearchContext searchContext)
		throws SearchException {

		String luceneResult = null;
		String collated = "";

		Map<String, List<String>> map = spellCheckKeywords(searchContext, 10);
		for (String token : map.keySet()){
			collated =
				collated.concat(map.get(token).get(0))
					.concat(StringPool.SPACE);
		}
		if (!collated.equals("")){
			luceneResult = collated.substring(0,collated.length()-1);
		}

		return luceneResult;

	}

	public Map<String, List<String>> spellCheckKeywords(
			SearchContext searchContext, int max)
		throws SearchException {

		Map<String, List<String>> luceneResult = null;

		try {
			luceneResult =
				_scopedIndexReader.suggestSimilar(searchContext,max);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return luceneResult;

	}

	public String[] suggestKeywordQueries(SearchContext searchContext, int max)
		throws SearchException {

		SolrQuery solrQuery = new SolrQuery();

		StringBundler sb = new StringBundler(5);

		sb.append(Field.KEYWORD_SEARCH);
		sb.append(StringPool.COLON);
		sb.append(StringPool.QUOTE);
		sb.append(searchContext.getKeywords());
		sb.append(StringPool.QUOTE);

		solrQuery.setRequestHandler(_suggesterURL);
		solrQuery.setQuery(sb.toString());
		solrQuery.setRows(max);

		String companyIdFilterQuery = Field.COMPANY_ID.concat(
			StringPool.COLON).concat(
			Long.toString(searchContext.getCompanyId()));

		solrQuery.setFilterQueries(companyIdFilterQuery);

		try {
			QueryResponse queryResponse = _solrServer.query(solrQuery);

			SolrDocumentList solrDocumentList = queryResponse.getResults();

			int numDocuments = solrDocumentList.size();

			String[] results = new String[numDocuments];

			for (int i = 0; i < numDocuments; i++) {
				SolrDocument solrDocument = solrDocumentList.get(i);

				results[i] = (String)solrDocument.getFieldValue(
					Field.KEYWORD_SEARCH);
			}

			return results;
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to execute Solr query", e);
			}

			throw new SearchException(e.getMessage());
		}
	}

	protected SolrQuery createSpellCheckQuery(
		SearchContext searchContext,
		Map<String, String> additionalQueryParameters) {

		SolrQuery solrQuery = new SolrQuery();

		Locale locale = searchContext.getLocale();

		String requestHandler = _spellCheckURLPrefix.concat(
			StringPool.UNDERLINE).concat(locale.toString());

		solrQuery.setRequestHandler(requestHandler);
		solrQuery.setParam("spellcheck.q", searchContext.getKeywords());

		for (Map.Entry<String, String> additionalQueryParameter :
				additionalQueryParameters.entrySet()) {

			solrQuery.setParam(
				additionalQueryParameter.getKey(),
				additionalQueryParameter.getValue());
		}

		return solrQuery;
	}

	public void setScopedIndexReader(ScopedIndexReader scopedIndexReader) {
		this._scopedIndexReader = scopedIndexReader;
	}

	private static Log _log =
		LogFactoryUtil.getLog(SolrQuerySuggesterImpl.class);

	private SolrServer _solrServer;
	private String _spellCheckURLPrefix = "/liferay_spellCheck";
	private String _suggesterURL = "/select";
	private ScopedIndexReader _scopedIndexReader;

}