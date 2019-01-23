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
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
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
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.search.QueryException;
import org.olat.search.SearchModule;
import org.olat.search.SearchService;
import org.olat.search.ServiceNotAvailableException;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * Controls the hole generation of a full-index. It run in its own thread the main index.
 * The sub-indexers can use a thread pool to parallelize the works.
 * 
 * @author Christian Guretzki
 */
public class OlatFullIndexer {
	
	private static final OLog log = Tracing.createLoggerFor(OlatFullIndexer.class);
	private static final int INDEX_MERGE_FACTOR = 1000;
	private static final int MAX_WAITING_COUNT = 600;// = 10Min
	private static final IndexerThreadFactory indexWriterThreadFactory = new IndexerThreadFactory("writer");
	private static final IndexerThreadFactory indexWorkersThreadFactory = new IndexerThreadFactory("worker");

	private String indexPath;
	private String tempIndexPath;

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
	
	private final int indexerPoolSize;
	
	/** Current status of full-indexer. */
	private FullIndexerStatus fullIndexerStatus;

	/** Used to build number of indexed documents per minute. */ 
	private long lastMinute;
	private int currentMinuteCounter;

	/* Define number of documents which will be added befor sleeping (indexInterval for CPU load). */
	int documentsPerInterval;
  /* Counts added documents in indexInterval. */
	private int sleepDocumentCounter = 0;
	/* List of Integer objects to count number of docs for each type. Key = document-type. */
	private Map<String,Integer> documentCounters;
	private Map<String,Integer> fileTypeCounters;

	private final MainIndexer mainIndexer;
	private final SearchService searchService;
	private final CoordinatorManager coordinatorManager;

	private static final Object indexerWriterBlock = new Object();
	private ThreadPoolExecutor indexerExecutor;
	private ThreadPoolExecutor indexerWriterExecutor;

	/**
	 * 
	 * @param tempIndexPath   Absolute file path to temporary index directory.
	 * @param index           Reference to index object.
	 * @param restartInterval Restart interval in milliseconds.
	 * @param indexInterval   Sleep time in milliseconds between adding documents.
	 */
	public OlatFullIndexer(Index index, SearchModule searchModule, SearchService searchService,
			MainIndexer mainIndexer, CoordinatorManager coordinatorManager) {
		this.index = index;
		this.mainIndexer = mainIndexer;
		this.searchService = searchService;
		this.coordinatorManager = coordinatorManager;
		// -1 because the thread pool used a CallerRunPolicy, which means the main thread
		// will do the work if the queue of the poll is full.
		if(searchModule.getFolderPoolSize() <= 2) {
			indexerPoolSize = 1;
		} else {
			indexerPoolSize = searchModule.getFolderPoolSize() - 1;
		}
		indexPath = searchModule.getFullIndexPath();
		tempIndexPath = searchModule.getFullTempIndexPath();
		indexInterval = searchModule.getIndexInterval();
		documentsPerInterval = searchModule.getDocumentsPerInterval();
		ramBufferSizeMB = searchModule.getRAMBufferSizeMB();
		fullIndexerStatus = new FullIndexerStatus(1);
		stopIndexing = true;
		initStatus();
		resetDocumentCounters();
	}
	
