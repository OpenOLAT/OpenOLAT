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
import java.util.Map;
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
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.BusinessGroupDAO;
import org.olat.modules.invitation.InvitationAdditionalInfos;
import org.olat.modules.invitation.InvitationModule;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.InvitationStatusEnum;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.model.InvitationWithRepositoryEntry;
import org.olat.modules.invitation.model.InvitationImpl;
import org.olat.modules.invitation.model.InvitationWithBusinessGroup;
import org.olat.modules.invitation.model.SearchInvitationParameters;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.manager.BinderDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.user.UserDataDeletable;
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
public class InvitationServiceImpl implements InvitationService, UserDataDeletable {
	
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
	private InvitationModule invitationModule;
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
	public Identity getOrCreateIdentityAndPersistInvitation(Invitation invitation, Group group, Locale locale, Identity doer) {
		// create identity only if such a user does not already exist
		
		Date expirationDate = null;
		int expireAfter = invitationModule.getExpirationAccountInDays();
		if(expireAfter > 0) {
			expirationDate = DateUtils.addDays(new Date(), expireAfter);
			expirationDate = CalendarUtils.endOfDay(expirationDate);
		}

		Identity invitee;
		if(invitation.getIdentity() != null) {
			invitee = invitation.getIdentity();
		} else {
			invitee = userManager.findUniqueIdentityByEmail(invitation.getMail());
			if (invitee == null) {
				User user = userManager.createUser(invitation.getFirstName(), invitation.getLastName(), invitation.getMail());
				InvitationAdditionalInfos additionInfos =  invitation.getAdditionalInfos();
				if(additionInfos != null) {
					for(Map.Entry<String,String> infosPair:additionInfos.getUserAttributes().entrySet()) {
						user.setProperty(infosPair.getKey(), infosPair.getValue());
					}
				}
				user.getPreferences().setLanguage(locale.toString());
				invitee = securityManager.createAndPersistIdentityAndUser(null, invitation.getMail(), null, user, null, null, null, null, expirationDate);
			} else if(invitee.getExpirationDate() != null && invitee.getExpirationDate().before(expirationDate)) {
				securityManager.saveIdentityExpirationDate(invitee, expirationDate, doer);
			} else if(invitee.getExpirationDate() == null && securityManager.getRoles(invitee).isInviteeOnly()) {
				securityManager.saveIdentityExpirationDate(invitee, expirationDate, doer);
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
	public boolean hasInvitations(RepositoryEntryRef entry) {
		return invitationDao.hasInvitations(entry);
	}

	@Override
	public boolean hasInvitations(BusinessGroupRef businessGroup) {
		return invitationDao.hasInvitations(businessGroup);
	}

	@Override
	public Invitation getInvitation(Invitation invitation) {
		if(invitation == null || invitation.getKey() == null) {
			return null;
		}
		return invitationDao.findInvitationByKey(invitation.getKey());
	}
	
	@Override
	public Invitation getInvitationByKey(Long key) {
		if(key == null) {
			return null;
		}
		return invitationDao.findInvitationByKey(key);
	}

	@Override
	public Invitation findInvitation(String token) {
		return invitationDao.findInvitationByToken(token);
	}
	
	@Override
	public Invitation findInvitation(Binder binder, IdentityRef identity) {
		return invitationDao.findInvitation(binder.getBaseGroup(), identity);
	}
	
	@Override
	public List<Invitation> findInvitations(Identity identity) {
		return invitationDao.findInvitations(identity);
	}

	@Override
	public List<InvitationWithRepositoryEntry> findInvitationsWithEntries(SearchInvitationParameters searchParams, boolean followToBusinessGroups) {
		return invitationDao.findInvitationsWithRepositoryEntries(searchParams, followToBusinessGroups);
	}

	@Override
	public List<InvitationWithBusinessGroup> findInvitationsWithBusinessGroups(SearchInvitationParameters searchParams) {
		return invitationDao.findInvitationsWitBusinessGroups(searchParams);
	}

	@Override
	public List<Invitation> findInvitations(RepositoryEntryRef entry, SearchInvitationParameters searchParams) {
		return invitationDao.findInvitations(entry, searchParams);
	}

	@Override
	public List<Invitation> findInvitations(BusinessGroupRef businessGroup, SearchInvitationParameters searchParams) {
		return invitationDao.findInvitations(businessGroup, searchParams);
	}

	@Override
	public Invitation findSimilarInvitation(InvitationTypeEnum type, String email, List<String> roles, Group group) {
		List<Invitation> invitations = invitationDao.findInvitations(type, email, group);
		for(Invitation invitation:invitations) {
			List<String> invitationRoles = invitation.getRoleList();
			if(invitationRoles.containsAll(roles) && roles.containsAll(invitationRoles)) {
				return invitation;
			}	
		}
		return null;
	}

	@Override
	public Invitation update(Invitation invitation, String firstName, String lastName, String email) {
		if(invitation.getIdentity() != null) {
			Identity identity = invitation.getIdentity();
			User user = identity.getUser();
			user.setProperty(UserConstants.FIRSTNAME, firstName);
			user.setProperty(UserConstants.LASTNAME, lastName);
			user.setProperty(UserConstants.EMAIL, email);
			userManager.updateUserFromIdentity(identity);
		} else {
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
	public void inactivateInvitations(RepositoryEntryRef entry, IdentityRef identity) {
		SearchInvitationParameters searchParams = new SearchInvitationParameters();
		searchParams.setIdentityKey(identity.getKey());
		List<Invitation> invitations = findInvitations(entry, searchParams);
		for(Invitation invitation:invitations) {
			invitation.setStatus(InvitationStatusEnum.inactive);
			update(invitation);
		}
	}

	@Override
	public Invitation update(Invitation invitation) {
		return invitationDao.update(invitation);
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
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		invitationDao.deleteInvitation(identity);
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
