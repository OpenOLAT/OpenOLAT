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

import org.hibernate.ObjectNotFoundException;
import org.hibernate.StaleObjectStateException;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.lifecycle.LifeCycleManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.taskExecutor.TaskExecutorManager;
import org.olat.core.gui.translator.Translator;
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
import org.olat.group.BusinessGroupService;
import org.olat.group.DeletableGroupData;
import org.olat.group.DeletableReference;
import org.olat.group.GroupLoggingAction;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.group.model.AddToGroupsEvent;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.group.properties.BusinessGroupPropertyManager;
import org.olat.group.right.BGRightManager;
import org.olat.group.ui.BGConfigFlags;
import org.olat.group.ui.BGMailHelper;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.syncservice.SyncSingleUserTask;
import org.olat.notifications.NotificationsManagerImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceImpl;
import org.olat.testutils.codepoints.server.Codepoint;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@Service("businessGroupService")
public class BusinessGroupServiceImpl implements BusinessGroupService {
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
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupDeletionManager businessGroupDeletionManager;
	

	private List<DeletableGroupData> deleteListeners = new ArrayList<DeletableGroupData>();

	
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
	public BusinessGroup createBusinessGroup(Identity creator, String name, String description,
			int minParticipants, int maxParticipants, boolean waitingListEnabled, boolean autoCloseRanksEnabled,
			OLATResource resource) {
		
		if(resource != null) {
			boolean groupExists = businessGroupRelationDAO.checkIfOneOrMoreNameExistsInContext(Collections.singleton(name), resource);
			if (groupExists) {
				// there is already a group with this name, return without creating a new group
				log.warn("A group with this name already exists! You will get null instead of a businessGroup returned!");
				return null;
			}
		}
		
		BusinessGroup group = businessGroupDAO.createAndPersist(creator, name, description,
				minParticipants, maxParticipants, waitingListEnabled, autoCloseRanksEnabled, false, false, false);
		
		if(resource instanceof OLATResourceImpl) {
			businessGroupRelationDAO.addRelationToResource(group, resource);
		}
		return group;
	}
	
	@Override
	public Set<BusinessGroup> createUniqueBusinessGroupsFor(final Set<String> allNames, final String description,
			final int minParticipants, final int maxParticipants, final boolean waitingListEnabled, final boolean autoCloseRanksEnabled,
			final OLATResource resource) {

	   //o_clusterOK by:cg
		Set<BusinessGroup> createdGroups = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(resource, new SyncerCallback<Set<BusinessGroup>>(){
	      public Set<BusinessGroup> execute() {
					if(checkIfOneOrMoreNameExistsInContext(allNames, resource)){
						// set error of non existing name
						return null;
					} else {
						// create bulkgroups only if there is no name which already exists.
						Set<BusinessGroup> newGroups = new HashSet<BusinessGroup>();
						for (String name : allNames) {
							BusinessGroup newGroup = createBusinessGroup(null, name, description, minParticipants, maxParticipants,
									waitingListEnabled, autoCloseRanksEnabled, resource);
							newGroups.add(newGroup);
						}
						return newGroups;
					}
	      }
		});
		return createdGroups;
	}

	@Override
	@Transactional
	public BusinessGroup mergeBusinessGroup(BusinessGroup group) {
		return businessGroupDAO.merge(group);
	}
	
