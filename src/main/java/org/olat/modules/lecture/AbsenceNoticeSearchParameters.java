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
package org.olat.modules.lecture;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.modules.lecture.ui.LectureRoles;

/**
 * 
 * Initial date: 25 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeSearchParameters {
	
	private Date endDate;
	private Date startDate;
	private Boolean authorized;
	private String searchString;
	private boolean linkedToRollCall;
	private AbsenceCategory absenceCategory;
	private final List<AbsenceNoticeType> typesList = new ArrayList<>(4);
	
	private IdentityRef teacher;
	private IdentityRef masterCoach;
	private IdentityRef participant;
	private List<OrganisationRef> managedOrganisations;
	
	public AbsenceNoticeSearchParameters() {
		//
	}

	public boolean isLinkedToRollCall() {
		return linkedToRollCall;
	}

	public void setLinkedToRollCall(boolean linkedToRollCall) {
		this.linkedToRollCall = linkedToRollCall;
	}

	public List<AbsenceNoticeType> getTypes() {
		return typesList;
	}
	
	public void setTypes(List<AbsenceNoticeType> types) {
		if(types == null || types.isEmpty()) {
			typesList.clear();
		} else {
			typesList.clear();
			typesList.addAll(types);
		}
	}

	public void addTypes(AbsenceNoticeType... types) {
		if(types != null) {
			for(AbsenceNoticeType type:types) {
				if(type != null) {
					typesList.add(type);
				}
			}
		}
	}

	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	
	public void setViewAs(IdentityRef identity, Roles roles, LectureRoles role) {
		switch(role) {
			case lecturemanager: setManagedOrganisations(roles.getOrganisationsWithRoles(OrganisationRoles.lecturemanager,
					OrganisationRoles.administrator, OrganisationRoles.principal)); break;
			case mastercoach: setMasterCoach(identity); break;
			case teacher: setTeacher(identity); break;
			default: setParticipant(identity); break;
		}
	}

	public IdentityRef getTeacher() {
		return teacher;
	}

	public void setTeacher(IdentityRef teacher) {
		this.teacher = teacher;
	}

	public IdentityRef getMasterCoach() {
		return masterCoach;
	}

	public void setMasterCoach(IdentityRef masterCoach) {
		this.masterCoach = masterCoach;
	}

	public List<OrganisationRef> getManagedOrganisations() {
		return managedOrganisations;
	}

	public void setManagedOrganisations(List<OrganisationRef> managedOrganisations) {
		this.managedOrganisations = managedOrganisations;
	}

	public IdentityRef getParticipant() {
		return participant;
	}

	public void setParticipant(IdentityRef participant) {
		this.participant = participant;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}

	public Boolean getAuthorized() {
		return authorized;
	}

	public void setAuthorized(Boolean authorized) {
		this.authorized = authorized;
	}

	public AbsenceCategory getAbsenceCategory() {
		return absenceCategory;
	}

	public void setAbsenceCategory(AbsenceCategory absenceCategory) {
		this.absenceCategory = absenceCategory;
	}
}
