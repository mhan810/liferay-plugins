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

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.search.SearchException;
import com.liferay.portal.kernel.util.StringPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import com.liferay.util.portlet.PortletProps;

/**
 * @author Daniela Zapata
 * @author David Mendez
 */
public class ScopedIndexWriter {

	public void deleteDocuments() throws SearchException {
		_spellCheckerServer.deleteDocuments();
	}

	public void indexDictionary(SearchContext searchContext)
		throws SearchException{

		Locale locale = searchContext.getLocale();

		String propsKeyExternal = locale.toString().concat(EXTERNAL_DICTIONARY);
		String dirExternal = PortletProps.get(propsKeyExternal);
		File fileExternal = new File(dirExternal);
		doIndexDictionary(fileExternal, locale);

		String propsKeyCustom = locale.toString().concat(CUSTOM_DICTIONARY);
		String dirCustom = PortletProps.get(propsKeyCustom);
		File fileCustom = new File(dirCustom);
		doIndexDictionary(fileCustom, locale);

	}

	public void doIndexDictionary(File file, Locale locale)
		throws SearchException {

		Set<Document> docs = new HashSet<Document>();

		try{

			BufferedReader in = new BufferedReader(new FileReader(file));
			String line;
			int i = 0;
			while((line = in.readLine()) != null){
				i++;
				String[] term = line.split(StringPool.SPACE);
				int weight = 0;
				if (term.length > 1){
					weight = Integer.parseInt(term[1]);
				}
				addDocument(docs, term[0], locale, weight);
				if(i == BATCH_NUM){
					_spellCheckerServer.addDocuments(docs);
					docs.clear();
					i = 0;
				}
			}
			_spellCheckerServer.addDocuments(docs);
			in.close();
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to execute Solr query", e);
			}

			throw new SearchException(e.getMessage());
		}

	}

	protected static Document addDocument(
		Set<Document> docs,String token, Locale locale, int weight) {

		int length = token.length();

		Document doc = new Document();

		Field wordField = new StringField("word", token, Field.Store.YES);
		Field weightField =	new StringField(
			"weight", String.valueOf(weight), Field.Store.YES);

		if (locale != null){
			Field localeField =	new StringField(
				"locale", locale.toString(), Field.Store.YES);

			doc.add(localeField);
		}

		doc.add(wordField);
		doc.add(weightField);

		addGram(token, doc, getMin(length), getMax(length));

		docs.add(doc);

		return doc;
	}

	private static void addGram(
		String text, Document doc, int ng1, int ng2) {

		int len = text.length();

		for (int ng = ng1; ng <= ng2; ng++) {

			String key = "gram" + ng;
			String end = null;

			for (int i = 0; i < len - ng + 1; i++) {

				String gram = text.substring(i, i + ng);
				Field ngramField = new StringField(key, gram, Field.Store.NO) ;

				doc.add(ngramField);
				if (i == 0) {

					Field startField =
						new StringField("start" + ng, gram, Field.Store.NO);
					doc.add(startField);
				}
				end = gram;
			}

			if (end != null) {
				Field endField =
					new StringField("end" + ng, end, Field.Store.NO);
				doc.add(endField);
			}
		}
	}

	private static int getMin(int l) {
		if (l > 5) {
			return 3;
		}
		else {
			return 2;
		}
	}

	private static int getMax(int l) {
		if (l > 5) {
			return 4;
		}
		else {
			return 3;
		}
	}

	public void setSpellCheckerServer(SpellCheckerServer spellCheckerServer) {
		_spellCheckerServer = spellCheckerServer;
	}

	private static Log _log =
		LogFactoryUtil.getLog(ScopedIndexWriter.class);

	private SpellCheckerServer _spellCheckerServer;

	private static final int BATCH_NUM = 1000;
	private static final String EXTERNAL_DICTIONARY =
		".external.dictionary.directory";
	private static final String CUSTOM_DICTIONARY =
		".custom.dictionary.directory";
}
