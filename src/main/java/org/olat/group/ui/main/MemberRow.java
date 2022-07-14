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
package org.olat.group.ui.main;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupShort;
import org.olat.group.model.MemberView;
import org.olat.modules.curriculum.CurriculumElementShort;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MemberRow {
	
	private Date firstTime;
	private Date lastTime;
	
	private String onlineStatus;
	private FormLink toolsLink;
	private FormLink chatLink;
	
	private final MemberView view;
	
	public MemberRow(MemberView view) {
		this.view = view;
		firstTime = view.getCreationDate();
		lastTime = view.getLastModified();
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	public FormLink getChatLink() {
		return chatLink;
	}

	public void setChatLink(FormLink chatLink) {
		this.chatLink = chatLink;
	}

	public String getOnlineStatus() {
		return onlineStatus;
	}

	public void setOnlineStatus(String onlineStatus) {
		this.onlineStatus = onlineStatus;
	}
	
	public MemberView getView() {
		return view;
	}
	
	public Long getIdentityKey() {
		return view.getIdentityKey();
	}

	public CourseMembership getMembership() {
		return view.getMemberShip();
	}

	public List<BusinessGroupShort> getGroups() {
		return view.getGroups();
	}


	public List<CurriculumElementShort> getCurriculumElements() {
		return view.getCurriculumElements();
	}

	public boolean isFullyManaged() {
		CourseMembership membership = getMembership();
		if(membership != null && !membership.isManagedMembersRepo() &&
				(membership.isRepositoryEntryOwner() || membership.isRepositoryEntryCoach() || membership.isRepositoryEntryParticipant())) {
			return false;
		}

		if(view.getGroups() != null) {
			for(BusinessGroupShort group:view.getGroups()) {
				if(!BusinessGroupManagedFlag.isManaged(group.getManagedFlags(), BusinessGroupManagedFlag.membersmanagement)) {
					return false;
				}
			}
		}
		
		return true;
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

	@Override
	public int hashCode() {
		return view.getIdentityKey() == null ? 2878 : view.getIdentityKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof MemberRow) {
			MemberRow member = (MemberRow)obj;
			return view.getIdentityKey() != null && view.getIdentityKey().equals(member.getView().getIdentityKey());
		}
		return false;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("memberView[identityKey=").append(view.getIdentityKey() == null ? "" : view.getIdentityKey()).append("]");
		return sb.toString();
	}
}