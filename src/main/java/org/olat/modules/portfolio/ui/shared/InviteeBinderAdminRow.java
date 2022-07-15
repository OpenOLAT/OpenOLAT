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
package org.olat.modules.portfolio.ui.shared;

import java.util.Date;

import org.olat.basesecurity.Invitation;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.portfolio.model.AssessedBinder;

/**
 * 
 * Initial date: 14 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InviteeBinderAdminRow {
	
	private final AssessedBinder binder;
	private final String ownerFullname;
	private final Invitation invitation;
	
	private FormLink invitationLink;
	
	public InviteeBinderAdminRow(AssessedBinder binder, String ownerFullname, Invitation invitation) {
		this.binder = binder;
		this.invitation = invitation;
		this.ownerFullname = ownerFullname;
	}

	public Long getBinderKey() {
		return binder.getBinderKey();
	}

	public String getBinderName() {
		return binder.getBinderTitle();
	}
	
	public Long getCourseKey() {
		return binder.getEntryKey();
	}

	public String getCourseName() {
		return binder.getEntryDisplayname();
	}
	
	public Long getOwnerKey() {
		Identity assessedIdentity = binder.getAssessedIdentity();
		return assessedIdentity == null ? null : assessedIdentity.getKey();
	}

	public String getOwnerFullname() {
		return ownerFullname;
	}

	public Date getInvitationDate() {
		return invitation == null ? null : invitation.getCreationDate();
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
