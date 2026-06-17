/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
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
