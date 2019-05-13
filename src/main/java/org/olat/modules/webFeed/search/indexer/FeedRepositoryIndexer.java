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
package org.olat.modules.webFeed.search.indexer;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.search.document.FeedItemDocument;
import org.olat.repository.RepositoryEntry;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.DefaultIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * The feed repository entry indexer
 * 
 * <P>
 * Initial Date: Aug 18, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class FeedRepositoryIndexer extends DefaultIndexer {

	private static final Logger log = Tracing.createLoggerFor(FeedRepositoryIndexer.class);

	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer) throws IOException,
			InterruptedException {
		RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
		// used for log messages
		String repoEntryName = "*name not available*";
		try {
			repoEntryName = repositoryEntry.getDisplayname();
			if (log.isDebugEnabled()) {
				log.debug("Indexing: " + repoEntryName);
			}
			Feed feed = FeedManager.getInstance().loadFeed(repositoryEntry.getOlatResource());
			if(feed != null) {
				// Only index items. Feed itself is indexed by RepositoryEntryIndexer.
				List<Item> publishedItems = FeedManager.getInstance().loadPublishedItems(feed);
				if (log.isDebugEnabled()) {
					log.debug("PublishedItems size=" + publishedItems.size());
				}
				for (Item item : publishedItems) {
					SearchResourceContext feedContext = new SearchResourceContext(searchResourceContext);
					feedContext.setDocumentType(getDocumentType());
					OlatDocument itemDoc = new FeedItemDocument(item, feedContext);
					indexer.addDocument(itemDoc.getLuceneDocument());
				}
			}
		} catch (NullPointerException e) {
			log.error("Error indexing feed:" + repoEntryName, e);
		}
	}

	/**
	 * @return The I18n key representing the document type
	 */
	protected abstract String getDocumentType();
}
