/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui;

import org.olat.modules.selectus.model.Attachment;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceStatus;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  3 mars 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ReferenceDocument {
	
	private final String fullName;
	private final Reference reference;
	private final String relativePath;
	
	public ReferenceDocument(Reference reference, String fullName) {
		this.reference = reference;
		this.fullName = fullName;
		
		Attachment letter = reference.getLetter();
		String attachmentPseudoHash = "0_zero";
		if(letter != null) {
			attachmentPseudoHash = letter.getVersion() + "_" + (letter.getSize() == null ? "zero" : letter.getSize());
		}
		relativePath = "/" + reference.getKey() + "/" + attachmentPseudoHash + "/" + (reference.getLetter() == null ? "letter.pdf" : reference.getLetter().getName()) + "?" + attachmentPseudoHash;
	}
	
	public boolean isSubmitted() {
		return reference.getReferenceStatus() == ReferenceStatus.submitted;
	}
	
	public String getReferenceStatus() {
		return reference.getReferenceStatus().name();
	}
	
	public String getFullName() {
		return fullName;
	}

	public String getInstitution() {
		return reference.getInstitution();
	}

	public String getRelativePath() {
		return relativePath;
	}
	
	public String getCssClass() {
		switch(reference.getReferenceStatus()) {
			case notSent: return "o_reference_status_filter o_not_sent";
			case sentAwaiting: return "o_reference_status_filter o_sent_awaiting";
			case submitted: return "o_reference_status_filter o_submitted";
			case late: return "<i class='o_icon o_reference_status_filter o_late";
			default: return "";
		}
	}
}
