package com.liferay.portal.search.solr;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.ngram.NGramTokenizer;

import java.io.Reader;

public class NGramAnalyzer extends Analyzer {

	int minNGram;
	int maxNGram;

	public NGramAnalyzer(int minNGram, int maxNGram) {

		this.minNGram = minNGram;
		this.maxNGram = maxNGram;
	}

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		//return new NGramTokenizer(reader,3,3);
		return new NGramTokenizer(reader, minNGram, maxNGram);

	}
}