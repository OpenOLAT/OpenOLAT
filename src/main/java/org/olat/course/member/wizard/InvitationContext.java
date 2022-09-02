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
package org.olat.course.member.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.modules.invitation.InvitationAdditionalInfos;
import org.olat.modules.invitation.model.InvitationAdditionalInfosImpl;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 5 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationContext {
	
	private final RepositoryEntry repoEntry;
	private final BusinessGroup businessGroup;
	private MemberPermissionChangeEvent memberPermissions;
	
	private final boolean overrideManaged;
	
	private String rawEmail;
	private String rawNames;
	private MailTemplate mailTemplate;
	
	private List<TransientInvitation> invitations = new ArrayList<>();

	private MailerResult result;
	
	private InvitationContext(RepositoryEntry repoEntry, BusinessGroup businessGroup, boolean overrideManaged) {
		this.repoEntry = repoEntry;
		this.businessGroup = businessGroup;
		this.overrideManaged = overrideManaged;
	}
	
	public static InvitationContext valueOf(BusinessGroup businessGroup) {
		return new InvitationContext(null, businessGroup, false);
	}
	
	public static InvitationContext valueOf(RepositoryEntry repoEntry, boolean overrideManaged) {
		return new InvitationContext(repoEntry, null, overrideManaged);
	}
	
	public RepositoryEntry getRepoEntry() {
		return repoEntry;
	}
	
	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}
	
	public List<TransientInvitation> getInvitations() {
		return new ArrayList<>(invitations);
	}

	public boolean isOverrideManaged() {
		return overrideManaged;
	}

	public String getRawEmail() {
		return rawEmail;
	}

	public void setRawEmail(String email) {
		this.rawEmail = email;
	}
	
	public String getRawNames() {
		return rawNames;
	}
	
	public void setRawNames(String rawNames) {
		this.rawNames = rawNames;
	}

	public MailerResult getResult() {
		return result;
	}

	public void setResult(MailerResult result) {
		this.result = result;
	}

	public MailTemplate getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(MailTemplate mailTemplate) {
		this.mailTemplate = mailTemplate;
	}

	public MemberPermissionChangeEvent getMemberPermissions() {
		return memberPermissions;
	}

	public void setMemberPermissions(MemberPermissionChangeEvent memberPermissions) {
		this.memberPermissions = memberPermissions;
	}
	
	public boolean hasInviteeOnly() {
		boolean inviteeOnly = false;
		for(TransientInvitation invitation:invitations) {
			inviteeOnly |= invitation.getIdentity() == null || invitation.isIdentityInviteeOnly();
		}
		return inviteeOnly;
	}
	
	public void clearInvitations() {
		invitations.clear();
	}
	
	public void setInvitation(String email, Identity identity, boolean identityInviteeOnly) {
		invitations.clear();
		
		TransientInvitation invitation = new TransientInvitation();
		invitation.setEmail(trim(email));
		invitation.setIdentity(identity);
		invitation.setIdentityInviteeOnly(identityInviteeOnly);
		invitations.add(invitation);
	}
	
	public void addInvitation(String email, String firstName, String lastName) {
		TransientInvitation invitation = new TransientInvitation();
		invitation.setEmail(trim(email));
		invitation.setFirstName(trim(firstName));
		invitation.setLastName(trim(lastName));
		invitation.setIdentityInviteeOnly(true);
		invitations.add(invitation);
	}
	
	public void addInvitation(String email, Identity identity, boolean identityInviteeOnly) {
		TransientInvitation invitation = new TransientInvitation();
		invitation.setEmail(trim(email));
		invitation.setIdentity(identity);
		invitation.setIdentityInviteeOnly(identityInviteeOnly);
		invitations.add(invitation);
	}
	
	private String trim(String val) {
		return val == null ? null : val.trim();
	}
	
	public class TransientInvitation {
		
		private String email;
		private String firstName;
		private String lastName;
		private final InvitationAdditionalInfos additionalInfos = new InvitationAdditionalInfosImpl();

		private Identity identity;
		private boolean identityInviteeOnly;
		
		public String getEmail() {
			return email;
		}

		public void setEmail(String email) {
			this.email = email;
		}

		public String getFirstName() {
			return firstName;
		}
		
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		
		public String getLastName() {
			return lastName;
		}
		
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
		
		public InvitationAdditionalInfos getAdditionalInfos() {
			return additionalInfos;
		}
		
		public Identity getIdentity() {
			return identity;
		}
		
		public void setIdentity(Identity identity) {
			this.identity = identity;
		}
		
		public boolean isIdentityInviteeOnly() {
			return identityInviteeOnly;
		}
		
		public void setIdentityInviteeOnly(boolean identityInviteeOnly) {
			this.identityInviteeOnly = identityInviteeOnly;
		}
	}
}
