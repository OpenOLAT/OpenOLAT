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

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.search.SearchModule;
import org.olat.search.SearchService;
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
	private String permanentIndexPath;
	
	private OlatFullIndexer fullIndexer;
	private SearchSpellChecker spellChecker;
	private LifeFullIndexer lifeIndexer;

	/**
	 * 
	 */
	public Index(SearchModule searchModule, SearchService searchService, SearchSpellChecker spellChecker, MainIndexer mainIndexer,
			LifeFullIndexer lifeIndexer, CoordinatorManager coordinatorManager) {
		this.spellChecker = spellChecker;
		this.indexPath = searchModule.getFullIndexPath();
		this.tempIndexPath = searchModule.getFullTempIndexPath();
		this.permanentIndexPath = searchModule.getFullPermanentIndexPath();
		this.lifeIndexer = lifeIndexer;
		
		fullIndexer = new OlatFullIndexer(this, searchModule, searchService, mainIndexer, coordinatorManager);
	}

	/**
	 * Start full-index thread.
	 */
	public void startFullIndex() {
		// do not start search engine in test mode, some repository tests might lead to nullpointers
		// since only dummy entries are generated (or fix the search service to handle those correctly)
		if (!Settings.isJUnitTest()) {
			lifeIndexer.fullIndex();
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
			Directory directory = FSDirectory.open(indexFile.toPath());
			return DirectoryReader.indexExists(directory);
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
	}
	
	public boolean existPermanentIndex() {
		try {
			File indexFile = new File(permanentIndexPath);
			Directory directory = FSDirectory.open(indexFile.toPath());
			return DirectoryReader.indexExists(directory);
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
		File indexDir = new File(indexPath);
		if (!indexDir.exists()) {
		  indexDir.mkdirs();
		}
		if (log.isDebug())  log.debug("Copy new generated Index from '" + tempIndexPath + "/main" + "' to '" + indexPath + "'");
		// Delete existing index files
		File tempIndexDir = new File(tempIndexPath);
		FileUtils.deleteDirsAndFiles(indexDir, true, false);
		FileUtils.copyDirContentsToDir(new File(tempIndexDir, "main") , indexDir ,true, "search indexer move tmp index");
		log.info("New generated Index ready to use." );
		
		spellChecker.createSpellIndex();
	}
	
	public OlatFullIndexer getIndexer() {
		return fullIndexer;
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
}
