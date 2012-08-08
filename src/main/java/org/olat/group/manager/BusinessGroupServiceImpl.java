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
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.DBRuntimeException;
import org.olat.core.logging.KnownIssueException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ActionType;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.mail.MailContext;
import org.olat.core.util.mail.MailContextImpl;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.resource.OLATResourceableJustBeforeDeletedEvent;
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
import org.olat.group.model.AddToGroupsEvent;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.group.model.BusinessGroupEnvironment;
import org.olat.group.model.DisplayMembers;
import org.olat.group.model.MembershipModification;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.right.BGRightManager;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.syncservice.SyncSingleUserTask;
import org.olat.notifications.NotificationsManagerImpl;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.resource.accesscontrol.ACService;
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
	private ACService acService;
	
	private List<DeletableGroupData> deleteListeners = new ArrayList<DeletableGroupData>();

	@PostConstruct
	public void init() {
		userDeletionManager.registerDeletableUserData(this);
	}
	
	public void registerDeletableGroupDataListener(DeletableGroupData listener) {
		this.deleteListeners.add(listener);
	}

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
			OLATResource resource) {

		BusinessGroup group = businessGroupDAO.createAndPersist(creator, name, description,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled, false, false, false);
		if(resource instanceof OLATResourceImpl) {
			businessGroupRelationDAO.addRelationToResource(group, resource);
			//add coach and participant permission
			securityManager.createAndPersistPolicyWithResource(group.getOwnerGroup(), Constants.PERMISSION_COACH, resource);
			securityManager.createAndPersistPolicyWithResource(group.getPartipiciantGroup(), Constants.PERMISSION_PARTI, resource);
		}
		return group;
	}

	@Override
	@Transactional
	public BusinessGroup updateBusinessGroup(final BusinessGroup group, final String name, final String description,
			final Integer minParticipants, final Integer maxParticipants) {
		
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<BusinessGroup>() {
			public BusinessGroup execute() {
				// refresh group to prevent stale object exception and context proxy issues
				BusinessGroup bg = loadBusinessGroup(group);
				bg.setName(name);
				bg.setDescription(description);
				bg.setMaxParticipants(minParticipants);
				bg.setMinParticipants(maxParticipants);
				bg.setLastUsage(new Date(System.currentTimeMillis()));
				return businessGroupDAO.merge(bg);
			}
		});
	}
	
	public BusinessGroup updateBusinessGroup(final BusinessGroup group, final String name, final String description,
			final Integer minParticipants, final Integer maxParticipants, final Boolean waitingList, final Boolean autoCloseRanks) {
		
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<BusinessGroup>() {
			public BusinessGroup execute() {
				// refresh group to prevent stale object exception and context proxy issues
				BusinessGroup bg = loadBusinessGroup(group);
				bg.setName(name);
				bg.setDescription(description);
				bg.setMaxParticipants(minParticipants);
				bg.setMinParticipants(maxParticipants);
				bg.setWaitingListEnabled(waitingList);
				if (waitingList != null && waitingList.booleanValue() && bg.getWaitingGroup() == null) {
					// Waitinglist is enabled but not created => Create waitingGroup
					SecurityGroup waitingGroup = securityManager.createAndPersistSecurityGroup();
					bg.setWaitingGroup(waitingGroup);
				}
				bg.setAutoCloseRanksEnabled(autoCloseRanks);
				bg.setLastUsage(new Date(System.currentTimeMillis()));
				return businessGroupDAO.merge(bg);
			}
		});
	}

	@Override
	public DisplayMembers getDisplayMembers(BusinessGroup group) {
		Property props = businessGroupPropertyManager.findProperty(group);
		DisplayMembers displayMembers = new DisplayMembers();
		displayMembers.setShowOwners(businessGroupPropertyManager.showOwners(props));
		displayMembers.setShowParticipants(businessGroupPropertyManager.showPartips(props));
		displayMembers.setShowWaitingList(businessGroupPropertyManager.showWaitingList(props));
		return displayMembers;
	}

	@Override
	public void updateDisplayMembers(BusinessGroup group, DisplayMembers displayMembers) {
		boolean showOwners = displayMembers.isShowOwners();
		boolean showPartips = displayMembers.isShowParticipants();
		boolean showWaitingList = displayMembers.isShowWaitingList();
		businessGroupPropertyManager.updateDisplayMembers(group, showOwners, showPartips, showWaitingList);
	}

	@Override
	@Transactional
	public BusinessGroup setLastUsageFor(final Identity identity, final BusinessGroup group) {
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<BusinessGroup>() {
			public BusinessGroup execute() {
				try {
					BusinessGroup reloadedBusinessGroup = loadBusinessGroup(group);
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
				} catch(DBRuntimeException e) {
					if(e.getCause() instanceof ObjectNotFoundException) {
						//group deleted
						return null;
					}
					throw e;
				}
			}
		});
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
	public BusinessGroup copyBusinessGroup(BusinessGroup sourceBusinessGroup, String targetName, String targetDescription, Integer targetMin,
			Integer targetMax, OLATResource targetResource, Map<BGArea, BGArea> areaLookupMap, boolean copyAreas, boolean copyCollabToolConfig,
			boolean copyRights, boolean copyOwners, boolean copyParticipants, boolean copyMemberVisibility, boolean copyWaitingList,
			boolean copyRelations) {

		// 1. create group, set waitingListEnabled, enableAutoCloseRanks like source business-group
		BusinessGroup newGroup = createBusinessGroup(null, targetName, targetDescription, targetMin, targetMax, 
				sourceBusinessGroup.getWaitingListEnabled(), sourceBusinessGroup.getAutoCloseRanksEnabled(), targetResource);
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
				if (areaLookupMap == null) {
					// reference target group to source groups areas
					areaManager.addBGToBGArea(newGroup, area);
				} else {
					// reference target group to mapped group areas
					BGArea mappedArea = (BGArea) areaLookupMap.get(area);
					areaManager.addBGToBGArea(newGroup, mappedArea);
				}
			}
		}
		// 5. copy owners
		if (copyOwners) {
			List<Identity> owners = securityManager.getIdentitiesOfSecurityGroup(sourceBusinessGroup.getOwnerGroup());
			for (Identity identity:owners) {
				securityManager.addIdentityToSecurityGroup(identity, newGroup.getOwnerGroup());
			}
		}
		// 6. copy participants
		if (copyParticipants) {
			List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(sourceBusinessGroup.getPartipiciantGroup());
			for(Identity identity:participants) {
				securityManager.addIdentityToSecurityGroup(identity, newGroup.getPartipiciantGroup());
			}
		}
		// 7. copy rights
		if (copyRights) {
			List<String> sourceRights = rightManager.findBGRights(sourceBusinessGroup);
			for (String sourceRight:sourceRights) {
				rightManager.addBGRight(sourceRight, newGroup);
			}
		}
		// 8. copy waiting-lisz
		if (copyWaitingList) {
			List<Identity> waitingList = securityManager.getIdentitiesOfSecurityGroup(sourceBusinessGroup.getWaitingGroup());
			for (Identity identity:waitingList) {
				securityManager.addIdentityToSecurityGroup(identity, newGroup.getWaitingGroup());
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
	public BusinessGroup mergeBusinessGroups(final Identity merger, final BusinessGroup targetGroup, final List<BusinessGroup> groupsToMerge) {
		groupsToMerge.remove(targetGroup);//to be sure

		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(targetGroup, new SyncerExecutor(){
			public void execute() {
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
					addOwner(merger, newOwner, targetGroup);
				}
				for(Identity newParticipant:newParticipants) {
					addParticipant(merger, newParticipant, targetGroup);
				}
				for(Identity newWaiter:newWaiters) {
					addToWaitingList(merger, newWaiter, targetGroup);
				}
			}
		});
		
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
	
	private void updateMembers(final Identity identity, final MembershipModification membersMod, final BusinessGroup group) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
			public void execute() {
				List<Identity> currentOwners = securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
				List<Identity> currentParticipants = securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
				List<Identity> currentWaitingList = securityManager.getIdentitiesOfSecurityGroup(group.getWaitingGroup());

				for(Identity owner:membersMod.getAddOwners()) {
					if(!currentOwners.contains(owner)) {
						addOwner(identity, owner, group);
					}
				}
				for(Identity participant:membersMod.getAddParticipants()) {
					if(!currentParticipants.contains(participant)) {
						addParticipant(identity, participant, group);
					}
				}
				for(Identity waitingIdentity:membersMod.getAddToWaitingList()) {
					if(!currentWaitingList.contains(waitingIdentity)) {
						addToWaitingList(identity, waitingIdentity, group);
					}
				}
				
				//remove owners
				List<Identity> ownerToRemove = new ArrayList<Identity>();
				for(Identity removed:membersMod.getRemovedIdentities()) {
					if(currentOwners.contains(removed)) {
						ownerToRemove.add(removed);
					}
					if(currentParticipants.contains(removed)) {
						removeParticipant(identity, removed, group);
					}
					if(currentWaitingList.contains(removed)) {
						removeFromWaitingList(identity, removed, group);
					}
				}
				removeOwners(identity, ownerToRemove, group);
		}});
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
			removeFromRepositoryEntrySecurityGroup(group);
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
			NotificationsManagerImpl.getInstance().deletePublishersOf(group);
	
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
	
	private void removeFromRepositoryEntrySecurityGroup(BusinessGroup group) {
		//TODO
		/*
		BGContext context = group.getGroupContext();
		if(context == null) return;//nothing to do
		
		
		BGContextManager contextManager = BGContextManagerImpl.getInstance();
		List<Identity> coaches = group.getOwnerGroup() == null ? Collections.<Identity>emptyList() :
			securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
		List<Identity> participants = group.getPartipiciantGroup() == null ? Collections.<Identity>emptyList() :
			securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
		List<RepositoryEntry> entries = contextManager.findRepositoryEntriesForBGContext(context);
		
		for(Identity coach:coaches) {
			List<BusinessGroup> businessGroups = contextManager.getBusinessGroupAsOwnerOfBGContext(coach, context) ;
			if(context.isDefaultContext() && businessGroups.size() == 1) {
				for(RepositoryEntry entry:entries) {
					if(entry.getTutorGroup() != null && securityManager.isIdentityInSecurityGroup(coach, entry.getTutorGroup())) {
						securityManager.removeIdentityFromSecurityGroup(coach, entry.getTutorGroup());
					}
				}
			}
		}
		
		for(Identity participant:participants) {
			List<BusinessGroup> businessGroups = contextManager.getBusinessGroupAsParticipantOfBGContext(participant, context) ;
			if(context.isDefaultContext() && businessGroups.size() == 1) {
				for(RepositoryEntry entry:entries) {
					if(entry.getParticipantGroup() != null && securityManager.isIdentityInSecurityGroup(participant, entry.getParticipantGroup())) {
						securityManager.removeIdentityFromSecurityGroup(participant, entry.getParticipantGroup());
					}
				}
			}
		}
		*/
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
		BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		for (Identity identity : addIdentities) {
			group = loadBusinessGroup(group); // reload business group
			if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
				response.getIdentitiesWithoutPermission().add(identity);
			}
			// Check if identity is already in group. make a db query in case
			// someone in another workflow already added this user to this group. if
			// found, add user to model
			else if (securityManager.isIdentityInSecurityGroup(identity, group.getOwnerGroup())) {
				response.getIdentitiesAlreadyInGroup().add(identity);
			} else {
	      // identity has permission and is not already in group => add it
				addOwner(ureqIdentity, identity, group);
				response.getAddedIdentities().add(identity);
				log.audit("added identity '" + identity.getName() + "' to securitygroup with key " + group.getOwnerGroup().getKey());
			}
		}
		return response;
	}
	
	private void addOwner(Identity ureqIdentity, Identity identity, BusinessGroup group) {
		securityManager.addIdentityToSecurityGroup(identity, group.getOwnerGroup());
		// add user to buddies rosters
		addToRoster(ureqIdentity, identity, group);
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identity));
		// send notification mail in your controller!
	}
	
	@Override
	public void addParticipant(Identity ureqIdentity, Identity identityToAdd, BusinessGroup group) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);

		securityManager.addIdentityToSecurityGroup(identityToAdd, group.getPartipiciantGroup());

		// add user to buddies rosters
		addToRoster(ureqIdentity, identityToAdd, group);
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identityToAdd);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToAdd));
		// send notification mail in your controller!
	}

	@Override
	public BusinessGroupAddResponse addParticipants(final Identity ureqIdentity, final List<Identity> addIdentities,
			final BusinessGroup group) {
		
		final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
			public void execute() {
				final BusinessGroup currBusinessGroup = loadBusinessGroup(group); // reload business group
				for (final Identity identity : addIdentities) {
					if (securityManager.isIdentityPermittedOnResourceable(identity, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_GUESTONLY)) {
						response.getIdentitiesWithoutPermission().add(identity);
					}
					// Check if identity is already in group. make a db query in case
					// someone in another workflow already added this user to this group. if
					// found, add user to model
					else if (securityManager.isIdentityInSecurityGroup(identity, currBusinessGroup.getPartipiciantGroup())) {
						response.getIdentitiesAlreadyInGroup().add(identity);
					} else {
						// identity has permission and is not already in group => add it
						addParticipant(ureqIdentity, identity, currBusinessGroup);
						response.getAddedIdentities().add(identity);
						log.audit("added identity '" + identity.getName() + "' to securitygroup with key " + currBusinessGroup.getPartipiciantGroup().getKey());
					}
				}
			}});
		return response;
	}

	@Override
	public void removeParticipant(Identity ureqIdentity, Identity identity, BusinessGroup group) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);

		securityManager.removeIdentityFromSecurityGroup(identity, group.getPartipiciantGroup());
		// remove user from buddies rosters
		removeFromRoster(identity, group);
		//remove subscriptions if user gets removed
		removeSubscriptions(identity, group);
		
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_REMOVED, getClass(), LoggingResourceable.wrap(identity), LoggingResourceable.wrap(group));
		// Check if a waiting-list with auto-close-ranks is configurated
		if ( group.getWaitingListEnabled().booleanValue() && group.getAutoCloseRanksEnabled().booleanValue() ) {
			// even when doOnlyPostRemovingStuff is set to true we really transfer the first Identity here
			transferFirstIdentityFromWaitingToParticipant(ureqIdentity, group);
		}	
		// send notification mail in your controller!
		
	}
	
	@Override
	public void removeParticipants(final Identity ureqIdentity, final List<Identity> identities, final BusinessGroup group) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
			public void execute() {
				for (Identity identity : identities) {
				  removeParticipant(ureqIdentity, identity, group);
				  log.audit("removed identiy '" + identity.getName() + "' from securitygroup with key " + group.getPartipiciantGroup().getKey());
				}
			}
		});
	}

	@Override
	public void addToWaitingList(Identity ureqIdentity, Identity identity, BusinessGroup group) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
		securityManager.addIdentityToSecurityGroup(identity, group.getWaitingGroup());

		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_TO_WAITING_LIST_ADDED, getClass(), LoggingResourceable.wrap(identity));
		// send notification mail in your controller!
	}
	
	@Override
	public BusinessGroupAddResponse addToWaitingList(final Identity ureqIdentity, final List<Identity> addIdentities,
			final BusinessGroup group) {
		
		final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		final BusinessGroup currBusinessGroup = loadBusinessGroup(group); // reload business group
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup, new SyncerExecutor(){
			public void execute() {
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
						addToWaitingList(ureqIdentity, identity, currBusinessGroup);
						response.getAddedIdentities().add(identity);
						log.audit("added identity '" + identity.getName() + "' to securitygroup with key " + currBusinessGroup.getPartipiciantGroup().getKey());
					}
				}
			}});
		return response;
	}

	@Override
	public void removeFromWaitingList(Identity ureqIdentity, Identity identity, BusinessGroup group) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
		securityManager.removeIdentityFromSecurityGroup(identity, group.getWaitingGroup());
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_FROM_WAITING_LIST_REMOVED, getClass(), LoggingResourceable.wrap(identity));
		// send notification mail in your controller!
	}
	
	@Override
	public void removeFromWaitingList(final Identity ureqIdentity, final List<Identity> identities, final BusinessGroup currBusinessGroup) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup, new SyncerExecutor(){
			public void execute() {
				for (Identity identity : identities) {
				  removeFromWaitingList(ureqIdentity, identity, currBusinessGroup);
				  log.audit("removed identiy '" + identity.getName() + "' from securitygroup with key " + currBusinessGroup.getOwnerGroup().getKey());
				}
			}
		});
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
	public BusinessGroupAddResponse moveIdentityFromWaitingListToParticipant(final List<Identity> identities, final Identity ureqIdentity,
			final BusinessGroup currBusinessGroup) {
		
		final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup,new SyncerExecutor(){
			public void execute() {
				for (final Identity identity : identities) {
					// check if idenity is allready in participant
					if (!securityManager.isIdentityInSecurityGroup(identity,currBusinessGroup.getPartipiciantGroup()) ) {
						// Idenity is not in participant-list => move idenity from waiting-list to participant-list
						addParticipant(ureqIdentity, identity, currBusinessGroup);
						removeFromWaitingList(ureqIdentity, identity, currBusinessGroup);
						response.getAddedIdentities().add(identity);
						// notification mail is handled in controller
					} else {
						response.getIdentitiesAlreadyInGroup().add(identity);
					}
				}
			}});
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
	public String[] addIdentityToGroups(final AddToGroupsEvent groupsEv, final Identity ident, final Identity addingIdentity) {
		String[] resultTextArgs = new String[2];
		boolean addToAnyGroup = false;

		// notify user about add for following groups:
		List<Long> notifyAboutAdd = new ArrayList<Long>();
		List<Long> mailKeys = groupsEv.getMailForGroupsList();
		
		// add to owner groups
		List<Long> ownerKeys = groupsEv.getOwnerGroupKeys();
		String ownerGroupnames = "";

		List<BusinessGroup> groups = loadBusinessGroups(ownerKeys);	
		for (BusinessGroup group : groups) {
			if (group != null && !securityManager.isIdentityInSecurityGroup(ident, group.getOwnerGroup())){
//				seems not to work, but would be the way to go!
//				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(group));
				addOwner(addingIdentity, ident, group);
				ownerGroupnames += group.getName() + ", ";
				addToAnyGroup = true;
				if (!notifyAboutAdd.contains(group.getKey()) && mailKeys.contains(group.getKey())) notifyAboutAdd.add(group.getKey());
			}
		}
		resultTextArgs[0] = ownerGroupnames.substring(0, ownerGroupnames.length() > 0 ? ownerGroupnames.length() - 2 : 0);

		// add to participant groups
		List<Long> participantKeys = groupsEv.getParticipantGroupKeys();
		String participantGroupnames = "";
		List<BusinessGroup> participantGroups = loadBusinessGroups(participantKeys);	
		for (BusinessGroup group : participantGroups) {
			if (group != null && !securityManager.isIdentityInSecurityGroup(ident, group.getPartipiciantGroup())) {
				final BusinessGroup toAddGroup = group;
//				seems not to work, but would be the way to go!
//				ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(group));
				CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
					public void execute() {
						addParticipant(addingIdentity, ident, toAddGroup);
					}});
				participantGroupnames += group.getName() + ", ";
				addToAnyGroup = true;
				if (!notifyAboutAdd.contains(group.getKey()) && mailKeys.contains(group.getKey())) notifyAboutAdd.add(group.getKey());
			}			
		}
		resultTextArgs[1] = participantGroupnames.substring(0, participantGroupnames.length() > 0 ? participantGroupnames.length() - 2 : 0);
		
		// send notification mails

		List<BusinessGroup> notifGroups = loadBusinessGroups(notifyAboutAdd);
		for (BusinessGroup group : notifGroups) {
			MailTemplate mailTemplate = BGMailHelper.createAddParticipantMailTemplate(group, addingIdentity);
			MailerWithTemplate mailer = MailerWithTemplate.getInstance();
			MailerResult mailerResult = mailer.sendMail(null, ident, null, null, mailTemplate, null);
			if (mailerResult.getReturnCode() != MailerResult.OK){
				log.debug("Problems sending Group invitation mail for identity: " + ident.getName() + " and group: " 
						+ group.getName() + " key: " + group.getKey() + " mailerresult: " + mailerResult.getReturnCode(), null);
			}
		}		
		
		if (addToAnyGroup) {
			return resultTextArgs;
		} else {
			return null;
		}
	}


	private void transferFirstIdentityFromWaitingToParticipant(Identity ureqIdentity, BusinessGroup group) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
		// Check if waiting-list is enabled and auto-rank-up
		if (group.getWaitingListEnabled().booleanValue() && group.getAutoCloseRanksEnabled().booleanValue()) {
			// Check if participant is not full
			Integer maxSize = group.getMaxParticipants();
			int reservations = acService.countReservations(group.getResource());
			int waitingPartipiciantSize = securityManager.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
			if (maxSize != null && (waitingPartipiciantSize + reservations) < maxSize.intValue()) {
				// ok it has free places => get first identity from Waitinglist
				List<Object[]> identities = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getWaitingGroup(), true/*sortedByAddedDate*/);
				int i = 0;
				boolean transferNotDone = true;
			  while (i<identities.size() && transferNotDone) {
			  	// It has an identity and transfer from waiting-list to participant-group is not done
					Object[] co = (Object[])identities.get(i++);
					Identity firstWaitingListIdentity = (Identity) co[0];
					//reload group
					group = loadBusinessGroup(group);
					// Check if firstWaitingListIdentity is not allready in participant-group
					if (!securityManager.isIdentityInSecurityGroup(firstWaitingListIdentity,group.getPartipiciantGroup())) {
						// move the identity from the waitinglist to the participant group
						
						ActionType formerStickyActionType = ThreadLocalUserActivityLogger.getStickyActionType();
						try{
							// OLAT-4955: force add-participant and remove-from-waitinglist logging actions 
							//            that get triggered in the next two methods to be of ActionType admin
							//            This is needed to make sure the targetIdentity ends up in the o_loggingtable
							ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
							addParticipant(ureqIdentity, firstWaitingListIdentity, group);
							removeFromWaitingList(ureqIdentity, firstWaitingListIdentity, group);
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
						transferNotDone = false;
				  }
				}
			}
		} else {
			log.warn("Called method transferFirstIdentityFromWaitingToParticipant but waiting-list or autoCloseRanks is disabled.");
		}
	}

	private void addToRoster(Identity ureqIdentity, Identity identity, BusinessGroup group) {
		if (InstantMessagingModule.isEnabled()) {
			//evaluate whether to sync or not
			boolean syncGroup = InstantMessagingModule.getAdapter().getConfig().isSyncLearningGroups();
			//only sync when a group is a certain type and this type is configured that you want to sync it
			if(syncGroup) { 
				String groupID = InstantMessagingModule.getAdapter().createChatRoomString(group);
				String groupDisplayName = group.getName();
				//course group enrolment is time critial so we move this in an separate thread and catch all failures 
				TaskExecutorManager.getInstance().runTask(new SyncSingleUserTask(ureqIdentity, groupID, groupDisplayName, identity));
			}
		}
	}
	
	@Override
	public void removeOwners(Identity ureqIdentity, Collection<Identity> identitiesToRemove, BusinessGroup group) {
		//fxdiff VCRP-2: access control
		for(Identity identity:identitiesToRemove) {
			securityManager.removeIdentityFromSecurityGroup(identity, group.getOwnerGroup());
			// remove user from buddies rosters
			removeFromRoster(identity, group);
			
			//remove subsciptions if user gets removed
			removeSubscriptions(identity, group);
			
			// notify currently active users of this business group
			if (identity.getKey().equals(ureqIdentity.getKey()) ) {
				BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.MYSELF_ASOWNER_REMOVED_EVENT, group, identity);
			} else {
	  		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
			}
			// do logging
			ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_REMOVED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identity));
			// send notification mail in your controller!
		}
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

	private void removeFromRoster(Identity identity, BusinessGroup group) {
		if (InstantMessagingModule.isEnabled()) {
			// only remove user from roster if not in other security group
			if (!isIdentityInBusinessGroup(identity, group)) {
				String groupID = InstantMessagingModule.getAdapter().createChatRoomString(group);
				InstantMessagingModule.getAdapter().removeUserFromFriendsRoster(groupID, identity.getName());
			}
		}
	}
	
	@Override
	@Transactional(readOnly=true)
	public boolean hasResources(BusinessGroup group) {
		return businessGroupRelationDAO.countResources(group) > 0;
	}

	@Override
	@Transactional
	public void addResourceTo(BusinessGroup group, OLATResource resource) {
		businessGroupRelationDAO.addRelationToResource(group, resource);
		//add coach and participant permission
		securityManager.createAndPersistPolicyWithResource(group.getOwnerGroup(), Constants.PERMISSION_COACH, resource);
		securityManager.createAndPersistPolicyWithResource(group.getPartipiciantGroup(), Constants.PERMISSION_PARTI, resource);
	}

	@Override
	@Transactional
	public void addResourcesTo(List<BusinessGroup> groups, List<OLATResource> resources) {
		if(groups == null || groups.isEmpty()) return;
		if(resources == null || resources.isEmpty()) return;
		for(BusinessGroup group:groups) {
			for(OLATResource resource:resources) {
				addResourceTo(group, resource);
			}
		}
	}

	@Override
	@Transactional
	public void removeResourceFrom(BusinessGroup group, OLATResource resource) {
		businessGroupRelationDAO.deleteRelation(group, resource);
		//remove permission
		securityManager.deletePolicy(group.getOwnerGroup(), Constants.PERMISSION_COACH, resource);
		securityManager.deletePolicy(group.getPartipiciantGroup(), Constants.PERMISSION_PARTI, resource);
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
	public List<BusinessGroupMembership> getBusinessGroupMembership(Identity identity, Collection<Long> businessGroups) {
		return businessGroupDAO.getMembershipInfoInBusinessGroups(identity, businessGroups);
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
	public BusinessGroupEnvironment importGroups(OLATResource resource, File fGroupExportXML) {
		return businessGroupImportExport.importGroups(resource, fGroupExportXML);
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
