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
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.lucene.LucenePackage;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WorkThreadInformations;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.search.SearchModule;
import org.olat.search.SearchService;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * Controls the hole generation of a full-index. Runs in own thread.
 * @author Christian Guretzki
 */
public class OlatFullIndexer {
	
	private static final OLog log = Tracing.createLoggerFor(OlatFullIndexer.class);
	private static final int INDEX_MERGE_FACTOR = 1000;

	private static final int MAX_SIZE_QUEUE = 500;
	private int numberIndexWriter = 5;

	private String  indexPath;
	private String  tempIndexPath;

	/**
	 * Reference to indexer for done callback.
	 */
	private Index index;
	private IndexWriter indexWriter;

	/** Flag to stop indexing. */
	private boolean stopIndexing;
  /** When restartIndexingWhenFinished is true, the restart interval in ms can be set. */
	private long indexInterval = 500;

	private double ramBufferSizeMB;
	
	/** Current status of full-indexer. */
	private FullIndexerStatus fullIndexerStatus;

	/** Used to build number of indexed documents per minute. */ 
	private long lastMinute;

	private int currentMinuteCounter;
	
	/** Queue to pass documents from indexer to index-writers. Only in multi-threaded mode. */
	private Vector<Document> documentQueue;
	private IndexWriterWorker[] indexWriterWorkers;

	/* Define number of documents which will be added befor sleeping (indexInterval for CPU load). */
	int documentsPerInterval;
  /* Counts added documents in indexInterval. */
	private int sleepDocumentCounter = 0;
	/* List of Integer objects to count number of docs for each type. Key = document-type. */
	private Map<String,Integer> documentCounters;
	private Map<String,Integer> fileTypeCounters;

	private MainIndexer mainIndexer;
	private CoordinatorManager coordinatorManager;

	
	/**
	 * 
	 * @param tempIndexPath   Absolute file path to temporary index directory.
	 * @param index           Reference to index object.
	 * @param restartInterval Restart interval in milliseconds.
	 * @param indexInterval   Sleep time in milliseconds between adding documents.
	 */
	public OlatFullIndexer(Index index, SearchModule searchModuleConfig,
			MainIndexer mainIndexer, CoordinatorManager coordinatorManager) {
		this.index = index;
		this.mainIndexer = mainIndexer;
		this.coordinatorManager = coordinatorManager;
		indexPath = searchModuleConfig.getFullIndexPath();
		tempIndexPath = searchModuleConfig.getFullTempIndexPath();
		indexInterval = searchModuleConfig.getIndexInterval();
		numberIndexWriter = searchModuleConfig.getNumberIndexWriter();
		documentsPerInterval = searchModuleConfig.getDocumentsPerInterval();
		ramBufferSizeMB = searchModuleConfig.getRAMBufferSizeMB();
		fullIndexerStatus = new FullIndexerStatus(numberIndexWriter);
		stopIndexing = true;
		documentQueue = new Vector<Document>();
		initStatus();
		resetDocumentCounters();
	}
	