	private void initStatus() {
		File indexDir = new File(indexPath);
		if (indexDir.exists()) {
			final AtomicLong last = new AtomicLong(1);
			try {
				Files.walkFileTree(indexDir.toPath(), new SimpleFileVisitor<Path>(){
					@Override
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
	 * At the end optimize and close new index. 
	 * The new index is stored in [temporary-index-path]/main
	 * @throws InterruptedException
	 */
	private void doIndex() throws InterruptedException{
		try {
			if(indexerExecutor == null) {
				BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(2);
				indexerExecutor = new ThreadPoolExecutor(indexerPoolSize, indexerPoolSize, 0L, TimeUnit.MILLISECONDS,
						queue, indexWorkersThreadFactory, new ThreadPoolExecutor.CallerRunsPolicy());
			}
			if(indexerWriterExecutor == null) {
				BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(2);
				indexerWriterExecutor = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, queue, indexWriterThreadFactory);
			}
			
			File tempIndexDir = new File(tempIndexPath);
			Directory tmpIndexPath = FSDirectory.open(new File(tempIndexDir, "main"));
			indexWriter = new IndexWriter(tmpIndexPath, newIndexWriterConfig());// analyzer, true, IndexWriter.MAX_TERM_LENGTH.UNLIMITED);
			indexWriter.deleteAll();
			
			SearchResourceContext searchResourceContext = new SearchResourceContext();
			log.info("doIndex start. OlatFullIndexer with Debug output");
			mainIndexer.doIndex(searchResourceContext, null /*no parent*/, this);
			DBFactory.getInstance().commitAndCloseSession();
	
			log.info("Wait until every folder indexer is finished");
			
			indexerExecutor.shutdown();
			indexerExecutor.awaitTermination(10, TimeUnit.MINUTES);
			DBFactory.getInstance().commitAndCloseSession();
			

			log.info("Wait until index writer executor is finished");
			int waitWriter = 0;
			while (indexerWriterExecutor.getActiveCount() > 0 && (waitWriter++ < MAX_WAITING_COUNT)) { 
				Thread.sleep(1000);
			}
			
			log.info("Close index writer executor");
			fullIndexerStatus.setIndexSize(indexWriter.maxDoc());
			//shutdown the index writer thread
			indexerWriterExecutor.submit(new CloseIndexCallable());
			indexerWriterExecutor.shutdown();
			indexerWriterExecutor.awaitTermination(1, TimeUnit.MINUTES);
		} catch (IOException e) {
			log.warn("Can not create IndexWriter, indexname=" + tempIndexPath, e);
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
			log.debug("doIndex: commit & close session");
			
			if(indexerExecutor != null) {
				indexerExecutor.shutdownNow();
				indexerExecutor = null;
			}
			if(indexerWriterExecutor != null) {
				indexerWriterExecutor.shutdownNow();
				indexerWriterExecutor = null;
			}
		}
	}
	
	public Future<Boolean> submit(Callable<Boolean> task) {
		if(indexerExecutor != null && !indexerExecutor.isShutdown()) {
			return indexerExecutor.submit(task);
		} else {
			log.error("Try to submit a task to index executor but it's closed.");
			return null;
		}
	}

	/**
	 * 
	 * @see java.lang.Runnable#run()
	 */
	public void run() {
		try {
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
	
	public Document getDocument(String businessPath) {
		try {
			return searchService.doSearch(businessPath);
		} catch (ServiceNotAvailableException | ParseException | QueryException e) {
			return null;
		}
	}
	
	/**
	 * Add a document to the index writer. The document is indexed by a single threaded executor,
	 * Lucene want that write operations happen within a single thread. The access is synchronized
	 * to block concurrent access to the executor. It blocks the text extractors and allow a
	 * ridiculously small queue but memory efficient.
	 * 
	 * @param document
	 * @throws IOException
	 */
	public void addDocument(Document document) throws IOException,InterruptedException {
		DBFactory.getInstance().commitAndCloseSession();
		
		if (!stopIndexing && indexerWriterExecutor != null && !indexerWriterExecutor.isShutdown()) {
			synchronized(indexerWriterBlock) {//once at a time please, wait, you have enough time
				Future<Boolean> future = indexerWriterExecutor.submit(new AddDocumentCallable(document));
				try {
					future.get();
				} catch (ExecutionException e) {
					log.error("", e);
				}
			}
		}

		incrementDocumentTypeCounter(document);
		incrementFileTypeCounter(document);
		fullIndexerStatus.setNumberAvailableFolderIndexer(indexerExecutor.getPoolSize());
		fullIndexerStatus.setNumberRunningFolderIndexer(indexerExecutor.getActiveCount());
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
			fileTypeCounters.put(fileType, Integer.valueOf(intValue));
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
		documentCounters.put(documentType, Integer.valueOf(intValue));
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
		fullIndexerStatus.setDocumentCounters(documentCounters);
		fullIndexerStatus.setFileTypeCounters(fileTypeCounters);
		fullIndexerStatus.setDocumentQueueSize(0);
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
	 * Check if the indexing process is interrupted.
	 * @return  TRUE: indexing process is interrupted.
	 */
	public boolean isInterupted() {
		return stopIndexing;
	}
	
	private void resetDocumentCounters() {
		documentCounters = new Hashtable<>();
		fileTypeCounters = new Hashtable<>();		
	}
	
	private class CloseIndexCallable implements Callable<Boolean> {

		@Override
		public Boolean call() throws Exception {
			indexWriter.commit();
			indexWriter.close();
			indexWriter = null;
			return Boolean.TRUE;
		}
	}
	
	private class AddDocumentCallable implements Callable<Boolean> {
		private final Document document;
		
		public AddDocumentCallable(Document document) {
			this.document = document;
		}

		@Override
		public Boolean call() throws Exception {
			indexWriter.addDocument(document);
			fullIndexerStatus.incrementDocumentCount();
			if (indexInterval != 0 && sleepDocumentCounter++ >= documentsPerInterval) {
				sleepDocumentCounter = 0;
				Thread.sleep(indexInterval);
			} else if (stopIndexing) {
				throw new InterruptedException("Do stop indexing at element=" + indexWriter.maxDoc());
			}
			countIndexPerMinute();
			return Boolean.TRUE;
		}
	}
	
	private static class IndexerThreadFactory implements ThreadFactory {

		private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        IndexerThreadFactory(String prefix) {
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
            namePrefix = "index-" + prefix + "-" +
                          poolNumber.getAndIncrement() +
                         "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
            if (t.isDaemon()) {
                t.setDaemon(false);
            }
            if (t.getPriority() != Thread.MIN_PRIORITY) {
                t.setPriority(Thread.MIN_PRIORITY);
            }
            return t;
        }
		
	}
}
