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
package org.olat.repository.manager;

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.RepositoryEntryDocument;
import org.olat.search.service.indexer.LifeFullIndexer;
import org.olat.search.service.indexer.LifeIndexer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * The life indexer for the repository entry is disabled.
 * It's not used anymore.
 * 
 * Initial date: 13.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
//@Service
public class RepositoryEntryLifeIndexer implements LifeIndexer {
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryLifeIndexer.class);
	private static final int BATCH_SIZE = 100;
	
	@Autowired
	private LifeFullIndexer lifeIndexer;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDAO;
	@Autowired
	private RepositoryEntryDocumentFactory documentFactory;
	
	@PostConstruct
	public void init() {
		lifeIndexer.addIndexer(this);
	}

	@Override
	public String getSupportedTypeName() {
		return RepositoryEntryDocument.TYPE;
	}

	@Override
	public void indexDocument(List<Long> keyList, LifeFullIndexer indexWriter) {
		SearchResourceContext ctxt = new SearchResourceContext();
		List<Document> docs = new ArrayList<>(keyList.size());
		for(Long key:keyList) {
			Document doc = documentFactory.createDocument(ctxt, key);
			docs.add(doc);
		}
		indexWriter.addDocuments(docs);	
	}

	@Override
	public void deleteDocument(Long key, LifeFullIndexer indexWriter) {
		String resourceUrl = documentFactory.getResourceUrl(key);
		indexWriter.deleteDocument(resourceUrl);
	}
	
	@Override
	public void fullIndex(LifeFullIndexer indexWriter) {
		SearchResourceContext ctxt = new SearchResourceContext();
		
		IndexWriter writer = null;
		try {
			writer = indexWriter.getAndLockWriter();
			
			int counter = 0;
			List<RepositoryEntry> items;
			do {
				items = repositoryEntryDAO.getAllRepositoryEntries(counter, BATCH_SIZE);
				for(RepositoryEntry item:items) {
					Document doc = documentFactory.createDocument(ctxt, item);
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
