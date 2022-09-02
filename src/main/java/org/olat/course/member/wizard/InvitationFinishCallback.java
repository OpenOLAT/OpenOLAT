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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.Invitation;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.course.member.wizard.InvitationContext.TransientInvitation;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.ui.main.MemberPermissionChangeEvent;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryMailing.RepositoryEntryMailTemplate;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryPermissionChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationFinishCallback implements StepRunnerCallback {
	
	private final InvitationContext context;

	@Autowired
	private MailManager mailManager;
	@Autowired
	private InvitationService invitationService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	public InvitationFinishCallback(InvitationContext context) {
		CoreSpringFactory.autowireObject(this);
		this.context = context;
	}

	@Override
	public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
		Step step = StepsMainRunController.DONE_UNCHANGED;
		
		for(TransientInvitation invitation:context.getInvitations()) {
			if(invitation.getIdentity() == null || invitation.isIdentityInviteeOnly()) {
				step = executeInvitation(ureq, wControl, invitation);
			} else {
				step = executeMembership(ureq, wControl, invitation.getIdentity());
			}
		}
		
		return step;
	}
	
	private Step executeMembership(UserRequest ureq, WindowControl wControl, Identity identity) {
		MemberPermissionChangeEvent changes = context.getMemberPermissions();
			
		MailTemplate template = context.getMailTemplate();
		//commit changes to the repository entry
		MailerResult result = new MailerResult();
		MailPackage reMailing = new MailPackage(template, result, wControl.getBusinessControl().getAsString(), template != null);
			
		Roles roles = ureq.getUserSession().getRoles();
		List<RepositoryEntryPermissionChangeEvent> repoChanges = changes.generateRepositoryChanges(List.of(identity));
		if(!repoChanges.isEmpty() && context.getRepoEntry() != null) {
			repositoryManager.updateRepositoryEntryMemberships(ureq.getIdentity(), roles, context.getRepoEntry(), repoChanges, reMailing);
		}
		//commit all changes to the group memberships
		List<BusinessGroupMembershipChange> allModifications = changes.generateBusinessGroupMembershipChange(List.of(identity));
		if(!allModifications.isEmpty()) {
			MailPackage mailing = new MailPackage(template, result, wControl.getBusinessControl().getAsString(), template != null);
			businessGroupService.updateMemberships(ureq.getIdentity(), allModifications, mailing);
		}

		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private Step executeInvitation(UserRequest ureq, WindowControl wControl, TransientInvitation transientInvitation) {
		MemberPermissionChangeEvent changes = context.getMemberPermissions();
		
		List<String> repoRoles = roles(changes);
		if(!repoRoles.isEmpty() && context.getRepoEntry() != null) {
			executeInvitation(ureq, wControl, transientInvitation, context.getRepoEntry(), null, repoRoles); 
		}
		
		List<BusinessGroupMembershipChange> allModifications = changes.getGroupChanges();
		if(allModifications != null && !allModifications.isEmpty()) {
			for(BusinessGroupMembershipChange allModification:allModifications) {
				BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(allModification.getGroupKey());
				List<String> groupRoles = roles(allModifications, allModification.getGroupKey());
				if(businessGroup != null && !groupRoles.isEmpty()) {
					executeInvitation(ureq, wControl, transientInvitation, context.getRepoEntry(), businessGroup, groupRoles);
				}
			}
		}

		return StepsMainRunController.DONE_MODIFIED;
	}
	
	private List<String> roles(MemberPermissionChangeEvent changes) {
		Set<String> roles = new HashSet<>();
		if(changes.getRepoTutor() != null && changes.getRepoTutor().booleanValue()) {
			roles.add(GroupRoles.coach.name());
		}
		if(changes.getRepoParticipant() != null && changes.getRepoParticipant().booleanValue()) {
			roles.add(GroupRoles.participant.name());
		}

		List<RepositoryEntryPermissionChangeEvent> repoChanges = changes.getRepoChanges();
		if(repoChanges != null && !repoChanges.isEmpty()) {
			for(RepositoryEntryPermissionChangeEvent repoChange:repoChanges) {
				if(repoChange.getRepoTutor() != null && repoChange.getRepoTutor().booleanValue()) {
					roles.add(GroupRoles.coach.name());
				}
				if(repoChange.getRepoParticipant() != null && repoChange.getRepoParticipant().booleanValue()) {
					roles.add(GroupRoles.participant.name());
				}
			}
		}
		return new ArrayList<>(roles);
	}
	
	private List<String> roles(List<BusinessGroupMembershipChange> changes, Long businessGroupKey) {
		Set<String> roles = new HashSet<>();
		for(BusinessGroupMembershipChange change:changes) {
			if(change.getGroupKey().equals(businessGroupKey)) {
				if(change.getTutor() != null && change.getTutor().booleanValue()) {
					roles.add(GroupRoles.coach.name());
				}
				if(change.getParticipant() != null && change.getParticipant().booleanValue()) {
					roles.add(GroupRoles.participant.name());
				}
			}
		}
		return new ArrayList<>(roles);
	}
	
	private Step executeInvitation(UserRequest ureq, WindowControl wControl, TransientInvitation transientInvitation,
			RepositoryEntry repoEntry, BusinessGroup businessGroup, List<String> roles) {
		Group group = null;
		OLATResourceable ores = null;
		InvitationTypeEnum type = null;
		if(repoEntry != null) {
			group = repositoryService.getDefaultGroup(repoEntry);
			ores = repoEntry;
			type = InvitationTypeEnum.repositoryEntry;
		} else if(businessGroup != null) {
			group = businessGroup.getBaseGroup();
			ores = businessGroup;
			type = InvitationTypeEnum.businessGroup;
		}
		
		Invitation invitation = invitationService.createInvitation(type);
		invitation.setFirstName(transientInvitation.getFirstName());
		invitation.setLastName(transientInvitation.getLastName());
		invitation.setMail(transientInvitation.getEmail());
		invitation.setAdditionalInfos(transientInvitation.getAdditionalInfos());
		invitation.setRegistration(true);
		invitation.setRoleList(roles);
		
		if(group == null) {
			return StepsMainRunController.DONE_UNCHANGED;
		}
		
		invitationService.getOrCreateIdentityAndPersistInvitation(invitation, group, ureq.getLocale(), ureq.getIdentity());
		
		ContactList contactList = new ContactList("Invitation");
		contactList.add(transientInvitation.getEmail());

		MailTemplate template = context.getMailTemplate();
		
		if(repoEntry != null) {
			String courseUrl = invitationService.toUrl(invitation, repoEntry);
			if(template instanceof RepositoryEntryMailTemplate) {
				((RepositoryEntryMailTemplate) template).setCourseUrl(courseUrl);
			}
		} else if(businessGroup != null) {
			String businessGroupUrl = invitationService.toUrl(invitation, businessGroup);
			template.addToContext("groupurl", businessGroupUrl);
		}
		
		MailerResult result = new MailerResult();
		MailContext ctxt = new MailContextImpl(ores, null, wControl.getBusinessControl().getAsString());
		MailBundle bundle = mailManager.makeMailBundle(ctxt, template, ureq.getIdentity(), null, result);
		bundle.setContactList(contactList);

		result = mailManager.sendExternMessage(bundle, result, true);
		context.setResult(result);

		return StepsMainRunController.DONE_MODIFIED;
	}
}
