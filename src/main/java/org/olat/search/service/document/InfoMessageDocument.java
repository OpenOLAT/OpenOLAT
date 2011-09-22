package org.olat.search.service.document;

import org.apache.lucene.document.Document;
import org.olat.commons.info.model.InfoMessage;
import org.olat.core.commons.services.search.OlatDocument;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.FilterFactory;
import org.olat.search.service.SearchResourceContext;

public class InfoMessageDocument extends OlatDocument {
	private static final OLog log = Tracing.createLoggerFor(InfoMessageDocument.class);

  //Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public final static String TYPE = "type.info.message";

	public InfoMessageDocument() {
		super();
	}
	
	public static Document createDocument(SearchResourceContext searchResourceContext, InfoMessage message) {		
		InfoMessageDocument messageDocument = new InfoMessageDocument();

		messageDocument.setTitle(message.getTitle());
		messageDocument.setContent(message.getMessage());
		messageDocument.setAuthor(message.getAuthor().getName());
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

		if (log.isDebug()) log.debug(messageDocument.toString());
		return messageDocument.getLuceneDocument();
	}
}
