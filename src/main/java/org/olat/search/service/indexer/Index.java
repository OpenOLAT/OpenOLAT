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

package org.olat.search.service.indexer;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.olat.core.commons.services.search.SearchModule;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.search.service.spell.SearchSpellChecker;

/**
 * Controls the existing index, copy new generated index from temporary directory to search direcotry. 
 * @author Christian Guretzki
 * 
 */
public class Index {
	
	private static OLog log = Tracing.createLoggerFor(Index.class);
	
	private String indexPath;
	private String tempIndexPath;
	
	private OlatFullIndexer fullIndexer;
	private SearchSpellChecker spellChecker;

	/**
	 * 
	 * @param indexPath       Absolute directory path of search index.
	 * @param tempIndexPath   Absolute directory path of temporary index.
	 * @param restartInterval Restart interval of full-index in milliseconds.
	 * @param indexInterval   Sleeping time in milliseconds between adding documents to index.
	 */
	public Index(SearchModule searchModuleConfig, SearchSpellChecker spellChecker, MainIndexer mainIndexer) {
		this.spellChecker = spellChecker;
		this.indexPath = searchModuleConfig.getFullIndexPath();
		this.tempIndexPath = searchModuleConfig.getFullTempIndexPath();
		
		fullIndexer = new OlatFullIndexer(this, searchModuleConfig, mainIndexer);
	}

	/**
	 * Start full-index thread.
	 */
	public void startFullIndex() {
		// do not start search engine in test mode, some repository tests might lead to nullpointers
		// since only dummy entries are generated (or fix the search service to handle those correctly)
		if ( ! Settings.isJUnitTest()) {
		  fullIndexer.startIndexing();
		}
	}
	
	/**
	 * Stop full-index thread.
	 */
	public void stopFullIndex() {
		fullIndexer.stopIndexing();
	}

	/**
	 * Check if index exist.
	 * @return true : Index exists.
	 */
	public boolean existIndex() {
		try {
			File indexFile = new File(indexPath);
			Directory directory = FSDirectory.open(indexFile);
			return IndexReader.indexExists(directory);
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
	}

	/**
	 * Check if indexing is complete done.
	 * @return true : Indexing is done.
	 */
	public void indexingIsDone() {
		// Full indexing is done => move tempIndex to index dir
		moveTempIndexToIndex(tempIndexPath,indexPath);
		spellChecker.createSpellIndex();
	}

	private void moveTempIndexToIndex(String tempIndexPath, String indexPath) {
		File indexDir = new File( indexPath );
		if (!indexDir.exists()) {
		  indexDir.mkdirs();
		}
		if (log.isDebug())  log.debug("Copy new generated Index from '" + tempIndexPath + "/main" + "' to '" + indexPath + "'");
		// Delete existing index files
		File tempIndexDir = new File(tempIndexPath);
		FileUtils.deleteDirsAndFiles(indexDir, true, false);
		FileUtils.copyDirContentsToDir(new File(tempIndexDir, "main") , indexDir ,true, "search indexer move tmp index");
		log.info("New generated Index ready to use." );
	}

	/**
	 * @return  Return current status of full-indexer.
	 */
	public FullIndexerStatus getFullIndexStatus() {
		return fullIndexer.getStatus();
	}

	public long getIndexInterval() {
		return fullIndexer.getIndexInterval();
	}
	
	/**
	 * Set index-interval of full-indexer
	 * @param indexInterval  New index-interval in milliseconds.
	 */
	public void setIndexInterval(long indexInterval) {
		fullIndexer.setIndexInterval(indexInterval);
	}

	/**
	 * @return  Creation date of current used search index. 
	 */
	public Date getCreationDate() {
		try {
			File indexFile = new File(indexPath);
			Directory directory = FSDirectory.open(indexFile);
			return new Date(IndexReader.getCurrentVersion(directory));
		} catch (IOException e) {
			return null;
		}
	}
	
}
