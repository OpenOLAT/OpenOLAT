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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.search.service.document;

import org.apache.lucene.document.Document;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.modules.fo.Message;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class ForumMessageDocument extends OlatDocument {

	private static final long serialVersionUID = -1668747274393652050L;

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
		if(StringHelper.containsNonWhitespace(message.getPseudonym())) {
			forumMessageDocument.setAuthor(message.getPseudonym());
		} else if(message.getCreator() != null) {
			forumMessageDocument.setAuthor(message.getCreator().getName());
		}
		
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
		return forumMessageDocument.getLuceneDocument();
	}
}
