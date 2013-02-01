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

package com.liferay.portal.search.solr.spell;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.spell.StringDistance;


/**
 * @author Daniela Zapata
 * @author David Mendez
 */
public class ScopedIndexReader {

	public Map<String, List<String>> suggestSimilar (
			SearchContext searchContext, int maxSuggestions)
		throws IOException, SearchException {

		Map<String,List<String>> suggestions =
			new HashMap<String,List<String>>();

		Locale locale = searchContext.getLocale();

		List<String> originals =
			DefaultAnalizer.tokenize(searchContext.getKeywords());

		for (String original : originals){
			
			String token = original.toLowerCase();

			List<String> similarTokens = suggestTokenSimilars(
				original, token, locale, maxSuggestions);

			suggestions.put(original, similarTokens);
		}

		return suggestions;

	}

	public List<String> suggestTokenSimilars (
			String original, String token, Locale locale, int maxSuggestions)
		throws IOException, SearchException {

		DefaultAnalizer result = DefaultAnalizer.analyze(token);

		BooleanQuery query = new BooleanQuery();

		addGramQuery(query, "gram3", result.gram2s);
		addGramQuery(query, "gram3", result.gram3s);
		addGramQuery(query, "gram4", result.gram4s);

		addEdgeQuery(query, "start2", result.start2);
		addEdgeQuery(query, "start3", result.start3);
		addEdgeQuery(query, "start4", result.start4);

		addEdgeQuery(query, "end2", result.end2);
		addEdgeQuery(query, "end3", result.end3);
		addEdgeQuery(query, "end4", result.end4);

		TermQuery termQuery = new TermQuery(new Term("word", result.input));
		query.add(termQuery, BooleanClause.Occur.SHOULD);

		Map<String, Float> words = new HashMap<String, Float>();

		searchTokenSimilars(token, original, query, words, locale);

		ValueComparator bvc =  new ValueComparator(words);
		TreeMap<String, Float> sortedWords = new TreeMap<String, Float>(bvc);

		sortedWords.putAll(words);

		List<String> listWords = new ArrayList(sortedWords.keySet());

		return listWords.subList(0, Math.min(maxSuggestions, listWords.size()));

	}

	public void searchTokenSimilars(String token, String original,
	        BooleanQuery query,	Map<String, Float> words, Locale locale)
		throws SearchException {

		TermsFilter localeFilter = new TermsFilter();
		Term localeTerm = new Term("locale", locale.toString());

		localeFilter.addTerm(localeTerm);
		FilteredQuery localeFilteredQuery =
			new FilteredQuery(query, localeFilter);

		TopDocs topDocs = 
			_spellCheckerServer.getTopDocs(localeFilteredQuery, 50);

		Map<String, Float> tokenSuggestions = new HashMap<String, Float>();

		boolean foundWord = false;

		for (int i = 0; i < Math.min(topDocs.totalHits, 50); i++) {

			ScoreDoc scoreDoc = topDocs.scoreDocs[i];

			Document doc = _spellCheckerServer.getDocument(scoreDoc.doc);

			String suggestion = doc.get("word");                 
			float weight = Float.parseFloat(doc.get("weight"));

			if (suggestion.equalsIgnoreCase(token)) {
				words.put(original, weight);
				foundWord = true;
				break;
			}

			float distance =
				_stringDistance.getDistance(token, suggestion);

			if (distance > threshold) {
				Float normalizedWeight = weight + distance;
				tokenSuggestions.put(suggestion, normalizedWeight);
			}
		}

		if(!foundWord){
			if(tokenSuggestions.isEmpty()){
				words.put(original, 0f);
			}
		    else {
				words.putAll(tokenSuggestions);
			}
		}

	}

	public void setStringDistance(StringDistance stringDistance) {
		_stringDistance = stringDistance;
	}

	public void setSpellCheckerServer(
		SpellCheckerServer spellCheckerServer) {
		_spellCheckerServer = spellCheckerServer;
	}

	private void addGramQuery(
		BooleanQuery query, String fieldName, List<String> grams) {
		for (String gram : grams) {
			query.add(new TermQuery(new Term(fieldName, gram)),
				BooleanClause.Occur.SHOULD);
		}
	}

	private void addEdgeQuery(
		BooleanQuery query, String fieldName, String fieldValue) {
		TermQuery start3Query = new TermQuery(new Term(fieldName, fieldValue));
		if (boostEdges) {
			start3Query.setBoost(2.0F);
		}
		query.add(start3Query, BooleanClause.Occur.SHOULD);
	}

	private class ValueComparator implements Comparator<String> {

		Map<String, Float> base;
		public ValueComparator(Map<String, Float> base) {
			this.base = base;
		}

		public int compare(String a, String b) {
			if (base.get(a) >= base.get(b)) {
				return -1;
			} else {
				return 1;
			}
		}
	}

	private float threshold = 0.8f;
	private boolean boostEdges = false;
	private StringDistance _stringDistance;
	private SpellCheckerServer _spellCheckerServer;

}
