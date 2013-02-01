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
import com.liferay.portal.search.solr.spell.DefaultAnalizer;
import com.liferay.portal.search.solr.spell.ScopedIndexReader;

import java.util.List;
import java.util.Map;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

/**
 * @author Michael C. Han
 */
public class SolrQuerySuggesterImpl implements QuerySuggester {

	public String spellCheckKeywords(SearchContext searchContext)
		throws SearchException {

		String collated = StringPool.BLANK;

		Map<String, List<String>> map = spellCheckKeywords(searchContext, 1);

		String keywords = searchContext.getKeywords();
		List<String> tokens = DefaultAnalizer.tokenize(keywords);

		for (String token : tokens) {
			if (!map.get(token).isEmpty()) {

				String suggestion = map.get(token).get(0);

				if (Character.isUpperCase(token.charAt(0))) {
					suggestion = suggestion.substring(0, 1).toUpperCase()
						.concat(suggestion.substring(1));
				}

				collated = collated.concat(suggestion)
					.concat(StringPool.SPACE);

			}
			else {
				collated =	collated.concat(token).concat(StringPool.SPACE);
			}
		}

		if (!collated.equals(StringPool.BLANK)) {
			collated = collated.substring(0,collated.length()-1);
		}

		return collated;

	}

	public Map<String, List<String>> spellCheckKeywords(
			SearchContext searchContext, int max)
		throws SearchException {

		Map<String, List<String>> suggestions;

		try {
			suggestions =
				_scopedIndexReader.suggestSimilar(searchContext,max);
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to execute spellchecking", e);
			}

			throw new SearchException(e.getMessage());
		}

		return suggestions;

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

	public void setScopedIndexReader(ScopedIndexReader scopedIndexReader) {
		this._scopedIndexReader = scopedIndexReader;
	}

	public void setSolrServer(SolrServer solrServer) {
		_solrServer = solrServer;
	}

	private static Log _log =
		LogFactoryUtil.getLog(SolrQuerySuggesterImpl.class);

	private SolrServer _solrServer;
	private String _suggesterURL = "/select";
	private ScopedIndexReader _scopedIndexReader;

}