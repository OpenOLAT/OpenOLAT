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
package org.olat.modules.invitation.ui;

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.Invitation;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.modules.project.ProjProject;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationRow {
	
	private Invitation invitation;
	private final Identity identity;
	private final FormLink urlLink;
	private final FormLink toolsLink;
	
	private RepositoryEntry entry;
	private BusinessGroup businessGroup;
	private ProjProject project;
	
	public InvitationRow(Invitation invitation, RepositoryEntry entry, BusinessGroup businessGroup,
			ProjProject project, FormLink urlLink, FormLink toolsLink) {
		this.invitation = invitation;
		this.urlLink = urlLink;
		this.toolsLink = toolsLink;
		identity = invitation.getIdentity();
		this.entry = entry;
		this.businessGroup = businessGroup;
		this.project = project;
	}
	
	public Date getInvitationDate() {
		return invitation.getCreationDate();
	}
	
	public InvitationStatusEnum getInvitationStatus() {
		return invitation.getStatus();
	}
	
	public List<String> getInvitationRoles() {
		return invitation.getRoleList();
	}
	
	public String getToken() {
		return invitation.getToken();
	}

	public Invitation getInvitation() {
		return invitation;
	}
	
	public void setInvitation(Invitation invitation) {
		this.invitation = invitation;
	}

	public Identity getIdentity() {
		return identity;
	}
	
	public String getResourceableTypeName() {
		if(entry != null) {
			return entry.getOlatResource().getResourceableTypeName();
		}
		if(businessGroup != null) {
			return businessGroup.getResourceableTypeName();
		}
		return null;
	}
	
	public RepositoryEntry getRepositoryEntry() {
		return entry;
	}
	
	public String getRepositoryEntryDisplayname() {
		return entry == null ? null : entry.getDisplayname();
	}
	
	public Long getRepositoryEntryKey() {
		return entry == null ? null : entry.getKey();
	}
	
	public String getRepositoryEntryExternalRef() {
		return entry == null ? null : entry.getExternalRef();
	}
	
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}
	
	public Long getBusinessGroupKey() {
		return businessGroup == null ? null : businessGroup.getKey();
	}
	
	public String getBusinessGroupName() {
		return businessGroup == null ? null : businessGroup.getName();
	}
	
	public ProjProject getProject() {
		return project;
	}
	
	public Long getProjectKey() {
		return project == null ? null : project.getKey();
	}
	
	public String getProjectTitle() {
		return project == null ? null : project.getTitle();
	}

	public FormLink getUrlLink() {
		return urlLink;
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}
}
