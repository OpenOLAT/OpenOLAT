/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.search.service.spell;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spell.Dictionary;
import org.apache.lucene.search.spell.DirectSpellChecker;
import org.apache.lucene.search.spell.LuceneDictionary;
import org.apache.lucene.search.spell.SpellChecker;
import org.apache.lucene.search.spell.SuggestMode;
import org.apache.lucene.search.spell.SuggestWord;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.search.SearchService;
import org.olat.search.model.OlatDocument;

/**
 * Spell-checker part inside of search-service.
 * Service to check certain search-query for similar available search.queries.
 * @author Christian Guretzki
 */
public class SearchSpellChecker {
	private static OLog log = Tracing.createLoggerFor(SearchSpellChecker.class);
		
	private static final String CONTENT_PATH = "_content";
	private static final String TITLE_PATH = "_title";
	private static final String DESCRIPTION_PATH = "_description";
	private static final String AUTHOR_PATH = "_author";
	
	private String indexPath;
	private String spellDictionaryPath;
	private SpellChecker spellChecker;
	private boolean isSpellCheckEnabled = true;
	
	
	public SearchSpellChecker() {
		//called by Spring
	}
	
	/**
	 * Check for valid similar search terms 
	 * @param query
	 * @return Returns list of String with similar search-words.
	 *         Returns null when spell-checker is disabled or has an exception.
	 */
  public Set<String> check(String query) {
  	try {
  		if(spellChecker==null) { //lazy initialization
  			try {
  				synchronized(spellDictionaryPath) {//o_clusterOK by:pb if service is only configured on one vm, which is recommended way
  				  File spellDictionaryFile = new File(spellDictionaryPath);
  				  Directory spellIndexDirectory = FSDirectory.open(spellDictionaryFile);
  					if (spellChecker==null && DirectoryReader.indexExists(spellIndexDirectory) && isSpellCheckEnabled ) {
  					  spellChecker = new SpellChecker(spellIndexDirectory);
  					  spellChecker.setAccuracy(0.7f);
  				  }
  				}
  	 		} catch (IOException e) {
  	 			log.warn("Can not initialze SpellChecker",e);
  			}
  		} 
  		if (spellChecker != null) {
  			String[] words = spellChecker.suggestSimilar(query,5);
  			// Remove dublicate 
  			Set<String> filteredList = new TreeSet<String>();
  			for (String word : words) {
  				filteredList.add(word);
				}
  			
  			/*
  			Set<String> directList = directCheck(query);
  			System.out.println("Direct: " + directList);
  			System.out.println("Filter: " + filteredList);
  			*/
  			
			  return filteredList;
  		}
  		
		} catch (IOException e) {
			log.warn("Can not spell check",e);
			return null;
		}
		return null;
  }
  

