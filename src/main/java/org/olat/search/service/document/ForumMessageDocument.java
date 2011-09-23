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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.search.service.document;

import org.apache.lucene.document.Document;
import org.olat.core.commons.services.search.OlatDocument;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.fo.Message;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class ForumMessageDocument extends OlatDocument {
	private static final OLog log = Tracing.createLoggerFor(ForumMessageDocument.class);

  //Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE = "type.forum.message";

	public ForumMessageDocument() {
		super();
	}

	public static Document createDocument(SearchResourceContext searchResourceContext, Message message) {		
		ForumMessageDocument forumMessageDocument = new ForumMessageDocument();

		forumMessageDocument.setTitle(message.getTitle());
		String msgContent = FilterFactory.getHtmlTagAndDescapingFilter().filter(message.getBody());
		forumMessageDocument.setContent(msgContent);
		forumMessageDocument.setAuthor(message.getCreator().getName());
		forumMessageDocument.setCreatedDate(message.getCreationDate());
		forumMessageDocument.setLastChange(message.getLastModified());
		forumMessageDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		if ( (searchResourceContext.getDocumentType() != null) && !searchResourceContext.getDocumentType().equals("")) {
			// Document is already set => take this value
			forumMessageDocument.setDocumentType(searchResourceContext.getDocumentType());
		} else {
  		forumMessageDocument.setDocumentType(TYPE);
		}
		forumMessageDocument.setCssIcon("o_fo_icon");
		forumMessageDocument.setParentContextType(searchResourceContext.getParentContextType());
		forumMessageDocument.setParentContextName(searchResourceContext.getParentContextName());

		// TODO: chg: What is with message attributes ?
		// ?? Identity modifier = message.getModifier();
		// ?? message.getParent();
		// ?? message.getThreadtop();
		
		if (log.isDebug()) log.debug(forumMessageDocument.toString());
		return forumMessageDocument.getLuceneDocument();
	}
}
