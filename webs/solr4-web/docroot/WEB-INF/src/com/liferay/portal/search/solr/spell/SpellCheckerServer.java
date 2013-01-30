package com.liferay.portal.search.solr.spell;

import java.io.File;

import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NativeFSLockFactory;
import org.apache.lucene.util.Version;


/**
 * Created with IntelliJ IDEA.
 * User: Liferay
 * Date: 1/28/13
 * Time: 11:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class SpellCheckerServer {

	public void addDocument(Document document) {
		try{
			initSpellWriter();
			spellIndexWriter.addDocument(document);
			close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void addDocuments(Set<Document> documents) {
		try{
			initSpellWriter();
			spellIndexWriter.addDocuments(documents);
			close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public void deleteDocuments(Term term) {
		try{
			initSpellWriter();
			spellIndexWriter.deleteDocuments(term);
			close();
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	public Set<Document> getDocuments(long companyId){
		Set<Document> docs = new HashSet<Document>();

		try{
			initIndexReader();

			String strCompanyId = String.valueOf(companyId);
			for(int i=0; i < baseIndexReader.maxDoc(); i++){
				Document document = baseIndexReader.document(i);
				if(document.get("companyId").equals(strCompanyId)){
					docs.add(document);
				}
			}

		}catch (Exception e){
			e.printStackTrace();
		}

		return docs;
	}

	public TopDocs getTopDocs (
		Query query, int results){

		TopDocs topDocs = null;
		try {

			initSpellReader();
			topDocs = spellIndexSearcher.search(query, results);

		} catch (Exception e){
			e.printStackTrace();
		}
		return topDocs;

	}

	public Document getDocument (int docId){

		Document doc = null;
		try{
			initSpellReader();
			doc = spellIndexReader.document(docId);
		}catch (Exception e){
			e.printStackTrace();
		}
		return doc;
	}

	public void initIndexReader() throws Exception{
		File indexDirFile = new File(_indexDir);
		Directory indexDir = FSDirectory.open(indexDirFile);
		baseIndexReader = DirectoryReader.open(indexDir);
		baseIndexSearcher = new IndexSearcher(baseIndexReader);
	}

	public void initSpellWriter() throws Exception{
		File dirFile = new File(_spellcheckDir);
		Directory companyDir = FSDirectory.open(dirFile, new NativeFSLockFactory());

		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(
			Version.LUCENE_40, new StandardAnalyzer(Version.LUCENE_40));
		spellIndexWriter = new IndexWriter(companyDir, indexWriterConfig);
	}

	public void initSpellReader() throws Exception{
		File dirFile = new File(_spellcheckDir);
		Directory companyDir = FSDirectory.open(dirFile);

		spellIndexReader = DirectoryReader.open(companyDir);
		spellIndexSearcher = new IndexSearcher(spellIndexReader);
	}

	public void setIndexDir(String indexDir) {
		this._indexDir = indexDir;
	}

	public void setSpellcheckDir(String spellcheckDir) {
		this._spellcheckDir = spellcheckDir;
	}

	public void close(){
		try{
			spellIndexWriter.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	private String _indexDir;
	private String _spellcheckDir;

	private IndexReader baseIndexReader;
	private IndexSearcher baseIndexSearcher;

	private IndexWriter spellIndexWriter;
	private IndexReader spellIndexReader;
	private IndexSearcher spellIndexSearcher;

}
