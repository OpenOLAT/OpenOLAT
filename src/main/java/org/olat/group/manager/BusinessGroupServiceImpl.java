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
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.Group;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.async.ProgressDelegate;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupMailing.MailType;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.BusinessGroupEnvironment;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.BusinessGroupMembershipImpl;
import org.olat.group.model.BusinessGroupMembershipViewImpl;
import org.olat.group.model.BusinessGroupMembershipsChanges;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupRelationModified;
import org.olat.group.model.EnrollState;
import org.olat.group.model.IdentityGroupKey;
import org.olat.group.model.LeaveOption;
import org.olat.group.model.MembershipModification;
import org.olat.group.model.OpenBusinessGroupRow;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRightsRole;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.group.ui.edit.BusinessGroupRepositoryEntryEvent;
import org.olat.repository.LeavingStatusList;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryShort;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.RepositoryEntryQueries;
import org.olat.repository.manager.RepositoryEntryRelationDAO;
import org.olat.repository.model.RepositoryEntryToGroupRelation;
import org.olat.repository.model.SearchRepositoryEntryParameters;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ResourceReservation;
import org.olat.resource.accesscontrol.manager.ACReservationDAO;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("businessGroupService")
public class BusinessGroupServiceImpl implements BusinessGroupService {
	private final Logger log = Tracing.createLoggerFor(BusinessGroupServiceImpl.class);

	@Autowired
	private BGAreaManager areaManager;
	@Autowired
	private BGRightManager rightManager;
	@Autowired
	private BusinessGroupModule groupModule;
	@Autowired
	private BusinessGroupDAO businessGroupDAO;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private ContactDAO contactDao;
	@Autowired
	private BusinessGroupQueries businessGroupQueries;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDAO;
	@Autowired
	private RepositoryEntryRelationDAO repositoryEntryRelationDao;
	@Autowired
	private BusinessGroupLifecycleManager businessGroupLifecycleManager;
	@Autowired
	private RepositoryEntryQueries repositoryEntryQueries;
	@Autowired
	private ACReservationDAO reservationDao;
	@Autowired
	private DB dbInstance;

	@Override
	public BusinessGroup createBusinessGroup(Identity creator, String name, String description, String technicalType,
			Integer minParticipants, Integer maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled,
			RepositoryEntry re) {
		return createBusinessGroup(creator, name,  description, technicalType, null, null,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled, re);
	}

	@Override
	public BusinessGroup createBusinessGroup(Identity creator, String name, String description, String technicalType,
			String externalId, String managedFlags, Integer minParticipants, Integer maxParticipants,
			boolean waitingListEnabled, boolean autoCloseRanksEnabled, RepositoryEntry re) {
		
		if("".equals(managedFlags) || "none".equals(managedFlags)) {
			managedFlags = null;
		}
		
		BusinessGroup group = businessGroupDAO.createAndPersist(creator, name, description, technicalType, externalId, managedFlags,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled, false, false, false, null);
		if(re != null) {
			addResourceTo(group, re);
		}
		return group;
	}

	@Override
	public BusinessGroup updateBusinessGroup(Identity ureqIdentity, BusinessGroup group, String name, String description,
			String externalId, String managedFlags, Integer minParticipants, Integer maxParticipants) {
		
		BusinessGroup bg = businessGroupDAO.loadForUpdate(group);

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
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		autoRankCheck(ureqIdentity, bg, previousMaxParticipants, events);
		BusinessGroup updatedGroup = businessGroupDAO.merge(bg);
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return updatedGroup;
	}

