/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.search.service.indexer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.LogDocMergePolicy;
import org.apache.lucene.index.LogMergePolicy;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.search.SearchModule;
import org.olat.search.model.AbstractOlatDocument;

/**
 * 
 * Initial date: 04.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class JmsIndexer implements MessageListener, LifeFullIndexer, ConfigOnOff {
	private static final int INDEX_MERGE_FACTOR = 1000;
	private static final Logger log = Tracing.createLoggerFor(JmsIndexer.class);
	
	private Queue jmsQueue;
	private Session indexerSession;
	private MessageConsumer consumer;
	private ConnectionFactory connectionFactory;
	private QueueConnection connection;
	
	private String enabled;
	private CoordinatorManager coordinatorManager;

	private String permanentIndexPath;
	private DirectoryReader reader;
	private IndexWriterHolder permanentIndexWriter;
	
	private double ramBufferSizeMB;
	private boolean indexingNode;

	private FullIndexerStatus fullIndexerStatus;

	private List<LifeIndexer> indexers = new ArrayList<>();
	
	public JmsIndexer(SearchModule searchModuleConfig, CoordinatorManager coordinatorManager) {
		indexingNode = searchModuleConfig.isSearchServiceEnabled();
		ramBufferSizeMB = searchModuleConfig.getRAMBufferSizeMB();
		permanentIndexPath = searchModuleConfig.getFullPermanentIndexPath();
		fullIndexerStatus = new FullIndexerStatus(0);
		this.coordinatorManager = coordinatorManager;
	}

	public Queue getJmsQueue() {
		return jmsQueue;
	}
	
	/**
	 * [Used by Spring]
	 * @param jmsQueue
	 */
	public void setJmsQueue(Queue jmsQueue) {
		this.jmsQueue = jmsQueue;
	}

	/**
	 * [Used by Spring]
	 * @param connectionFactory
	 */
	public void setConnectionFactory(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
	}
	
	/**
	 * [used by Spring]
	 * @param searchService
	 */
	public void setSearchServiceEnabled(String enabled) {
		this.enabled = enabled;
	}

	@Override
	public boolean isEnabled() {
		return enabled != null && "enabled".equalsIgnoreCase(enabled);
	}

	public void setIndexers(List<LifeIndexer> indexers) {
		if(indexers != null) {
			for(LifeIndexer indexer:indexers){
				addIndexer(indexer);
			}
		}
	}

	@Override
	public void addIndexer(LifeIndexer indexer) {
		indexers.add(indexer);
	}
	
	public List<LifeIndexer> getIndexerByType(String type) {
		List<LifeIndexer> indexerByType = new ArrayList<>();
		for(LifeIndexer indexer:indexers) {
			if(type.equals(indexer.getSupportedTypeName())) {
				indexerByType.add(indexer);
			}
		}
		return indexerByType;
	}

	/**
	 * [used by Spring]
	 * @throws JMSException
	 */
	public void springInit() throws JMSException {
		initDirectory();
		initQueue();
	}
	
	public void initQueue() throws JMSException {
		connection = (QueueConnection)connectionFactory.createConnection();
		connection.start();
		log.info("springInit: JMS connection started with connectionFactory={}", connectionFactory);

		if(indexingNode) {
			//listen to the queue only if indexing node
			indexerSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			consumer = indexerSession.createConsumer(jmsQueue);
			consumer.setMessageListener(this);
		}
	}
	
	public void initDirectory() {
		try {
			File tempIndexDir = new File(permanentIndexPath);
			Directory indexPath = FSDirectory.open(tempIndexDir.toPath());
			if(indexingNode) {
				permanentIndexWriter = new IndexWriterHolder(indexPath, this);
				boolean created = permanentIndexWriter.ensureIndexExists();
				if(created) {
					IndexerEvent event = new IndexerEvent(IndexerEvent.INDEX_CREATED);
					coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(event, IndexerEvent.INDEX_ORES);
				}
			}
			reader = DirectoryReader.open(indexPath);
		} catch (IOException e) {
			log.error("", e);
		}
	}
	
	public LogMergePolicy newLogMergePolicy() {
		LogMergePolicy logmp = new LogDocMergePolicy();
		logmp.setCalibrateSizeByDeletes(true);
		logmp.setMergeFactor(INDEX_MERGE_FACTOR);
		return logmp;
	}
	
	public IndexWriterConfig newIndexWriterConfig() {
		Analyzer analyzer = new StandardAnalyzer();
		IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
		indexWriterConfig.setMergePolicy(newLogMergePolicy());
		indexWriterConfig.setRAMBufferSizeMB(ramBufferSizeMB);// for better performance set to 48MB (see lucene docu 'how to make indexing faster")
		indexWriterConfig.setOpenMode(OpenMode.CREATE_OR_APPEND);
		return indexWriterConfig;
	}
	
	/**
	 * [used by Spring]
	 */
	public void stop() {
		closeQueue();
		closeWriter();
	}
	
	public void closeQueue() {
		if(consumer != null) {
			try {
				consumer.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
		if(connection != null) {
			try {
				indexerSession.close();
				connection.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
	}
	
	public void closeWriter() {
		try {
			permanentIndexWriter.close();
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	@Override
	public void fullIndex() {
		log.info("start full reindex of life index");
		fullIndexerStatus.indexingStarted();
		for(LifeIndexer indexer:indexers) {
			indexer.fullIndex(this);
		}
		fullIndexerStatus.indexingFinished();
		log.info("end full reindex of life index");
	}

	@Override
	public FullIndexerStatus getStatus() {
		return fullIndexerStatus;
	}

	@Override
	public void indexDocument(String type, Long key) {
		sendMessage(new JmsIndexWork(JmsIndexWork.INDEX, type, key));
	}

	@Override
	public void indexDocument(String type, List<Long> keyList) {
		sendMessage(new JmsIndexWork(JmsIndexWork.INDEX, type, keyList));
	}
	
	private void sendMessage(JmsIndexWork workUnit) {
		QueueSender sender;
		QueueSession session;
		try {
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			ObjectMessage message = session.createObjectMessage();
			message.setObject(workUnit);
			
			sender = session.createSender(getJmsQueue());
			sender.send(message, DeliveryMode.NON_PERSISTENT, 3, 120000);
			session.close();
		} catch (JMSException e) {
			log.error("", e );
		}
	}

	@Override
	public void deleteDocument(String type, Long key) {
		indexDocument(type, Collections.singletonList(key));
	}

	@Override
	public void onMessage(Message message) {
		if(message instanceof ObjectMessage) {
			try {
				ObjectMessage objMsg = (ObjectMessage)message;
				JmsIndexWork workUnit = (JmsIndexWork)objMsg.getObject();
				if(JmsIndexWork.INDEX.equals(workUnit.getAction())) {
					doIndex(workUnit);
				} else if(JmsIndexWork.DELETE.equals(workUnit.getAction())) {
					doDelete(workUnit);
				}
				message.acknowledge();
			} catch (JMSException e) {
				log.error("", e);
			} finally {
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
	}
	
	private void doIndex(JmsIndexWork workUnit) {
		if(isEnabled()) {
			String type = workUnit.getIndexType();
			List<LifeIndexer> lifeIndexers = getIndexerByType(type);
			for(LifeIndexer indexer:lifeIndexers) {
				indexer.indexDocument(workUnit.getKeyList(), this);
			}
		}
	}
	
	private void doDelete(JmsIndexWork workUnit) {
		if(isEnabled()) {
			String type = workUnit.getIndexType();
			List<LifeIndexer> lifeIndexers = getIndexerByType(type);
			for(LifeIndexer indexer:lifeIndexers) {
				if(workUnit.getKeyList() != null && !workUnit.getKeyList().isEmpty()) {
					for(Long key:workUnit.getKeyList()) {
						indexer.deleteDocument(key, this);
					}
				}
			}
		}
	}
	
	private DirectoryReader getReader() throws IOException {
		if(reader == null) {
			File tempIndexDir = new File(permanentIndexPath);
			Directory indexPath = FSDirectory.open(tempIndexDir.toPath());
			reader = DirectoryReader.open(indexPath);
		} else {
			DirectoryReader newReader = DirectoryReader.openIfChanged(reader);
			if(newReader != null) {
				reader = newReader;
			}
		}
		return reader;
	}

	@Override
	public IndexWriter getAndLockWriter() throws IOException {
		return permanentIndexWriter.getAndLock();
	}

	@Override
	public void releaseWriter(IndexWriter writer) {
		permanentIndexWriter.release(writer);
	}

	@Override
	public void deleteDocument(String resourceUrl) {
		IndexWriter writer = null;
		try {
			Term uuidTerm = new Term(AbstractOlatDocument.RESOURCEURL_FIELD_NAME, resourceUrl);
			writer = permanentIndexWriter.getAndLock();
			writer.deleteDocuments(uuidTerm);
		} catch (IOException e) {
			log.error("", e);
		} finally {
			permanentIndexWriter.release(writer);
		}
	}
	
	/**
	 * Add or update a lucene document in the permanent index.
	 * @param uuid
	 * @param document
	 */
	@Override
	public void addDocuments(List<Document> documents) {
		if(documents == null || documents.isEmpty()) return;//nothing to do
		
		IndexWriter writer = null;
		try {
			DirectoryReader currentReader = getReader();
			IndexSearcher searcher = new IndexSearcher(currentReader);
			writer = permanentIndexWriter.getAndLock();
			
			for(Document document:documents) {
				if(document != null) {
					String resourceUrl = document.get(AbstractOlatDocument.RESOURCEURL_FIELD_NAME);
					Term uuidTerm = new Term(AbstractOlatDocument.RESOURCEURL_FIELD_NAME, resourceUrl);
					TopDocs hits = searcher.search(new TermQuery(uuidTerm), 10);
					if(hits.totalHits > 0) {
						writer.updateDocument(uuidTerm, document);
					} else {
						writer.addDocument(document);
					}
				}
			}
		} catch (IOException e) {
			log.error("", e);
		} finally {
			permanentIndexWriter.release(writer);
		}
	}

	@Override
	public void addDocument(Document document, IndexWriter writer) {
		try {
			String resourceUrl = document.get(AbstractOlatDocument.RESOURCEURL_FIELD_NAME);
			Term uuidTerm = new Term(AbstractOlatDocument.RESOURCEURL_FIELD_NAME, resourceUrl);
			DirectoryReader currentReader = getReader();
			IndexSearcher searcher = new IndexSearcher(currentReader);
			TopDocs hits = searcher.search(new TermQuery(uuidTerm), 10);
			if(hits.totalHits > 0) {
				writer.updateDocument(uuidTerm, document);
			} else {
				writer.addDocument(document);
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
