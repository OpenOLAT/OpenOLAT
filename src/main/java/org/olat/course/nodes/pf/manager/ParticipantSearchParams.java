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
package org.olat.course.nodes.pf.manager;

import java.util.Collection;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroupRef;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.curriculum.CurriculumElementRef;

/**
 * 
 * Initial date: 30 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantSearchParams {
	
	private Identity identity;
	private boolean admin;
	private Collection<AssessmentObligation> assessmentObligations;
	private List<BusinessGroupRef> businessGroupRefs;
	private List<CurriculumElementRef> curriculumElements;
	
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public boolean isAdmin() {
		return admin;
	}
	
	public void setAdmin(boolean admin) {
		this.admin = admin;
	}
	
	public Collection<AssessmentObligation> getAssessmentObligations() {
		return assessmentObligations;
	}
	
	public void setAssessmentObligations(Collection<AssessmentObligation> assessmentObligations) {
		this.assessmentObligations = assessmentObligations;
	}
	
	public List<BusinessGroupRef> getBusinessGroupRefs() {
		return businessGroupRefs;
	}
	
	public void setBusinessGroupRefs(List<BusinessGroupRef> businessGroupRefs) {
		this.businessGroupRefs = businessGroupRefs;
	}
	
	public List<CurriculumElementRef> getCurriculumElements() {
		return curriculumElements;
	}
	
	public void setCurriculumElements(List<CurriculumElementRef> curriculumElements) {
		this.curriculumElements = curriculumElements;
	}

}
