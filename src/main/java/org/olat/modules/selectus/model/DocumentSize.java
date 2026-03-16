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
public class DocumentSize {
	
	private final DocumentEnum document;
	private final int size;
	
	public DocumentSize(DocumentEnum document, int size) {
		this.document = document;
		this.size = size;
	}

	public DocumentEnum getDocument() {
		return document;
	}

	public int getSize() {
		return size;
	}

	@Override
	public int hashCode() {
		return document.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof DocumentSize) {
			DocumentSize size = (DocumentSize)obj;
			return document.equals(size.document);
		}
		return false;
	}
}
