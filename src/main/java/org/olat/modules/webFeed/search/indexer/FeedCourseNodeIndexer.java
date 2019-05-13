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

import org.apache.lucene.document.Document;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.webFeed.Feed;
import org.olat.modules.webFeed.Item;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.search.document.FeedItemDocument;
import org.olat.modules.webFeed.search.document.FeedNodeDocument;
import org.olat.repository.RepositoryEntry;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.CourseNodeDocument;
import org.olat.search.service.indexer.DefaultIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;
import org.olat.search.service.indexer.repository.course.CourseNodeIndexer;

/**
 * Indexer for feed course nodes
 * 
 * <P>
 * Initial Date: Aug 18, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class FeedCourseNodeIndexer extends DefaultIndexer implements CourseNodeIndexer {

	private static final Logger log = Tracing.createLoggerFor(FeedRepositoryIndexer.class);

	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer)
	throws IOException, InterruptedException {
		//
	}

	/**
	 * @see org.olat.search.service.indexer.Indexer#doIndex(org.olat.search.service.SearchResourceContext,
	 *      java.lang.Object, org.olat.search.service.indexer.OlatFullIndexer)
	 */
	@Override
	public void doIndex(SearchResourceContext courseResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexer)
			throws IOException, InterruptedException {
		
		SearchResourceContext courseNodeResourceContext = createSearchResourceContext(courseResourceContext, courseNode, getDocumentType());
		Document document = CourseNodeDocument.createDocument(courseNodeResourceContext, courseNode);
		indexer.addDocument(document);
		
		RepositoryEntry repositoryEntry = courseNode.getReferencedRepositoryEntry();
		if(repositoryEntry != null) {
			// used for log messages
			String repoEntryName = "*name not available*";
			try {
				repoEntryName = repositoryEntry.getDisplayname();
				if (log.isDebugEnabled()) {
					log.info("Indexing: " + repoEntryName);
				}
				Feed feed = FeedManager.getInstance().loadFeed(repositoryEntry.getOlatResource());
				List<Item> publishedItems = FeedManager.getInstance().loadPublishedItems(feed);

				// Create the olatDocument for the feed course node itself
				OlatDocument feedNodeDoc = new FeedNodeDocument(feed, courseNodeResourceContext);
				indexer.addDocument(feedNodeDoc.getLuceneDocument());
				
				// Only index items. FeedImpl itself is indexed by RepositoryEntryIndexer.
				for (Item item : publishedItems) {
					OlatDocument itemDoc = new FeedItemDocument(item, courseNodeResourceContext);
					indexer.addDocument(itemDoc.getLuceneDocument());
				}
			} catch (NullPointerException e) {
				log.error("Error indexing feed:" + repoEntryName, e);
			}
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
