/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  3 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationDocument {
	private final String label;
	private final String cssClass;
	private final String relativePath;
	private final FormLink delLink;
	private boolean image;
	
	public ApplicationDocument(String label, String relativePath) {
		this(label, "o_filetype_pdf", relativePath);
	}
	
	public ApplicationDocument(String label, String cssClass, String relativePath) {
		this(label, cssClass, relativePath, null);
	}
	
	public ApplicationDocument(String label, String cssClass, String relativePath, FormLink delLink) {
		this.label = label;
		this.cssClass = cssClass;
		this.relativePath = relativePath;
		this.delLink = delLink;
	}

	public String getLabel() {
		return label;
	}

	public String getRelativePath() {
		return relativePath;
	}
	
	public String getCssClass() {
		return cssClass;
	}

	public FormLink getDelLink() {
		return delLink;
	}

	public boolean isImage() {
		return image;
	}

	public void setImage(boolean image) {
		this.image = image;
	}
}
