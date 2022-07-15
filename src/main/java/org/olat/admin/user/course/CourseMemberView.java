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
package org.olat.admin.user.course;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.Invitation;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.MemberView;
import org.olat.group.ui.main.CourseMembership;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementShort;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 16 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseMemberView {

	private Date firstTime;
	private Date lastTime;
	private final MemberView memberView;
	
	private final Invitation invitation;
	private FormLink invitationLink;
	
	public CourseMemberView(MemberView view, Invitation invitation) {
		this.memberView = view;
		this.invitation = invitation;
	}
	
	public Long getRepoKey() {
		return memberView.getRepositoryEntryKey();
	}
	
	public String getDisplayName() {
		return memberView.getRepositoryEntryDisplayName();
	}
	
	public String getExternalId() {
		return memberView.getRepositoryEntryExternalId() ;
	}

	public String getExternalRef() {
		return memberView.getRepositoryEntryExternalRef();
	}
	
	public RepositoryEntry getEntry() {
		return memberView.getRepositoryEntry();
	}

	public Date getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(Date firstTime) {
		if(firstTime == null) return;
		if(this.firstTime == null || this.firstTime.compareTo(firstTime) > 0) {
			this.firstTime = firstTime;
		}
	}

	public Date getLastTime() {
		return lastTime;
	}

	public void setLastTime(Date lastTime) {
		if(lastTime == null) return;
		if(this.lastTime == null || this.lastTime.compareTo(lastTime) < 0) {
			this.lastTime = lastTime;
		}
	}

	public CourseMembership getMembership() {
		return memberView.getMemberShip();
	}
	
	public List<BusinessGroupShort> getGroups() {
		return memberView.getGroups();
	}
	
	public List<CurriculumElementShort> getCurriculumElements() {
		return memberView.getCurriculumElements();
	}
	
	public boolean isFullyManaged() {
		CourseMembership membership = getMembership();
		if(membership != null && !membership.isManagedMembersRepo() &&
				(membership.isRepositoryEntryOwner() || membership.isRepositoryEntryCoach() || membership.isRepositoryEntryParticipant())) {
			return false;
		}

		List<BusinessGroupShort> groups = getGroups();
		if(groups != null) {
			for(BusinessGroupShort group:groups) {
				if(!BusinessGroupManagedFlag.isManaged(group.getManagedFlags(), BusinessGroupManagedFlag.membersmanagement)) {
					return false;
				}
			}
		}
		
		List<CurriculumElementShort> elements = getCurriculumElements();
		if(elements != null) {
			for(CurriculumElementShort element:elements) {
				if(!CurriculumElementManagedFlag.isManaged(element.getManagedFlags(), CurriculumElementManagedFlag.members)) {
					return false;
				}
			}
		}

		return true;
	}

	public Invitation getInvitation() {
		return invitation;
	}

	public FormLink getInvitationLink() {
		return invitationLink;
	}

	public void setInvitationLink(FormLink invitationLink) {
		this.invitationLink = invitationLink;
	}
}
