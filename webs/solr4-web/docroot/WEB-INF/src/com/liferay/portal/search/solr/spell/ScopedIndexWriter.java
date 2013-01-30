package com.liferay.portal.search.solr.spell;


import com.liferay.portal.kernel.search.Field;
import com.liferay.portal.kernel.search.SearchContext;
import com.liferay.portal.kernel.util.LocaleUtil;
import com.liferay.portal.kernel.util.StringPool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;

/**
 * Created with IntelliJ IDEA.
 * User: Liferay
 * Date: 1/24/13
 * Time: 4:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class ScopedIndexWriter {

	public void addDocument (
		SearchContext searchContext,
		com.liferay.portal.kernel.search.Document document){

		Set<Document> docs =
			new HashSet<Document>();

		extractDocuments(docs, searchContext, document);

		_spellCheckerServer.addDocuments(docs);

	}

	public void addDocuments(
		SearchContext searchContext,
	    Collection<com.liferay.portal.kernel.search.Document> documents) {

		Set<Document> docs =
			new HashSet<Document>();

		Iterator iterator = documents.iterator();

		while(iterator.hasNext()){
			com.liferay.portal.kernel.search.Document document =
				(com.liferay.portal.kernel.search.Document)iterator.next();
			extractDocuments(docs, searchContext, document);
		}

		_spellCheckerServer.addDocuments(docs);
	}

	public void addLuceneDocuments(
		SearchContext searchContext,
		Collection<Document> documents) {

		Set<Document> docs =
			new HashSet<Document>();

		Iterator iterator = documents.iterator();

		while(iterator.hasNext()){
			Document document =	(Document)iterator.next();
			extractLuceneDocuments(docs, searchContext, document);
		}

		_spellCheckerServer.addDocuments(docs);
	}

	public void extractDocuments(Set<Document> docs,
		SearchContext searchContext,
		com.liferay.portal.kernel.search.Document document) {

		Map<String,Field> fields = document.getFields();
		Set<String> fieldNames = fields.keySet();

		long companyId = searchContext.getCompanyId();
		long[] groupIds = searchContext.getGroupIds();

		for(String fieldName : fieldNames){

			for (String spellField: _spellFields){

				if(fieldName.startsWith(spellField)){
					Field field = document.getField(fieldName);

					if (field.isLocalized()){

						Map<Locale, String> map = field.getLocalizedValues();
						Set<Locale> locales = map.keySet();

						for (Locale locale : locales){
							String word = map.get(locale);
							if (!word.isEmpty()){
								extractDocumentsGroups(
									docs, groupIds, word, locale, companyId);
							}
						}
					}
					else{

						Locale locale = getLocaleFromFieldName(fieldName);
						if(locale != null) {
							String word = document.get(fieldName);
							extractDocumentsGroups(
								docs, groupIds, word, locale, companyId);
						}
					}
				}
			}
		}
	}

	public void extractLuceneDocuments(Set<Document> docs,
	                             SearchContext searchContext,
	                             Document document) {

		List<IndexableField> fields = document.getFields();

		long companyId = searchContext.getCompanyId();
		long[] groupIds = searchContext.getGroupIds();

		for(IndexableField indexableField : fields){

			String fieldName = indexableField.name();
			for (String spellField: _spellFields){

				if(fieldName.startsWith(spellField)){

					Locale locale = getLocaleFromFieldName(fieldName);
					if(locale != null) {
						String word = document.get(fieldName);
						extractDocumentsGroups(
							docs, groupIds, word, locale, companyId);
					}
				}
			}
		}
	}

	public void extractDocumentsGroups(
		Set<Document> docs,
		long[] groupIds, String word, Locale locale, long companyId) {

		if(groupIds == null){
			addDocumentForIndex(docs, word, locale, companyId, 0);
		}
		else{
			for (long groupId : groupIds){
				addDocumentForIndex(docs, word, locale, companyId, groupId);
			}
		}
	}

	public void addDocumentForIndex(
		Set<Document> docs,
		String dictionaryEntry,
		Locale locale, long companyId, long groupId) {

		List<String> tokens = AnalysisResult.tokenizer(dictionaryEntry);

		for (String token : tokens){

			int length = token.length();

			Document doc = createDocument(
				companyId,
				groupId,
				locale,
				token,
				getMin(length),
				getMax(length));

			docs.add(doc);
		}
	}

	public void indexDictionary (SearchContext searchContext) {

		deleteDocuments(searchContext.getCompanyId());
		deleteDocuments(0);

		Set<Document> docs =
			_spellCheckerServer.getDocuments(searchContext.getCompanyId());

		addLuceneDocuments(searchContext, docs);

		indexExternalDictionary(searchContext.getLocale());
	}

	public void deleteDocuments (long companyId){

		String strCompanyId = String.valueOf(companyId);
		Term term = new Term("companyId", strCompanyId);
		_spellCheckerServer.deleteDocuments(term);

	}

    public Locale getLocaleFromFieldName(String field){
		Locale locale = null;
		String[] split = field.split(StringPool.UNDERLINE, 2);
		if(split.length == 1){
			return locale;
		}
		else if(split.length > 1){
			locale = LocaleUtil.fromLanguageId(split[1]);
			return locale;
		}
		else {
			return locale;
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

	public void setSpellFields(Set<String> spellFields) {
		this._spellFields = spellFields;
	}
	protected static Document createDocument(
		long companyId,
		long groupId, Locale locale, String text, int ng1, int ng2) {

		Document doc = new Document();

		org.apache.lucene.document.Field word = new StringField("word", text, org.apache.lucene.document.Field.Store.YES);
		org.apache.lucene.document.Field companyIdField =
			new StringField("companyId", String.valueOf(companyId), org.apache.lucene.document.Field.Store.YES);
		org.apache.lucene.document.Field groupIdField =
			new StringField("groupId", String.valueOf(groupId), org.apache.lucene.document.Field.Store.YES);

		if (locale != null){
			org.apache.lucene.document.Field localeField =
				new StringField("locale", locale.toString(), org.apache.lucene.document.Field.Store.YES);
			doc.add(localeField);
		}
		doc.add(word); // orig term
		doc.add(companyIdField);
		doc.add(groupIdField);


		addGram(text, doc, ng1, ng2);

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
				org.apache.lucene.document.Field ngramField = new StringField(key, gram, org.apache.lucene.document.Field.Store.NO) ;

				// spellchecker does not use positional queries, but we want freqs
				// for scoring these multivalued n-gram fields.
				doc.add(ngramField);
				if (i == 0) {
					// only one term possible in the startXXField, TF/pos and norms aren't
					// needed.
					org.apache.lucene.document.Field startField = new StringField("start" + ng, gram, org.apache.lucene.document.Field.Store.NO);
					doc.add(startField);
				}
				end = gram;
			}
			if (end != null) { // may not be present if len==ng1
				// only one term possible in the endXXField, TF/pos and norms aren't needed.
				org.apache.lucene.document.Field endField = new StringField("end" + ng, end, org.apache.lucene.document.Field.Store.NO);
				doc.add(endField);
			}
		}
	}

	private void indexExternalDictionary(Locale locale){

		String dir = _supportedLocalesDirs.get(locale.toString());
		Set<Document> docs = new HashSet<Document>();

		File file = new File(dir);
		try{
			BufferedReader in = new BufferedReader(new FileReader(file));
			String line = null;
			int i = 0;
			while((line = in.readLine()) != null){
				i++;
				addDocumentForIndex(docs, line, locale, 0, 0);
				if(i == batchNum){
					_spellCheckerServer.addDocuments(docs);
					docs.clear();
					i = 0;
				}
			}
			in.close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void setSupportedLocales(Map<String, String> supportedLocalesDirs) {
		this._supportedLocalesDirs = supportedLocalesDirs;
	}

	public void setSpellCheckerServer(SpellCheckerServer spellCheckerServer) {
		this._spellCheckerServer = spellCheckerServer;
	}

	private Set<String> _spellFields;
	private Map<String, String> _supportedLocalesDirs;
	private SpellCheckerServer _spellCheckerServer;

	private static final int batchNum = 1000;
}
