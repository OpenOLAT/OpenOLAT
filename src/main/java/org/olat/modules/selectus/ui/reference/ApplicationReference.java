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
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReference {
	
	private final Reference reference;
	private final FormLink sendLink;
	
	public ApplicationReference(Reference reference, FormLink sendLink) {
		this.reference = reference;
		this.sendLink = sendLink;
	}

	public Reference getReference() {
		return reference;
	}

	public FormLink getSendLink() {
		return sendLink;
	}
}
