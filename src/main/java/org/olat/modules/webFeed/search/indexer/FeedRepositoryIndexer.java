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

import org.olat.core.commons.services.search.OlatDocument;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
import org.olat.modules.webFeed.search.document.FeedItemDocument;
import org.olat.repository.RepositoryEntry;
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

	/**
	 * @see org.olat.search.service.indexer.Indexer#doIndex(org.olat.search.service.SearchResourceContext,
	 *      java.lang.Object, org.olat.search.service.indexer.OlatFullIndexer)
	 */
	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer) throws IOException,
			InterruptedException {
		RepositoryEntry repositoryEntry = (RepositoryEntry) parentObject;
		// used for log messages
		String repoEntryName = "*name not available*";
		try {
			repoEntryName = repositoryEntry.getDisplayname();
			if (isLogDebugEnabled()) {
				logDebug("Indexing: " + repoEntryName);
			}
			Feed feed = FeedManager.getInstance().getFeed(repositoryEntry.getOlatResource());

			// Only index items. Feed itself is indexed by RepositoryEntryIndexer.
			if (isLogDebugEnabled()) {
				logDebug("PublishedItems size=" + feed.getPublishedItems().size());
			}
			for (Item item : feed.getPublishedItems()) {
				SearchResourceContext feedContext = new SearchResourceContext(searchResourceContext);
				feedContext.setDocumentType(getDocumentType());
				OlatDocument itemDoc = new FeedItemDocument(item, feedContext);
				indexer.addDocument(itemDoc.getLuceneDocument());
			}
		} catch (NullPointerException e) {
			logError("Error indexing feed:" + repoEntryName, e);
		}

	}

	/**
	 * @see org.olat.search.service.indexer.Indexer#getSupportedTypeName()
	 */
	public abstract String getSupportedTypeName();

	/**
	 * @return The I18n key representing the document type
	 */
	protected abstract String getDocumentType();
}
