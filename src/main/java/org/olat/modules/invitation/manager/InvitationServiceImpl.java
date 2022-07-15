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
package org.olat.modules.invitation.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.Invitation;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.model.InvitationEntry;
import org.olat.modules.invitation.model.InvitationImpl;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.manager.BinderDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class InvitationServiceImpl implements InvitationService {
	
	private static final Logger log = Tracing.createLoggerFor(InvitationServiceImpl.class);
	
	@Autowired
	private GroupDAO groupDao;
	@Autowired
	private BinderDAO binderDao;
	@Autowired
	private UserManager userManager;
	@Autowired
	private InvitationDAO invitationDao;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupDAO businessGroupDao;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private OrganisationService organisationService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	
	@Override
	public Invitation createInvitation(InvitationTypeEnum type) {
		return invitationDao.createInvitation(type);
	}
	
	@Override
	public Identity createIdentityFrom(Invitation invitation, Locale locale) {
		if(invitation.getIdentity() != null) {
			return securityManager.loadIdentityByKey(invitation.getIdentity().getKey());
		}
		
		String tempUsername = UUID.randomUUID().toString();
		User user = userManager.createUser(invitation.getFirstName(), invitation.getLastName(), invitation.getMail());
		user.getPreferences().setLanguage(locale.toString());
		Identity invitee = securityManager.createAndPersistIdentityAndUser(null, tempUsername, null, user, null, null, null, null, null);
		groupDao.addMembershipTwoWay(invitation.getBaseGroup(), invitee, GroupRoles.invitee.name());
		organisationService.addMember(invitee, OrganisationRoles.invitee);
		return invitee;
	}
	
	@Override
	public Identity  getOrCreateIdentityAndPersistInvitation(Invitation invitation, Group group, Locale locale) {
		// create identity only if such a user does not already exist

		Identity invitee;
		if(invitation.getIdentity() != null) {
			invitee = invitation.getIdentity();
		} else {
			invitee = userManager.findUniqueIdentityByEmail(invitation.getMail());
			if (invitee == null) {
				User user = userManager.createUser(invitation.getFirstName(), invitation.getLastName(), invitation.getMail());
				user.getPreferences().setLanguage(locale.toString());
				invitee = securityManager.createAndPersistIdentityAndUser(null, invitation.getMail(), null, user, null, null, null, null, null);
			}
		}
		
		// create the invitation
		group = groupDao.loadGroup(group.getKey());
		((InvitationImpl)invitation).setCreationDate(new Date());
		((InvitationImpl)invitation).setBaseGroup(group);
		((InvitationImpl)invitation).setIdentity(invitee);
		invitationDao.persist(invitation);

		// add invitee to the security group of that portfolio element
		groupDao.addMembershipTwoWay(group, invitee, GroupRoles.invitee.name());
		organisationService.addMember(invitee, OrganisationRoles.invitee);			
		return invitee;
	}

	@Override
	public long countInvitations() {
		return invitationDao.countInvitations();
	}

	@Override
	public boolean hasInvitations(String token) {
		return invitationDao.hasInvitations(token);
	}

	@Override
	public Invitation findInvitation(String token) {
		return invitationDao.findInvitation(token);
	}
	
	@Override
	public Invitation findInvitation(Binder binder, IdentityRef identity) {
		return invitationDao.findInvitation(binder.getBaseGroup(), identity);
	}

	@Override
	public List<InvitationEntry> findInvitations(Identity identity) {
		return invitationDao.findInvitations(identity);
	}

	@Override
	public List<Invitation> findInvitations(RepositoryEntryRef entry) {
		return invitationDao.findInvitation(entry);
	}

	@Override
	public List<Invitation> findInvitations(BusinessGroupRef businessGroup) {
		return invitationDao.findInvitation(businessGroup);
	}

	@Override
	public Invitation update(Invitation invitation, String firstName, String lastName, String email) {
		List<Identity> identities = groupDao.getMembers(invitation.getBaseGroup(), GroupRoles.invitee.name());
		for(Identity identity:identities) {
			User user = identity.getUser();
			if(email.equals(user.getEmail())) {
				user.setProperty(UserConstants.FIRSTNAME, firstName);
				user.setProperty(UserConstants.LASTNAME, lastName);
				user.setProperty(UserConstants.EMAIL, email);
				userManager.updateUserFromIdentity(identity);
			}
		}
		
		invitation.setFirstName(firstName);
		invitation.setLastName(lastName);
		invitation.setMail(email);
		return invitationDao.update(invitation);
	}

	@Override
	public void acceptInvitation(Invitation invitation, Identity identity) {
		if(invitation.getType() == InvitationTypeEnum.binder) {
			return;// nothing to do
		}
		
		MailPackage mailing = new MailPackage(false);
		Roles roles = securityManager.getRoles(identity);
		List<String> memberRoles = invitation.getRoleList();
		if(invitation.getType() == InvitationTypeEnum.businessGroup) {
			BusinessGroup businessGroup = businessGroupDao.findBusinessGroup(invitation.getBaseGroup());
			if(businessGroup != null) {
				if(memberRoles == null || memberRoles.isEmpty() || memberRoles.contains(GroupRoles.participant.name())) {
					businessGroupService.addParticipants(identity, roles, List.of(identity), businessGroup, mailing);
				}
				if(memberRoles != null && memberRoles.contains(GroupRoles.coach.name())) {
					businessGroupService.addOwners(identity, roles, List.of(identity), businessGroup, mailing);
				}
			} else {
				log.warn("Business group of invitation not found: {}", invitation.getKey());
			}
		} else if(invitation.getType() == InvitationTypeEnum.repositoryEntry) {
			IdentitiesAddEvent iae = new IdentitiesAddEvent(List.of(identity));
			List<RepositoryEntry> entries = repositoryEntryDao.loadByResourceGroup(invitation.getBaseGroup());
			if(entries.isEmpty()) {
				log.warn("Repository entry of invitation not found: {}", invitation.getKey());
			} else {
				for(RepositoryEntry entry:entries) {
					if(memberRoles == null || memberRoles.isEmpty() || memberRoles.contains(GroupRoles.participant.name())) {
						repositoryManager.addParticipants(identity, roles, iae, entry, mailing);
					}
					if(memberRoles != null && memberRoles.contains(GroupRoles.coach.name())) {
						repositoryManager.addTutors(identity, roles, iae, entry, mailing);
					}
				}
			}
		}
	}
	
	@Override
	public Invitation update(Invitation invitation, Identity identity) {
		((InvitationImpl)invitation).setIdentity(identity);
		return invitationDao.update(invitation);
	}

	@Override
	public void deleteInvitation(Invitation invitation) {
		invitationDao.deleteInvitation(invitation);
	}
	
	@Override
	public String toUrl(Invitation invitation) {
		if(invitation == null || invitation.getType() == null) return null;
		
		switch(invitation.getType()) {
			case repositoryEntry: 
				List<RepositoryEntry> repositoryEntries = repositoryEntryDao.loadByResourceGroup(invitation.getBaseGroup());
				if(!repositoryEntries.isEmpty()) {
					return toUrl(invitation, repositoryEntries.get(0));
				}
				break;
			case businessGroup:
				BusinessGroup businessGroup = businessGroupDao.findBusinessGroup(invitation.getBaseGroup());
				return toUrl(invitation, businessGroup);
			case binder:
				Binder binder = binderDao.loadByGroup(invitation.getBaseGroup());
				return toUrl(invitation, binder);
		}
		return toUrl(invitation, List.of());
	}

	@Override
	public String toUrl(Invitation invitation, RepositoryEntry repositoryEntry) {
		List<ContextEntry> entries;
		if(repositoryEntry != null) {
			entries = BusinessControlFactory.getInstance().createCEListFromString(repositoryEntry);
		} else {	
			entries = List.of();
		}
		return toUrl(invitation, entries);
	}

	@Override
	public String toUrl(Invitation invitation, BusinessGroup businessGroup) {
		List<ContextEntry> entries;
		if(businessGroup != null) {
			entries = BusinessControlFactory.getInstance().createCEListFromString(businessGroup);
		} else {	
			entries = List.of();
		}
		return toUrl(invitation, entries);
	}
	
	@Override
	public String toUrl(Invitation invitation, Binder binder) {
		List<ContextEntry> entries;
		if(binder != null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("BinderInvitation", binder.getKey());
			entries = BusinessControlFactory.getInstance().createCEListFromString(ores);
		} else {
			entries = List.of();
		}
		return toUrl(invitation, entries);
	}

	private String toUrl(Invitation invitation, List<ContextEntry> entries) {
		String businessPath = BusinessControlFactory.getInstance().getAsString(entries);
		String restUri = BusinessControlFactory.getInstance().getURLFromBusinessPathString(businessPath);
		restUri += "?invitation=" + invitation.getToken();
		return restUri;
	}

}
