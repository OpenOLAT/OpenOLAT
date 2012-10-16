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

import javax.annotation.PostConstruct;

import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleObjectStateException;
import org.olat.admin.user.delete.service.UserDeletionManager;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.KnownIssueException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerManagerFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupOrder;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.BusinessGroupView;
import org.olat.group.DeletableGroupData;
import org.olat.group.DeletableReference;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.BGMembership;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.BGResourceRelation;
import org.olat.group.model.BusinessGroupEnvironment;
import org.olat.group.model.BusinessGroupMembershipChange;
import org.olat.group.model.BusinessGroupMembershipImpl;
import org.olat.group.model.BusinessGroupMembershipViewImpl;
import org.olat.group.model.BusinessGroupMembershipsChanges;
import org.olat.group.model.DisplayMembers;
import org.olat.group.model.EnrollState;
import org.olat.group.model.IdentityGroupKey;
import org.olat.group.model.MembershipModification;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.right.BGRightManager;
import org.olat.group.right.BGRightsRole;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.instantMessaging.IMConfigSync;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.syncservice.SyncUserListTask;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryShort;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.ResourceReservation;
import org.olat.testutils.codepoints.server.Codepoint;
import org.olat.user.UserDataDeletable;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
	private BusinessGroupDAO businessGroupDAO;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDAO;
	@Autowired
	private BusinessGroupImportExport businessGroupImportExport;
	@Autowired
	private BusinessGroupArchiver businessGroupArchiver;
	@Autowired
	private BusinessGroupPropertyDAO businessGroupPropertyManager;
	@Autowired
	private UserDeletionManager userDeletionManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private ACService acService;
	@Autowired
	private DB dbInstance;
	
	private List<DeletableGroupData> deleteListeners = new ArrayList<DeletableGroupData>();

	@PostConstruct
	public void init() {
		userDeletionManager.registerDeletableUserData(this);
	}
	
	@Override
	public void registerDeletableGroupDataListener(DeletableGroupData listener) {
		this.deleteListeners.add(listener);
	}

	@Override
	public List<String> getDependingDeletablableListFor(BusinessGroup currentGroup, Locale locale) {
		List<String> deletableList = new ArrayList<String>();
		for (DeletableGroupData deleteListener : deleteListeners) {
			DeletableReference deletableReference = deleteListener.checkIfReferenced(currentGroup, locale);
			if (deletableReference.isReferenced()) {
				deletableList.add(deletableReference.getName());
			}
		}
		return deletableList;
	}
	
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

		BusinessGroup group = businessGroupDAO.createAndPersist(creator, name, description,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled, false, false, false);
		if(re != null) {
			addResourceTo(group, re);
		}
		return group;
	}

	@Override
	@Transactional
	public BusinessGroup updateBusinessGroup(Identity ureqIdentity, BusinessGroup group, String name, String description,
			Integer minParticipants, Integer maxParticipants) {
		
		SyncUserListTask syncIM = new SyncUserListTask(group);
		BusinessGroup bg = businessGroupDAO.loadForUpdate(group.getKey());

		Integer previousMaxParticipants = bg.getMaxParticipants();
		bg.setName(name);
		bg.setDescription(description);
		bg.setMaxParticipants(maxParticipants);
		bg.setMinParticipants(minParticipants);
		bg.setLastUsage(new Date(System.currentTimeMillis()));
		//auto rank if possible
		autoRankCheck(ureqIdentity, bg, previousMaxParticipants, syncIM);
		BusinessGroup updatedGroup = businessGroupDAO.merge(bg);

		syncIM(syncIM, updatedGroup);
		return updatedGroup;
	}

	@Override
	public BusinessGroup updateBusinessGroup(Identity ureqIdentity, BusinessGroup group, String name, String description,
			Integer minParticipants, Integer maxParticipants, Boolean waitingList, Boolean autoCloseRanks) {
		
		SyncUserListTask syncIM = new SyncUserListTask(group);
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
		autoRankCheck(ureqIdentity, bg, previousMaxParticipants, syncIM);
		return businessGroupDAO.merge(bg);
	}
	
	private void autoRankCheck(Identity identity, BusinessGroup updatedGroup, Integer previousMaxParticipants, SyncUserListTask syncIM) {
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
			transferFirstIdentityFromWaitingToParticipant(identity, updatedGroup, syncIM);
		}
	}

	@Override
	public DisplayMembers getDisplayMembers(BusinessGroup group) {
		Property props = businessGroupPropertyManager.findProperty(group);
		DisplayMembers displayMembers = new DisplayMembers();
		displayMembers.setShowOwners(businessGroupPropertyManager.showOwners(props));
		displayMembers.setShowParticipants(businessGroupPropertyManager.showPartips(props));
		displayMembers.setShowWaitingList(businessGroupPropertyManager.showWaitingList(props));
		displayMembers.setOwnersPublic(businessGroupPropertyManager.isOwnersPublic(props));
		displayMembers.setParticipantsPublic(businessGroupPropertyManager.isPartipsPublic(props));
		displayMembers.setWaitingListPublic(businessGroupPropertyManager.isWaitingListPublic(props));
		displayMembers.setDownloadLists(businessGroupPropertyManager.isDownloadLists(props));
		return displayMembers;
	}

	@Override
	public void updateDisplayMembers(BusinessGroup group, DisplayMembers displayMembers) {
		boolean showOwners = displayMembers.isShowOwners();
		boolean showPartips = displayMembers.isShowParticipants();
		boolean showWaitingList = displayMembers.isShowWaitingList();
		boolean ownersPublic = displayMembers.isOwnersPublic();
		boolean partipsPublic = displayMembers.isParticipantsPublic();
		boolean waitingListPublic = displayMembers.isWaitingListPublic();
		boolean downloadLists = displayMembers.isDownloadLists();
		businessGroupPropertyManager.updateDisplayMembers(group, showOwners, showPartips, showWaitingList,
				ownersPublic, partipsPublic, waitingListPublic, downloadLists);
	}

	@Override
	@Transactional
	public BusinessGroup setLastUsageFor(final Identity identity, final BusinessGroup group) {
		BusinessGroup reloadedBusinessGroup = businessGroupDAO.loadForUpdate(group.getKey());
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
		return businessGroupDAO.merge(reloadedBusinessGroup);
	}

	@Override
	@Transactional
	public BusinessGroup loadBusinessGroup(BusinessGroup group) {
		return businessGroupDAO.load(group.getKey());
	}

	@Override
	@Transactional
	public BusinessGroup loadBusinessGroup(Long key) {
		return businessGroupDAO.load(key);
	}

	@Override
	@Transactional
	public BusinessGroup loadBusinessGroup(OLATResource resource) {
		return businessGroupDAO.load(resource.getResourceableId());
	}

	@Override
	@Transactional
	public List<BusinessGroup> loadBusinessGroups(Collection<Long> keys) {
		return businessGroupDAO.load(keys);
	}

	@Override
	public List<BusinessGroupShort> loadShortBusinessGroups(Collection<Long> keys) {
		return businessGroupDAO.loadShort(keys);
	}

	@Override
	@Transactional
	public List<BusinessGroup> loadAllBusinessGroups() {
		return businessGroupDAO.loadAll();
	}

	@Override
	@Transactional
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
			for (int i = 0; i < CollaborationTools.TOOLS.length; i++) {
				String tool = CollaborationTools.TOOLS[i];
				newTools.setToolEnabled(tool, oldTools.isToolEnabled(tool));
			}			
			String oldNews = oldTools.lookupNews();
			newTools.saveNews(oldNews);
		}
		// 3. copy member visibility
		if (copyMemberVisibility) {
			businessGroupPropertyManager.copyConfigurationFromGroup(sourceBusinessGroup, newGroup);
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
			List<OLATResource> resources = businessGroupRelationDAO.findResources(Collections.singletonList(sourceBusinessGroup), 0, -1);
			for(OLATResource resource:resources) {
				businessGroupRelationDAO.addRelationToResource(newGroup, resource);
			}	
		}
		return newGroup;
	}

	@Override
	public BusinessGroup mergeBusinessGroups(final Identity merger, BusinessGroup targetGroup, final List<BusinessGroup> groupsToMerge) {
		groupsToMerge.remove(targetGroup);//to be sure
		final SyncUserListTask syncIM = new SyncUserListTask(targetGroup);

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
		
		for(Identity newOwner:newOwners) {
			addOwner(newOwner, targetGroup, syncIM);
		}
		for(Identity newParticipant:newParticipants) {
			addParticipant(newParticipant, targetGroup, syncIM);
		}
		for(Identity newWaiter:newWaiters) {
			addToWaitingList(newWaiter, targetGroup);
		}
			
		syncIM(syncIM, targetGroup);
		for(BusinessGroup group:groupsToMerge) {
			deleteBusinessGroup(group);
		}
		return targetGroup;
	}

	@Override
	public void updateMembership(Identity identity, MembershipModification membersMod, List<BusinessGroup> groups) {
		for(BusinessGroup group:groups) {
			updateMembers(identity, membersMod, group);
		}
	}
	
	private void updateMembers(final Identity identity, final MembershipModification membersMod, BusinessGroup group) {
		final SyncUserListTask syncIM = new SyncUserListTask(group);
		
		group = businessGroupDAO.loadForUpdate(group.getKey());
		
		List<Identity> currentOwners = securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
		List<Identity> currentParticipants = securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
		List<Identity> currentWaitingList = securityManager.getIdentitiesOfSecurityGroup(group.getWaitingGroup());

		for(Identity owner:membersMod.getAddOwners()) {
			if(!currentOwners.contains(owner)) {
				addOwner(owner, group, syncIM);
			}
		}
		for(Identity participant:membersMod.getAddParticipants()) {
			if(!currentParticipants.contains(participant)) {
				addParticipant(participant, group, syncIM);
			}
		}
		for(Identity waitingIdentity:membersMod.getAddToWaitingList()) {
			if(!currentWaitingList.contains(waitingIdentity)) {
				addToWaitingList(waitingIdentity, group);
			}
		}
		
		//remove owners
		List<Identity> ownerToRemove = new ArrayList<Identity>();
		for(Identity removed:membersMod.getRemovedIdentities()) {
			if(currentOwners.contains(removed)) {
				ownerToRemove.add(removed);
			}
			if(currentParticipants.contains(removed)) {
				removeParticipant(identity, removed, group, syncIM);
			}
			if(currentWaitingList.contains(removed)) {
				removeFromWaitingList(removed, group);
			}
		}
		removeOwners(identity, ownerToRemove, group);
		
		//release lock
		dbInstance.commit();
		
		syncIM(syncIM, group);
	}

	@Override
	@Transactional
	public void updateMemberships(final Identity ureqIdentity, final List<BusinessGroupMembershipChange> changes) {
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
		
		List<BusinessGroup> groups = loadBusinessGroups(changesMap.keySet());
		for(BusinessGroup group:groups) {
			BusinessGroupMembershipsChanges changesWrapper = changesMap.get(group.getKey());
			SyncUserListTask syncIM = new SyncUserListTask(group);
			group = businessGroupDAO.loadForUpdate(group.getKey());
					
			for(Identity id:changesWrapper.addToWaitingList) {
				addToWaitingList(id, group);
			}
			for(Identity id:changesWrapper.removeFromWaitingList) {
				removeFromWaitingList(id, group);
			}
			for(Identity id:changesWrapper.addTutors) {
				addOwner(id, group, syncIM);
			}
			for(Identity id:changesWrapper.removeTutors) {
				removeOwner(ureqIdentity, id, group, syncIM);
			}
			for(Identity id:changesWrapper.addParticipants) {
				addParticipant(id, group, syncIM);
			}
			for(Identity id:changesWrapper.removeParticipants) {
				removeParticipant(ureqIdentity, id, group, syncIM);
			}
			//release lock
			dbInstance.commit();
			
			syncIM(syncIM, group);
		}
	}

	@Override
	@Transactional
	public BusinessGroup findBusinessGroup(SecurityGroup secGroup) {
		return businessGroupDAO.findBusinessGroup(secGroup);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroup> findBusinessGroupsOwnedBy(Identity identity, OLATResource resource) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, true, false);
		return businessGroupDAO.findBusinessGroups(params, resource, 0, -1);
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroup> findBusinessGroupsAttendedBy(Identity identity, OLATResource resource) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, false, true);
		return businessGroupDAO.findBusinessGroups(params, resource, 0, -1);
	}
	
	@Override
	public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(Identity identity,  OLATResource resource) {
		return businessGroupDAO.findBusinessGroupsWithWaitingListAttendedBy(identity, resource);
	}
	
	@Override
	@Transactional(readOnly=true)
	public int countBusinessGroups(SearchBusinessGroupParams params, OLATResource resource) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.countBusinessGroups(params, resource);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroup> findBusinessGroups(SearchBusinessGroupParams params, OLATResource resource,
			int firstResult, int maxResults, BusinessGroupOrder... ordering) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.findBusinessGroups(params, resource, firstResult, maxResults);
	}

	@Override
	@Transactional(readOnly=true)
	public int countBusinessGroupViews(SearchBusinessGroupParams params, OLATResource resource) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.countBusinessGroupViews(params, resource);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroupView> findBusinessGroupViews(SearchBusinessGroupParams params, OLATResource resource, int firstResult,
			int maxResults, BusinessGroupOrder... ordering) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.findBusinessGroupViews(params, resource, firstResult, maxResults);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroupView> findBusinessGroupViewsWithAuthorConnection(Identity author) {
		return businessGroupDAO.findBusinessGroupWithAuthorConnection(author);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Long> toGroupKeys(String groupNames, OLATResource resource) {
		return businessGroupDAO.toGroupKeys(groupNames, resource);
	}

	@Override
	@Transactional(readOnly=true)
	public int countContacts(Identity identity) {
		return businessGroupDAO.countContacts(identity);
	}

	@Override
	@Transactional(readOnly=true)
	public List<Identity> findContacts(Identity identity, int firstResult, int maxResults) {
		return businessGroupDAO.findContacts(identity, firstResult, maxResults);
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
			for (DeletableGroupData deleteListener : deleteListeners) {
				if(log.isDebug()) {
					log.debug("deleteBusinessGroup: call deleteListener=" + deleteListener);
				}
				deleteListener.deleteGroupDataFor(group);
			} 
			
			// 0) Delete from project broker 
			ProjectBrokerManagerFactory.getProjectBrokerManager().deleteGroupDataFor(group);
			// 1) Delete all group properties
			
			CollaborationTools ct = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
			ct.deleteTools(group);// deletes everything concerning properties&collabTools
			
			// 1.b)delete display member property
			businessGroupPropertyManager.deleteDisplayMembers(group);
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
	
			// delete potential jabber group roster
			if (InstantMessagingModule.isEnabled()) {
				String groupID = InstantMessagingModule.getAdapter().createChatRoomString(group);
				InstantMessagingModule.getAdapter().deleteRosterGroup(groupID);
			}
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
		// finally send email
		MailerWithTemplate mailer = MailerWithTemplate.getInstance();
		MailTemplate mailTemplate = BGMailHelper.createDeleteGroupMailTemplate(businessGroupTodelete, deletedBy);
		if (mailTemplate != null) {
			//fxdiff VCRP-16: intern mail system
			MailContext context = new MailContextImpl(businessPath);
			MailerResult mailerResult = mailer.sendMailAsSeparateMails(context, users, null, null, mailTemplate, null);
			//MailHelper.printErrorsAndWarnings(mailerResult, wControl, locale);
			return mailerResult;
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
	public BusinessGroupAddResponse addOwners(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup group) {
		SyncUserListTask syncIM = new SyncUserListTask(group);
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		for (Identity identity : addIdentities) {
			group = loadBusinessGroup(group); // reload business group
			if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
				response.getIdentitiesWithoutPermission().add(identity);
			} else if(addOwner(identity, group, syncIM)) {
				response.getAddedIdentities().add(identity);
				log.audit("added identity '" + identity.getName() + "' to securitygroup with key " + group.getOwnerGroup().getKey());
			} else {
				response.getIdentitiesAlreadyInGroup().add(identity);
			}
		}
		syncIM(syncIM, group);
		return response;
	}
	
	private boolean addOwner(Identity identity, BusinessGroup group, SyncUserListTask syncIM) {
		if (!securityManager.isIdentityInSecurityGroup(identity, group.getOwnerGroup())) {
			securityManager.addIdentityToSecurityGroup(identity, group.getOwnerGroup());
			// add user to buddies rosters
			if(syncIM != null) {
				syncIM.addUserToAdd(identity.getName());
			}
			// notify currently active users of this business group
			BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
			// do logging
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identity));
			return true;
		}
		return false;
	}
	
	private boolean addParticipant(Identity identityToAdd, BusinessGroup group, SyncUserListTask syncIM) {
		if(!securityManager.isIdentityInSecurityGroup(identityToAdd, group.getPartipiciantGroup())) {
			securityManager.addIdentityToSecurityGroup(identityToAdd, group.getPartipiciantGroup());
			// add user to buddies rosters
			if(syncIM != null) {
				syncIM.addUserToAdd(identityToAdd.getName());
			}
			
			// notify currently active users of this business group
			BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identityToAdd);
			// do logging
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToAdd));
			// send notification mail in your controller!
			return true;
		}
		return false;
	}

	@Override
	@Transactional
	public BusinessGroupAddResponse addParticipants(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup group) {	
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		SyncUserListTask syncIM = new SyncUserListTask(group);
		
		BusinessGroup currBusinessGroup = businessGroupDAO.loadForUpdate(group.getKey());	
		for (final Identity identity : addIdentities) {
			if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
				response.getIdentitiesWithoutPermission().add(identity);
			} else if(addParticipant(identity, currBusinessGroup, syncIM)) {
				response.getAddedIdentities().add(identity);
				log.audit("added identity '" + identity.getName() + "' to securitygroup with key " + currBusinessGroup.getPartipiciantGroup().getKey());
			} else {
				response.getIdentitiesAlreadyInGroup().add(identity);
			}
		}

		syncIM(syncIM, group);
		return response;
	}

	private void removeParticipant(Identity ureqIdentity, Identity identity, BusinessGroup group, SyncUserListTask syncIM) {

		securityManager.removeIdentityFromSecurityGroup(identity, group.getPartipiciantGroup());
		// remove user from buddies rosters
		syncIM.addUserToRemove(identity.getName());
		//remove subscriptions if user gets removed
		removeSubscriptions(identity, group);
		
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_REMOVED, getClass(), LoggingResourceable.wrap(identity), LoggingResourceable.wrap(group));
		// Check if a waiting-list with auto-close-ranks is configurated
		if ( group.getWaitingListEnabled().booleanValue() && group.getAutoCloseRanksEnabled().booleanValue() ) {
			// even when doOnlyPostRemovingStuff is set to true we really transfer the first Identity here
			transferFirstIdentityFromWaitingToParticipant(ureqIdentity, group, syncIM);
		}	
		// send notification mail in your controller!
		
	}
	
	@Override
	@Transactional
	public void removeParticipants(Identity ureqIdentity, List<Identity> identities, BusinessGroup group) {
		final SyncUserListTask syncIM = new SyncUserListTask(group);
		group = businessGroupDAO.loadForUpdate(group.getKey());
		for (Identity identity : identities) {
		  removeParticipant(ureqIdentity, identity, group, syncIM);
		  log.audit("removed identiy '" + identity.getName() + "' from securitygroup with key " + group.getPartipiciantGroup().getKey());
		}
		syncIM(syncIM, group);
	}

	@Override
	public void removeMembers(Identity ureqIdentity, List<Identity> identities, OLATResource resource) {
		if(identities == null || identities.isEmpty() || resource == null) return;//nothing to do
		
		List<BusinessGroup> groups = findBusinessGroups(null, resource, 0, -1);
		if(groups.isEmpty()) return;//nothing to do
		
		Map<Long,BusinessGroup> keyToGroupMap = new HashMap<Long,BusinessGroup>();
		for(BusinessGroup group:groups) {
			keyToGroupMap.put(group.getKey(), group);
		}
		final Map<Long,Identity> keyToIdentityMap = new HashMap<Long,Identity>();
		for(Identity identity:identities) {
			keyToIdentityMap.put(identity.getKey(), identity);
		}
		
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
			SyncUserListTask syncIM = new SyncUserListTask(nextGroup);
			nextGroupMembership = removeGroupMembers(ureqIdentity, currentMembership, nextGroup, keyToIdentityMap, itMembership, syncIM);
			//release the lock
			dbInstance.commit();
			syncIM(syncIM, nextGroup);
		}
	}
	
	private final BusinessGroupMembershipViewImpl removeGroupMembers(Identity ureqIdentity, BusinessGroupMembershipViewImpl currentMembership,
			BusinessGroup currentGroup, Map<Long,Identity> keyToIdentityMap, Iterator<BusinessGroupMembershipViewImpl> itMembership,
			SyncUserListTask syncIM) {

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
					removeOwner(ureqIdentity, id, currentGroup, syncIM);
				}
				if(membership.getParticipantGroupKey() != null) {
					removeParticipant(ureqIdentity, id, currentGroup, syncIM);
				}
				if(membership.getWaitingGroupKey() != null) {
					removeFromWaitingList(id, currentGroup);
				}
			} else {
				return membership;
			}
		} while (itMembership.hasNext());

		return null;
	}

	private void addToWaitingList(Identity identity, BusinessGroup group) {
		securityManager.addIdentityToSecurityGroup(identity, group.getWaitingGroup());

		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_TO_WAITING_LIST_ADDED, getClass(), LoggingResourceable.wrap(identity));
		// send notification mail in your controller!
	}
	
	@Override
	public BusinessGroupAddResponse addToWaitingList(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup group) {
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		BusinessGroup currBusinessGroup = businessGroupDAO.loadForUpdate(group.getKey()); // reload business group

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
				addToWaitingList(identity, currBusinessGroup);
				response.getAddedIdentities().add(identity);
				log.audit("added identity '" + identity.getName() + "' to securitygroup with key " + currBusinessGroup.getPartipiciantGroup().getKey());
			}
		}
		return response;
	}

	private final void removeFromWaitingList(Identity identity, BusinessGroup group) {
		securityManager.removeIdentityFromSecurityGroup(identity, group.getWaitingGroup());
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_FROM_WAITING_LIST_REMOVED, getClass(), LoggingResourceable.wrap(identity));
		// send notification mail in your controller!
	}
	
	@Override
	public void removeFromWaitingList(Identity ureqIdentity, List<Identity> identities, BusinessGroup currBusinessGroup) {
		currBusinessGroup = businessGroupDAO.loadForUpdate(currBusinessGroup.getKey());
		
		for (Identity identity : identities) {
		  removeFromWaitingList(identity, currBusinessGroup);
		  log.audit("removed identiy '" + identity.getName() + "' from securitygroup with key " + currBusinessGroup.getOwnerGroup().getKey());
		}
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
	public BusinessGroupAddResponse moveIdentityFromWaitingListToParticipant(List<Identity> identities, Identity ureqIdentity,
			BusinessGroup currBusinessGroup) {
		
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		SyncUserListTask syncIM = new SyncUserListTask(currBusinessGroup);
		currBusinessGroup = businessGroupDAO.loadForUpdate(currBusinessGroup.getKey());
		
		for (Identity identity : identities) {
			// check if idenity is allready in participant
			if (!securityManager.isIdentityInSecurityGroup(identity,currBusinessGroup.getPartipiciantGroup()) ) {
				// Idenity is not in participant-list => move idenity from waiting-list to participant-list
				addParticipant(identity, currBusinessGroup, syncIM);
				removeFromWaitingList(identity, currBusinessGroup);
				response.getAddedIdentities().add(identity);
				// notification mail is handled in controller
			} else {
				response.getIdentitiesAlreadyInGroup().add(identity);
			}
		}
		
		syncIM(syncIM, currBusinessGroup);
		return response;
	}

	@Override
	public BusinessGroupAddResponse addToSecurityGroupAndFireEvent(Identity ureqIdentity, List<Identity> addIdentities, SecurityGroup secGroup) {
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		for (Identity identity : addIdentities) {
			if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
				response.getIdentitiesWithoutPermission().add(identity);
			}
			// Check if identity is already in group. make a db query in case
			// someone in another workflow already added this user to this group. if
			// found, add user to model
			else if (securityManager.isIdentityInSecurityGroup(identity, secGroup)) {
				response.getIdentitiesAlreadyInGroup().add(identity);
			} else {
	      // identity has permission and is not already in group => add it
				securityManager.addIdentityToSecurityGroup(identity, secGroup);
				ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(), LoggingResourceable.wrap(identity));
				
				response.getAddedIdentities().add(identity);
				log.audit("added identity '" + identity.getName() + "' to securitygroup with key " + secGroup.getKey());
			}
		}
		return response;
	}
	
	@Override
	public void removeAndFireEvent(Identity ureqIdentity, List<Identity> identities, SecurityGroup secGroup) {
		for (Identity identity : identities) {
			securityManager.removeIdentityFromSecurityGroup(identity, secGroup);
		  log.audit("removed identiy '" + identity.getName() + "' from securitygroup with key " + secGroup.getKey());
		}
	}
	
	@Override
	public EnrollState enroll(final BusinessGroup group,  final Identity identity) {
		final BusinessGroup reloadedGroup = businessGroupDAO.loadForUpdate(group.getKey());
		
		log.info("doEnroll start: group=" + OresHelper.createStringRepresenting(group), identity.getName());
		EnrollState enrollStatus = new EnrollState();

		ResourceReservation reservation = acService.getReservation(identity, reloadedGroup.getResource());
		SyncUserListTask syncIM = new SyncUserListTask(reloadedGroup);
		
		//reservation has the highest priority over max participant or other settings
		if(reservation != null) {
			addParticipant(identity, reloadedGroup, syncIM);
			enrollStatus.setEnrolled(BGMembership.participant);
			log.info("doEnroll (reservation) - setIsEnrolled ", identity.getName());
			if(reservation != null) {
				acService.removeReservation(reservation);
			}
		} else if (reloadedGroup.getMaxParticipants() != null) {
			int participantsCounter = securityManager.countIdentitiesOfSecurityGroup(reloadedGroup.getPartipiciantGroup());
			int reservations = acService.countReservations(reloadedGroup.getResource());
			
			log.info("doEnroll - participantsCounter: " + participantsCounter + ", reservations: " + reservations + " maxParticipants: " + reloadedGroup.getMaxParticipants().intValue(), identity.getName());
			if (reservation == null && (participantsCounter + reservations) >= reloadedGroup.getMaxParticipants().intValue()) {
				// already full, show error and updated choose page again
				if (reloadedGroup.getWaitingListEnabled().booleanValue()) {
					addToWaitingList(identity, reloadedGroup);
					enrollStatus.setEnrolled(BGMembership.waiting);
				} else {
					// No Waiting List => List is full
					enrollStatus.setI18nErrorMessage("error.group.full");
					enrollStatus.setFailed(true);
				}
			} else {
				//enough place
				addParticipant(identity, reloadedGroup, syncIM);
				enrollStatus.setEnrolled(BGMembership.participant);
				log.info("doEnroll - setIsEnrolled ", identity.getName());
			}
		} else {
			if (log.isDebug()) log.debug("doEnroll as participant beginTransaction");
			addParticipant(identity, reloadedGroup, syncIM);
			enrollStatus.setEnrolled(BGMembership.participant);						
			if (log.isDebug()) log.debug("doEnroll as participant committed");
		}
		
		syncIM(syncIM, reloadedGroup);
		log.info("doEnroll end", identity.getName());
		return enrollStatus;
	}

	private void transferFirstIdentityFromWaitingToParticipant(Identity ureqIdentity, BusinessGroup group, SyncUserListTask syncIM) {

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
							addParticipant(firstWaitingListIdentity, group, syncIM);
							removeFromWaitingList(firstWaitingListIdentity, group);
						} finally {
							ThreadLocalUserActivityLogger.setStickyActionType(formerStickyActionType);
						}
						// send a notification mail if available
						MailTemplate mailTemplate = BGMailHelper.createWaitinglistTransferMailTemplate(group, ureqIdentity);
						if (mailTemplate != null) {
							MailerWithTemplate mailer = MailerWithTemplate.getInstance();
							//fxdiff VCRP-16: intern mail system
							MailContext context = new MailContextImpl("[BusinessGroup:" + group.getKey() + "]");
							mailer.sendMail(context, firstWaitingListIdentity, null, null, mailTemplate, null);
							// Does not report errors to current screen because this is the identity who triggered the transfer
							log.warn("Could not send WaitinglistTransferMail for identity=" + firstWaitingListIdentity.getName());
						}						
						counter++;
				  }
				}
			}
		} else {
			log.warn("Called method transferFirstIdentityFromWaitingToParticipant but waiting-list or autoCloseRanks is disabled.");
		}
	}
	
	private void syncIM(SyncUserListTask task, BusinessGroup group) {
		if (!task.isEmpty() && InstantMessagingModule.isEnabled()) {
			//evaluate whether to sync or not
			IMConfigSync syncGroup = InstantMessagingModule.getAdapter().getConfig().getSyncGroupsConfig();
			//only sync when a group is a certain type and this type is configured that you want to sync it
			if(syncGroup.equals(IMConfigSync.allGroups) || 
					(syncGroup.equals(IMConfigSync.perConfig) && isChatEnableFor(group))) { 

				//course group enrolment is time critial so we move this in an separate thread and catch all failures 
				try {
					TaskExecutorManager.getInstance().runTask(task);
				} catch (Exception e) {
					log.error("Error trying to sync the roster of the business group: " + group, e);
				}
			}
		}
	}
	
	private boolean isChatEnableFor(BusinessGroup group) {
		CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
		if(tools == null) {
			return false;
		}
		return tools.isToolEnabled(CollaborationTools.TOOL_CHAT);
	}
	
	private void removeOwner(Identity ureqIdentity, Identity identityToRemove, BusinessGroup group, SyncUserListTask syncIM) {
		securityManager.removeIdentityFromSecurityGroup(identityToRemove, group.getOwnerGroup());
		// remove user from buddies rosters
		syncIM.addUserToRemove(identityToRemove.getName());
		
		//remove subsciptions if user gets removed
		removeSubscriptions(identityToRemove, group);
		
		// notify currently active users of this business group
		if (identityToRemove.getKey().equals(ureqIdentity.getKey()) ) {
			BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.MYSELF_ASOWNER_REMOVED_EVENT, group, identityToRemove);
		} else {
  		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identityToRemove);
		}
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToRemove));
	}
	
	@Override
	public void removeOwners(Identity ureqIdentity, Collection<Identity> identitiesToRemove, BusinessGroup group) {
		SyncUserListTask syncIM = new SyncUserListTask(group);
		for(Identity identityToRemove:identitiesToRemove) {
			removeOwner(ureqIdentity, identityToRemove, group, syncIM);
		}
		syncIM(syncIM, group);
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
	@Transactional(readOnly=true)
	public boolean hasResources(BusinessGroup group) {
		return businessGroupRelationDAO.countResources(group) > 0;
	}
	
	@Override
	@Transactional(readOnly=true)
	public boolean hasResources(List<BusinessGroup> groups) {
		return businessGroupRelationDAO.countResources(groups) > 0;
	}

	@Override
	@Transactional
	public void addResourceTo(BusinessGroup group, RepositoryEntry re) {
		businessGroupRelationDAO.addRelationToResource(group, re.getOlatResource());
		//add author permission
		securityManager.createAndPersistPolicyWithResource(re.getOwnerGroup(), Constants.PERMISSION_ACCESS, group.getResource());
		//add coach and participant permission
		securityManager.createAndPersistPolicyWithResource(group.getOwnerGroup(), Constants.PERMISSION_COACH, re.getOlatResource());
		securityManager.createAndPersistPolicyWithResource(group.getPartipiciantGroup(), Constants.PERMISSION_PARTI, re.getOlatResource());
	}

	@Override
	@Transactional
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
	@Transactional
	public void removeResourceFrom(BusinessGroup group, RepositoryEntry re) {
		businessGroupRelationDAO.deleteRelation(group, re.getOlatResource());
		//remove author permission
		securityManager.deletePolicy(re.getOwnerGroup(), Constants.PERMISSION_ACCESS, group.getResource());
		//remove permission
		securityManager.deletePolicy(group.getOwnerGroup(), Constants.PERMISSION_COACH, re.getOlatResource());
		securityManager.deletePolicy(group.getPartipiciantGroup(), Constants.PERMISSION_PARTI, re.getOlatResource());
	}
	
	@Override
	@Transactional
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
	}

	@Override
	@Transactional(readOnly=true)
	public List<OLATResource> findResources(Collection<BusinessGroup> groups, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findResources(groups, firstResult, maxResults);
	}

	@Override
	@Transactional(readOnly=true)
	public List<RepositoryEntry> findRepositoryEntries(Collection<BusinessGroup> groups, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findRepositoryEntries(groups, firstResult, maxResults);
	}

	@Override
	@Transactional(readOnly=true)
	public List<RepositoryEntryShort> findShortRepositoryEntries(Collection<BusinessGroupShort> groups, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findShortRepositoryEntries(groups, firstResult, maxResults);
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<BGRepositoryEntryRelation> findRelationToRepositoryEntries(Collection<Long> groupKeys, int firstResult, int maxResults) {
		return businessGroupRelationDAO.findRelationToRepositoryEntries(groupKeys, firstResult, maxResults);
	}

	@Override
	@Transactional(readOnly=true)
	public boolean isIdentityInBusinessGroup(Identity identity, BusinessGroup businessGroup) {
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
	@Transactional(readOnly=true)
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
	@Transactional(readOnly=true)
	public boolean isIdentityInBusinessGroup(Identity identity, Long groupKey,
			boolean ownedById, boolean attendedById, OLATResource resource) {
		return businessGroupRelationDAO.isIdentityInBusinessGroup(identity, groupKey, ownedById, attendedById, resource);
	}
	
	@Override
	public void exportGroups(List<BusinessGroup> groups, List<BGArea> areas, File fExportFile,
			BusinessGroupEnvironment env, boolean backwardsCompatible) {
		businessGroupImportExport.exportGroups(groups, areas, fExportFile, env, backwardsCompatible);
	}

	@Override
	public BusinessGroupEnvironment importGroups(RepositoryEntry re, File fGroupExportXML) {
		return businessGroupImportExport.importGroups(re, fGroupExportXML);
	}

	@Override
	public void archiveGroups(List<BusinessGroup> groups, File exportFile) {
		businessGroupArchiver.archiveGroups(groups, exportFile);
	}

	@Override
	public File archiveGroupMembers(OLATResource resource, List<String> columnList, List<BusinessGroup> groupList, String archiveType, Locale locale, String charset) {
		return businessGroupArchiver.archiveGroupMembers(resource, columnList, groupList, archiveType, locale, charset);
	}
}
