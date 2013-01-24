package com.liferay.portal.search.solr;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.Soundex;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttributeImpl;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Index;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopDocsCollector;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import com.liferay.portal.kernel.search.Hits;

public class ScopedSpellChecker {

	private String spellIndexDir;
	private boolean boostEdges;
	private int editDistanceCutoff = 3;
	private int soundexDiffCutoff = 4;

	private IndexReader spellIndexReader;
	private IndexWriter spellIndexWriter;
	private IndexSearcher spellIndexSearcher;

	//private FieldSelector = new

	private class AnalysisResult {
		public String input;
		public List<String> gram3s = new ArrayList<String>();
		public List<String> gram4s = new ArrayList<String>();
		public String start3 = "";
		public String start4 = "";
		public String end3 = "";
		public String end4 = "";
	}

	public void setBoostEdges(boolean boostEdges) {
		this.boostEdges = boostEdges;
	}

	public void setEditDistanceCutoff(int editDistanceCutoff) {
		this.editDistanceCutoff = editDistanceCutoff;
	}

	public void setSoundexDiffCutoff(int soundexDiffCutoff) {
		this.soundexDiffCutoff = soundexDiffCutoff;
	}

	public void setSpellIndexDir(String spellIndexDir) {
		this.spellIndexDir = spellIndexDir;
	}

	public void init() throws Exception {
		File spellIndexDirFile = new File(spellIndexDir);

		Directory directory = FSDirectory.open(spellIndexDirFile);

		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
			Version.LUCENE_35, new StandardAnalyzer(Version.LUCENE_35));

		this.spellIndexWriter = new IndexWriter(directory, indexWriterConfig);

		this.spellIndexReader = IndexReader.open(spellIndexWriter, false);

		this.spellIndexSearcher = new IndexSearcher(spellIndexReader);
	}

	public void destroy() throws Exception {
		spellIndexWriter.close();
		spellIndexSearcher.close();
	}

	public void flush() throws Exception {
		spellIndexWriter.commit();
	}

	public void addToDictionary(String dictionaryEntry) throws IOException {
		AnalysisResult result = analyze(dictionaryEntry);
		Document doc = new Document();
		doc.add(new Field("word", result.input, Store.YES, Index.ANALYZED));
		for (String gram3 : result.gram3s) {
			doc.add(new Field("gram3", gram3, Store.YES, Index.ANALYZED));
		}
		for (String gram4 : result.gram4s) {
			doc.add(new Field("gram4", gram4, Store.YES, Index.ANALYZED));
		}
		doc.add(new Field("start3", result.start3, Store.YES, Index.ANALYZED));
		doc.add(new Field("start4", result.start4, Store.YES, Index.ANALYZED));
		doc.add(new Field("end3", result.end3, Store.YES, Index.ANALYZED));
		doc.add(new Field("end4", result.end4, Store.YES,Index.ANALYZED));
		spellIndexWriter.addDocument(doc);
	}

	public List<String> suggestSimilar(String input, int maxSuggestions)
		throws IOException, EncoderException {
		AnalysisResult result = analyze(input);
		BooleanQuery query = new BooleanQuery();
		addGramQuery(query, "gram3", result.gram3s);
		addGramQuery(query, "gram4", result.gram4s);
		addEdgeQuery(query, "start3", result.start3);
		addEdgeQuery(query, "end3", result.end3);
		addEdgeQuery(query, "start4", result.start4);
		addEdgeQuery(query, "end4", result.end4);
		Set<String> words = new HashSet<String>();

		TopDocs topDocs = spellIndexSearcher.search(query, 5);
		int numHits = topDocs.totalHits;
		for (int i = 0; i < numHits; i++) {
			ScoreDoc scoreDoc = topDocs.scoreDocs[i];
			String suggestion = spellIndexReader
				.document(scoreDoc.doc).get("word");
			// if the suggestion is the same as the input, ignore it
			if (suggestion.equalsIgnoreCase(input)) {
				continue;
			}
			// if the suggestion is within the specified levenshtein's distance, include it
			if (StringUtils.getLevenshteinDistance(input, suggestion) < editDistanceCutoff) {
				words.add(suggestion);
			}
			// if they sound the same, include it
			Soundex soundex = new Soundex();
			if (soundex.difference(input, suggestion) >= soundexDiffCutoff) {
				words.add(suggestion);
			}
		}
		List<String> wordlist = new ArrayList<String>();
		wordlist.addAll(words);
		return wordlist.subList(0, Math.min(maxSuggestions, wordlist.size()));
	}

	private AnalysisResult analyze(String input) throws IOException {
		AnalysisResult result = new AnalysisResult();
		result.input = input;
		TokenStream tokenStream =
			new NGramTokenizer(new StringReader(input), 3, 4);

		OffsetAttribute offsetAttribute =
			tokenStream.getAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute =
			tokenStream.getAttribute(CharTermAttribute.class);

		while (tokenStream.incrementToken()) {
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();

			String text = charTermAttribute.toString();

			if (text.length() == 3) {
				result.gram3s.add(text);
			} else if (text.length() == 4) {
				result.gram4s.add(text);
			} else {
				continue;
			}
		}
		result.start3 = input.substring(0, Math.min(input.length(), 3));
		result.start4 = input.substring(0, Math.min(input.length(), 4));
		result.end3 = input.substring(Math.max(0, input.length() - 3), input.length());
		result.end4 = input.substring(Math.max(0, input.length() - 4), input.length());
		return result;
	}

	private void addGramQuery(BooleanQuery query, String fieldName, List<String> grams) {
		for (String gram : grams) {
			query.add(new TermQuery(new Term(fieldName, gram)), Occur.SHOULD);
		}
	}

	private void addEdgeQuery(BooleanQuery query, String fieldName, String fieldValue) {
		TermQuery start3Query = new TermQuery(new Term(fieldName, fieldValue));
		if (boostEdges) {
			start3Query.setBoost(2.0F);
		}
		query.add(start3Query, Occur.SHOULD);
	}
}