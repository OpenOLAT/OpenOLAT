/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

import org.olat.modules.selectus.DocumentEnum;

/**
 * 
 * Initial date: 30 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentName {
	
	private final DocumentEnum document;
	private final String name;
	
	public DocumentName(DocumentEnum document, String name) {
		this.document = document;
		this.name = name;
	}
	
	public DocumentEnum getDocument() {
		return document;
	}
	
	public String getName() {
		return name;
	}
}
