package org.olat.core.commons.services.pdf;

/**
 * 
 * Initial date: 16 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PdfDocument {
	
	private final String name;
	private final byte[] content;
	
	public PdfDocument(String name, byte[] content) {
		this.name = name;
		this.content = content;
	}
	
	public String getName() {
		return name;
	}

	public byte[] getContent() {
		return content;
	}
}