  public Set<String> directCheck(String query) {
		try {
			File indexPathIO = new File(indexPath);
			Directory spellIndexDirectory = FSDirectory.open(indexPathIO);
			DirectoryReader indexReader = DirectoryReader.open(spellIndexDirectory);

			String[] fields = new String[]{ OlatDocument.TITLE_FIELD_NAME, OlatDocument.DESCRIPTION_FIELD_NAME, OlatDocument.AUTHOR_FIELD_NAME, OlatDocument.CONTENT_FIELD_NAME};
			Set<String> filteredList = new TreeSet<String>();
			for(String field:fields) {
				Term term = new Term(field, query);
				DirectSpellChecker spellChecker = new DirectSpellChecker();
				spellChecker.setAccuracy(0.7f);
				spellChecker.setMaxInspections(10);
				spellChecker.setMaxEdits(2);
				//spellChecker.setDistance(new NGramDistance(4));
				
				SuggestWord[] words = spellChecker.suggestSimilar(term, 5, indexReader, SuggestMode.SUGGEST_ALWAYS, 0.7f);
				for (SuggestWord word : words) {
					filteredList.add(word.string);
				}
			}
			
			return filteredList;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
  }
  	
  /**
   * Creates a new spell-check index based on search-index 
   *
   */
  public void createSpellIndex() {
	  if (isSpellCheckEnabled) {
		  IndexReader indexReader = null;
		  try {
		  	log.info("Start generating Spell-Index...");
		    long startSpellIndexTime = 0;
		    if (log.isDebug()) startSpellIndexTime = System.currentTimeMillis();
	      Directory indexDir = FSDirectory.open(new File(indexPath));
		    indexReader = DirectoryReader.open(indexDir);
	      // 1. Create content spellIndex 
		    File spellDictionaryFile = new File(spellDictionaryPath);
	      Directory contentSpellIndexDirectory = FSDirectory.open(new File(spellDictionaryPath + CONTENT_PATH));//true
	      SpellChecker contentSpellChecker = new SpellChecker(contentSpellIndexDirectory);
	      Dictionary contentDictionary = new LuceneDictionary(indexReader, OlatDocument.CONTENT_FIELD_NAME);

				Analyzer analyzer = new StandardAnalyzer(SearchService.OO_LUCENE_VERSION);
	      IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SearchService.OO_LUCENE_VERSION, analyzer);
	      
	      contentSpellChecker.indexDictionary(contentDictionary, indexWriterConfig, true);
	      // 2. Create title spellIndex 
	      Directory titleSpellIndexDirectory = FSDirectory.open(new File(spellDictionaryPath + TITLE_PATH));//true
	      SpellChecker titleSpellChecker = new SpellChecker(titleSpellIndexDirectory);
	      Dictionary titleDictionary = new LuceneDictionary(indexReader, OlatDocument.TITLE_FIELD_NAME);
	      titleSpellChecker.indexDictionary(titleDictionary, indexWriterConfig, true);
	      // 3. Create description spellIndex 
	      Directory descriptionSpellIndexDirectory = FSDirectory.open(new File(spellDictionaryPath + DESCRIPTION_PATH));//true
	      SpellChecker descriptionSpellChecker = new SpellChecker(descriptionSpellIndexDirectory);
	      Dictionary descriptionDictionary = new LuceneDictionary(indexReader, OlatDocument.DESCRIPTION_FIELD_NAME);
	      descriptionSpellChecker.indexDictionary(descriptionDictionary, indexWriterConfig, true);
	      // 4. Create author spellIndex 
	      Directory authorSpellIndexDirectory = FSDirectory.open(new File(spellDictionaryPath + AUTHOR_PATH));//true
	      SpellChecker authorSpellChecker = new SpellChecker(authorSpellIndexDirectory);
	      Dictionary authorDictionary = new LuceneDictionary(indexReader, OlatDocument.AUTHOR_FIELD_NAME);
	      authorSpellChecker.indexDictionary(authorDictionary, indexWriterConfig, true);
	      
	      // Merge all part spell indexes (content,title etc.) to one common spell index
	      Directory spellIndexDirectory = FSDirectory.open(spellDictionaryFile);//true
	      
	      IndexWriter merger = new IndexWriter(spellIndexDirectory, indexWriterConfig);
	      Directory[] directories = { contentSpellIndexDirectory, titleSpellIndexDirectory, descriptionSpellIndexDirectory, authorSpellIndexDirectory};
	      for(Directory directory:directories) { 
	      	merger.addIndexes(directory);
	      }
	     
	      merger.close();
	      contentSpellChecker.close();
	      titleSpellChecker.close();
	      descriptionSpellChecker.close();
	      authorSpellChecker.close();
	      
	      spellChecker = new SpellChecker(spellIndexDirectory);
	      spellChecker.setAccuracy(0.7f);
	       if (log.isDebug()) log.debug("SpellIndex created in " + (System.currentTimeMillis() - startSpellIndexTime) + "ms");
	      log.info("New generated Spell-Index ready to use.");
		  } catch(IOException ioEx) { 
		  	log.warn("Can not create SpellIndex",ioEx);
		  } finally {
	      if (indexReader != null) {
	          try {
							indexReader.close();
						} catch (IOException e) {
							log.warn("Can not close indexReader properly",e);
						}
	      }
			}
	  }
	}

  /**
   * 
   * @param indexPath  Sets the absolute file-path to search index directory.
   */
	public void setIndexPath(String indexPath) {
		this.indexPath = indexPath;
	}

	/**
	 * 
	 * @param isSpellCheckEnabled  Sets the absolute file-path to spell-check index directory.
	 */
	public void setSpellCheckEnabled(boolean isSpellCheckEnabled) {
		this.isSpellCheckEnabled = isSpellCheckEnabled;
	}

	/**
	 * 
	 * @param spellDictionaryPath  Enable/disable spell-checker
	 */
	public void setSpellDictionaryPath(String spellDictionaryPath) {
		this.spellDictionaryPath = spellDictionaryPath;
	} 
	
}
