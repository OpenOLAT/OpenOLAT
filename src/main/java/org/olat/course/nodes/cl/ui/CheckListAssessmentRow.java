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
package org.olat.course.nodes.cl.ui;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.user.propertyhandlers.UserPropertyHandler;


/**
 * 
 * This is a compact view of the assessment data with indexed arrays. It prevent
 * to have 1000x identities in memory which is a memory issue.
 * 
 * Initial date: 07.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListAssessmentRow {
	
	private List<Long> groupKeys;
	private List<Long> curriculumElementKeys;
	private AssessmentObligation assessmentObligation;
	private boolean fakeParticipant;
	private final Long identityKey;
	private final String identityName;
	private final String[] identityProps;
	private Boolean[] checked;
	private Float[] scores;
	private MultipleSelectionElement[] checkedEl;
	private final Float totalPoints;
	
	public CheckListAssessmentRow(Identity identity, Boolean[] checked, Float[] scores, Float totalPoints,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		identityKey = identity.getKey();
		identityName = identity.getName();
		this.checked = checked;
		this.scores = scores;
		this.totalPoints = totalPoints;
		
		identityProps = new String[userPropertyHandlers.size()];
		for(int i=userPropertyHandlers.size(); i-->0; ) {
			identityProps[i] = userPropertyHandlers.get(i).getUserProperty(identity.getUser(), locale);
		}
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}

	public String getIdentityName() {
		return identityName;
	}

	public List<Long> getGroupKeys() {
		return groupKeys;
	}

	public void setGroupKeys(List<Long> groupKeys) {
		this.groupKeys = groupKeys;
	}
	
	public List<Long> getCurriculumElmentKeys() {
		return curriculumElementKeys;
	}
	
	public void setCurriculumElementKeys(List<Long> curriculumElementKeys) {
		this.curriculumElementKeys = curriculumElementKeys;
	}

	public AssessmentObligation getAssessmentObligation() {
		return assessmentObligation;
	}

	public void setAssessmentObligation(AssessmentObligation assessmentObligation) {
		this.assessmentObligation = assessmentObligation;
	}

	public boolean isFakeParticipant() {
		return fakeParticipant;
	}

	public void setFakeParticipant(boolean fakeParticipant) {
		this.fakeParticipant = fakeParticipant;
	}

	public String getIdentityProp(int index) {
		return identityProps[index];
	}
	
	public String[] getIdentityProps() {
		return identityProps;
	}

	public Float getTotalPoints() {
		return totalPoints;
	}

	public Float[] getScores() {
		return scores;
	}

	public void setScores(Float[] scores) {
		this.scores = scores;
	}

	public Boolean[] getChecked() {
		return checked;
	}
	
	public void setChecked(Boolean[] checked) {
		this.checked = checked;
	}

	public MultipleSelectionElement[] getCheckedEl() {
		return checkedEl;
	}

	public void setCheckedEl(MultipleSelectionElement[] checkedEl) {
		this.checkedEl = checkedEl;
	}
}