	@Override
	@Transactional
	public BusinessGroup setLastUsageFor(final BusinessGroup group) {
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerCallback<BusinessGroup>() {
			public BusinessGroup execute() {
				try {
					BusinessGroup reloadedBusinessGroup = loadBusinessGroup(group);
					reloadedBusinessGroup.setLastUsage(new Date());
					LifeCycleManager.createInstanceFor(reloadedBusinessGroup).deleteTimestampFor(SEND_DELETE_EMAIL_ACTION);
					return mergeBusinessGroup(reloadedBusinessGroup);
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
	@Transactional
	public List<BusinessGroup> loadAllBusinessGroups() {
		return businessGroupDAO.loadAll();
	}
	
	@Override
	public boolean checkIfOneOrMoreNameExistsInContext(Set<String> names, OLATResource resource) {
		return businessGroupRelationDAO.checkIfOneOrMoreNameExistsInContext(names, resource);
	}

	@Override
	public boolean checkIfOneOrMoreNameExistsInContext(Set<String> names, BusinessGroup group) {
		return businessGroupRelationDAO.checkIfOneOrMoreNameExistsInContext(names, group);
	}

	@Override
	public BusinessGroup copyBusinessGroup(BusinessGroup sourceBusinessGroup, String targetName, String targetDescription, Integer targetMin,
			Integer targetMax, OLATResource targetResource, Map<BGArea, BGArea> areaLookupMap, boolean copyAreas, boolean copyCollabToolConfig,
			boolean copyRights, boolean copyOwners, boolean copyParticipants, boolean copyMemberVisibility, boolean copyWaitingList) {

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
			BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(newGroup);
			bgpm.copyConfigurationFromGroup(sourceBusinessGroup);
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
		return newGroup;
	}

	@Override
	@Transactional
	public BusinessGroup findBusinessGroup(SecurityGroup secGroup) {
		return businessGroupDAO.findBusinessGroup(secGroup);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroup> findBusinessGroupsOwnedBy(Identity identity, OLATResource resource) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		return businessGroupDAO.findBusinessGroups(params, identity, true, false, resource, 0, -1);
	}
	
	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroup> findBusinessGroupsAttendedBy(Identity identity, OLATResource resource) {
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		return businessGroupDAO.findBusinessGroups(params, identity, false, true, resource, 0, -1);
	}
	
	@Override
	public List<BusinessGroup> findBusinessGroupsWithWaitingListAttendedBy(Identity identity,  OLATResource resource) {
		return businessGroupDAO.findBusinessGroupsWithWaitingListAttendedBy(identity, resource);
	}
	
	@Override
	@Transactional(readOnly=true)
	public int countBusinessGroups(SearchBusinessGroupParams params, Identity identity,
			boolean ownedById, boolean attendedById, OLATResource resource) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.countBusinessGroups(params, identity, ownedById, attendedById, resource);
	}

	@Override
	@Transactional(readOnly=true)
	public List<BusinessGroup> findBusinessGroups(SearchBusinessGroupParams params, Identity identity,
			boolean ownedById, boolean attendedById, OLATResource resource, int firstResult, int maxResults) {
		if(params == null) {
			params = new SearchBusinessGroupParams();
		}
		return businessGroupDAO.findBusinessGroups(params, identity, ownedById, attendedById, resource, firstResult, maxResults);
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
	public void deleteGroupsAfterLifeCycle(List<BusinessGroup> groups) {
		businessGroupDeletionManager.deleteGroups(groups);
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
			BusinessGroupPropertyManager bgpm = new BusinessGroupPropertyManager(group);
			bgpm.deleteDisplayMembers();
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
	public List<BusinessGroup> getDeletableGroups(int lastLoginDuration) {
		return businessGroupDAO.getDeletableGroups(lastLoginDuration);
	}

	@Override
	public List<BusinessGroup> getGroupsInDeletionProcess(int deleteEmailDuration) {
		return businessGroupDAO.getGroupsInDeletionProcess(deleteEmailDuration);
	}

	@Override
	public List<BusinessGroup> getGroupsReadyToDelete(int deleteEmailDuration) {
		return businessGroupDAO.getGroupsReadyToDelete(deleteEmailDuration);
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
	public String sendDeleteEmailTo(List<BusinessGroup> selectedGroups, MailTemplate mailTemplate, boolean isTemplateChanged, String keyEmailSubject, 
			String keyEmailBody, Identity sender, Translator pT) {
		
		return businessGroupDeletionManager.sendDeleteEmailTo(selectedGroups, mailTemplate, isTemplateChanged, keyEmailSubject, keyEmailBody, sender, pT);
	}
	
	private void removeFromRepositoryEntrySecurityGroup(BusinessGroup group) {
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
	public void addOwner(Identity ureqIdentity, Identity identity, BusinessGroup group, BGConfigFlags flags) {
		//fxdiff VCRP-1,2: access control of resources
		List<RepositoryEntry> res = businessGroupRelationDAO.findRepositoryEntries(Collections.singletonList(group), 0, 1);
		for(RepositoryEntry re:res) {
			if(re.getTutorGroup() == null) {
				repositoryManager.createTutorSecurityGroup(re);
				repositoryManager.updateRepositoryEntry(re);
			}
			if(re.getTutorGroup() != null && !securityManager.isIdentityInSecurityGroup(identity, re.getTutorGroup())) {
				securityManager.addIdentityToSecurityGroup(identity, re.getTutorGroup());
			}
		}
		securityManager.addIdentityToSecurityGroup(identity, group.getOwnerGroup());
		
		// add user to buddies rosters
		addToRoster(ureqIdentity, identity, group, flags);
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identity);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_OWNER_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identity));
		// send notification mail in your controller!
	}

	@Override
	public BusinessGroupAddResponse addOwners(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup group, BGConfigFlags flags) {
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
				addOwner(ureqIdentity, identity, group, flags);
				response.getAddedIdentities().add(identity);
				log.audit("added identity '" + identity.getName() + "' to securitygroup with key " + group.getOwnerGroup().getKey());
			}
		}
		return response;
	}
	
	@Override
	public void addParticipant(Identity ureqIdentity, Identity identityToAdd, BusinessGroup group, BGConfigFlags flags) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);

		//fxdiff VCRP-1,2: access control of resources
		List<RepositoryEntry> res = businessGroupRelationDAO.findRepositoryEntries(Collections.singletonList(group), 0, -1);
		for(RepositoryEntry re:res) {
			if(re.getParticipantGroup() == null) {
				repositoryManager.createParticipantSecurityGroup(re);
				repositoryManager.updateRepositoryEntry(re);
			}
			if(re.getParticipantGroup() != null && !securityManager.isIdentityInSecurityGroup(identityToAdd, re.getParticipantGroup())) {
				securityManager.addIdentityToSecurityGroup(identityToAdd, re.getParticipantGroup());
			}
		}
		securityManager.addIdentityToSecurityGroup(identityToAdd, group.getPartipiciantGroup());

		// add user to buddies rosters
		addToRoster(ureqIdentity, identityToAdd, group, flags);
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, group, identityToAdd);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_ADDED, getClass(), LoggingResourceable.wrap(group), LoggingResourceable.wrap(identityToAdd));
		// send notification mail in your controller!
	}

	@Override
	public BusinessGroupAddResponse addParticipants(final Identity ureqIdentity, final List<Identity> addIdentities,
			final BusinessGroup group, final BGConfigFlags flags) {
		
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
						addParticipant(ureqIdentity, identity, currBusinessGroup, flags);
						response.getAddedIdentities().add(identity);
						log.audit("added identity '" + identity.getName() + "' to securitygroup with key " + currBusinessGroup.getPartipiciantGroup().getKey());
					}
				}
			}});
		return response;
	}

	@Override
	public void removeParticipant(Identity ureqIdentity, Identity identity, BusinessGroup group, BGConfigFlags flags) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
		
		//fxdiff VCRP-2: access control
		List<RepositoryEntry> entries = businessGroupRelationDAO.findRepositoryEntries(Collections.singletonList(group), 0, -1);
		for(RepositoryEntry entry:entries) {
			if(entry.getParticipantGroup() != null && securityManager.isIdentityInSecurityGroup(identity, entry.getParticipantGroup())) {
				securityManager.removeIdentityFromSecurityGroup(identity, entry.getParticipantGroup());
			}
		}
		securityManager.removeIdentityFromSecurityGroup(identity, group.getPartipiciantGroup());

		// remove user from buddies rosters
		removeFromRoster(identity, group, flags);
		
		//remove subsciptions if user gets removed
		removeSubscriptions(identity, group);
		
		// notify currently active users of this business group
		BusinessGroupModifiedEvent.fireModifiedGroupEvents(BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT, group, identity);
		// do logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_PARTICIPANT_REMOVED, getClass(), LoggingResourceable.wrap(identity), LoggingResourceable.wrap(group));
		// Check if a waiting-list with auto-close-ranks is configurated
		if ( group.getWaitingListEnabled().booleanValue() && group.getAutoCloseRanksEnabled().booleanValue() ) {
			// even when doOnlyPostRemovingStuff is set to true we really transfer the first Identity here
			transferFirstIdentityFromWaitingToParticipant(ureqIdentity, group, flags);
		}	
		// send notification mail in your controller!
		
	}
	
	@Override
	public void removeParticipants(final Identity ureqIdentity, final List<Identity> identities, final BusinessGroup group, final BGConfigFlags flags) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(group, new SyncerExecutor(){
			public void execute() {
				for (Identity identity : identities) {
				  removeParticipant(ureqIdentity, identity, group, flags);
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
			final BusinessGroup group, final BGConfigFlags flags) {
		
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
					else if (securityManager.isIdentityInSecurityGroup(identity, currBusinessGroup.getWaitingGroup())) {
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
	public void removeFromWaitingList(final Identity ureqIdentity, final List<Identity> identities, final BusinessGroup currBusinessGroup,
			final BGConfigFlags flags) {
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
		int pos = 0;
		for (int i = 0; i<identities.size(); i++) {
		  Object[] co = identities.get(i);
		  Identity waitingListIdentity = (Identity) co[0];
		  if (waitingListIdentity.getName().equals(identity.getName()) ) {
		  	pos = i+1;// '+1' because list begins with 0 
		  }
		}
		return pos;
	}

	@Override
	public BusinessGroupAddResponse moveIdentityFromWaitingListToParticipant(final List<Identity> identities, final Identity ureqIdentity,
			final BusinessGroup currBusinessGroup, final BGConfigFlags flags) {
		
		final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(currBusinessGroup,new SyncerExecutor(){
			public void execute() {
				for (final Identity identity : identities) {
					// check if idenity is allready in participant
					if (!securityManager.isIdentityInSecurityGroup(identity,currBusinessGroup.getPartipiciantGroup()) ) {
						// Idenity is not in participant-list => move idenity from waiting-list to participant-list
						addParticipant(ureqIdentity, identity, currBusinessGroup, flags);
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
		final BGConfigFlags flags = BGConfigFlags.createBuddyGroupDefaultFlags();
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
				addOwner(addingIdentity, ident, group, flags);
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
						addParticipant(addingIdentity, ident, toAddGroup, flags);
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


	private void transferFirstIdentityFromWaitingToParticipant(Identity ureqIdentity, BusinessGroup group, BGConfigFlags flags) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().assertAlreadyDoInSyncFor(group);
		// Check if waiting-list is enabled and auto-rank-up
		if (group.getWaitingListEnabled().booleanValue() && group.getAutoCloseRanksEnabled().booleanValue()) {
			// Check if participant is not full
			Integer maxSize = group.getMaxParticipants();
			int waitingPartipiciantSize = securityManager.countIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
			if ( (maxSize != null) && (waitingPartipiciantSize < maxSize.intValue()) ) {
				// ok it has free places => get first idenity from Waitinglist
				List<Object[]> identities = securityManager.getIdentitiesAndDateOfSecurityGroup(group.getWaitingGroup(), true/*sortedByAddedDate*/);
				int i = 0;
				boolean transferNotDone = true;
			  while (i<identities.size() && transferNotDone) {
			  	// It has an identity and transfer from waiting-list to participant-group is not done
					Object[] co = (Object[])identities.get(i++);
					Identity firstWaitingListIdentity = (Identity) co[0];
					//reload group
					group = (BusinessGroup)DBFactory.getInstance().loadObject(group, true);
					// Check if firstWaitingListIdentity is not allready in participant-group
					if (!securityManager.isIdentityInSecurityGroup(firstWaitingListIdentity,group.getPartipiciantGroup())) {
						// move the identity from the waitinglist to the participant group
						
						ActionType formerStickyActionType = ThreadLocalUserActivityLogger.getStickyActionType();
						try{
							// OLAT-4955: force add-participant and remove-from-waitinglist logging actions 
							//            that get triggered in the next two methods to be of ActionType admin
							//            This is needed to make sure the targetIdentity ends up in the o_loggingtable
							ThreadLocalUserActivityLogger.setStickyActionType(ActionType.admin);
							addParticipant(ureqIdentity, firstWaitingListIdentity, group, flags);
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

	private void addToRoster(Identity ureqIdentity, Identity identity, BusinessGroup group, BGConfigFlags flags) {
		if (flags.isEnabled(BGConfigFlags.BUDDYLIST) && InstantMessagingModule.isEnabled()) {
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
	public void removeOwners(Identity ureqIdentity, Collection<Identity> identitiesToRemove, BusinessGroup group, BGConfigFlags flags) {
		//fxdiff VCRP-2: access control
		List<RepositoryEntry> entries = businessGroupRelationDAO.findRepositoryEntries(Collections.singletonList(group), 0, -1);
		for(RepositoryEntry entry:entries) {
			if(entry.getTutorGroup() != null) {
				for(Identity identity:identitiesToRemove) {
					if(securityManager.isIdentityInSecurityGroup(identity, entry.getTutorGroup())) {
						securityManager.removeIdentityFromSecurityGroup(identity, entry.getTutorGroup());
					}
				}
			}
		}

		for(Identity identity:identitiesToRemove) {
			securityManager.removeIdentityFromSecurityGroup(identity, group.getOwnerGroup());
			// remove user from buddies rosters
			removeFromRoster(identity, group, flags);
			
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

	private void removeFromRoster(Identity identity, BusinessGroup group, BGConfigFlags flags) {
		if (flags.isEnabled(BGConfigFlags.BUDDYLIST) && InstantMessagingModule.isEnabled()) {
			// only remove user from roster if not in other security group
			if (!isIdentityInBusinessGroup(identity, group)) {
				String groupID = InstantMessagingModule.getAdapter().createChatRoomString(group);
				InstantMessagingModule.getAdapter().removeUserFromFriendsRoster(groupID, identity.getName());
			}
		}
	}
	
	@Override
	public boolean hasResources(BusinessGroup group) {
		return businessGroupRelationDAO.countResources(group) > 0;
	}

	@Override
	@Transactional
	public void addResourceTo(BusinessGroup group, OLATResource resource) {
		businessGroupRelationDAO.addRelationToResource(group, resource);
	}

	@Override
	public void removeResourceFrom(BusinessGroup group, OLATResource resource) {
		businessGroupRelationDAO.deleteRelation(group, resource);
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
	public boolean isIdentityInBusinessGroup(Identity identity, String groupName,
			boolean ownedById, boolean attendedById, OLATResource resource) {
		return businessGroupRelationDAO.isIdentityInBusinessGroup(identity, groupName, resource);
	}
	
	@Override
	public void exportGroups(List<BusinessGroup> groups, File fExportFile) {
		businessGroupImportExport.exportGroups(groups, fExportFile);
	}

	@Override
	public void importGroups(OLATResource resource, File fGroupExportXML) {
		businessGroupImportExport.importGroups(resource, fGroupExportXML);
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
