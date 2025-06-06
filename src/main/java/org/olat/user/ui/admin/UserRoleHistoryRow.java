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
package org.olat.user.ui.admin;

import java.util.Date;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupMembershipHistory;
import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 23 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class UserRoleHistoryRow {
	
	private Identity identity;
	private String userDisplayName;
	private String actorDisplayName;
	private String activity;
	private final Group group;
	private final boolean inherited;
	private final Long organisationKey;
	private final OrganisationRoles role;
	private final String organisationName;
	private final String organisationPath;
	private final GroupMembershipHistory point;
	private GroupMembershipStatus previousStatus;
	
	private FormLink noteLink;
	
	public UserRoleHistoryRow(Identity identity, String userDisplayName,
			String organisationName, Long organisationKey, String organisationPath, GroupMembershipHistory point) {
		this.point = point;
		this.group = point.getGroup();
		this.identity = identity;
		this.userDisplayName = userDisplayName;
		this.organisationKey = organisationKey;
		this.organisationName = organisationName;
		this.organisationPath = organisationPath;
		role = OrganisationRoles.isValue(point.getRole())
				? OrganisationRoles.valueOf(point.getRole())
				: null;
		inherited = point.isInherited();
	}
	
	public Long getHistoryKey() {
		return point.getKey();
	}
	
	public Date getDate() {
		return point.getCreationDate();
	}
	
	public Long getOrganisationKey() {
		return organisationKey;
	}
	
	public String getOrganisationName() {
		return organisationName;
	}
	
	public String getOrganisationPath() {
		return organisationPath;
	}
	
	public OrganisationRoles getRole() {
		return role;
	}
	
	public boolean isInherited() {
		return inherited;
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
