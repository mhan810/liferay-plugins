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
import com.liferay.portal.kernel.search.SearchException;

import java.io.File;

import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.util.Version;

/**
 * @author Daniela Zapata
 * @author David Mendez
 */
public class SpellCheckerServer {

	public void addDocuments(Set<Document> documents)
		throws SearchException {
		try{
			initSpellWriter();
			spellIndexWriter.addDocuments(documents);
			closeSpellWriter();
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to index documents", e);
			}

			throw new SearchException(e.getMessage());
		}
	}

	public void deleteDocuments() throws SearchException {
		try{
			initSpellWriter();
			spellIndexWriter.deleteAll();
			closeSpellWriter();
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to delete documents", e);
			}

			throw new SearchException(e.getMessage());
		}
	}

	public Document getDocument(int docId) throws SearchException {

		Document doc;
		try{
			initSpellReader();
			doc = spellIndexReader.document(docId);
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to retrieve the document", e);
			}

			throw new SearchException(e.getMessage());
		}
		return doc;
	}

	public TopDocs getTopDocs(Query query, int results)
		throws SearchException {

		TopDocs topDocs = null;
		try {

			initSpellReader();
			topDocs = spellIndexSearcher.search(query, results);

		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to execute the search", e);
			}

			throw new SearchException(e.getMessage());
		}
		return topDocs;

	}

	public void initSpellWriter() throws Exception {

		File dirFile = new File(_spellcheckDir);

		Directory companyDir = FSDirectory.open(dirFile, new NativeFSLockFactory());

		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
			Version.LUCENE_40, new StandardAnalyzer(Version.LUCENE_40));

		spellIndexWriter = new IndexWriter(companyDir, indexWriterConfig);

	}

	public void initSpellReader() throws Exception {

		File dirFile = new File(_spellcheckDir);

		Directory companyDir = FSDirectory.open(dirFile);

		spellIndexReader = DirectoryReader.open(companyDir);

		spellIndexSearcher = new IndexSearcher(spellIndexReader);

	}

	public void closeSpellWriter() throws SearchException {
		try{
			spellIndexWriter.close();
		}
		catch (Exception e) {
			if (_log.isDebugEnabled()) {
				_log.debug("Unable to close the IndexWriter", e);
			}

			throw new SearchException(e.getMessage());
		}
	}

	public void setSpellcheckDir(String spellcheckDir) {
		_spellcheckDir = spellcheckDir;
	}

	private static Log _log =
		LogFactoryUtil.getLog(SpellCheckerServer.class);

	private String _spellcheckDir;

	private IndexWriter spellIndexWriter;
	private IndexReader spellIndexReader;
	private IndexSearcher spellIndexSearcher;

}
