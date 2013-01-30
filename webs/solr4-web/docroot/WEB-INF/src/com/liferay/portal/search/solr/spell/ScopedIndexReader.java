package com.liferay.portal.search.solr.spell;

import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.LocaleUtil;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queries.ChainedFilter;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.queries.TermsFilter;
import org.apache.lucene.search.TopDocs;

/**
 * Created with IntelliJ IDEA.
 * User: Liferay
 * Date: 1/28/13
 * Time: 10:33 AM
 * To change this template use File | Settings | File Templates.
 */
public class ScopedIndexReader {

	public List<String> suggestTokenSimilars(
			String token,
			Locale locale, long companyId, long[] groupIds, int maxSuggestions)
		throws IOException {

		AnalysisResult result = AnalysisResult.analyze(token);

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

		Set<String> words = new HashSet<String>();

		for (long groupId : groupIds){
			searchTokenSimilars(
				token, query, words,
				locale, companyId, groupId, maxSuggestions);
		}

		searchTokenSimilars(token, query, words, locale, 0, 0, maxSuggestions);

		List<String> wordlist = new ArrayList<String>();
		wordlist.addAll(words);
		return wordlist.subList(0, Math.min(maxSuggestions, wordlist.size()));

	}

	public void searchTokenSimilars(
		String token, BooleanQuery query, Set<String> words, Locale locale,
		long companyId, long groupId, int maxSuggestions) {

		TermsFilter companyIdFilter = new TermsFilter();
		Term companyIdTerm =
			new Term("companyId", String.valueOf(companyId));
		companyIdFilter.addTerm(companyIdTerm);
		FilteredQuery companyIdFilteredQuery =
			new FilteredQuery(query, companyIdFilter);

		TermsFilter groupIdFilter = new TermsFilter();
		Term localeTerm = new Term("locale", locale.toString());
		groupIdFilter.addTerm(localeTerm);
		FilteredQuery groupIdFilteredQuery =
			new FilteredQuery(companyIdFilteredQuery, groupIdFilter);

		TermsFilter localeFilter = new TermsFilter();
		Term groupIdTerm = new Term("groupId", String.valueOf(groupId));
		localeFilter.addTerm(groupIdTerm);
		FilteredQuery localeFilteredQuery =
			new FilteredQuery(groupIdFilteredQuery, localeFilter);

		TopDocs topDocs = _spellCheckerServer
			.getTopDocs(localeFilteredQuery, maxSuggestions);

		for (int i = 0; i < Math.min(topDocs.totalHits, maxSuggestions); i++) {
			ScoreDoc scoreDoc = topDocs.scoreDocs[i];
			Document doc = _spellCheckerServer.getDocument(scoreDoc.doc);
			String suggestion = doc.get("word");

			if (suggestion.equalsIgnoreCase(token)) {
				continue;
			}
			if (StringUtils.getLevenshteinDistance(token, suggestion) <
				editDistanceCutoff) {
				words.add(suggestion);
			}
		}
	}

	public Map<String, List<String>> suggestSimilar (
			SearchContext searchContext, int maxSuggestions)
		throws IOException {

		Map<String, List<String>> suggestions =
			new LinkedHashMap<String, List<String>>();

		Locale locale = searchContext.getLocale();
		long companyId = searchContext.getCompanyId();
		long[] groupIds = searchContext.getGroupIds();

		List<String> tokens =
			AnalysisResult.tokenizer(searchContext.getKeywords());

	    for (String token : tokens){
		    List<String> suggestionsToken = suggestTokenSimilars(
			    token, locale, companyId, groupIds, maxSuggestions);

		    suggestions.put(token, suggestionsToken);
		}

		return suggestions;

	}

	private void addGramQuery(
		BooleanQuery query, String fieldName, List<String> grams) {
		for (String gram : grams) {
			query.add(new TermQuery(new Term(fieldName, gram)), BooleanClause.Occur.SHOULD);
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

	public void setSpellCheckerServer(SpellCheckerServer spellCheckerServer) {
		this._spellCheckerServer = spellCheckerServer;
	}

	private int editDistanceCutoff = 3;
	private boolean boostEdges = false;
	private SpellCheckerServer _spellCheckerServer;

}
