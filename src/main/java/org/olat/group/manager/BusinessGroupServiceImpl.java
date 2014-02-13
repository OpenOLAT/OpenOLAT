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
package org.olat.group.manager;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleObjectStateException;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.KnownIssueException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.async.ProgressDelegate;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupLazy;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.BusinessGroupView;
import org.olat.group.DeletableGroupData;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupMailing.MailType;
import org.olat.group.model.BGMembership;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.BGResourceRelation;
import org.olat.group.model.BusinessGroupEnvironment;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.BusinessGroupMembershipImpl;
import org.olat.group.model.BusinessGroupMembershipViewImpl;
import org.olat.group.model.BusinessGroupMembershipsChanges;
import org.olat.group.model.EnrollState;
import org.olat.group.model.IdentityGroupKey;
import org.olat.group.model.MembershipModification;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRightsRole;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.resource.accesscontrol.model.ResourceReservation;
import org.olat.testutils.codepoints.server.Codepoint;
import org.olat.user.UserDataDeletable;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("businessGroupService")
public class BusinessGroupServiceImpl implements BusinessGroupService, UserDataDeletable {
	private final OLog log = Tracing.createLoggerFor(BusinessGroupServiceImpl.class);

	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BGRightManager rightManager;
	@Autowired
	private BusinessGroupModule groupModule;
	@Autowired
	private BusinessGroupDAO businessGroupDAO;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ContactDAO contactDao;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDAO;
	@Autowired
	private BusinessGroupImportExport businessGroupImportExport;
	@Autowired
	private BusinessGroupArchiver businessGroupArchiver;
	@Autowired
	private UserDeletionManager userDeletionManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private ACService acService;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private DB dbInstance;
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		// remove as Participant 
		List<BusinessGroup> attendedGroups = findBusinessGroupsAttendedBy(identity, null);
		for (Iterator<BusinessGroup> iter = attendedGroups.iterator(); iter.hasNext();) {
			securityManager.removeIdentityFromSecurityGroup(identity, iter.next().getPartipiciantGroup());
		}
		log.debug("Remove partipiciant identity=" + identity + " from " + attendedGroups.size() + " groups");
		// remove from waitinglist 
		List<BusinessGroup> waitingGroups = findBusinessGroupsWithWaitingListAttendedBy(identity, null);
		for (Iterator<BusinessGroup> iter = waitingGroups.iterator(); iter.hasNext();) {
			securityManager.removeIdentityFromSecurityGroup(identity, iter.next().getWaitingGroup());
		}
		log.debug("Remove from waiting-list identity=" + identity + " in " + waitingGroups.size() + " groups");

