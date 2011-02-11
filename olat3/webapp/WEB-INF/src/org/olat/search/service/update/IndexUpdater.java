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
* <p>
*/ 

package org.olat.search.service.update;

import java.io.File;
import java.util.List;
import java.util.Vector;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.olat.core.commons.services.search.OlatDocument;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

/**
 * The IndexUpdater thread controls the update of existing search index.
 * The update thread could be disabled with config parameter 'updateInterval=0'.
 * @author Christian Guretzki
 */
public class IndexUpdater implements Runnable {
	private static final int INDEX_MERGE_FACTOR = 1000;
	private static OLog log = Tracing.createLoggerFor(IndexUpdater.class);

	private String indexPath;

	private Thread updaterThread = null;
	private long updateInterval;

	private boolean stopUpdater;

	private List<Document> updateQueue;
	private List<Document> deleteQueue;

	/**
	 * 
	 * @param indexPath      Absolute file-path of existing index-directory which will be updated 
	 * @param updateInterval Updater sleeps this time [ms] between running again. 
	 */
	public IndexUpdater(String indexPath, long updateInterval) {
		this.indexPath = indexPath;
		updateQueue = new Vector<Document>();
		deleteQueue = new Vector<Document>();
		this.updateInterval = updateInterval;
		stopUpdater = true;
		if (updateInterval != 0) {
		  startUpdater();
		} else {
			log.info("IndexUpdater is disabled");
		}
	}
	
	/**
	 * Add new or changed index document to update-queue.
	 * @param document  New or changed index document.
	 */
	// o_clusterNOK: IndexUpdater is only prove of concept (with groups) and NOT designed for cluster !!!
	public void addToIndex(Document document) {
		// The IndexUpdate is disabled with updateInterval == 0 => do not add documents 
		if (updateInterval != 0) {
			updateQueue.add(document);
		}
	}

	/**
	 * Add index document to delete-queue.
	 * @param document  Delete this index document.
	 */
	// o_clusterNOK: IndexUpdater is only prove of concept (with groups) and NOT designed for cluster !!!
	public void deleteFromIndex(Document document) {
		// The IndexUpdate is disabled with updateInterval == 0 => do not add documents 
		if (updateInterval != 0) {
		  deleteQueue.add(document);
		}
	}

	public void run() {
		boolean runAgain = true;
		try {
			while (runAgain && !this.stopUpdater) {
				log.info("Updater starts...");
				doUpdate();
				log.info("Updater done ");
				if (updateInterval == 0) {
					log.debug("do not run again");
					runAgain = false;
				} else {
					if (log.isDebug()) log.debug("Updater sleep=" + updateInterval + "ms");
					Thread.sleep(updateInterval);
					if (log.isDebug()) log.debug("Restart updater");
				}
			}
		} catch(InterruptedException iex) {
			log.info("FullIndexer was interrupted ;" + iex.getMessage());
		}
		stopUpdater = true;
		log.info("quit indexing run.");
	}

	/**
	 * Check update and delete-queue. Update existing index and writes new index file. 
	 *
	 */
	private void doUpdate() {
		if (!updateQueue.isEmpty() || !deleteQueue.isEmpty()) {
			try {
				log.info("updateQueue.size=" + updateQueue.size() + " deleteQueue.size" + deleteQueue.size());
				// 0. make copy of queue's and delete it 
				List<Document> updateCopy;
				synchronized (updateQueue) {
	 			  updateCopy = new Vector<Document>(updateQueue);
	 			  updateQueue.clear();
				}
				List<Document> deleteCopy;
				synchronized (deleteQueue) { 
					deleteCopy = new Vector<Document>(deleteQueue);
					deleteQueue.clear();
				}
				// 1. Open Index Reader
				File indexFile = new File(indexPath);
	      Directory directory = FSDirectory.open(indexFile);
	      IndexReader indexReader = IndexReader.open(directory);

	      log.info("befor delete: indexReader.numDocs()=" + indexReader.numDocs());
				// 2. Delete old Document
	      // loop over all documents in updateQueue
	      for (int i = 0; i < updateCopy.size(); i++) {
	      	String resourceUrl = updateCopy.get(i).get(OlatDocument.RESOURCEURL_FIELD_NAME);
		      Term term = new Term(OlatDocument.RESOURCEURL_FIELD_NAME, resourceUrl );
		      log.info("updateQueue:delete documents with resourceUrl=" + resourceUrl );
		      indexReader.deleteDocuments(term);					
				}
	      // loop over all documents in deleteQueue
	      for (int i = 0; i < deleteCopy.size(); i++) {
	      	String resourceUrl = deleteCopy.get(i).get(OlatDocument.RESOURCEURL_FIELD_NAME);
		      Term term = new Term(OlatDocument.RESOURCEURL_FIELD_NAME, resourceUrl );
		      log.info("deleteQueue:delete documents with resourceUrl='" + resourceUrl + "'");
		      indexReader.deleteDocuments(term);
		      
				}
	      log.info("after delete: indexReader.numDocs()=" + indexReader.numDocs());
				// 3. Close reader
	      indexReader.close();
	      directory.close();
	      
				// 4. open writer
	      IndexWriter indexWriter = new IndexWriter(directory, new StandardAnalyzer(Version.LUCENE_CURRENT), false, IndexWriter.MaxFieldLength.UNLIMITED);
				indexWriter.setMergeFactor(INDEX_MERGE_FACTOR); //for better performance
				// 5. Add new Document
	      for (int i = 0; i < updateCopy.size(); i++) {
	      	Document document = updateCopy.get(i);
	      	log.info("addDocument:" + document);
	      	indexWriter.addDocument(document);					
				}
				// 6. Close writer
	      long startOptimizeTime = 0;
	      if (log.isDebug()) startOptimizeTime = System.currentTimeMillis();
	      indexWriter.optimize();// TODO:chg: dauert ev. zulange oder nocht noetig
	      if (log.isDebug()) log.debug("Optimized in " + (System.currentTimeMillis() - startOptimizeTime) + "ms");
	      indexWriter.close();
			} catch (Exception ex) {
				log.warn("Exception during doUpdate. ", ex);
			}
		} else {
			log.debug("Queues are ampty.");
		}
	}

	/**
	 * Start updater thread.
	 */
	public void startUpdater() {
    //	 Start updateThread
		if ( (updaterThread == null) || !updaterThread.isAlive()) {
			log.info("start Updater thread...");
			if (stopUpdater) {
				updaterThread = new Thread(this, "Updater");
				stopUpdater = false;
		    // Set to lowest priority
		    updaterThread.setPriority(Thread.MIN_PRIORITY);
		    updaterThread.setDaemon(true);
				updaterThread.start();
			}
		} else {
			if (log.isDebug()) log.debug("Updater allready running");
		}
	}

	/**
	 * Stop update thread asynchron.
	 */
	public void stopUpdater() {
		if (updaterThread.isAlive()) {
			stopUpdater = true;
			updaterThread.interrupt();
			if (log.isDebug()) log.debug("stop Updater");
		}
	}

	
}
