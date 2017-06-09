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
package org.olat.modules.webFeed.search.document;

import org.olat.modules.webFeed.Feed;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * OlatDocument holding feed item information for search indexing.
 * 
 * <P>
 * Initial Date: Aug 18, 2009 <br>
 * 
 * @author gwassmann
 */
public class FeedNodeDocument extends OlatDocument {

	private static final long serialVersionUID = 5585883273458311252L;

	public FeedNodeDocument(Feed feed, SearchResourceContext searchResourceContext) {
		super();
		setTitle(feed.getTitle());
		setAuthor(feed.getAuthor());
		setDescription(feed.getDescription());
		setLastChange(feed.getLastModified());
		setResourceUrl(searchResourceContext.getResourceUrl());
		setDocumentType(searchResourceContext.getDocumentType());
		setParentContextType(searchResourceContext.getParentContextType());
		setParentContextName(searchResourceContext.getParentContextName());
		if (getDocumentType().equals("type.repository.entry.FileResource.PODCAST") || getDocumentType().equals("type.course.node.podcast"))
			setCssIcon("o_podcast_icon");
		else if ((getDocumentType().equals("type.repository.entry.FileResource.BLOG")) || getDocumentType().equals("type.course.node.blog"))
			setCssIcon("o_blog_icon");
	}
}
