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
package org.olat.modules.curriculum.ui.member;

import java.util.Date;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.group.ui.main.CourseMembership;
import org.olat.modules.curriculum.CurriculumRoles;

/**
 * 
 * Initial date: 26 nov. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class MemberHistoryDetailsRow {
	
	private Identity identity;
	private String userDisplayName;
	private String actorDisplayName;
	private String activity;
	private final Group group;
	private final CurriculumRoles role;
	private final CourseMembership membership;
	private final GroupMembershipHistory point;
	private GroupMembershipStatus previousStatus;
	private final String curriculumElementName;
	
	private FormLink noteLink;
	
	public MemberHistoryDetailsRow(Identity identity, String userDisplayName, String curriculumElementName, GroupMembershipHistory point) {
		this.point = point;
		this.group = point.getGroup();
		this.identity = identity;
		this.userDisplayName = userDisplayName;
		this.curriculumElementName = curriculumElementName;
		membership = new CourseMembership();
		membership.setCurriculumElementRole(point.getRole());
		role = CurriculumRoles.isValueOf(point.getRole()) ? CurriculumRoles.valueOf(point.getRole()) : null;
	}
	
	public Long getHistoryKey() {
		return point.getKey();
	}
	
	public Date getDate() {
		return point.getCreationDate();
	}
	
	public CurriculumRoles getRole() {
		return role;
	}
	
	public String getCurriculumElementName() {
		return curriculumElementName;
	}
	
	public CourseMembership getMembership() {
		return membership;
	}
	
	public GroupMembershipStatus getStatus() {
		return point.getStatus();
	}
	
	public Group getGroup() {
		return group;
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public String getActivity() {
		return activity;
	}

	public void setActivity(String activity) {
		this.activity = activity;
	}

	public GroupMembershipStatus getPreviousStatus() {
		return previousStatus;
	}

	public void setPreviousStatus(GroupMembershipStatus previousStatus) {
		this.previousStatus = previousStatus;
	}
	
	public String getAdminNote() {
		return point.getAdminNote();
	}

	public String getUserDisplayName() {
		return userDisplayName;
	}

	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}

	public String getActorDisplayName() {
		return actorDisplayName;
	}

	public void setActorDisplayName(String actorDisplayName) {
		this.actorDisplayName = actorDisplayName;
	}

	public FormLink getNoteLink() {
		return noteLink;
	}

	public void setNoteLink(FormLink noteLink) {
		this.noteLink = noteLink;
	}
}
