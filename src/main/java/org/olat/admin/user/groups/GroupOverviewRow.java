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
package org.olat.admin.user.groups;

import org.olat.basesecurity.Invitation;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupShort;

/**
 * 
 * Initial date: 05.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupOverviewRow implements BusinessGroupShort {
	private final BusinessGroupMembership member;
	private final Boolean allowLeave;
	private final Long key;
	private final String name;
	private final BusinessGroupManagedFlag[] managedflags;
	
	private final Invitation invitation;
	private FormLink invitationLink;

	public GroupOverviewRow(BusinessGroup businessGroup, BusinessGroupMembership member, Invitation invitation, Boolean allowLeave) {
		key = businessGroup.getKey();
		name = businessGroup.getName();
		managedflags = businessGroup.getManagedFlags();
		this.member = member;
		this.invitation = invitation;
		this.allowLeave = allowLeave;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public String getResourceableTypeName() {
		return OresHelper.calculateTypeName(BusinessGroup.class);
	}

	@Override
	public Long getResourceableId() {
		return key;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public BusinessGroupManagedFlag[] getManagedFlags() {
		return managedflags;
	}

	public BusinessGroupMembership getMembership() {
		return member;
	}

	public Boolean getAllowLeave() {
		return allowLeave;
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

	@Override
	public int hashCode() {
		return key.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof GroupOverviewRow) {
			GroupOverviewRow item = (GroupOverviewRow)obj;
			return key != null && key.equals(item.key);
		}
		return false;
	}
}