		// remove as owner
		List<BusinessGroup> ownerGroups = findBusinessGroupsOwnedBy(identity, null);
		for (Iterator<BusinessGroup> iter = ownerGroups.iterator(); iter.hasNext();) {
			BusinessGroup businessGroup = iter.next();
			securityManager.removeIdentityFromSecurityGroup(identity, businessGroup.getOwnerGroup());
			if (securityManager.countIdentitiesOfSecurityGroup(businessGroup.getOwnerGroup()) == 0) {
				securityManager.addIdentityToSecurityGroup(userDeletionManager.getAdminIdentity(), businessGroup.getOwnerGroup());
				log.info("Delete user-data, add Administrator-identity as owner of businessGroup=" + businessGroup.getName());
			}
		}
		log.debug("Remove owner identity=" + identity + " from " + ownerGroups.size() + " groups");
		log.debug("All entries in groups deleted for identity=" + identity);
	}

	@Override
	public BusinessGroup createBusinessGroup(Identity creator, String name, String description,
			Integer minParticipants, Integer maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled,
			RepositoryEntry re) {
		return createBusinessGroup(creator, name,  description, null, null,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled, re);
	}

	@Override
	public BusinessGroup createBusinessGroup(Identity creator, String name, String description,
			String externalId, String managedFlags, Integer minParticipants, Integer maxParticipants,
			boolean waitingListEnabled, boolean autoCloseRanksEnabled, RepositoryEntry re) {
		
		if("".equals(managedFlags) || "none".equals(managedFlags)) {
			managedFlags = null;
		}
		
		BusinessGroup group = businessGroupDAO.createAndPersist(creator, name, description, externalId, managedFlags,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled, false, false, false);
		if(re != null) {
			addResourceTo(group, re);
		}
		return group;
	}

	@Override
	public BusinessGroup updateBusinessGroup(Identity ureqIdentity, BusinessGroup group, String name, String description,
			String externalId, String managedFlags, Integer minParticipants, Integer maxParticipants) {
		
		BusinessGroup bg = businessGroupDAO.loadForUpdate(group.getKey());

		Integer previousMaxParticipants = bg.getMaxParticipants();
		bg.setName(name);
		bg.setDescription(description);
		bg.setMaxParticipants(maxParticipants);
		bg.setMinParticipants(minParticipants);
		bg.setLastUsage(new Date(System.currentTimeMillis()));
		
		//strip
		if("none".equals(managedFlags) || "".equals(managedFlags)) {
			managedFlags = null;
		}
		bg.setManagedFlagsString(managedFlags);
		bg.setExternalId(externalId);

		//auto rank if possible
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		autoRankCheck(ureqIdentity, bg, previousMaxParticipants, events);
		BusinessGroup updatedGroup = businessGroupDAO.merge(bg);
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return updatedGroup;
	}

	@Override
	public BusinessGroup updateBusinessGroup(Identity ureqIdentity, BusinessGroup group, String name, String description,
			Integer minParticipants, Integer maxParticipants, Boolean waitingList, Boolean autoCloseRanks) {
		
		BusinessGroup bg = businessGroupDAO.loadForUpdate(group.getKey());
		
		Integer previousMaxParticipants = bg.getMaxParticipants();
		bg.setName(name);
		bg.setDescription(description);
		bg.setMaxParticipants(maxParticipants);
		bg.setMinParticipants(minParticipants);
		bg.setWaitingListEnabled(waitingList);
		if (waitingList != null && waitingList.booleanValue() && bg.getWaitingGroup() == null) {
			// Waitinglist is enabled but not created => Create waitingGroup
			SecurityGroup waitingGroup = securityManager.createAndPersistSecurityGroup();
			bg.setWaitingGroup(waitingGroup);
		}
		bg.setAutoCloseRanksEnabled(autoCloseRanks);
		bg.setLastUsage(new Date(System.currentTimeMillis()));
		//auto rank if possible

		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		autoRankCheck(ureqIdentity, bg, previousMaxParticipants, events);
		BusinessGroup mergedGroup = businessGroupDAO.merge(bg);
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return mergedGroup;
	}
	
	private void autoRankCheck(Identity identity, BusinessGroup updatedGroup, Integer previousMaxParticipants,
			List<BusinessGroupModifiedEvent.Deferred> events) {
		if(updatedGroup.getWaitingListEnabled() == null || !updatedGroup.getWaitingListEnabled().booleanValue()
				|| updatedGroup.getAutoCloseRanksEnabled() == null || !updatedGroup.getAutoCloseRanksEnabled().booleanValue()) {
			//do not check further, no waiting list, no automatic ranks
			return;
		}
		
		int currentMaxNumber = updatedGroup.getMaxParticipants() == null || updatedGroup.getMaxParticipants().intValue() <= 0
				? -1 : updatedGroup.getMaxParticipants().intValue();
		int previousMaxNumber = previousMaxParticipants == null || previousMaxParticipants.intValue() <= 0
				? -1 : previousMaxParticipants.intValue();
		
		if(currentMaxNumber > previousMaxNumber) {
			//I can rank up some users
			transferFirstIdentityFromWaitingToParticipant(identity, updatedGroup, null, events);
		}
	}

	@Override
	public BusinessGroup updateDisplayMembers(BusinessGroup group,
			boolean ownersIntern, boolean participantsIntern, boolean waitingListIntern,
			boolean ownersPublic, boolean participantsPublic, boolean waitingListPublic,
			boolean download) {
		
		BusinessGroup reloadedBusinessGroup = businessGroupDAO.loadForUpdate(group.getKey());
		BusinessGroup mergedGroup = null;
		if(reloadedBusinessGroup != null) {
			reloadedBusinessGroup.setOwnersVisibleIntern(ownersIntern);
			reloadedBusinessGroup.setOwnersVisiblePublic(ownersPublic);
			reloadedBusinessGroup.setParticipantsVisibleIntern(participantsIntern);
			reloadedBusinessGroup.setParticipantsVisiblePublic(participantsPublic);
			reloadedBusinessGroup.setWaitingListVisibleIntern(waitingListIntern);
			reloadedBusinessGroup.setWaitingListVisiblePublic(waitingListPublic);
			reloadedBusinessGroup.setDownloadMembersLists(download);
			mergedGroup = businessGroupDAO.merge(reloadedBusinessGroup);
		}
		dbInstance.commit();
		return mergedGroup;
	}

	@Override
	public BusinessGroup setLastUsageFor(final Identity identity, final BusinessGroup group) {
		BusinessGroup reloadedBusinessGroup = businessGroupDAO.loadForUpdate(group.getKey());
		BusinessGroup mergedGroup = null;
		if(reloadedBusinessGroup != null) {
			reloadedBusinessGroup.setLastUsage(new Date());
			if(identity != null) {
				List<SecurityGroup> secGroups = new ArrayList<SecurityGroup>();
				if(group.getOwnerGroup() != null) {
					secGroups.add(group.getOwnerGroup());
				}
				if(group.getPartipiciantGroup() != null) {
					secGroups.add(group.getPartipiciantGroup());
				}
				if(group.getWaitingGroup() != null) {
					secGroups.add(group.getWaitingGroup());
				}
				securityManager.touchMembership(identity, secGroups);
			}
			mergedGroup = businessGroupDAO.merge(reloadedBusinessGroup);
		}
		dbInstance.commit();
		return mergedGroup;
	}

	@Override
	public BusinessGroup loadBusinessGroup(BusinessGroup group) {
		return businessGroupDAO.load(group.getKey());
	}

	@Override
	public BusinessGroup loadBusinessGroup(Long key) {
		return businessGroupDAO.load(key);
	}

	@Override
	public BusinessGroup loadBusinessGroup(OLATResource resource) {
		return businessGroupDAO.load(resource.getResourceableId());
	}

	@Override
	public List<BusinessGroup> loadBusinessGroups(Collection<Long> keys) {
		return businessGroupDAO.load(keys);
	}

	@Override
	public List<BusinessGroupShort> loadShortBusinessGroups(Collection<Long> keys) {
		return businessGroupDAO.loadShort(keys);
	}

	@Override
	public List<BusinessGroup> loadAllBusinessGroups() {
		return businessGroupDAO.loadAll();
	}

	@Override
	public BusinessGroup copyBusinessGroup(Identity identity, BusinessGroup sourceBusinessGroup, String targetName, String targetDescription,
			Integer targetMin, Integer targetMax,  boolean copyAreas, boolean copyCollabToolConfig, boolean copyRights,
			boolean copyOwners, boolean copyParticipants, boolean copyMemberVisibility, boolean copyWaitingList, boolean copyRelations) {

		// 1. create group, set waitingListEnabled, enableAutoCloseRanks like source business-group
		BusinessGroup newGroup = createBusinessGroup(null, targetName, targetDescription, targetMin, targetMax, 
				sourceBusinessGroup.getWaitingListEnabled(), sourceBusinessGroup.getAutoCloseRanksEnabled(), null);
		// return immediately with null value to indicate an already take groupname
		if (newGroup == null) { 
			return null;
		}
		// 2. copy tools
		if (copyCollabToolConfig) {
			CollaborationToolsFactory toolsF = CollaborationToolsFactory.getInstance();
			// get collab tools from original group and the new group
			CollaborationTools oldTools = toolsF.getOrCreateCollaborationTools(sourceBusinessGroup);
			CollaborationTools newTools = toolsF.getOrCreateCollaborationTools(newGroup);
			// copy the collab tools settings
			String[] availableTools = CollaborationToolsFactory.getInstance().getAvailableTools().clone();
			for (int i = 0; i < availableTools.length; i++) {
				String tool = availableTools[i];
				newTools.setToolEnabled(tool, oldTools.isToolEnabled(tool));
			}			
			String oldNews = oldTools.lookupNews();
			newTools.saveNews(oldNews);
		}
		// 3. copy member visibility
		if (copyMemberVisibility) {
			newGroup.setOwnersVisibleIntern(sourceBusinessGroup.isOwnersVisibleIntern());
			newGroup.setOwnersVisiblePublic(sourceBusinessGroup.isOwnersVisiblePublic());
			newGroup.setParticipantsVisibleIntern(sourceBusinessGroup.isParticipantsVisibleIntern());
			newGroup.setParticipantsVisiblePublic(sourceBusinessGroup.isParticipantsVisiblePublic());
			newGroup.setWaitingListVisibleIntern(sourceBusinessGroup.isWaitingListVisibleIntern());
			newGroup.setWaitingListVisiblePublic(sourceBusinessGroup.isWaitingListVisiblePublic());
			newGroup.setDownloadMembersLists(sourceBusinessGroup.isDownloadMembersLists());
		}
		// 4. copy areas
		if (copyAreas) {
			List<BGArea> areas = areaManager.findBGAreasOfBusinessGroup(sourceBusinessGroup);
			for(BGArea area : areas) {
				// reference target group to source groups areas
				areaManager.addBGToBGArea(newGroup, area);
			}
		}
		// 5. copy owners
		if (copyOwners) {
			List<Identity> owners = securityManager.getIdentitiesOfSecurityGroup(sourceBusinessGroup.getOwnerGroup());
			if(owners.isEmpty()) {
				securityManager.addIdentityToSecurityGroup(identity, newGroup.getOwnerGroup());
			} else {
				for (Identity owner:owners) {
					securityManager.addIdentityToSecurityGroup(owner, newGroup.getOwnerGroup());
				}
			}
		} else {
			securityManager.addIdentityToSecurityGroup(identity, newGroup.getOwnerGroup());
		}
		// 6. copy participants
		if (copyParticipants) {
			List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(sourceBusinessGroup.getPartipiciantGroup());
			for(Identity participant:participants) {
				securityManager.addIdentityToSecurityGroup(participant, newGroup.getPartipiciantGroup());
			}
		}
		// 7. copy rights
		if (copyRights) {
			List<String> participantRights = rightManager.findBGRights(sourceBusinessGroup, BGRightsRole.participant);
			for (String sourceRight:participantRights) {
				rightManager.addBGRight(sourceRight, newGroup, BGRightsRole.participant);
			}
			List<String> tutorRights = rightManager.findBGRights(sourceBusinessGroup, BGRightsRole.tutor);
			for (String sourceRight:tutorRights) {
				rightManager.addBGRight(sourceRight, newGroup, BGRightsRole.tutor);
			}
			
		}
		// 8. copy waiting-lisz
		if (copyWaitingList) {
			List<Identity> waitingList = securityManager.getIdentitiesOfSecurityGroup(sourceBusinessGroup.getWaitingGroup());
			for (Identity waiting:waitingList) {
				securityManager.addIdentityToSecurityGroup(waiting, newGroup.getWaitingGroup());
			}
		}
		//9. copy relations
		if(copyRelations) {
			List<RepositoryEntry> resources = businessGroupRelationDAO.findRepositoryEntries(Collections.singletonList(sourceBusinessGroup), 0, -1);
			addResourcesTo(Collections.singletonList(newGroup), resources);
		}
		return newGroup;
	}

	@Override
	public BusinessGroup mergeBusinessGroups(final Identity ureqIdentity, BusinessGroup targetGroup,
			final List<BusinessGroup> groupsToMerge, MailPackage mailing) {
		groupsToMerge.remove(targetGroup);//to be sure
		Roles ureqRoles = securityManager.getRoles(ureqIdentity);

		targetGroup = businessGroupDAO.loadForUpdate(targetGroup.getKey());
		Set<Identity> currentOwners
			= new HashSet<Identity>(securityManager.getIdentitiesOfSecurityGroup(targetGroup.getOwnerGroup()));
		Set<Identity> currentParticipants 
			= new HashSet<Identity>(securityManager.getIdentitiesOfSecurityGroup(targetGroup.getPartipiciantGroup()));
		Set<Identity> currentWaiters
			= new HashSet<Identity>(securityManager.getIdentitiesOfSecurityGroup(targetGroup.getWaitingGroup()));

		Set<Identity> newOwners = new HashSet<Identity>();
		Set<Identity> newParticipants = new HashSet<Identity>();
		Set<Identity> newWaiters = new HashSet<Identity>();
		
		//collect the owners
		for(BusinessGroup group:groupsToMerge) {
			List<Identity> owners = securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
			owners.removeAll(currentOwners);
			newOwners.addAll(owners);
		}
		
		//collect the participants but test if they are not already owners
		for(BusinessGroup group:groupsToMerge) {
			List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
			participants.removeAll(currentParticipants);
			for(Identity participant:participants) {
				if(!newOwners.contains(participant)) {
					newParticipants.add(participant);
				}
			}
		}
		
		//collect the waiting list but test if they are not already owners or participants
		for(BusinessGroup group:groupsToMerge) {
			List<Identity> waitingList = securityManager.getIdentitiesOfSecurityGroup(group.getWaitingGroup());
			waitingList.removeAll(currentWaiters);
			for(Identity waiter:waitingList) {
				if(!newOwners.contains(waiter) && !newParticipants.contains(waiter)) {
					newWaiters.add(waiter);
				}
			}
		}

		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		
		for(Identity newOwner:newOwners) {
			addOwner(ureqIdentity, ureqRoles, newOwner, targetGroup, mailing, events);
		}
		for(Identity newParticipant:newParticipants) {
			addParticipant(ureqIdentity, ureqRoles, newParticipant, targetGroup, mailing, events);
		}
		for(Identity newWaiter:newWaiters) {
			addToWaitingList(ureqIdentity, newWaiter, targetGroup, mailing, events);
		}
			
		for(BusinessGroup group:groupsToMerge) {
			deleteBusinessGroup(group);
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return targetGroup;
	}

	@Override
	public void updateMembership(Identity ureqIdentity, MembershipModification membersMod,
			List<BusinessGroup> groups, MailPackage mailing) {
		Roles ureqRoles = securityManager.getRoles(ureqIdentity);
		for(BusinessGroup group:groups) {
			updateMembers(ureqIdentity, ureqRoles, membersMod, group, mailing);
		}
	}
	
	private void updateMembers(Identity ureqIdentity, Roles ureqRoles, MembershipModification membersMod,
			BusinessGroup group, MailPackage mailing) {
		group = businessGroupDAO.loadForUpdate(group.getKey());
		
		List<Identity> currentOwners = securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
		List<Identity> currentParticipants = securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
		List<Identity> currentWaitingList = securityManager.getIdentitiesOfSecurityGroup(group.getWaitingGroup());
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();

		for(Identity owner:membersMod.getAddOwners()) {
			if(!currentOwners.contains(owner)) {
				addOwner(ureqIdentity, ureqRoles, owner, group, mailing, events);
			}
		}
		for(Identity participant:membersMod.getAddParticipants()) {
			if(!currentParticipants.contains(participant)) {
				addParticipant(ureqIdentity, ureqRoles, participant, group, mailing, events);
			}
		}
		for(Identity waitingIdentity:membersMod.getAddToWaitingList()) {
			if(!currentWaitingList.contains(waitingIdentity)) {
				addToWaitingList(ureqIdentity, waitingIdentity, group, mailing, events);
			}
		}

		for(Identity removed:membersMod.getRemovedIdentities()) {
			if(currentOwners.contains(removed)) {
				removeOwner(ureqIdentity, removed, group, events);
			}
			if(currentParticipants.contains(removed)) {
				removeParticipant(ureqIdentity, removed, group, mailing, events);
			}
			if(currentWaitingList.contains(removed)) {
				removeFromWaitingList(ureqIdentity, removed, group, mailing, events);
			}
		}
		
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}

	@Override
	public void updateMemberships(final Identity ureqIdentity, final List<BusinessGroupMembershipChange> changes,
			MailPackage mailing) {
		Roles ureqRoles = securityManager.getRoles(ureqIdentity);
		Map<Long,BusinessGroupMembershipsChanges> changesMap = new HashMap<Long,BusinessGroupMembershipsChanges>();
		for(BusinessGroupMembershipChange change:changes) {
			BusinessGroupMembershipsChanges changesWrapper;
			if(changesMap.containsKey(change.getGroupKey())) {
				changesWrapper = changesMap.get(change.getGroupKey());
			} else {
				changesWrapper = new BusinessGroupMembershipsChanges();
				changesMap.put(change.getGroupKey(), changesWrapper);
			}
			
			Identity id = change.getMember();
			if(change.getTutor() != null) {
				if(change.getTutor().booleanValue()) {
					changesWrapper.addTutors.add(id);
				} else {
					changesWrapper.removeTutors.add(id);
				}
			}
			
			if(change.getParticipant() != null) {
				if(change.getParticipant().booleanValue()) {
					changesWrapper.addParticipants.add(id);
				} else {
					changesWrapper.removeParticipants.add(id);
				}	
			}
			
			if(change.getWaitingList() != null) {
				if(change.getWaitingList().booleanValue()) {
					changesWrapper.addToWaitingList.add(id);
				} else {
					changesWrapper.removeFromWaitingList.add(id);
				}
			}
		}
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		List<BusinessGroup> groups = loadBusinessGroups(changesMap.keySet());
		for(BusinessGroup group:groups) {
			BusinessGroupMembershipsChanges changesWrapper = changesMap.get(group.getKey());
			group = businessGroupDAO.loadForUpdate(group.getKey());
					
			for(Identity id:changesWrapper.addToWaitingList) {
				addToWaitingList(ureqIdentity, id, group, mailing, events);
			}
			for(Identity id:changesWrapper.removeFromWaitingList) {
				removeFromWaitingList(ureqIdentity, id, group, mailing, events);
			}
			for(Identity id:changesWrapper.addTutors) {
				addOwner(ureqIdentity, ureqRoles, id, group, mailing, events);
			}
			for(Identity id:changesWrapper.removeTutors) {
				removeOwner(ureqIdentity, id, group, events);
			}
			for(Identity id:changesWrapper.addParticipants) {
				addParticipant(ureqIdentity, ureqRoles, id, group, mailing, events);
			}
			for(Identity id:changesWrapper.removeParticipants) {
				removeParticipant(ureqIdentity, id, group, mailing, events);
			}
			//release lock
			dbInstance.commit();
		}
		
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}

	@Override
	public BusinessGroup findBusinessGroup(SecurityGroup secGroup) {
		return businessGroupDAO.findBusinessGroup(secGroup);
	}

	@Override
	public List<BusinessGroupLazy> findBusinessGroups(Identity identity, int maxResults, BusinessGroupOrder... orderBy) {
		return businessGroupDAO.findBusinessGroup(identity, maxResults, orderBy);
	}

	@Override
	public List<BusinessGroup> findBusinessGroupsOwnedBy(Identity identity, OLATResource resource) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, true, false);
		return businessGroupDAO.findBusinessGroups(params, resource, 0, -1);
	}
	
	@Override
	public List<BusinessGroup> findBusinessGroupsAttendedBy(Identity identity, OLATResource resource) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, false, true);
		return businessGroupDAO.findBusinessGroups(params, resource, 0, -1);
	}
	
	@Override
	public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(Identity identity,  OLATResource resource) {
		return businessGroupDAO.findBusinessGroupsWithWaitingListAttendedBy(identity, resource);
	}
	
	@Override
	public int countBusinessGroups(SearchBusinessGroupParams params, OLATResource resource) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.countBusinessGroups(params, resource);
	}

	@Override
	public List<BusinessGroup> findBusinessGroups(SearchBusinessGroupParams params, OLATResource resource,
			int firstResult, int maxResults, BusinessGroupOrder... ordering) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.findBusinessGroups(params, resource, firstResult, maxResults);
	}

	@Override
	public int countBusinessGroupViews(SearchBusinessGroupParams params, OLATResource resource) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.countBusinessGroupViews(params, resource);
	}

	@Override
	public List<BusinessGroupView> findBusinessGroupViews(SearchBusinessGroupParams params, OLATResource resource, int firstResult,
			int maxResults, BusinessGroupOrder... ordering) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.findBusinessGroupViews(params, resource, firstResult, maxResults);
	}

	@Override
	public List<BusinessGroupView> findBusinessGroupViewsWithAuthorConnection(Identity author) {
		return businessGroupDAO.findBusinessGroupWithAuthorConnection(author);
	}

	@Override
	public List<Long> toGroupKeys(String groupNames, OLATResource resource) {
		return businessGroupRelationDAO.toGroupKeys(groupNames, resource);
	}

	@Override
	public int countContacts(Identity identity) {
		return contactDao.countContacts(identity);
	}

	@Override
	public List<Identity> findContacts(Identity identity, int firstResult, int maxResults) {
		return contactDao.findContacts(identity, firstResult, maxResults);
	}

	@Override
	public void deleteBusinessGroup(BusinessGroup group) {
		try{
			OLATResourceableJustBeforeDeletedEvent delEv = new OLATResourceableJustBeforeDeletedEvent(group);
			// notify all (currently running) BusinessGroupXXXcontrollers
			// about the deletion which will occur.
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(delEv, group);
	
			// refresh object to avoid stale object exceptions
			group = loadBusinessGroup(group);
			// 0) Loop over all deletableGroupData
			Map<String,DeletableGroupData> deleteListeners = CoreSpringFactory.getBeansOfType(DeletableGroupData.class);
			for (DeletableGroupData deleteListener : deleteListeners.values()) {
				if(log.isDebug()) {
					log.debug("deleteBusinessGroup: call deleteListener=" + deleteListener);
				}
				deleteListener.deleteGroupDataFor(group);
			} 
			
			// 1) Delete all group properties
			CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
			ct.deleteTools(group);// deletes everything concerning properties&collabTools
			// 1.c)delete user in security groups
			//removeFromRepositoryEntrySecurityGroup(group);
			// 2) Delete the group areas
			areaManager.deleteBGtoAreaRelations(group);
			// 3) Delete the group object itself on the database
			businessGroupRelationDAO.deleteRelations(group);
			businessGroupDAO.delete(group);
			// 4) Delete the associated security groups
			if(group.getOwnerGroup() != null) {
				securityManager.deleteSecurityGroup(group.getOwnerGroup());
			}
			// in all cases the participant groups
			if(group.getPartipiciantGroup() != null) {
				securityManager.deleteSecurityGroup(group.getPartipiciantGroup());
			}
			// Delete waiting-group when one exists
			if (group.getWaitingGroup() != null) {
				securityManager.deleteSecurityGroup(group.getWaitingGroup());
			}
	
			// delete the publisher attached to this group (e.g. the forum and folder
			// publisher)
			notificationsManager.deletePublishersOf(group);

			dbInstance.commit();
	
			log.audit("Deleted Business Group", group.toString());
		} catch(DBRuntimeException dbre) {
			Throwable th = dbre.getCause();
			if ((th instanceof ObjectNotFoundException) && th.getMessage().contains("org.olat.group.BusinessGroupImpl")) {
				//group already deleted
				return;
			}
			if ((th instanceof StaleObjectStateException) &&
					(th.getMessage().startsWith("Row was updated or deleted by another transaction"))) {
				// known issue OLAT-3654
				log.info("Group was deleted by another user in the meantime. Known issue OLAT-3654");
				throw new KnownIssueException("Group was deleted by another user in the meantime", 3654);
			} else {
				throw dbre;
			}
		}
	}

	@Override
	public MailerResult deleteBusinessGroupWithMail(BusinessGroup businessGroupTodelete, String businessPath, Identity deletedBy, Locale locale) {
		Codepoint.codepoint(this.getClass(), "deleteBusinessGroupWithMail");
			
		// collect data for mail
		BaseSecurity secMgr = BaseSecurityManager.getInstance();
		List<Identity> users = new ArrayList<Identity>();
		SecurityGroup ownerGroup = businessGroupTodelete.getOwnerGroup();
		if (ownerGroup != null) {
			List<Identity> owner = secMgr.getIdentitiesOfSecurityGroup(ownerGroup);
			users.addAll(owner);
		}
		SecurityGroup partGroup = businessGroupTodelete.getPartipiciantGroup();
		if (partGroup != null) {
			List<Identity> participants = secMgr.getIdentitiesOfSecurityGroup(partGroup);
			users.addAll(participants);
		}
		SecurityGroup watiGroup = businessGroupTodelete.getWaitingGroup();
		if (watiGroup != null) {
			List<Identity> waiting = secMgr.getIdentitiesOfSecurityGroup(watiGroup);
			users.addAll(waiting);
		}
		// now delete the group first
		deleteBusinessGroup(businessGroupTodelete);
		dbInstance.commit();
		// finally send email
		MailTemplate mailTemplate = BGMailHelper.createDeleteGroupMailTemplate(businessGroupTodelete, deletedBy);
		if (mailTemplate != null) {
			String metaId = UUID.randomUUID().toString();
			MailContext context = new MailContextImpl(businessPath);
			MailerResult result = new MailerResult();
			MailBundle[] bundles = mailManager.makeMailBundles(context, users, mailTemplate, null, metaId, result);
			result.append(mailManager.sendMessage(bundles));
			return result;
		}
		return null;
	}

	@Override
	public int countMembersOf(OLATResource resource, boolean owner, boolean attendee) {
		return businessGroupRelationDAO.countMembersOf(resource, owner, attendee);
	}

	@Override
	public List<Identity> getMembersOf(OLATResource resource, boolean owner, boolean attendee) {
		return businessGroupRelationDAO.getMembersOf(resource, owner, attendee);
	}

	@Override
	public BusinessGroupAddResponse addOwners(Identity ureqIdentity, Roles ureqRoles, List<Identity> addIdentities,
			BusinessGroup group, MailPackage mailing) {
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		for (Identity identity : addIdentities) {
			group = loadBusinessGroup(group); // reload business group
			if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
				response.getIdentitiesWithoutPermission().add(identity);
			} else if(addOwner(ureqIdentity, ureqRoles, identity, group, mailing, events)) {
				response.getAddedIdentities().add(identity);
			} else {
				response.getIdentitiesAlreadyInGroup().add(identity);
			}
		}
		
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return response;
	}
	
	private boolean addOwner(Identity ureqIdentity, Roles ureqRoles, Identity identityToAdd, BusinessGroup group, MailPackage mailing,
			List<BusinessGroupModifiedEvent.Deferred> events) {
		if (!securityManager.isIdentityInSecurityGroup(identityToAdd, group.getOwnerGroup())) {
			boolean mustAccept = true;
			if(ureqIdentity != null && ureqIdentity.equals(identityToAdd)) {
				mustAccept = false;//adding itself, we hope that he knows what he makes
			} else if(ureqRoles == null || ureqIdentity == null) {
				mustAccept = false;//administrative task
			} else {
				mustAccept = groupModule.isAcceptMembership(ureqRoles);
			}

			if(mustAccept) {
				ResourceReservation olderReservation = reservationDao.loadReservation(identityToAdd, group.getResource());
				if(olderReservation == null) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MONTH, 6);
					Date expiration = cal.getTime();
					ResourceReservation reservation =
							reservationDao.createReservation(identityToAdd, "group_coach", expiration, group.getResource());
					if(reservation != null) {
						BusinessGroupMailing.sendEmail(ureqIdentity, identityToAdd, group, MailType.addCoach, mailing);
						// logging
						log.audit("Idenitity(.key):" + ureqIdentity.getKey() + " added identity '" + identityToAdd.getName() + "' to securitygroup with key " + group.getOwnerGroup().getKey());
					}
				}
			} else {
				internalAddCoach(ureqIdentity, identityToAdd, group, events);
				BusinessGroupMailing.sendEmail(ureqIdentity, identityToAdd, group, MailType.addCoach, mailing);
			}
			return true;
		}
		return false;
	}
	
	private void internalAddCoach(Identity ureqIdentity, Identity identityToAdd, BusinessGroup group,
			List<BusinessGroupModifiedEvent.Deferred> events) {
		
		securityManager.addIdentityToSecurityGroup(identityToAdd, group.getOwnerGroup());
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identityToAdd);
		if(events != null) {
			events.add(event);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToAdd));
		log.audit("Idenitity(.key):" + ureqIdentity.getKey() + " added identity '" + identityToAdd.getName() + "' to securitygroup with key " + group.getOwnerGroup().getKey());
	}
	
	private boolean addParticipant(Identity ureqIdentity, Roles ureqRoles, Identity identityToAdd, BusinessGroup group,
			MailPackage mailing, List<BusinessGroupModifiedEvent.Deferred> events) {
		
		if(!securityManager.isIdentityInSecurityGroup(identityToAdd, group.getPartipiciantGroup())) {
			boolean mustAccept = true;
			if(ureqIdentity != null && ureqIdentity.equals(identityToAdd)) {
				mustAccept = false;//adding itself, we hope that he knows what he makes
			} else if(ureqRoles == null || ureqIdentity == null) {
				mustAccept = false;//administrative task
			} else {
				mustAccept = groupModule.isAcceptMembership(ureqRoles);
			}
			
			if(mustAccept) {
				ResourceReservation olderReservation = reservationDao.loadReservation(identityToAdd, group.getResource());
				if(olderReservation == null) {
					Calendar cal = Calendar.getInstance();
					cal.add(Calendar.MONTH, 6);
					Date expiration = cal.getTime();
					ResourceReservation reservation =
							reservationDao.createReservation(identityToAdd, "group_participant", expiration, group.getResource());
					if(reservation != null) {
						BusinessGroupMailing.sendEmail(ureqIdentity, identityToAdd, group, MailType.addParticipant, mailing);
					}
				}
			} else {
				internalAddParticipant(ureqIdentity, identityToAdd, group, events);
				BusinessGroupMailing.sendEmail(ureqIdentity, identityToAdd, group, MailType.addParticipant, mailing);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * this method is for internal usage only. It add the identity to to group without synchronization or checks!
	 * @param ureqIdentity
	 * @param ureqRoles
	 * @param identityToAdd
	 * @param group
	 * @param syncIM
	 */
	private void internalAddParticipant(Identity ureqIdentity, Identity identityToAdd, BusinessGroup group,
			List<BusinessGroupModifiedEvent.Deferred> events) {
		
		securityManager.addIdentityToSecurityGroup(identityToAdd, group.getPartipiciantGroup());

		// notify currently active users of this business group
		BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identityToAdd);
		if(events != null) {
			events.add(event);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToAdd));
		log.audit("Idenitity(.key):" + ureqIdentity.getKey() + " added identity '" + identityToAdd.getName() + "' to securitygroup with key " + group.getPartipiciantGroup().getKey());
		// send notification mail in your controller!
	}

	@Override
	public BusinessGroupAddResponse addParticipants(Identity ureqIdentity, Roles ureqRoles, List<Identity> addIdentities,
			BusinessGroup group, MailPackage mailing) {	
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();

		BusinessGroup currBusinessGroup = businessGroupDAO.loadForUpdate(group.getKey());	
		for (final Identity identity : addIdentities) {
			if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
				response.getIdentitiesWithoutPermission().add(identity);
			} else if(addParticipant(ureqIdentity, ureqRoles, identity, currBusinessGroup, mailing, events)) {
				response.getAddedIdentities().add(identity);
			} else {
				response.getIdentitiesAlreadyInGroup().add(identity);
			}
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return response;
	}
	
	@Override
	public void cancelPendingParticipation(Identity ureqIdentity, ResourceReservation reservation) {
		if(reservation != null && "BusinessGroup".equals(reservation.getResource().getResourceableTypeName())) {
			BusinessGroup group = businessGroupDAO.loadForUpdate(reservation.getResource().getResourceableId());
			if(group != null) {
				List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
				transferFirstIdentityFromWaitingToParticipant(ureqIdentity, group, null, events);
				dbInstance.commit();
				BusinessGroupModifiedEvent.fireDeferredEvents(events);
			}
		}
	}

	@Override
	public void acceptPendingParticipation(Identity ureqIdentity, Identity reservationOwner, OLATResource resource) {
		ResourceReservation reservation = acService.getReservation(reservationOwner, resource);
		if(reservation != null && "BusinessGroup".equals(resource.getResourceableTypeName())) {
			BusinessGroup group = businessGroupDAO.loadForUpdate(resource.getResourceableId());
			List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
			if(group != null) {
				String type = reservation.getType();
				if("group_coach".equals(type)) {
					if(!securityManager.isIdentityInSecurityGroup(reservationOwner, group.getOwnerGroup())) {
						internalAddCoach(ureqIdentity, reservationOwner, group, events);
					}
				} else if("group_participant".equals(type)) {
					if(!securityManager.isIdentityInSecurityGroup(reservationOwner, group.getPartipiciantGroup())) {
						internalAddParticipant(ureqIdentity, reservationOwner, group, events);
					}
				}
			}
			reservationDao.deleteReservation(reservation);
			dbInstance.commit();
			BusinessGroupModifiedEvent.fireDeferredEvents(events);
		}
	}

	private void removeParticipant(Identity ureqIdentity, Identity identity, BusinessGroup group, MailPackage mailing,
			List<BusinessGroupModifiedEvent.Deferred> events) {

		boolean removed = securityManager.removeIdentityFromSecurityGroup(identity, group.getPartipiciantGroup());
		if(removed) {
			//remove subscriptions if user gets removed
			removeSubscriptions(identity, group);
			
			// notify currently active users of this business group
			BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
			if(events != null) {
				events.add(event);
			}
			// do logging
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_REMOVED, getClass(), LoggingResourceable.wrap(identity), LoggingResourceable.wrap(group));
			log.audit("Idenitity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getName() + "' from securitygroup with key " + group.getPartipiciantGroup().getKey());
			// Check if a waiting-list with auto-close-ranks is configurated
			if ( group.getWaitingListEnabled().booleanValue() && group.getAutoCloseRanksEnabled().booleanValue() ) {
				// even when doOnlyPostRemovingStuff is set to true we really transfer the first Identity here
				transferFirstIdentityFromWaitingToParticipant(ureqIdentity, group, null, events);
			}	
			// send mail
			BusinessGroupMailing.sendEmail(ureqIdentity, identity, group, MailType.removeParticipant, mailing);
		}
	}
	
	@Override
	public void removeParticipants(Identity ureqIdentity, List<Identity> identities, BusinessGroup group, MailPackage mailing) {
		group = businessGroupDAO.loadForUpdate(group.getKey());
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		for (Identity identity : identities) {
		  removeParticipant(ureqIdentity, identity, group, mailing, events);
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}

	@Override
	public void removeMembers(Identity ureqIdentity, List<Identity> identities, OLATResource resource, MailPackage mailing) {
		if(identities == null || identities.isEmpty() || resource == null) return;//nothing to do
		
		List<BusinessGroup> groups = null;
		if("BusinessGroup".equals(resource.getResourceableTypeName())) {
			//it's a group resource
			BusinessGroup group = loadBusinessGroup(resource);
			if(group != null) {
				groups = Collections.singletonList(group);
			}
		} else {	
			groups = findBusinessGroups(null, resource, 0, -1);
		}
		if(groups == null || groups.isEmpty()) {
			return;//nothing to do
		}
		
		//remove managed groups
		for(Iterator<BusinessGroup> groupIt=groups.iterator(); groupIt.hasNext(); ) {
			boolean managed = BusinessGroupManagedFlag.isManaged(groupIt.next(), BusinessGroupManagedFlag.membersmanagement);
			if(managed) {
				groupIt.remove();
			}
		}
		
		if(groups.isEmpty()) {
			return;//nothing to do
		}

		List<OLATResource> groupResources = new ArrayList<OLATResource>();
		Map<Long,BusinessGroup> keyToGroupMap = new HashMap<Long,BusinessGroup>();
		for(BusinessGroup group:groups) {
			groupResources.add(group.getResource());
			keyToGroupMap.put(group.getKey(), group);
		}
		final Map<Long,Identity> keyToIdentityMap = new HashMap<Long,Identity>();
		for(Identity identity:identities) {
			keyToIdentityMap.put(identity.getKey(), identity);
		}

		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		List<BusinessGroupMembershipViewImpl> memberships = businessGroupDAO.getMembershipInfoInBusinessGroups(groups, identities);
		Collections.sort(memberships, new BusinessGroupMembershipViewComparator());

		BusinessGroupMembershipViewImpl nextGroupMembership = null;
		for(final Iterator<BusinessGroupMembershipViewImpl> itMembership=memberships.iterator(); nextGroupMembership != null || itMembership.hasNext(); ) {
			final BusinessGroupMembershipViewImpl currentMembership;
			if(nextGroupMembership == null) {
				currentMembership = itMembership.next();
			} else {
				currentMembership = nextGroupMembership;
				nextGroupMembership = null;
			}
			
			Long groupKey = currentMembership.getGroupKey();
			BusinessGroup nextGroup = businessGroupDAO.loadForUpdate(groupKey);
			nextGroupMembership = removeGroupMembers(ureqIdentity, currentMembership, nextGroup, keyToIdentityMap, itMembership, mailing, events);
			//release the lock
			dbInstance.commit();
		}

		List<ResourceReservation> reservations = acService.getReservations(groupResources);
		for(ResourceReservation reservation:reservations) {
			if(identities.contains(reservation.getIdentity())) {
				reservationDao.deleteReservation(reservation);
			}
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}
	
	private final BusinessGroupMembershipViewImpl removeGroupMembers(Identity ureqIdentity, BusinessGroupMembershipViewImpl currentMembership,
			BusinessGroup currentGroup, Map<Long,Identity> keyToIdentityMap, Iterator<BusinessGroupMembershipViewImpl> itMembership,
			MailPackage mailing, List<BusinessGroupModifiedEvent.Deferred> events) {

		BusinessGroupMembershipViewImpl previsousComputedMembership = currentMembership;
		BusinessGroupMembershipViewImpl membership;

		do {
			if(previsousComputedMembership != null) {
				membership = previsousComputedMembership;
				previsousComputedMembership = null;
			} else if(itMembership.hasNext()) {
				membership = itMembership.next();
			} else {
				//security, nothing to do
				return null;
			}
			
			if(currentGroup.getKey().equals(membership.getGroupKey())) {
				Identity id = keyToIdentityMap.get(membership.getIdentityKey());
				if(membership.getOwnerGroupKey() != null) {
					removeOwner(ureqIdentity, id, currentGroup, events);
				}
				if(membership.getParticipantGroupKey() != null) {
					removeParticipant(ureqIdentity, id, currentGroup, mailing, events);
				}
				if(membership.getWaitingGroupKey() != null) {
					removeFromWaitingList(ureqIdentity, id, currentGroup, mailing, events);
				}
			} else {
				return membership;
			}
		} while (itMembership.hasNext());

		return null;
	}

	private void addToWaitingList(Identity ureqIdentity, Identity identity, BusinessGroup group, MailPackage mailing,
			List<BusinessGroupModifiedEvent.Deferred> events) {
		securityManager.addIdentityToSecurityGroup(identity, group.getWaitingGroup());

		// notify currently active users of this business group
		BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
		if(events != null) {
			events.add(event);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_TO_WAITING_LIST_ADDED, getClass(), LoggingResourceable.wrap(identity));
		log.audit("Idenitity(.key):" + ureqIdentity.getKey() + " added identity '" + identity.getName() + "' to securitygroup with key " + group.getPartipiciantGroup().getKey());
		// send mail
		BusinessGroupMailing.sendEmail(ureqIdentity, identity, group, MailType.addToWaitingList, mailing);
	}
	
	@Override
	public BusinessGroupAddResponse addToWaitingList(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup group, MailPackage mailing) {
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		BusinessGroup currBusinessGroup = businessGroupDAO.loadForUpdate(group.getKey()); // reload business group
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();

		for (final Identity identity : addIdentities) {	
			if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
				response.getIdentitiesWithoutPermission().add(identity);
			}
			// Check if identity is already in group. make a db query in case
			// someone in another workflow already added this user to this group. if
			// found, add user to model
			else if (securityManager.isIdentityInSecurityGroup(ureqIdentity, currBusinessGroup.getWaitingGroup()) 
					|| securityManager.isIdentityInSecurityGroup(ureqIdentity, currBusinessGroup.getPartipiciantGroup()) ) {
				response.getIdentitiesAlreadyInGroup().add(identity);
			} else {
				// identity has permission and is not already in group => add it
				addToWaitingList(ureqIdentity, identity, currBusinessGroup, mailing, events);
				response.getAddedIdentities().add(identity);
			}
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return response;
	}

	private final void removeFromWaitingList(Identity ureqIdentity, Identity identity, BusinessGroup group, MailPackage mailing,
			List<BusinessGroupModifiedEvent.Deferred> events) {
		securityManager.removeIdentityFromSecurityGroup(identity, group.getWaitingGroup());
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
		if(events != null) {
			events.add(event);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_FROM_WAITING_LIST_REMOVED, getClass(), LoggingResourceable.wrap(identity));
		log.audit("Idenitity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getName() + "' from securitygroup with key " + group.getOwnerGroup().getKey());
		// send mail
		BusinessGroupMailing.sendEmail(ureqIdentity, identity, group, MailType.removeToWaitingList, mailing);
	}
	
	@Override
	public void removeFromWaitingList(Identity ureqIdentity, List<Identity> identities, BusinessGroup businessGroup, MailPackage mailing) {
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		businessGroup = businessGroupDAO.loadForUpdate(businessGroup.getKey());
		for (Identity identity : identities) {
		  removeFromWaitingList(ureqIdentity, identity, businessGroup, mailing, events);
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}
	
	@Override
	public int getPositionInWaitingListFor(Identity identity, BusinessGroup businessGroup) {
		// get position in waiting-list
		List<Object[]> identities = securityManager.getIdentitiesAndDateOfSecurityGroup(businessGroup.getWaitingGroup(), true);
		for (int i = 0; i<identities.size(); i++) {
		  Object[] co = identities.get(i);
		  Identity waitingListIdentity = (Identity) co[0];
		  if (waitingListIdentity.equals(identity) ) {
		  	return i+1;// '+1' because list begins with 0 
		  }
		}
		return -1;
	}

	@Override
	public BusinessGroupAddResponse moveIdentityFromWaitingListToParticipant(Identity ureqIdentity, List<Identity> identities, 
			BusinessGroup currBusinessGroup, MailPackage mailing) {
		
		Roles ureqRoles = securityManager.getRoles(ureqIdentity);
		
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		currBusinessGroup = businessGroupDAO.loadForUpdate(currBusinessGroup.getKey());
		
		for (Identity identity : identities) {
			// check if identity is already in participant
			if (!securityManager.isIdentityInSecurityGroup(identity,currBusinessGroup.getPartipiciantGroup()) ) {
				// Identity is not in participant-list => move idenity from waiting-list to participant-list
				addParticipant(ureqIdentity, ureqRoles, identity, currBusinessGroup, mailing, events);
				removeFromWaitingList(ureqIdentity, identity, currBusinessGroup, mailing, events);
				response.getAddedIdentities().add(identity);
				// notification mail is handled in controller
			} else {
				response.getIdentitiesAlreadyInGroup().add(identity);
			}
		}

		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return response;
	}
	
	@Override
	public void removeAndFireEvent(Identity ureqIdentity, List<Identity> identities, SecurityGroup secGroup) {
		for (Identity identity : identities) {
			securityManager.removeIdentityFromSecurityGroup(identity, secGroup);
		  log.audit("Idenitity(.key):" + ureqIdentity.getKey() + " removed identity '" + identity.getName() + "' from securitygroup with key " + secGroup.getKey());
		}
	}
	
	@Override
	public EnrollState enroll(Identity ureqIdentity, Roles ureqRoles, Identity identity, BusinessGroup group,
			MailPackage mailing) {
		final BusinessGroup reloadedGroup = businessGroupDAO.loadForUpdate(group.getKey());
		
		log.info("doEnroll start: group=" + OresHelper.createStringRepresenting(group), identity.getName());
		EnrollState enrollStatus = new EnrollState();
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();

		ResourceReservation reservation = acService.getReservation(identity, reloadedGroup.getResource());
		
		//reservation has the highest priority over max participant or other settings
		if(reservation != null) {
			addParticipant(ureqIdentity, ureqRoles, identity, reloadedGroup, mailing, events);
			enrollStatus.setEnrolled(BGMembership.participant);
			log.info("doEnroll (reservation) - setIsEnrolled ", identity.getName());
			if(reservation != null) {
				reservationDao.deleteReservation(reservation);
			}
		} else if (reloadedGroup.getMaxParticipants() != null) {
			int participantsCounter = securityManager.countIdentitiesOfSecurityGroup(reloadedGroup.getPartipiciantGroup());
			int reservations = acService.countReservations(reloadedGroup.getResource());
			
			log.info("doEnroll - participantsCounter: " + participantsCounter + ", reservations: " + reservations + " maxParticipants: " + reloadedGroup.getMaxParticipants().intValue(), identity.getName());
			if (reservation == null && (participantsCounter + reservations) >= reloadedGroup.getMaxParticipants().intValue()) {
				// already full, show error and updated choose page again
				if (reloadedGroup.getWaitingListEnabled().booleanValue()) {
					addToWaitingList(ureqIdentity, identity, reloadedGroup, mailing, events);
					enrollStatus.setEnrolled(BGMembership.waiting);
				} else {
					// No Waiting List => List is full
					enrollStatus.setI18nErrorMessage("error.group.full");
					enrollStatus.setFailed(true);
				}
			} else {
				//enough place
				addParticipant(ureqIdentity, ureqRoles, identity, reloadedGroup, mailing, events);
				enrollStatus.setEnrolled(BGMembership.participant);
				log.info("doEnroll - setIsEnrolled ", identity.getName());
			}
		} else {
			if (log.isDebug()) log.debug("doEnroll as participant beginTransaction");
			addParticipant(ureqIdentity, ureqRoles, identity, reloadedGroup, mailing, events);
			enrollStatus.setEnrolled(BGMembership.participant);						
			if (log.isDebug()) log.debug("doEnroll as participant committed");
		}

		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		log.info("doEnroll end", identity.getName());
		return enrollStatus;
	}

	/**
	 * Don't forget to lock the business group before calling this method.
	 * @param ureqIdentity
	 * @param group
	 * @param mailing
	 * @param syncIM
	 */
	private void transferFirstIdentityFromWaitingToParticipant(Identity ureqIdentity, BusinessGroup group, 
			MailPackage mailing, List<BusinessGroupModifiedEvent.Deferred> events) {

		// Check if waiting-list is enabled and auto-rank-up
		if (group.getWaitingListEnabled() != null && group.getWaitingListEnabled().booleanValue()
				&& group.getAutoCloseRanksEnabled() != null && group.getAutoCloseRanksEnabled().booleanValue()) {
			// Check if participant is not full
			Integer maxSize = group.getMaxParticipants();
			int reservations = acService.countReservations(group.getResource());
			int partipiciantSize = securityManager.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
			if (maxSize != null && (partipiciantSize + reservations) < maxSize.intValue()) {
				// ok it has free places => get first identities from waiting list
				List<Object[]> identities = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getWaitingGroup(), true/*sortedByAddedDate*/);
				
				int counter = 0;
				int freeSlot = maxSize - (partipiciantSize + reservations);
			  for(Object[] co: identities) {
			  	if(counter >= freeSlot) {
			  		break;
			  	}
			  	
			  	// It has an identity and transfer from waiting-list to participant-group is not done
					Identity firstWaitingListIdentity = (Identity) co[0];
					// Check if firstWaitingListIdentity is not allready in participant-group
					if (!securityManager.isIdentityInSecurityGroup(firstWaitingListIdentity,group.getPartipiciantGroup())) {
						// move the identity from the waitinglist to the participant group
						
						ActionType formerStickyActionType = ThreadLocalUserActivityLogger.getStickyActionType();
						try{
							// OLAT-4955: force add-participant and remove-from-waitinglist logging actions 
							//            that get triggered in the next two methods to be of ActionType admin
							//            This is needed to make sure the targetIdentity ends up in the o_loggingtable
							ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
							MailPackage subMailing = new MailPackage(false);//doesn0t send these emails but a specific one
							addParticipant(ureqIdentity, null, firstWaitingListIdentity, group, subMailing, events);
							removeFromWaitingList(ureqIdentity, firstWaitingListIdentity, group, subMailing, events);
						} finally {
							ThreadLocalUserActivityLogger.setStickyActionType(formerStickyActionType);
						}

						BusinessGroupMailing.sendEmail(ureqIdentity, firstWaitingListIdentity, group, MailType.graduateFromWaitingListToParticpant, mailing);				
						counter++;
				  }
				}
			}
		}
	}
	
	private void removeOwner(Identity ureqIdentity, Identity identityToRemove, BusinessGroup group,
			List<BusinessGroupModifiedEvent.Deferred> events) {
		
		securityManager.removeIdentityFromSecurityGroup(identityToRemove, group.getOwnerGroup());
		
		//remove subsciptions if user gets removed
		removeSubscriptions(identityToRemove, group);
		
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.Deferred event;
		if (identityToRemove.getKey().equals(ureqIdentity.getKey()) ) {
			event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.MYSELF_ASOWNER_REMOVED_EVENT, group, identityToRemove);
		} else {
  		event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identityToRemove);
		}
		if(events != null) {
			events.add(event);
		}
		
		// do logging
		log.audit("Idenitity(.key):" + ureqIdentity.getKey() + " removed identiy '" + identityToRemove.getName() + "' from securitygroup with key " + group.getOwnerGroup().getKey());
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToRemove));
	}
	
	@Override
	public void removeOwners(Identity ureqIdentity, Collection<Identity> identitiesToRemove, BusinessGroup group) {
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<BusinessGroupModifiedEvent.Deferred>();
		for(Identity identityToRemove:identitiesToRemove) {
			removeOwner(ureqIdentity, identityToRemove, group, events);
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}
	
	private void removeSubscriptions(Identity identity, BusinessGroup group) {
		NotificationsManager notiMgr = NotificationsManager.getInstance();
		List<Subscriber> l = notiMgr.getSubscribers(identity);
		for (Iterator<Subscriber> iterator = l.iterator(); iterator.hasNext();) {
			Subscriber subscriber = iterator.next();
			Long resId = subscriber.getPublisher().getResId();
			Long groupKey = group.getKey();
			if (resId != null && groupKey != null && resId.equals(groupKey)) {
				notiMgr.unsubscribe(subscriber);
			}
		}
	}
	
	@Override
	public boolean hasResources(BusinessGroup group) {
		return businessGroupRelationDAO.countResources(group) > 0;
	}
	
	@Override
	public boolean hasResources(List<BusinessGroup> groups) {
		return businessGroupRelationDAO.countResources(groups) > 0;
	}

	@Override
	public void addResourceTo(BusinessGroup group, RepositoryEntry re) {
		businessGroupRelationDAO.addRelationToResource(group, re.getOlatResource());
		//add author permission
		securityManager.createAndPersistPolicyWithResource(re.getOwnerGroup(), Constants.PERMISSION_ACCESS, group.getResource());
		//add coach and participant permission
		securityManager.createAndPersistPolicyWithResource(group.getOwnerGroup(), Constants.PERMISSION_COACH, re.getOlatResource());
		securityManager.createAndPersistPolicyWithResource(group.getPartipiciantGroup(), Constants.PERMISSION_PARTI, re.getOlatResource());
	}

	@Override
	public void addResourcesTo(List<BusinessGroup> groups, List<RepositoryEntry> resources) {
		if(groups == null || groups.isEmpty()) return;
		if(resources == null || resources.isEmpty()) return;
		
		List<Long> groupKeys = new ArrayList<Long>();
		for(BusinessGroup group:groups) {
			groupKeys.add(group.getKey());
		}

		//check for duplicate entries
		List<BGResourceRelation> relations = businessGroupRelationDAO.findRelations(groupKeys, 0, -1);
		for(BusinessGroup group:groups) {
			for(RepositoryEntry re:resources) {
				boolean relationExists = false;
				for(BGResourceRelation relation:relations) {
					if(relation.getGroup().equals(group) && relation.getResource().equals(re.getOlatResource())) {
						relationExists = true;
					}
				}
				if(!relationExists) {
					addResourceTo(group, re);
				}
			}
		}
	}

	@Override
	public void dedupMembers(Identity ureqIdentity, boolean coaches, boolean participants, ProgressDelegate delegate) {
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
		params.setRoles(new Roles(true, false, false, false, false, false, false));
		params.setResourceTypes(Collections.singletonList("CourseModule"));
		
		float ratio = -1.0f;
		if(delegate != null) {
			int numOfEntries = repositoryManager.countGenericANDQueryWithRolesRestriction(params);
			ratio = 100.0f / (float)numOfEntries;
		}

		int counter = 0;
		int countForCommit = 0;
		float actual = 100.0f;
		int batch = 25;
		List<RepositoryEntry> entries;
		do {
			entries = repositoryManager.genericANDQueryWithRolesRestriction(params, counter, batch, true);
			for(RepositoryEntry re:entries) {
				countForCommit += 2 + dedupSingleRepositoryentry(ureqIdentity, re, coaches, participants, false);
				if(countForCommit > 25) {
					dbInstance.intermediateCommit();
					countForCommit = 0;
				}
			}
			counter += entries.size();
			if(delegate != null) {
				actual -= (entries.size() * ratio);
				delegate.setActual(actual);
			}
		} while(entries.size() == batch);
		
		if(delegate != null) {
			delegate.finished();
		}
	}

	@Override
	public void dedupMembers(Identity ureqIdentity, RepositoryEntry entry, boolean coaches, boolean participants) {
		dedupSingleRepositoryentry(ureqIdentity, entry, coaches, participants, false);
		dbInstance.commit();
	}
	
	@Override
	public int countDuplicateMembers(RepositoryEntry entry, boolean coaches, boolean participants) {
		return dedupSingleRepositoryentry(null, entry, coaches, participants, true);
	}

	private int dedupSingleRepositoryentry(Identity ureqIdentity, RepositoryEntry entry, boolean coaches, boolean participants, boolean dryRun) {
		int count = 0;
		
		List<BusinessGroup> groups = null;//load only if needed
		if(coaches && entry.getTutorGroup() != null) {
			List<Identity> repoTutorList = securityManager.getIdentitiesOfSecurityGroup(entry.getTutorGroup());
			if(!repoTutorList.isEmpty()) {
				SearchBusinessGroupParams params = new SearchBusinessGroupParams();
				groups = businessGroupDAO.findBusinessGroups(params, entry.getOlatResource(), 0, -1);
				List<SecurityGroup> ownerSecGroups = new ArrayList<SecurityGroup>();
				for(BusinessGroup group:groups) {
					ownerSecGroups.add(group.getOwnerGroup());
				}
				
				List<Identity> ownerList = securityManager.getIdentitiesOfSecurityGroups(ownerSecGroups);
				repoTutorList.retainAll(ownerList);
				if(!dryRun) {
					repositoryManager.removeTutors(ureqIdentity, repoTutorList, entry);
				}
				count += repoTutorList.size();
			}
		}
		
		if(participants && entry.getParticipantGroup() != null) {
			List<Identity> repoParticipantList = securityManager.getIdentitiesOfSecurityGroup(entry.getParticipantGroup());
			if(!repoParticipantList.isEmpty()) {
			
				if(groups == null) {
					SearchBusinessGroupParams params = new SearchBusinessGroupParams();
					groups = businessGroupDAO.findBusinessGroups(params, entry.getOlatResource(), 0, -1);
				}
				List<SecurityGroup> participantSecGroups = new ArrayList<SecurityGroup>();
				for(BusinessGroup group:groups) {
					participantSecGroups.add(group.getPartipiciantGroup());
				}
				List<Identity> participantList = securityManager.getIdentitiesOfSecurityGroups(participantSecGroups);
				repoParticipantList.retainAll(participantList);
				if(!dryRun) {
					repositoryManager.removeParticipants(ureqIdentity, repoParticipantList, entry, null, false);
				}
				count += repoParticipantList.size();
			}
		}
		return count;
	}

	@Override
	public void removeResourceFrom(List<BusinessGroup> groups, RepositoryEntry re) {
		if(groups == null || groups.isEmpty()) {
			return; // nothing to do
		}
		
		int count = 0;
		for(BusinessGroup group:groups) {
			businessGroupRelationDAO.deleteRelation(group, re.getOlatResource());
			//remove author permission
			securityManager.deletePolicy(re.getOwnerGroup(), Constants.PERMISSION_ACCESS, group.getResource());
			//remove permission
			securityManager.deletePolicy(group.getOwnerGroup(), Constants.PERMISSION_COACH, re.getOlatResource());
			securityManager.deletePolicy(group.getPartipiciantGroup(), Constants.PERMISSION_PARTI, re.getOlatResource());
			if(count++ % 20 == 0) {
				dbInstance.commit();
			}
		}
		dbInstance.commit();
	}
	
	@Override
	public void removeResource(OLATResource resource) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> groups = findBusinessGroups(params, resource, 0, -1);
		for(BusinessGroup group:groups) {
			businessGroupRelationDAO.deleteRelation(group, resource);
			//remove author permission
			//securityManager.deletePolicy(re.getOwnerGroup(), Constants.PERMISSION_ACCESS, group.getResource());
			//remove permission
			securityManager.deletePolicy(group.getOwnerGroup(), Constants.PERMISSION_COACH, resource);
			securityManager.deletePolicy(group.getPartipiciantGroup(), Constants.PERMISSION_PARTI, resource);
		}
		dbInstance.commit();
	}

	@Override
	public List<OLATResource> findResources(Collection<BusinessGroup> groups, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findResources(groups, firstResult, maxResults);
	}

	@Override
	public List<RepositoryEntry> findRepositoryEntries(Collection<BusinessGroup> groups, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findRepositoryEntries(groups, firstResult, maxResults);
	}

	@Override
	public List<RepositoryEntryShort> findShortRepositoryEntries(Collection<BusinessGroupShort> groups, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findShortRepositoryEntries(groups, firstResult, maxResults);
	}
	
	@Override
	public List<BGRepositoryEntryRelation> findRelationToRepositoryEntries(Collection<Long> groupKeys, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findRelationToRepositoryEntries(groupKeys, firstResult, maxResults);
	}

	@Override
	public boolean isIdentityInBusinessGroup(Identity identity, BusinessGroup businessGroup) {
		if(businessGroup == null || identity == null) return false;
		
		SecurityGroup participants = businessGroup.getPartipiciantGroup();
		if (participants != null && securityManager.isIdentityInSecurityGroup(identity, participants)) {
			return true;
		}
		SecurityGroup owners = businessGroup.getOwnerGroup();
		if (owners != null && securityManager.isIdentityInSecurityGroup(identity, owners)) {
			return true;
		}
		return false;
	}

	@Override
	public List<BusinessGroupMembership> getBusinessGroupsMembership(Collection<BusinessGroup> businessGroups) {
		return businessGroupDAO.getBusinessGroupsMembership(businessGroups);
	}

	@Override
	public List<BusinessGroupMembership> getBusinessGroupMembership(Collection<Long> businessGroups, Identity... identity) {
		List<BusinessGroupMembershipViewImpl> views =
				businessGroupDAO.getMembershipInfoInBusinessGroups(businessGroups, identity);

		Map<IdentityGroupKey, BusinessGroupMembershipImpl> memberships = new HashMap<IdentityGroupKey, BusinessGroupMembershipImpl>();
		for(BusinessGroupMembershipViewImpl membership: views) {
			if(membership.getOwnerGroupKey() != null) {
				Long groupKey = membership.getOwnerGroupKey();
				IdentityGroupKey key = new IdentityGroupKey(membership.getIdentityKey(), groupKey);
				if(!memberships.containsKey(key)) {
					memberships.put(key, new BusinessGroupMembershipImpl(membership.getIdentityKey(), groupKey));
				}
				BusinessGroupMembershipImpl mb = memberships.get(key);
				mb.setOwner(true);
				mb.setCreationDate(membership.getCreationDate());
				mb.setLastModified(membership.getLastModified());
			}
			if(membership.getParticipantGroupKey() != null) {
				Long groupKey = membership.getParticipantGroupKey();
				IdentityGroupKey key = new IdentityGroupKey(membership.getIdentityKey(), groupKey);
				if(!memberships.containsKey(key)) {
					memberships.put(key, new BusinessGroupMembershipImpl(membership.getIdentityKey(), groupKey));
				}
				BusinessGroupMembershipImpl mb = memberships.get(key);
				mb.setParticipant(true);
				mb.setCreationDate(membership.getCreationDate());
				mb.setLastModified(membership.getLastModified());
			}
			if(membership.getWaitingGroupKey() != null) {
				Long groupKey = membership.getWaitingGroupKey();
				IdentityGroupKey key = new IdentityGroupKey(membership.getIdentityKey(), groupKey);
				if(!memberships.containsKey(key)) {
					memberships.put(key, new BusinessGroupMembershipImpl(membership.getIdentityKey(), groupKey));
				}
				BusinessGroupMembershipImpl mb = memberships.get(key);
				mb.setWaiting(true);
				mb.setCreationDate(membership.getCreationDate());
				mb.setLastModified(membership.getLastModified());
			}
		}
		
		return new ArrayList<BusinessGroupMembership>(memberships.values());
	}
	
	@Override
	public boolean isIdentityInBusinessGroup(Identity identity, Long groupKey,
			boolean ownedById, boolean attendedById, OLATResource resource) {
		return businessGroupRelationDAO.isIdentityInBusinessGroup(identity, groupKey, ownedById, attendedById, resource);
	}
	
	@Override
	public void exportGroups(List<BusinessGroup> groups, List<BGArea> areas, File fExportFile,
			BusinessGroupEnvironment env, boolean runtimeDatas, boolean backwardsCompatible) {
		businessGroupImportExport.exportGroups(groups, areas, fExportFile, env, runtimeDatas, backwardsCompatible);
	}

	@Override
	public BusinessGroupEnvironment importGroups(RepositoryEntry re, File fGroupExportXML) {
		return businessGroupImportExport.importGroups(re, fGroupExportXML);
	}

	@Override
	public void archiveGroups(List<BusinessGroup> groups, File exportFile) {
		businessGroupArchiver.archiveGroups(groups, exportFile);
	}
}
