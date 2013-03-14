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

import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.manager.QuestionItemDocumentFactory;
import org.olat.modules.qpool.model.QItemDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * 
 * Initial date: 25.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionItemIndexer implements LifeIndexer {
	private static final OLog log = Tracing.createLoggerFor(QuestionItemIndexer.class);
	private static final int BATCH_SIZE = 100;

	@Override
	public String getSupportedTypeName() {
		return QItemDocument.TYPE;
	}

	@Override
	public void indexDocument(Long key, LifeFullIndexer indexWriter) {
		QuestionItemDocumentFactory docFactory = CoreSpringFactory.getImpl(QuestionItemDocumentFactory.class);
		
		SearchResourceContext ctxt = new SearchResourceContext();
		Document doc = docFactory.createDocument(ctxt, key);
		indexWriter.addDocument(doc);	
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
			List<QuestionItem> items;
			do {
				items = qpoolService.getAllItems(counter, BATCH_SIZE);
				for(QuestionItem item:items) {
					Document doc = docFactory.createDocument(ctxt, item);
					indexWriter.addDocument(doc, writer);
				}
				counter += items.size();
			} while(items.size() == BATCH_SIZE);
			
		} catch (Exception e) {
			log.error("", e);
		} finally {
			indexWriter.releaseWriter(writer);
		}
	}
}
