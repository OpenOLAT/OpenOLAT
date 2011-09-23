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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.webFeed.search.indexer;


/**
 * Indexer for blog repository entry
 * 
 * <P>
 * Initial Date: Aug 19, 2009 <br>
 * 
 * @author gwassmann
 */
public class PodcastCourseNodeIndexer extends FeedCourseNodeIndexer {

	public static final String TYPE = "type.course.node.podcast";
	private static final String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.PodcastCourseNode";

	/**
	 * @see org.olat.modules.webFeed.search.indexer.FeedRepositoryIndexer#getSupportedTypeName()
	 */
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}

	/**
	 * @see org.olat.modules.webFeed.search.indexer.FeedRepositoryIndexer#getDocumentType()
	 */
	protected String getDocumentType() {
		return TYPE;
	}
}
