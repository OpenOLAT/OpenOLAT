/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.document;

import java.io.InputStream;

import org.olat.core.util.FileUtils;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  28 sept. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PDFDocument {
	
	private final Type type;
	private final String name;
	private InputStream summaryStream;
	private InputStream documentStream;
	
	
	public PDFDocument(Type type, String name, InputStream summaryStream) {
		this(type, name, summaryStream, null);
	}
	
	public PDFDocument(Type type, String name, InputStream summaryStream, InputStream documentStream) {
		this.type = type;
		this.name = name;
		this.summaryStream = summaryStream;
		this.documentStream = documentStream;
	}
	
	public Type getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	public InputStream getSummaryStream() {
		return summaryStream;
	}

	public InputStream getDocumentStream() {
		return documentStream;
	}
	
	public void close() {
		FileUtils.closeSafely(summaryStream);
		FileUtils.closeSafely(documentStream);
		summaryStream = null;
		documentStream = null;
	}

	public enum Type {
		INTRODUCTION,
		DOCUMENT
	}
}
