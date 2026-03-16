/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;


/**
 * 
 * Initial date: 11.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DocumentOption {
	
	private final DocumentEnum doc;
	private final int maxSize;
	
	public DocumentOption(DocumentEnum doc, int maxSize) {
		this.doc = doc;
		this.maxSize = maxSize;
	}

	public DocumentEnum getDoc() {
		return doc;
	}

	public int getMaxSize() {
		return maxSize;
	}
}