	private void initStatus() {
		File indexDir = new File(indexPath);
		if (indexDir.exists()) {
			final AtomicLong last = new AtomicLong(1);
			try {
				Files.walkFileTree(indexDir.toPath(), new SimpleFileVisitor<Path>(){
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						if(attrs.isRegularFile()) {
							FileTime time = attrs.lastModifiedTime();
							long timeInMillis = time.toMillis();
							if(timeInMillis > 0 && last.longValue() < timeInMillis) {
								last.set(timeInMillis);
							}
						}
				        return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				log.error("", e);
			}
			fullIndexerStatus.setLastFullIndexTime(last.get());
		} else {
			fullIndexerStatus.setLastFullIndexTime(1);
		}
	}
	
	/**
	 * Start full indexer thread.
	 */
	public void startIndexing() {
    //	 Start updateThread
		if (stopIndexing) {
			log.info("start full indexing thread...");
			stopIndexing = false;
			resetDocumentCounters();
			run();
		}
	}

	/**
	 * Stop full indexer thread asynchron.
	 */
	public void stopIndexing() {
		stopIndexing = true;
		if (log.isDebug()) log.debug("stop current indexing when");
	}
	
	
	public LogMergePolicy newLogMergePolicy() {
		LogMergePolicy logmp = new LogDocMergePolicy();
		logmp.setCalibrateSizeByDeletes(true);
		logmp.setMergeFactor(INDEX_MERGE_FACTOR);
		return logmp;
	}

	
	public IndexWriterConfig newIndexWriterConfig() {
		Analyzer analyzer = new StandardAnalyzer(SearchService.OO_LUCENE_VERSION);
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(SearchService.OO_LUCENE_VERSION, analyzer);
		indexWriterConfig.setMergePolicy(newLogMergePolicy());
		indexWriterConfig.setRAMBufferSizeMB(ramBufferSizeMB);// for better performance set to 48MB (see lucene docu 'how to make indexing faster")
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		return indexWriterConfig;
	}

	/**
	 * Create index-writer object. In multi-threaded mode ctreates an array of index-workers.
	 * Start indexing with main-index as root object. Index recursive all elements.
	 * At the end optimze and close new index. 
	 * The new index is stored in [temporary-index-path]/main
	 * @throws InterruptedException
	 */
	private void doIndex() throws InterruptedException{
		try {
			WorkThreadInformations.setLongRunningTask("indexer");
			
			File tempIndexDir = new File(tempIndexPath);
			Directory indexPath = FSDirectory.open(new File(tempIndexDir, "main"));
			
			indexWriter = new IndexWriter(indexPath, newIndexWriterConfig());// analyzer, true, IndexWriter.MAX_TERM_LENGTH.UNLIMITED);
			indexWriter.deleteAll();
			
			// Create IndexWriterWorker
			log.info("Running with " + numberIndexWriter + " IndexerWriterWorker");
			indexWriterWorkers = new IndexWriterWorker[numberIndexWriter];
			Directory[] partIndexDirs = new Directory[numberIndexWriter];
			for (int i = 0; i < numberIndexWriter; i++) {
				IndexWriterWorker indexWriterWorker = new IndexWriterWorker(i, tempIndexDir, this);
				indexWriterWorkers[i] = indexWriterWorker;
				indexWriterWorkers[i].start();
				partIndexDirs[i] = indexWriterWorkers[i].getIndexDir();
			}
			
			SearchResourceContext searchResourceContext = new SearchResourceContext();
			log.info("doIndex start. OlatFullIndexer with Debug output");
			mainIndexer.doIndex(searchResourceContext, null /*no parent*/, this);
	
			log.info("Wait until every folder indexer is finished");
			
			DBFactory.getInstance().commitAndCloseSession();
			// check if every folder indexer is finished max waiting-time 10Min (=waitingCount-limit = 60) 
			int waitingCount = 0;
			int MAX_WAITING_COUNT = 60;// = 10Min
			while (FolderIndexerWorkerPool.getInstance().isIndexerRunning() && (waitingCount++ < MAX_WAITING_COUNT) ) { 
				Thread.sleep(10000);
			}
			if (waitingCount >= MAX_WAITING_COUNT) log.info("Finished with max waiting time!");
			log.info("Set Finish-flag for each indexWriterWorkers");
			// Set Finish-flag
			for (int i = 0; i < numberIndexWriter; i++) {
				indexWriterWorkers[i].finishIndexing();
			}

			log.info("Wait until every indexworker is finished");
			// check if every indexworker is finished max waiting-time 10Min (=waitingCount-limit = 60)
			waitingCount = 0;
			while (!areIndexingDone() && (waitingCount++ < MAX_WAITING_COUNT) ) {
				Thread.sleep(10000);
			}
			if (waitingCount >= MAX_WAITING_COUNT) log.info("Finished with max waiting time!");
			
			// Merge all partIndex
			DBFactory.getInstance().commitAndCloseSession();
			if(partIndexDirs.length > 0) {
				log.info("Start merging part Indexes");
				indexWriter.addIndexes(partIndexDirs);
				log.info("Added all part Indexes");
			}
			fullIndexerStatus.setIndexSize(indexWriter.maxDoc());
			indexWriter.close();
			indexWriter = null;
			indexWriterWorkers = null;
		} catch (IOException e) {
			log.warn("Can not create IndexWriter, indexname=" + tempIndexPath, e);
		} finally {
			WorkThreadInformations.unsetLongRunningTask("indexer");
			DBFactory.getInstance().commitAndCloseSession();
			log.debug("doIndex: commit & close session");
		}
	}
	
	/**
	 * Ensure that all indexworker is finished
	 * @return
	 */
	private boolean areIndexingDone() {
		if(indexWriterWorkers != null && indexWriterWorkers.length > 0) {
			if(!documentQueue.isEmpty()) {
				return false;
			}
			
			for(IndexWriterWorker worker:indexWriterWorkers) {
				if(!worker.isClosed()) {
					return false;
				}
			}
		}
		return documentQueue.isEmpty();
	}

	/**
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
			//TODO: Workround : does not start immediately
			Thread.sleep(10000);

			log.info("full indexing starts... Lucene-version:" + LucenePackage.get().getImplementationVersion());
			fullIndexerStatus.indexingStarted();
			doIndex();
			index.indexingIsDone();
			fullIndexerStatus.indexingFinished();
			log.info("full indexing done in " + fullIndexerStatus.getIndexingTime() + "ms");
			
			//created because the index is deleted and copied
			IndexerEvent event = new IndexerEvent(IndexerEvent.INDEX_CREATED);
			coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(event, IndexerEvent.INDEX_ORES);
			
			//OLAT-5630 - dump more infos about the indexer run - for analysis later
			FullIndexerStatus status = getStatus();
			log.info("full indexing summary: started:           "+status.getFullIndexStartedAt());
			log.info("full indexing summary: counter:           "+status.getDocumentCount());
			log.info("full indexing summary: index.per.minute:  "+status.getIndexPerMinute());
			log.info("full indexing summary: finished:          "+status.getLastFullIndexDateString());
			log.info("full indexing summary: time:              "+status.getIndexingTime()+" ms");
			log.info("full indexing summary: size:              "+status.getIndexSize());
			
			log.info("full indexing summary: document counters: "+status.getDocumentCounters());
			log.info("full indexing summary: file type counters:"+status.getFileTypeCounters());
			log.info("full indexing summary: excluded counter:  "+status.getExcludedDocumentCount());

		} catch(InterruptedException iex) {
			log.info("FullIndexer was interrupted ;" + iex.getMessage());
		} catch(Throwable ex) {
			try {
				log.error("Error during full-indexing:" + ex.getMessage() , ex);
			} catch (NullPointerException nex) {
				// no logging available (shut down) => do nothing
			}
		}
		fullIndexerStatus.setStatus(FullIndexerStatus.STATUS_STOPPED);
		stopIndexing = true;
		try {
			log.info("quit indexing run.");
		} catch (NullPointerException nex) {
			// no logging available (shut down)=> do nothing
		}
	}
	


	
	/**
	 * Callback to addDocument to indexWriter.
	 * @param document
	 * @throws IOException
	 */
	public void addDocument(Document document) throws IOException,InterruptedException {
		if (numberIndexWriter == 0 ) {
			indexWriter.addDocument(document);
			fullIndexerStatus.incrementDocumentCount();
			if (indexInterval != 0 && sleepDocumentCounter++ >= documentsPerInterval) {
				sleepDocumentCounter  = 0;
				Thread.sleep(indexInterval);
			} else {
				// do not sleep, check for stopping indexing
				if (stopIndexing) {
					throw new InterruptedException("Do stop indexing at element=" + indexWriter.maxDoc());
				}
			}
			countIndexPerMinute();
		} else {
			// clusterOK by:cg synchronizes only access of index-writer, indexer runs only on one cluster-node
			synchronized (documentQueue) {
				while (documentQueue.size() > MAX_SIZE_QUEUE) {
					log.warn("Document queue over " + MAX_SIZE_QUEUE);
					Thread.sleep(60000);
				}
				documentQueue.add(document);
				fullIndexerStatus.incrementDocumentCount();
				fullIndexerStatus.setDocumentQueueSize(documentQueue.size());
				countIndexPerMinute();
				if (log.isDebug()) log.debug("documentQueue.add size=" + documentQueue.size());
	      // check for stopping indexing
				if (stopIndexing) {
					throw new InterruptedException("Do stop indexing at element=" + indexWriter.maxDoc());
				}
			}
		}
		incrementDocumentTypeCounter(document);
		incrementFileTypeCounter(document);
// TODO:cg/07.10.2010		try to fix Indexer ERROR 'Overdue resource check-out stack trace.' on OLATNG
//                      close and commit after each document		
//		if (fullIndexerStatus.getDocumentCount() % 20 == 0) {
			// Do commit after certain number of documents because the transaction should not be too big
			DBFactory.getInstance().commitAndCloseSession();
			log.debug("DB: intermediateCommit");
//		}
	}
	
	private void incrementFileTypeCounter(Document document) {
		String fileType = document.get(OlatDocument.FILETYPE_FIELD_NAME);
		if ( (fileType != null) && (!fileType.equals(""))) {
			int intValue = 0;
			if (fileTypeCounters.containsKey(fileType)) {
			  Integer fileCounter = fileTypeCounters.get(fileType);
			  intValue = fileCounter.intValue();
			}
			intValue++;
			fileTypeCounters.put(fileType, new Integer(intValue));
		}
	}

	private void incrementDocumentTypeCounter(Document document) {
		String documentType = document.get(OlatDocument.DOCUMENTTYPE_FIELD_NAME);
		int intValue = 0;
		if (documentCounters.containsKey(documentType)) {
		  Integer docCounter = documentCounters.get(documentType);
		  intValue = docCounter.intValue();
		}
		intValue++;
		documentCounters.put(documentType, new Integer(intValue));
	}

	private void countIndexPerMinute() {
		long currentTime = System.currentTimeMillis();
		if (lastMinute+60000 > currentTime) {
			// it is teh same minute
			currentMinuteCounter++;
		} else {
			fullIndexerStatus.setIndexPerMinute(currentMinuteCounter);
			currentMinuteCounter = 0;
			if (lastMinute+120000 > currentTime) {
  			lastMinute = lastMinute+60000;
			} else {
				lastMinute = currentTime;
			}
		}		
	}

	/**
	 * @return  Return current full-indexer status.
	 */
	public FullIndexerStatus getStatus() {
		if (indexWriterWorkers != null) {
			// IndexWorker exist => set current document-counter
			for (int i = 0; i < numberIndexWriter; i++) {
				fullIndexerStatus.setPartDocumentCount(indexWriterWorkers[i].getDocCount(),i);
			}
		}
		fullIndexerStatus.setDocumentCounters(documentCounters);
		fullIndexerStatus.setFileTypeCounters(fileTypeCounters);
		fullIndexerStatus.setDocumentQueueSize(documentQueue.size());
		return fullIndexerStatus;
	}
	
	public long getIndexInterval() {
		return indexInterval;
	}
	
	/**
	 * @param indexInterval The indexInterval to set.
	 */
	public void setIndexInterval(long indexInterval) {
		this.indexInterval = indexInterval;
	}
	
	/**
	 * @return  Return document-queue which is used in multi-threaded mode.
	 */
	public List<Document> getDocumentQueue() {
		return documentQueue;
	}

	/**
	 * Check if the indexing process is interrupted.
	 * @return  TRUE: indexing process is interrupted.
	 */
	public boolean isInterupted() {
		return stopIndexing;
	}
	
	private void resetDocumentCounters() {
		documentCounters = new Hashtable<String,Integer>();
		fileTypeCounters = new Hashtable<String,Integer>();		
	}
}
