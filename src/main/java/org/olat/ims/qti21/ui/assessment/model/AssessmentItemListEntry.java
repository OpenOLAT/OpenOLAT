/**
 * <a href="http://www.openolat.org">
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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.ui.assessment.model;

import java.math.BigDecimal;

import org.olat.core.id.Identity;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestSession;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;

/**
 * 
 * Initial date: 1 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemListEntry {
	
	private final String label;
	private String labelCssClass;
	private String title;
	private String titleCssClass;
	private final Identity assessedIdentity;
	private final AssessmentItemRef itemRef;
	private AssessmentItemSession itemSession;
	private AssessmentTestSession testSession;
	
	
	public AssessmentItemListEntry(Identity assessedIdentity, 
			AssessmentTestSession testSession, AssessmentItemSession itemSession, 
			AssessmentItemRef itemRef, String label, String labelCssClass) {
		this.itemSession = itemSession;
		this.testSession = testSession;
		this.itemRef = itemRef;
		this.label = label;
		this.labelCssClass = labelCssClass;
		this.assessedIdentity = assessedIdentity;
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getLabelCssClass() {
		return labelCssClass;
	}
	
	public void setLabelCssClass(String cssClass) {
		labelCssClass = cssClass;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitleCssClass() {
		return titleCssClass;
	}

	public void setTitleCssClass(String titleCssClass) {
		this.titleCssClass = titleCssClass;
	}

	public AssessmentItemRef getItemRef() {
		return itemRef;
	}

	public AssessmentItemSession getItemSession() {
		return itemSession;
	}
	
	public void setItemSession(AssessmentItemSession itemSession) {
		this.itemSession = itemSession;
	}

	public AssessmentTestSession getTestSession() {
		return testSession;
	}
	
	public void setTestSession(AssessmentTestSession testSession) {
		this.testSession = testSession;
	}

	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}
	
	public BigDecimal getFinalScore() {
		if(itemSession == null) return null;
		
		if(itemSession.getManualScore() != null) {
			return itemSession.getManualScore();
		}
		return itemSession.getScore();
	}
	
	public BigDecimal getManualScore() {
		return itemSession == null ? null : itemSession.getManualScore();
	}
	
	public boolean isToReview() {
		return itemSession != null && itemSession.isToReview();
	}

	@Override
	public int hashCode() {
		return itemSession.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof AssessmentItemListEntry) {
			AssessmentItemListEntry entry = (AssessmentItemListEntry)obj;
			return assessedIdentity.equals(entry.assessedIdentity)
					&& itemRef.getIdentifier().equals(entry.itemRef.getIdentifier());
		}
		return false;
	}
}
