package com.liferay.portal.search.solr.spell;

import com.liferay.portal.kernel.util.StringPool;

import java.io.IOException;
import java.io.StringReader;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.ngram.NGramTokenizer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.util.Version;

/**
 * Created with IntelliJ IDEA.
 * User: Liferay
 * Date: 1/28/13
 * Time: 10:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class AnalysisResult {

	public String input;

	public List<String> gram2s = new ArrayList<String>();
	public List<String> gram3s = new ArrayList<String>();
	public List<String> gram4s = new ArrayList<String>();

	public String start2 = "";
	public String start3 = "";
	public String start4 = "";

	public String end2 = "";
	public String end3 = "";
	public String end4 = "";

	public static AnalysisResult analyze(String input) throws IOException {

		AnalysisResult result = new AnalysisResult();
		result.input = input;
		TokenStream tokenStream =
			new NGramTokenizer(new StringReader(input), 2, 4);

		OffsetAttribute offsetAttribute =
			tokenStream.getAttribute(OffsetAttribute.class);
		CharTermAttribute charTermAttribute =
			tokenStream.getAttribute(CharTermAttribute.class);

		while (tokenStream.incrementToken()) {
			int startOffset = offsetAttribute.startOffset();
			int endOffset = offsetAttribute.endOffset();

			String text = charTermAttribute.toString();

			if (text.length() == 2) {
				result.gram2s.add(text);
			} else if (text.length() == 3) {
				result.gram3s.add(text);
			} else if (text.length() == 4) {
				result.gram4s.add(text);
			} else {
				continue;
			}
		}

		result.start2 = input.substring(0, Math.min(input.length(), 2));
		result.start3 = input.substring(0, Math.min(input.length(), 3));
		result.start4 = input.substring(0, Math.min(input.length(), 4));

		result.end2 =
			input.substring(Math.max(0, input.length() - 2), input.length());
		result.end3 =
			input.substring(Math.max(0, input.length() - 3), input.length());
		result.end4 =
			input.substring(Math.max(0, input.length() - 4), input.length());

		return result;
	}

	public static List<String> tokenizer (String keyword) {

		List<String> result = new ArrayList<String>();

		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
		try {
			TokenStream tokenStream = analyzer.tokenStream(null, new StringReader(keyword));
			CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);

			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				result.add(charTermAttribute.toString());
			}
			tokenStream.end();
			tokenStream.close();

	    } catch (Exception e){
			e.printStackTrace();
		}

		return result;
	}

}