	@Override
	public BusinessGroup updateBusinessGroup(Identity ureqIdentity, BusinessGroup group, String name, String description,
			Integer minParticipants, Integer maxParticipants, Boolean waitingList, Boolean autoCloseRanks) {
		
		BusinessGroup bg = businessGroupDAO.loadForUpdate(group);
		
		Integer previousMaxParticipants = bg.getMaxParticipants();
		bg.setName(name);
		bg.setDescription(description);
		bg.setMaxParticipants(maxParticipants);
		bg.setMinParticipants(minParticipants);
		bg.setWaitingListEnabled(waitingList);
		bg.setAutoCloseRanksEnabled(autoCloseRanks);
		bg.setLastUsage(new Date(System.currentTimeMillis()));
		//auto rank if possible

		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		autoRankCheck(ureqIdentity, bg, previousMaxParticipants, events);
		BusinessGroup mergedGroup = businessGroupDAO.merge(bg);
		//prevents lazy loading issues
		mergedGroup.getBaseGroup().getKey();
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
		
		BusinessGroup reloadedBusinessGroup = businessGroupDAO.loadForUpdate(group);
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
			//prevent lazy loading issues
			mergedGroup.getBaseGroup().getKey();
		}
		dbInstance.commit();
		return mergedGroup;
	}

	@Override
	public BusinessGroup updateAllowToLeaveBusinessGroup(BusinessGroup group, boolean allow) {
		BusinessGroup reloadedBusinessGroup = businessGroupDAO.loadForUpdate(group);
		BusinessGroup mergedGroup = null;
		if(reloadedBusinessGroup != null) {
			reloadedBusinessGroup.setAllowToLeave(allow);
			mergedGroup = businessGroupDAO.merge(reloadedBusinessGroup);
			//prevent lazy loading issues
			mergedGroup.getBaseGroup().getKey();
		}
		dbInstance.commit();
		return mergedGroup;
	}

	@Override
	public BusinessGroup setLastUsageFor(final Identity identity, final BusinessGroup group) {
		BusinessGroup reloadedBusinessGroup = businessGroupDAO.loadForUpdate(group);
		BusinessGroup mergedGroup = null;
		if(reloadedBusinessGroup != null) {
			reloadedBusinessGroup.setLastUsage(new Date());
			if(identity != null) {
				businessGroupRelationDAO.touchMembership(identity, group);
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
	public void copyBusinessGroup(Identity identity, BusinessGroup sourceBusinessGroup,
			List<String> targetNames, String targetDescription, Integer targetMin, Integer targetMax, boolean copyAreas,
			boolean copyCollabToolConfig, boolean copyRights, boolean copyOwners, boolean copyParticipants,
			boolean copyMemberVisibility, boolean copyWaitingList, boolean copyRelations, Boolean allowToLeave) {
		for(String targetName:targetNames) {
			copyBusinessGroup(identity, sourceBusinessGroup, targetName, targetDescription,
					targetMin, targetMax, copyAreas, copyCollabToolConfig, copyRights,
					copyOwners, copyParticipants, copyMemberVisibility, copyWaitingList, copyRelations, allowToLeave);
		}
	}

	@Override
	public BusinessGroup copyBusinessGroup(Identity identity, BusinessGroup sourceBusinessGroup, String targetName, String targetDescription,
			Integer targetMin, Integer targetMax,  boolean copyAreas, boolean copyCollabToolConfig, boolean copyRights,
			boolean copyOwners, boolean copyParticipants, boolean copyMemberVisibility, boolean copyWaitingList,
			boolean copyRelations, Boolean allowToLeave) {

		// 1. create group, set waitingListEnabled, enableAutoCloseRanks like source business-group
		BusinessGroup newGroup = businessGroupDAO.createAndPersist(null, targetName, targetDescription,
				sourceBusinessGroup.getTechnicalType(), null, null,
				targetMin, targetMax, sourceBusinessGroup.getWaitingListEnabled(), sourceBusinessGroup.getAutoCloseRanksEnabled(),
				false, false, false, allowToLeave);
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
			List<Identity> owners = businessGroupRelationDAO.getMembers(sourceBusinessGroup, GroupRoles.coach.name());
			if(owners.isEmpty()) {
				businessGroupRelationDAO.addRole(identity, newGroup, GroupRoles.coach.name());
			} else {
				for (Identity owner:owners) {
					businessGroupRelationDAO.addRole(owner, newGroup, GroupRoles.coach.name());
				}
			}
		} else {
			businessGroupRelationDAO.addRole(identity, newGroup, GroupRoles.coach.name());
		}
		// 6. copy participants
		if (copyParticipants) {
			List<Identity> participants = businessGroupRelationDAO.getMembers(sourceBusinessGroup, GroupRoles.participant.name());
			for(Identity participant:participants) {
				businessGroupRelationDAO.addRole(participant, newGroup, GroupRoles.participant.name());
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
			List<Identity> waitingList = getMembers(sourceBusinessGroup, GroupRoles.waiting.name());
			for (Identity waiting:waitingList) {
				businessGroupRelationDAO.addRole(waiting, newGroup, GroupRoles.waiting.name());
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

		targetGroup = businessGroupDAO.loadForUpdate(targetGroup);
		Set<Identity> currentOwners
			= new HashSet<>(businessGroupRelationDAO.getMembers(targetGroup, GroupRoles.coach.name()));
		Set<Identity> currentParticipants 
			= new HashSet<>(businessGroupRelationDAO.getMembers(targetGroup, GroupRoles.participant.name()));
		Set<Identity> currentWaiters
			= new HashSet<>(businessGroupRelationDAO.getMembers(targetGroup, GroupRoles.waiting.name()));

		Set<Identity> newOwners = new HashSet<>();
		Set<Identity> newParticipants = new HashSet<>();
		Set<Identity> newWaiters = new HashSet<>();
		
		//collect the owners
		for(BusinessGroup group:groupsToMerge) {
			List<Identity> owners = businessGroupRelationDAO.getMembers(group, GroupRoles.coach.name());
			owners.removeAll(currentOwners);
			newOwners.addAll(owners);
		}
		
		//collect the participants but test if they are not already owners
		for(BusinessGroup group:groupsToMerge) {
			List<Identity> participants = businessGroupRelationDAO.getMembers(group, GroupRoles.participant.name());
			participants.removeAll(currentParticipants);
			for(Identity participant:participants) {
				if(!newOwners.contains(participant)) {
					newParticipants.add(participant);
				}
			}
		}
		
		//collect the waiting list but test if they are not already owners or participants
		for(BusinessGroup group:groupsToMerge) {
			List<Identity> waitingList = businessGroupRelationDAO.getMembers(group, GroupRoles.waiting.name());
			waitingList.removeAll(currentWaiters);
			for(Identity waiter:waitingList) {
				if(!newOwners.contains(waiter) && !newParticipants.contains(waiter)) {
					newWaiters.add(waiter);
				}
			}
		}

		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		
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
			boolean sendMail = mailing != null && mailing.isSendEmail();
			businessGroupLifecycleManager.deleteBusinessGroup(group, ureqIdentity, sendMail);
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
		group = businessGroupDAO.loadForUpdate(group);
		
		List<Identity> currentOwners = businessGroupRelationDAO.getMembers(group, GroupRoles.coach.name());
		List<Identity> currentParticipants = businessGroupRelationDAO.getMembers(group, GroupRoles.participant.name());
		List<Identity> currentWaitingList = businessGroupRelationDAO.getMembers(group, GroupRoles.waiting.name());
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();

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
		Map<Long,BusinessGroupMembershipsChanges> changesMap = new HashMap<>();
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
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		List<BusinessGroup> groups = loadBusinessGroups(changesMap.keySet());
		for(BusinessGroup group:groups) {
			BusinessGroupMembershipsChanges changesWrapper = changesMap.get(group.getKey());
			group = businessGroupDAO.loadForUpdate(group);
					
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
	public List<BusinessGroup> findBusinessGroups(Identity identity, int maxResults, BusinessGroupOrder... orderBy) {
		return businessGroupDAO.findBusinessGroup(identity, maxResults, orderBy);
	}

	@Override
	public List<BusinessGroup> findBusinessGroupsOwnedBy(Identity identity) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, true, false);
		return businessGroupDAO.findBusinessGroups(params, null, 0, -1);
	}
	
	@Override
	public List<BusinessGroup> findBusinessGroupsAttendedBy(Identity identity) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, false, true);
		return businessGroupDAO.findBusinessGroups(params, null, 0, -1);
	}
	
	@Override
	public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(Identity identity,  RepositoryEntryRef resource) {
		return businessGroupDAO.findBusinessGroupsWithWaitingListAttendedBy(identity, resource);
	}
	
	@Override
	public int countBusinessGroups(SearchBusinessGroupParams params, RepositoryEntryRef resource) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.countBusinessGroups(params, resource);
	}

	@Override
	public List<BusinessGroup> findBusinessGroups(SearchBusinessGroupParams params, RepositoryEntryRef resource,
			int firstResult, int maxResults, BusinessGroupOrder... ordering) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.findBusinessGroups(params, resource, firstResult, maxResults);
	}

	@Override
	public List<StatisticsBusinessGroupRow> findBusinessGroupsWithMemberships(BusinessGroupQueryParams params, IdentityRef identity) {
		return businessGroupQueries.searchBusinessGroupsWithMemberships(params, identity);
	}

	@Override
	public List<StatisticsBusinessGroupRow> findBusinessGroupsFromRepositoryEntry(BusinessGroupQueryParams params, IdentityRef identity, RepositoryEntryRef entry) {
		return businessGroupQueries.searchBusinessGroupsForRepositoryEntry(params, identity, entry);
	}

	@Override
	public List<StatisticsBusinessGroupRow> findBusinessGroupsForSelection(BusinessGroupQueryParams params, IdentityRef identity) {
		return businessGroupQueries.searchBusinessGroupsForSelection(params, identity);
	}

	@Override
	public List<StatisticsBusinessGroupRow> findBusinessGroupsStatistics(BusinessGroupQueryParams params) {
		return businessGroupQueries.searchBusinessGroupsStatistics(params);
	}

	@Override
	public List<OpenBusinessGroupRow> findPublishedBusinessGroups(BusinessGroupQueryParams params, IdentityRef identity) {
		return businessGroupQueries.searchPublishedBusinessGroups(params, identity);
	}

	@Override
	public List<Long> toGroupKeys(String groupNames, RepositoryEntryRef resource) {
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
	public List<Identity> getMembersOf(RepositoryEntryRef resource, boolean owner, boolean attendee) {
		return businessGroupRelationDAO.getMembersOf(resource, owner, attendee);
	}

	@Override
	public void leave(Identity identity, RepositoryEntry entry, LeavingStatusList status, MailPackage mailing) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.setIdentity(identity);
		params.setAttendee(true);
		List<BusinessGroup> groups = businessGroupDAO.findBusinessGroups(params, entry, 0, -1);
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		for(BusinessGroup group:groups) {
			if(BusinessGroupManagedFlag.isManaged(group, BusinessGroupManagedFlag.membersmanagement)) {
				status.setWarningManagedGroup(true);
			} else if(businessGroupRelationDAO.countResources(group) > 1) {
				status.setWarningGroupWithMultipleResources(true);
			} else {
				removeParticipant(identity, identity, group, mailing, null);
			}
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}

	@Override
	public BusinessGroupAddResponse addOwners(Identity ureqIdentity, Roles ureqRoles, List<Identity> addIdentities,
			BusinessGroup group, MailPackage mailing) {
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		for (Identity identity : addIdentities) {
			group = loadBusinessGroup(group); // reload business group
			if (securityManager.isGuest(identity)) {
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
		if (!businessGroupRelationDAO.hasRole(identityToAdd, group, GroupRoles.coach.name())) {
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
						log.info(Tracing.M_AUDIT, "Identity(.key):{} added identity '{}' to group with key {}",
							ureqIdentity.getKey(), identityToAdd.getKey(), group.getKey());
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
		
		businessGroupRelationDAO.addRole(identityToAdd, group, GroupRoles.coach.name());
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identityToAdd);
		if(events != null) {
			events.add(event);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToAdd));
		log.info(Tracing.M_AUDIT, "Identity(.key):{} added identity '{}' to group with key {}",
			ureqIdentity.getKey(), identityToAdd.getKey(), group.getKey());
	}
	
	private boolean addParticipant(Identity ureqIdentity, Roles ureqRoles, Identity identityToAdd, BusinessGroup group,
			MailPackage mailing, List<BusinessGroupModifiedEvent.Deferred> events) {
		
		if(!businessGroupRelationDAO.hasRole(identityToAdd, group, GroupRoles.participant.name())) {
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

					BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent
							.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_ADD_PENDING_EVENT, group, identityToAdd);
					events.add(event);
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
		
		businessGroupRelationDAO.addRole(identityToAdd, group, GroupRoles.participant.name());

		// notify currently active users of this business group
		BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identityToAdd);
		if(events != null) {
			events.add(event);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToAdd));
		log.info(Tracing.M_AUDIT, "Identity(.key):{} added identity '{}' to group with key {}",
			ureqIdentity.getKey(), identityToAdd.getKey(), group.getKey());
		// send notification mail in your controller!
	}

	@Override
	public BusinessGroupAddResponse addParticipants(Identity ureqIdentity, Roles ureqRoles, List<Identity> addIdentities,
			BusinessGroup group, MailPackage mailing) {	
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();

		BusinessGroup currBusinessGroup = businessGroupDAO.loadForUpdate(group);	
		for (final Identity identity : addIdentities) {
			if (securityManager.isGuest(identity)) {
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
				List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
				transferFirstIdentityFromWaitingToParticipant(ureqIdentity, group, null, events);
				dbInstance.commit();
				BusinessGroupModifiedEvent.fireDeferredEvents(events);
			}
		}
	}

	@Override
	public void acceptPendingParticipation(Identity ureqIdentity, Identity reservationOwner, OLATResource resource) {
		ResourceReservation reservation = reservationDao.loadReservation(reservationOwner, resource);
		if(reservation != null && "BusinessGroup".equals(resource.getResourceableTypeName())) {
			BusinessGroup group = businessGroupDAO.loadForUpdate(resource.getResourceableId());
			List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
			if(group != null) {
				String type = reservation.getType();
				if("group_coach".equals(type)) {
					if(!businessGroupRelationDAO.hasRole(reservationOwner, group, GroupRoles.coach.name())) {
						internalAddCoach(ureqIdentity, reservationOwner, group, events);
					}
				} else if("group_participant".equals(type)) {
					if(!businessGroupRelationDAO.hasRole(reservationOwner, group, GroupRoles.participant.name())) {
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
		boolean removed = businessGroupRelationDAO.removeRole(identity, group, GroupRoles.participant.name());
		if(removed) {
			// notify currently active users of this business group
			BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
			if(events != null) {
				events.add(event);
			}
			// do logging
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_REMOVED, getClass(), LoggingResourceable.wrap(identity), LoggingResourceable.wrap(group));
			log.info(Tracing.M_AUDIT, "Identity(.key):{} removed identity '{}' from group with key {}",
				ureqIdentity.getKey(), identity.getKey(), group.getKey());
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
	public LeaveOption isAllowToLeaveBusinessGroup(Identity identity, BusinessGroup group) {
		LeaveOption opt;
		if(groupModule.isAllowLeavingGroupOverride()) {
			if(group.isAllowToLeave()) {
				opt = new LeaveOption();
			} else {
				ContactList list = getAdminContactList(identity, group);
				opt = new LeaveOption(false, list);
			}
		} else if(groupModule.isAllowLeavingGroupCreatedByAuthors() && groupModule.isAllowLeavingGroupCreatedByLearners()) {
			opt = new LeaveOption();
		} else if(!groupModule.isAllowLeavingGroupCreatedByAuthors() && !groupModule.isAllowLeavingGroupCreatedByLearners()) {
			ContactList list = getAdminContactList(identity, group);
			opt = new LeaveOption(false, list);
		} else {
			int numOfCoaches = countMembers(group, GroupRoles.coach.name());
			if(numOfCoaches == 0) {
				int numOfResources = businessGroupRelationDAO.countResources(group);
				if(numOfResources > 0) {
					//author group
					if(groupModule.isAllowLeavingGroupCreatedByAuthors()) {
						opt = new LeaveOption();
					} else {
						ContactList list = getAdminContactList(identity, group);
						opt = new LeaveOption(false, list);
					}

				//learner group
				} else if(groupModule.isAllowLeavingGroupCreatedByLearners()) {
					opt = new LeaveOption();
				} else {
					ContactList list = getAdminContactList(identity, group);
					opt = new LeaveOption(false, list);
				}
			} else {
				int numOfAuthors = businessGroupRelationDAO.countAuthors(group);
				if(numOfAuthors > 0) {
					if(groupModule.isAllowLeavingGroupCreatedByAuthors()) {
						opt = new LeaveOption();
					} else {
						ContactList list = getAdminContactList(identity, group);
						opt = new LeaveOption(false, list);
					}
				} else if(groupModule.isAllowLeavingGroupCreatedByLearners()) {
					opt = new LeaveOption();
				} else {
					ContactList list = getAdminContactList(identity, group);
					opt = new LeaveOption(false, list);
				}	
			}
		}
		return opt;
	}
	
	private ContactList getAdminContactList(Identity identity, BusinessGroup group) {
		ContactList list = new ContactList("Contact");
		List<Identity> coaches = getMembers(group, GroupRoles.coach.name());
		if(coaches.isEmpty()) {
			Collection<BusinessGroup> groups = Collections.singletonList(group);
			List<RepositoryEntry> entries = businessGroupRelationDAO.findRepositoryEntries(groups, 0, -1);
			coaches.addAll(repositoryService.getMembers(entries, RepositoryEntryRelationType.defaultGroup, GroupRoles.coach.name()));
			
			if(coaches.isEmpty()) {
				//get system administrators of the user's organisations
				Roles roles = securityManager.getRoles(identity);
				List<OrganisationRef> identityOrgs = roles.getOrganisationsWithRole(OrganisationRoles.user);
				SearchIdentityParams identityParams = new SearchIdentityParams();
				identityParams.setOrganisations(identityOrgs);
				identityParams.setRoles(new OrganisationRoles[]{ OrganisationRoles.administrator });
				identityParams.setStatus(Identity.STATUS_VISIBLE_LIMIT);
				List<Identity> admins = securityManager.getIdentitiesByPowerSearch(identityParams, 0, -1);
				list.addAllIdentites(admins);
			}
		}
		
		list.addAllIdentites(coaches);
		return list;
	}

	@Override
	public void removeParticipants(Identity ureqIdentity, List<Identity> identities, BusinessGroup group, MailPackage mailing) {
		group = businessGroupDAO.loadForUpdate(group);
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		for (Identity identity : identities) {
		  removeParticipant(ureqIdentity, identity, group, mailing, events);
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}

	@Override
	public void removeMembers(Identity ureqIdentity, List<Identity> identities, OLATResource resource, MailPackage mailing, boolean overrideManaged) {
		if(identities == null || identities.isEmpty() || resource == null) return;//nothing to do
		
		List<BusinessGroup> groups = null;
		if("BusinessGroup".equals(resource.getResourceableTypeName())) {
			//it's a group resource
			BusinessGroup group = loadBusinessGroup(resource);
			if(group != null) {
				groups = Collections.singletonList(group);
			}
		} else {
			RepositoryEntryRef re = repositoryManager.lookupRepositoryEntry(resource, false);
			groups = findBusinessGroups(null, re, 0, -1);
		}
		if(groups == null || groups.isEmpty()) {
			return;//nothing to do
		}
		
		//remove managed groups
		if(!overrideManaged) {
			for(Iterator<BusinessGroup> groupIt=groups.iterator(); groupIt.hasNext(); ) {
				boolean managed = BusinessGroupManagedFlag.isManaged(groupIt.next(), BusinessGroupManagedFlag.membersmanagement);
				if(managed) {
					groupIt.remove();
				}
			}
		}
		
		if(groups.isEmpty()) {
			return;//nothing to do
		}

		List<OLATResource> groupResources = new ArrayList<>();
		Map<Long,BusinessGroup> idToGroup = new HashMap<>();
		for(BusinessGroup group:groups) {
			groupResources.add(group.getResource());
			idToGroup.put(group.getKey(), group);
		}
		final Map<Long,Identity> keyToIdentityMap = new HashMap<>();
		for(Identity identity:identities) {
			keyToIdentityMap.put(identity.getKey(), identity);
		}

		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
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
			BusinessGroup nextGroup = businessGroupDAO.loadForUpdate(idToGroup.get(groupKey));
			nextGroupMembership = removeGroupMembers(ureqIdentity, currentMembership, nextGroup, keyToIdentityMap, itMembership, mailing, events);
			//release the lock
			dbInstance.commit();
		}

		List<ResourceReservation> reservations = reservationDao.loadReservations(groupResources);
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
				if(GroupRoles.coach.name().equals(membership.getRole())) {
					removeOwner(ureqIdentity, id, currentGroup, events);
				}
				if(GroupRoles.participant.name().equals(membership.getRole())) {
					removeParticipant(ureqIdentity, id, currentGroup, mailing, events);
				}
				if(GroupRoles.waiting.name().equals(membership.getRole())) {
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
		if (!businessGroupRelationDAO.hasRole(identity, group, GroupRoles.waiting.name())) {
			internalAddToWaitingList(ureqIdentity, identity, group, mailing, events);
		}
	}

	/**
	 * This method is for internal usage only. It adds the identity to to group without synchronization or checks!
	 *
	 */
	private void internalAddToWaitingList(Identity ureqIdentity, Identity identity, BusinessGroup group, MailPackage mailing,
										  List<BusinessGroupModifiedEvent.Deferred> events) {
		businessGroupRelationDAO.addRole(identity, group, GroupRoles.waiting.name());

		// notify currently active users of this business group
		BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
		if(events != null) {
			events.add(event);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_TO_WAITING_LIST_ADDED, getClass(), LoggingResourceable.wrap(identity));
		log.info(Tracing.M_AUDIT, "Identity(.key):{} added identity '{}' to group with key {}",
			ureqIdentity.getKey(), identity.getKey(), group.getKey());
		// send mail
		BusinessGroupMailing.sendEmail(ureqIdentity, identity, group, MailType.addToWaitingList, mailing);
	}
	
	@Override
	public BusinessGroupAddResponse addToWaitingList(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup group, MailPackage mailing) {
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		BusinessGroup currBusinessGroup = businessGroupDAO.loadForUpdate(group); // reload business group
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();

		for (final Identity identity : addIdentities) {	
			if (securityManager.isGuest(identity)) {
				response.getIdentitiesWithoutPermission().add(identity);
			}
			// Check if identity is already in group. make a db query in case
			// someone in another workflow already added this user to this group. if
			// found, add user to model
			else {
				List<String> roles = businessGroupRelationDAO.getRoles(identity, currBusinessGroup);
				if (roles.contains(GroupRoles.waiting.name()) || roles.contains(GroupRoles.participant.name())) {
					response.getIdentitiesAlreadyInGroup().add(identity);
				} else {
					// identity has permission and is not already in group => add it
					internalAddToWaitingList(ureqIdentity, identity, currBusinessGroup, mailing, events);
					response.getAddedIdentities().add(identity);
				}
			}
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return response;
	}

	private final void removeFromWaitingList(Identity ureqIdentity, Identity identity, BusinessGroup group, MailPackage mailing,
			List<BusinessGroupModifiedEvent.Deferred> events) {
		businessGroupRelationDAO.removeRole(identity, group, GroupRoles.waiting.name());
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.Deferred event = BusinessGroupModifiedEvent.createDeferredEvent(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
		if(events != null) {
			events.add(event);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_FROM_WAITING_LIST_REMOVED, getClass(), LoggingResourceable.wrap(identity));
		log.info(Tracing.M_AUDIT, "Identity(.key):{} removed identity '{}' from group with key {}",
			ureqIdentity.getKey(), identity.getKey(), group.getKey());
		// send mail
		BusinessGroupMailing.sendEmail(ureqIdentity, identity, group, MailType.removeToWaitingList, mailing);
	}
	
	@Override
	public void removeFromWaitingList(Identity ureqIdentity, List<Identity> identities, BusinessGroup businessGroup, MailPackage mailing) {
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		businessGroup = businessGroupDAO.loadForUpdate(businessGroup);
		for (Identity identity : identities) {
		  removeFromWaitingList(ureqIdentity, identity, businessGroup, mailing, events);
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}
	
	@Override
	public int getPositionInWaitingListFor(IdentityRef identity, BusinessGroupRef businessGroup) {
		// get position in waiting-list
		List<Long> identities = businessGroupRelationDAO.getMemberKeysOrderByDate(businessGroup, GroupRoles.waiting.name());
		for (int i = 0; i<identities.size(); i++) {
			Long waitingListIdentity = identities.get(i);
			if (waitingListIdentity.equals(identity.getKey()) ) {
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
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		currBusinessGroup = businessGroupDAO.loadForUpdate(currBusinessGroup);
		
		for (Identity identity : identities) {
			// check if identity is already in participant
			if (!businessGroupRelationDAO.hasRole(identity, currBusinessGroup, GroupRoles.participant.name()) ) {
				// Identity is not in participant-list => move identity from waiting-list to participant-list
				addParticipant(ureqIdentity, ureqRoles, identity, currBusinessGroup, mailing, events);
				removeFromWaitingList(ureqIdentity, identity, currBusinessGroup, null, events);
				response.getAddedIdentities().add(identity);
				// notification mail is handled in controller
			} else {
				if (businessGroupRelationDAO.hasRole(identity, currBusinessGroup, GroupRoles.waiting.name()) ) {
					removeFromWaitingList(ureqIdentity, identity, currBusinessGroup, mailing, events);
				}
				response.getIdentitiesAlreadyInGroup().add(identity);
			}
		}

		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		return response;
	}
	
	@Override
	public EnrollState enroll(Identity ureqIdentity, Roles ureqRoles, Identity identity, BusinessGroup group,
			MailPackage mailing) {
		final BusinessGroup reloadedGroup = businessGroupDAO.loadForUpdate(group);
		
		log.info("doEnroll start: group={} for {}", OresHelper.createStringRepresenting(group), identity.getKey());
		EnrollState enrollStatus = new EnrollState();
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();

		ResourceReservation reservation = reservationDao.loadReservation(identity, reloadedGroup.getResource());
		
		//reservation has the highest priority over max participant or other settings
		if(reservation != null) {
			addParticipant(ureqIdentity, ureqRoles, identity, reloadedGroup, mailing, events);
			enrollStatus.setEnrolled(GroupRoles.participant);
			log.info("doEnroll (reservation) - setIsEnrolled {}", identity.getKey());
			reservationDao.deleteReservation(reservation);
		} else if (reloadedGroup.getMaxParticipants() != null) {
			int participantsCounter = businessGroupRelationDAO.countEnrollment(reloadedGroup);
			int reservations = reservationDao.countReservations(reloadedGroup.getResource());
			
			log.info("doEnroll - participantsCounter: {}, reservations: {} maxParticipants: {} for {}",
					participantsCounter, reservations, reloadedGroup.getMaxParticipants(), identity.getKey());
			if ((participantsCounter + reservations) >= reloadedGroup.getMaxParticipants().intValue()) {
				// already full, show error and updated choose page again
				if (reloadedGroup.getWaitingListEnabled().booleanValue()) {
					addToWaitingList(ureqIdentity, identity, reloadedGroup, mailing, events);
					enrollStatus.setEnrolled(GroupRoles.waiting);
				} else {
					// No Waiting List => List is full
					enrollStatus.setI18nErrorMessage("error.group.full");
					enrollStatus.setFailed(true);
				}
			} else {
				//enough place
				addParticipant(ureqIdentity, ureqRoles, identity, reloadedGroup, mailing, events);
				enrollStatus.setEnrolled(GroupRoles.participant);
				log.info("doEnroll - setIsEnrolled {}", identity.getKey());
			}
		} else {
			log.debug("doEnroll as participant beginTransaction");
			addParticipant(ureqIdentity, ureqRoles, identity, reloadedGroup, mailing, events);
			enrollStatus.setEnrolled(GroupRoles.participant);						
			log.debug("doEnroll as participant committed");
		}

		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
		log.info("doEnroll end {}", identity.getKey());
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
			int reservations = reservationDao.countReservations(group.getResource());
			int partipiciantSize = businessGroupRelationDAO.countRoles(group, GroupRoles.participant.name());
			if (maxSize != null && (partipiciantSize + reservations) < maxSize.intValue()) {
				// ok it has free places => get first identities from waiting list
				List<Identity> identities = businessGroupRelationDAO.getMembersOrderByDate(group, GroupRoles.waiting.name());
				
				int counter = 0;
				int freeSlot = maxSize - (partipiciantSize + reservations);
				for(Identity firstWaitingListIdentity: identities) {
					if(counter >= freeSlot) {
						break;
					}
			  	
					// It has an identity and transfer from waiting-list to participant-group is not done
					// Check if firstWaitingListIdentity is not allready in participant-group
					if (!businessGroupRelationDAO.hasRole(firstWaitingListIdentity, group, GroupRoles.participant.name())) {
						// move the identity from the waitinglist to the participant group
						
						ActionType formerStickyActionType = ThreadLocalUserActivityLogger.getStickyActionType();
						try{
							// OLAT-4955: force add-participant and remove-from-waitinglist logging actions 
							//            that get triggered in the next two methods to be of ActionType admin
							//            This is needed to make sure the targetIdentity ends up in the o_loggingtable
							ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
							// Don't send mails for the sub-actions "adding to group" and "remove from waiting list", instead
							// send a specific "graduate from waiting list" mailing a few lines below
							MailPackage subMailing = new MailPackage(false);
							addParticipant(ureqIdentity, null, firstWaitingListIdentity, group, subMailing, events);
							removeFromWaitingList(ureqIdentity, firstWaitingListIdentity, group, subMailing, events);
						} finally {
							ThreadLocalUserActivityLogger.setStickyActionType(formerStickyActionType);
						}

						// Send mail to let user know he is now in group
						if (mailing == null) {
							mailing = new MailPackage(true);
						}

						BusinessGroupMailing.sendEmail(null, firstWaitingListIdentity, group, MailType.graduateFromWaitingListToParticpant, mailing);				
						counter++;
				  }
				}
			}
		}
	}
	
	private void removeOwner(Identity ureqIdentity, Identity identityToRemove, BusinessGroup group,
			List<BusinessGroupModifiedEvent.Deferred> events) {
		
		businessGroupRelationDAO.removeRole(identityToRemove, group, GroupRoles.coach.name());
		
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
		log.info(Tracing.M_AUDIT, "Identity(.key):{} removed identiy '{}' from group with key {}",
			ureqIdentity.getKey(), identityToRemove.getKey(), group.getKey());
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToRemove));
	}
	
	@Override
	public void removeOwners(Identity ureqIdentity, Collection<Identity> identitiesToRemove, BusinessGroup group) {
		List<BusinessGroupModifiedEvent.Deferred> events = new ArrayList<>();
		for(Identity identityToRemove:identitiesToRemove) {
			removeOwner(ureqIdentity, identityToRemove, group, events);
		}
		dbInstance.commit();
		BusinessGroupModifiedEvent.fireDeferredEvents(events);
	}
	
	@Override
	public boolean hasResources(BusinessGroup group) {
		return businessGroupRelationDAO.countResources(group) > 0;
	}
	
	@Override
	public boolean hasResources(List<BusinessGroup> groups) {
		return businessGroupRelationDAO.hasResources(groups);
	}

	@Override
	public void addResourceTo(BusinessGroup group, RepositoryEntry re) {
		businessGroupRelationDAO.addRelationToResource(group, re);
		BusinessGroupRepositoryEntryEvent.fireEvents(BusinessGroupRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED, group, re);
	}

	@Override
	public void addResourcesTo(List<BusinessGroup> groups, List<RepositoryEntry> resources) {
		if(groups == null || groups.isEmpty()) return;
		if(resources == null || resources.isEmpty()) return;

		
		List<Group> baseGroupKeys = new ArrayList<>();
		for(BusinessGroup group:groups) {
			baseGroupKeys.add(group.getBaseGroup());
		}

		//check for duplicate entries
		List<RepositoryEntryToGroupRelation> relations = repositoryEntryRelationDao.getRelations(baseGroupKeys);
		for(BusinessGroup group:groups) {
			//reload the base group to prevent lazy loading exception
			Group baseGroup = businessGroupRelationDAO.getGroup(group);
			if(baseGroup == null) {
				continue;
			}
			for(RepositoryEntry re:resources) {
				boolean found = false;
				for(RepositoryEntryToGroupRelation relation:relations) {
					if(relation.getGroup().equals(baseGroup) && relation.getEntry().equals(re)) {
						found = true;
					}
				}
				if(!found) {
					repositoryEntryRelationDao.createRelation(baseGroup, re);
					BusinessGroupRepositoryEntryEvent.fireEvents(BusinessGroupRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED, group, re);
				}
			}
		}
	}

	@Override
	public void dedupMembers(Identity ureqIdentity, boolean coaches, boolean participants, ProgressDelegate delegate) {
		SearchRepositoryEntryParameters params = new SearchRepositoryEntryParameters();
		params.setRoles(Roles.administratorRoles());
		params.setResourceTypes(Collections.singletonList("CourseModule"));
		
		float ratio = -1.0f;
		if(delegate != null) {
			int numOfEntries = repositoryEntryQueries.countEntries(params);
			ratio = 100.0f / numOfEntries;
		}

		int counter = 0;
		int countForCommit = 0;
		float actual = 100.0f;
		int batch = 25;
		List<RepositoryEntry> entries;
		do {
			entries = repositoryEntryQueries.searchEntries(params, counter, batch, true);
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
		if(coaches) {
			List<Identity> repoTutorList = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.coach.name());
			if(!repoTutorList.isEmpty()) {
				SearchBusinessGroupParams params = new SearchBusinessGroupParams();
				groups = businessGroupDAO.findBusinessGroups(params, entry, 0, -1);
				List<Identity> ownerList = getMembers(groups, GroupRoles.participant.name());
				repoTutorList.retainAll(ownerList);
				if(!dryRun) {
					repositoryManager.removeTutors(ureqIdentity, repoTutorList, entry, new MailPackage(false));
				}
				count += repoTutorList.size();
			}
		}
		
		if(participants) {
			List<Identity> repoParticipantList = repositoryEntryRelationDao.getMembers(entry, RepositoryEntryRelationType.entryAndCurriculums, GroupRoles.participant.name());
			if(!repoParticipantList.isEmpty()) {
			
				if(groups == null) {
					SearchBusinessGroupParams params = new SearchBusinessGroupParams();
					groups = businessGroupDAO.findBusinessGroups(params, entry, 0, -1);
				}
				List<Identity> participantList = getMembers(groups, GroupRoles.participant.name());
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
		
		List<BusinessGroupRelationModified> events = new ArrayList<>();
		
		int count = 0;
		for(BusinessGroup group:groups) {
			businessGroupRelationDAO.deleteRelation(group, re);
			areaManager.removeBGFromAreas(group, re.getOlatResource());
			events.add(new BusinessGroupRelationModified(BusinessGroupRelationModified.RESOURCE_REMOVED_EVENT, group.getKey(), re.getKey()));
			if(count++ % 20 == 0) {
				dbInstance.commit();
			}
		}
		dbInstance.commit();
		
		for(BusinessGroupRelationModified event:events) {
			CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.fireEventToListenersOf(event, OresHelper.lookupType(BusinessGroup.class));
		}
	}
	
	@Override
	public void removeResource(RepositoryEntry re) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		List<BusinessGroup> groups = findBusinessGroups(params, re, 0, -1);
		removeResourceFrom(groups, re);
	}

	@Override
	public List<RepositoryEntry> findRepositoryEntries(Collection<? extends BusinessGroupRef> groups, int firstResult, int maxResults) {
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
	public Group getGroup(BusinessGroup group) {
		return businessGroupRelationDAO.getGroup(group);
	}
	
	@Override
	public boolean hasRoles(IdentityRef identity, BusinessGroupRef businessGroup, String role) {
		return businessGroupRelationDAO.hasRole(identity, businessGroup, role);
	}

	@Override
	public List<Identity> getMembers(BusinessGroupRef businessGroup, String... roles) {
		return businessGroupRelationDAO.getMembers(businessGroup, roles);
	}
	
	@Override
	public List<Identity> getMembers(List<BusinessGroup> businessGroups, String... roles) {
		List<Identity> ids = new ArrayList<>();
		for(BusinessGroupRef businessGroup:businessGroups) {
			ids.addAll(businessGroupRelationDAO.getMembers(businessGroup, roles));
		}
		return ids;
	}

	@Override
	public int countMembers(BusinessGroup businessGroup, String... roles) {
		return businessGroupRelationDAO.countRoles(businessGroup, roles);
	}

	@Override
	public boolean isIdentityInBusinessGroup(IdentityRef identity, BusinessGroupRef businessGroup) {
		if(businessGroup == null || identity == null) return false;
		List<String> roles = businessGroupRelationDAO.getRoles(identity, businessGroup);
		if(roles == null || roles.isEmpty() || (roles.size() == 1 &&  GroupRoles.waiting.name().equals(roles.get(0)))) {
			return false;
		}
		return !roles.isEmpty();
	}

	@Override
	public List<String> getIdentityRolesInBusinessGroup(IdentityRef identity, BusinessGroupRef businessGroup) {
		return businessGroupRelationDAO.getRoles(identity, businessGroup);
	}

	@Override
	public List<BusinessGroupMembership> getBusinessGroupsMembership(Collection<BusinessGroup> businessGroups) {
		return businessGroupDAO.getBusinessGroupsMembership(businessGroups);
	}

	@Override
	public List<BusinessGroupMembership> getBusinessGroupMembership(Collection<Long> businessGroups, Identity... identity) {
		List<BusinessGroupMembershipViewImpl> views =
				businessGroupDAO.getMembershipInfoInBusinessGroups(businessGroups, identity);

		Map<IdentityGroupKey, BusinessGroupMembershipImpl> memberships = new HashMap<>();
		for(BusinessGroupMembershipViewImpl membership: views) {
			if(GroupRoles.coach.name().equals(membership.getRole())) {
				Long groupKey = membership.getGroupKey();
				IdentityGroupKey key = new IdentityGroupKey(membership.getIdentityKey(), groupKey);
				if(!memberships.containsKey(key)) {
					memberships.put(key, new BusinessGroupMembershipImpl(membership.getIdentityKey(), groupKey));
				}
				BusinessGroupMembershipImpl mb = memberships.get(key);
				mb.setOwner(true);
				mb.setCreationDate(membership.getCreationDate());
				mb.setLastModified(membership.getLastModified());
			}
			if(GroupRoles.participant.name().equals(membership.getRole())) {
				Long groupKey = membership.getGroupKey();
				IdentityGroupKey key = new IdentityGroupKey(membership.getIdentityKey(), groupKey);
				if(!memberships.containsKey(key)) {
					memberships.put(key, new BusinessGroupMembershipImpl(membership.getIdentityKey(), groupKey));
				}
				BusinessGroupMembershipImpl mb = memberships.get(key);
				mb.setParticipant(true);
				mb.setCreationDate(membership.getCreationDate());
				mb.setLastModified(membership.getLastModified());
			}
			if(GroupRoles.waiting.name().equals(membership.getRole())) {
				Long groupKey = membership.getGroupKey();
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
		
		return new ArrayList<>(memberships.values());
	}
	
	@Override
	public boolean isIdentityInBusinessGroup(Identity identity, Long groupKey,
			boolean ownedById, boolean attendedById, RepositoryEntryRef resource) {
		return businessGroupRelationDAO.isIdentityInBusinessGroup(identity, groupKey, ownedById, attendedById, resource);
	}

	@Override
	public List<Identity> getIdentitiesWithRole(String role) {
		return businessGroupRelationDAO.getIdentitiesWithRole(role);
	}

	@Override
	public void exportGroups(List<BusinessGroup> groups, List<BGArea> areas, File fExportFile) {
		BusinessGroupImportExport exporter = new BusinessGroupImportExport(dbInstance, areaManager, this, groupModule);
		exporter.exportGroups(groups, areas, fExportFile);
	}

	@Override
	public BusinessGroupEnvironment importGroups(RepositoryEntry re, File fGroupExportXML) {
		BusinessGroupImportExport importer = new BusinessGroupImportExport(dbInstance, areaManager, this, groupModule);
		return importer.importGroups(re, fGroupExportXML);
	}

	@Override
	public void archiveGroups(List<BusinessGroup> groups, File exportFile) {
		BusinessGroupArchiver archiver = new BusinessGroupArchiver(dbInstance);
		archiver.archiveGroups(groups, exportFile);
	}
}
