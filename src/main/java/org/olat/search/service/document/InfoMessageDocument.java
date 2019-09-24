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
package org.olat.search.service.document;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.commons.info.InfoMessage;
import org.olat.core.logging.Tracing;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

public class InfoMessageDocument extends OlatDocument {

	private static final long serialVersionUID = 4632827059160372302L;
	private static final Logger log = Tracing.createLoggerFor(InfoMessageDocument.class);

  //Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE = "type.info.message";

	public InfoMessageDocument() {
		super();
	}
	
	public static Document createDocument(SearchResourceContext searchResourceContext, InfoMessage message) {		
		InfoMessageDocument messageDocument = new InfoMessageDocument();

		messageDocument.setTitle(message.getTitle());
		messageDocument.setContent(message.getMessage());
		if(message.getAuthor() != null) {
			messageDocument.setAuthor(message.getAuthor().getName());
		}
		messageDocument.setCreatedDate(message.getCreationDate());
		messageDocument.setLastChange(message.getCreationDate());
		messageDocument.setResourceUrl(searchResourceContext.getResourceUrl());
		if ( (searchResourceContext.getDocumentType() != null) && !searchResourceContext.getDocumentType().equals("")) {
			// Document is already set => take this value
			messageDocument.setDocumentType(searchResourceContext.getDocumentType());
		} else {
  		messageDocument.setDocumentType(TYPE);
		}
		messageDocument.setCssIcon("o_infomsg_icon");
		messageDocument.setParentContextType(searchResourceContext.getParentContextType());
		messageDocument.setParentContextName(searchResourceContext.getParentContextName());

		if (log.isDebugEnabled()) log.debug(messageDocument.toString());
		return messageDocument.getLuceneDocument();
	}
}
