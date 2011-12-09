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
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.webFeed.dispatching.Path;
import org.olat.modules.webFeed.managers.FeedManager;
import org.olat.modules.webFeed.models.Feed;
import org.olat.modules.webFeed.models.Item;
import org.olat.modules.webFeed.search.document.FeedItemDocument;
import org.olat.repository.RepositoryEntry;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.Indexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * The feed repository entry indexer
 * 
 * <P>
 * Initial Date: Aug 18, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class FeedRepositoryIndexer extends LogDelegator implements Indexer {

	/**
	 * @see org.olat.search.service.indexer.Indexer#checkAccess(org.olat.core.id.context.ContextEntry,
	 *      org.olat.core.id.context.BusinessControl, org.olat.core.id.Identity,
	 *      org.olat.core.id.Roles)
	 */
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		return true;
	}

	/**
	 * @see org.olat.search.service.indexer.Indexer#doIndex(org.olat.search.service.SearchResourceContext,
	 *      java.lang.Object, org.olat.search.service.indexer.OlatFullIndexer)
	 */
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

			// Set the document type, e.g. type.repository.entry.FileResource.BLOG
			searchResourceContext.setDocumentType(getDocumentType());
			searchResourceContext.setParentContextType(getDocumentType());
			searchResourceContext.setParentContextName(repoEntryName);

			// Make sure images are displayed properly
			// TODO:GW It's only working for public resources, because base url is
			// personal. -> fix
			String mapperBaseURL = Path.getFeedBaseUri(feed, null, null, null);
			Filter mediaUrlFilter = FilterFactory.getBaseURLToMediaRelativeURLFilter(mapperBaseURL);

			// Only index items. Feed itself is indexed by RepositoryEntryIndexer.
			if (isLogDebugEnabled()) {
				logDebug("PublishedItems size=" + feed.getPublishedItems().size());
			}
			for (Item item : feed.getPublishedItems()) {
				OlatDocument itemDoc = new FeedItemDocument(item, searchResourceContext, mediaUrlFilter);
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
