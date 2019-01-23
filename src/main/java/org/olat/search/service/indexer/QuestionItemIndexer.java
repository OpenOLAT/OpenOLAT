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

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.manager.QuestionItemDocumentFactory;
import org.olat.modules.qpool.model.QItemDocument;
import org.olat.search.service.SearchResourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 25.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("questionItemIndexer")
public class QuestionItemIndexer implements LifeIndexer {
	private static final OLog log = Tracing.createLoggerFor(QuestionItemIndexer.class);
	private static final int BATCH_SIZE = 100;
	
	@Autowired
	private DB dbInstance;

	@Override
	public String getSupportedTypeName() {
		return QItemDocument.TYPE;
	}

	@Override
	public void indexDocument(List<Long> keyList, LifeFullIndexer indexWriter) {
		QuestionItemDocumentFactory docFactory = CoreSpringFactory.getImpl(QuestionItemDocumentFactory.class);
		
		List<Document> docs = new ArrayList<>(keyList.size());
		for(Long key:keyList) {
			SearchResourceContext ctxt = new SearchResourceContext();
			Document doc = docFactory.createDocument(ctxt, key);
			docs.add(doc);
		}
		indexWriter.addDocuments(docs);	
	}

	@Override
	public void deleteDocument(Long key, LifeFullIndexer indexWriter) {
		QuestionItemDocumentFactory docFactory = CoreSpringFactory.getImpl(QuestionItemDocumentFactory.class);
		String resourceUrl = docFactory.getResourceUrl(key);
		indexWriter.deleteDocument(resourceUrl);	
	}

	@Override
	public void fullIndex(LifeFullIndexer indexWriter) {
		QPoolService qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		QuestionItemDocumentFactory docFactory = CoreSpringFactory.getImpl(QuestionItemDocumentFactory.class);
		SearchResourceContext ctxt = new SearchResourceContext();
		
		IndexWriter writer = null;
		try {
			writer = indexWriter.getAndLockWriter();
			
			int counter = 0;
			List<QuestionItemFull> items;
			do {
				items = qpoolService.getAllItems(counter, BATCH_SIZE);
				for(QuestionItemFull item:items) {
					Document doc = docFactory.createDocument(ctxt, item);
					indexWriter.addDocument(doc, writer);
				}
				counter += items.size();
				indexWriter.getStatus().addDocumentCount(items.size());
				dbInstance.commitAndCloseSession();
			} while(items.size() == BATCH_SIZE);
			log.info(counter + " question items indexed.");
		} catch (Exception e) {
			log.error("", e);
		} finally {
			indexWriter.releaseWriter(writer);
			dbInstance.commitAndCloseSession();
		}
	}
}
