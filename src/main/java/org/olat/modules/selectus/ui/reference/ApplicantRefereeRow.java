/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import org.olat.core.gui.components.form.flexible.elements.FormLink;

import org.olat.modules.selectus.model.Reference;

/**
 * 
 * Initial date: 20 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicantRefereeRow {
	
	private final Reference reference;
	private FormLink toolsLink;
	
	public ApplicantRefereeRow(Reference reference) {
		this.reference = reference;
	}
	
	public Reference getReference() {
		return reference;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}
}
