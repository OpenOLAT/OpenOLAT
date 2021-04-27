/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.projectbroker.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.hibernate.LazyInitializationException;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SecurityGroup;
import org.olat.basesecurity.manager.SecurityGroupDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupAddResponse;
import org.olat.group.BusinessGroupService;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**
 * 
 * @author guretzki
 */
@Service
public class ProjectGroupManagerImpl implements ProjectGroupManager {
	
	private static final Logger log = Tracing.createLoggerFor(ProjectGroupManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private ProjectDAO projectDao;
	@Autowired
	private SecurityGroupDAO securityGroupDao;
	@Autowired
	private ProjectBrokerManager projectBrokerManager;
	@Autowired
	private BusinessGroupService businessGroupService;

	//////////////////////
	// ACCOUNT MANAGEMENT
	//////////////////////
	@Override
	public Long getAccountManagerGroupKey(CoursePropertyManager cpm, CourseNode courseNode) {
		Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
		if (accountManagerGroupProperty != null) {
			return accountManagerGroupProperty.getLongValue();
		}
		return null;
	}
	
	@Override
	public BusinessGroup getAccountManagerGroupFor(CoursePropertyManager cpm, CourseNode courseNode, ICourse course, String groupName, String groupDescription, Identity identity) {
		BusinessGroup accountManagerGroup = null;
		try {
			Long groupKey = null;
			Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
			// Check if account-manager-group-key-property already exist
			if (accountManagerGroupProperty != null) {
				groupKey = accountManagerGroupProperty.getLongValue();
				log.debug("accountManagerGroupProperty=" + accountManagerGroupProperty + "  groupKey=" + groupKey);
			} 
			log.debug("groupKey={}", groupKey);
			if (groupKey != null) {
				accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey);
				log.debug("load businessgroup={}", accountManagerGroup);
				if (accountManagerGroup != null) {
					return accountManagerGroup;
				} else {
					if (accountManagerGroupProperty != null) {
						cpm.deleteProperty(accountManagerGroupProperty);
					}
					groupKey = null;
					log.warn("ProjectBroker: Account-manager does no longer exist, create a new one");
				}
			} else {
				log.debug("No group for project-broker exist => create a new one");
				RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				accountManagerGroup = businessGroupService.createBusinessGroup(identity, groupName, groupDescription, BusinessGroup.BUSINESS_TYPE,//TODO group type
						-1, -1, false, false, re);
				int i = 2;
				while (accountManagerGroup == null) {
					// group with this name exist already, try another name
					accountManagerGroup = businessGroupService.createBusinessGroup(identity, groupName + " _" + i, groupDescription, BusinessGroup.BUSINESS_TYPE,//TODO group type
							-1, -1, false, false, re);
					i++;
				}
				log.debug("createAndPersistBusinessGroup businessgroup={}", accountManagerGroup);			
				
				if (accountManagerGroupProperty != null) {
					accountManagerGroupProperty.setLongValue(accountManagerGroup.getKey());
					cpm.updateProperty(accountManagerGroupProperty);
				} else {
					saveAccountManagerGroupKey(accountManagerGroup.getKey(), cpm, courseNode);
				}
				log.debug("created account-manager default businessgroup={}", accountManagerGroup);
			}
		} catch (AssertException e) {
			log.error("", e);
			if(tryToRepareAccountManagerProperty(cpm, courseNode)) {
				accountManagerGroup = getAccountManagerGroupFor(cpm, courseNode, course, groupName, groupDescription, identity);
			}
		} 
		return accountManagerGroup;
	}
	
	@Override
	public void saveAccountManagerGroupKey(Long accountManagerGroupKey, CoursePropertyManager cpm, CourseNode courseNode) {
		Property accountManagerGroupKeyProperty = cpm.createCourseNodePropertyInstance(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY, null, accountManagerGroupKey, null, null);
		cpm.saveProperty(accountManagerGroupKeyProperty);	
		log.debug("saveAccountManagerGroupKey accountManagerGroupKey={}", accountManagerGroupKey);
	}

	@Override
	public boolean isAccountManager(Identity identity, CoursePropertyManager cpm, CourseNode courseNode, UserCourseEnvironment userCourseEnv) {
		try {
			Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
			if (accountManagerGroupProperty != null) {
				Long groupKey = accountManagerGroupProperty.getLongValue();
				BusinessGroup accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey);
				if (accountManagerGroup != null) {
					return isAccountManager(identity,  accountManagerGroup, userCourseEnv);
				}
			}
		} catch (AssertException e) {//detected multiple properties
			log.error("", e);
			if(tryToRepareAccountManagerProperty(cpm, courseNode)) {
				return isAccountManager(identity, cpm, courseNode, userCourseEnv);
			}
		}
		return false;
 	}
	
	private boolean tryToRepareAccountManagerProperty(CoursePropertyManager cpm, CourseNode courseNode) {
		List<Property> properties = cpm.findCourseNodeProperties(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
		if(properties.size() > 1) {
			Collections.sort(properties, (p1, p2) -> p1.getCreationDate().compareTo(p2.getCreationDate()));
			for(int i=1; i<properties.size(); i++) {
				cpm.deleteProperty(properties.get(i));
			}
			dbInstance.commit();
		}
		return false;
	}

	@Override
	public void deleteAccountManagerGroup( CoursePropertyManager cpm, CourseNode courseNode) {
		log.debug("deleteAccountManagerGroup start...");
  	Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
  	if (accountManagerGroupProperty != null) {
  		Long groupKey = accountManagerGroupProperty.getLongValue();
  		if (groupKey != null) {
				BusinessGroup accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey);
				if (accountManagerGroup != null) {
					BusinessGroupService bgs = businessGroupService;
					bgs.deleteBusinessGroup(accountManagerGroup);
					log.info(Tracing.M_AUDIT, "ProjectBroker: Deleted accountManagerGroup={}", accountManagerGroup);
				} else {
					log.debug("deleteAccountManagerGroup: accountManagerGroup=" + accountManagerGroup + " has already been deleted");
				}
			}
  		cpm.deleteProperty(accountManagerGroupProperty);
			log.debug("deleteAccountManagerGroup: deleted accountManagerGroupProperty={}", accountManagerGroupProperty );
 	} else {
			log.debug("deleteAccountManagerGroup: found no accountManagerGroup-key");
		}
	}

	@Override
	public BusinessGroup updateAccountManagerGroupName(Identity ureqIdentity, String groupName, String groupDescription, BusinessGroup accountManagerGroup) {
		// group could have been deleted, see FXOLAT-295
		if (accountManagerGroup != null){
			BusinessGroupService bgs = businessGroupService;
			BusinessGroup reloadedBusinessGroup = bgs.loadBusinessGroup(accountManagerGroup);
			return bgs.updateBusinessGroup(ureqIdentity, reloadedBusinessGroup, groupName, groupDescription,
					reloadedBusinessGroup.getExternalId(), reloadedBusinessGroup.getManagedFlagsString(),
					reloadedBusinessGroup.getMinParticipants(), reloadedBusinessGroup.getMaxParticipants());
		}
		return null;
	}


	////////////////////////////
	// PROJECT GROUP MANAGEMENT
	////////////////////////////
	@Override
	public BusinessGroup createProjectGroupFor(Long projectBrokerId, Identity identity, String groupName, String groupDescription, Long courseId) {
		CourseGroupManager cgm = CourseFactory.loadCourse(courseId).getCourseEnvironment().getCourseGroupManager();
		RepositoryEntry re = cgm.getCourseEntry();

		log.debug("createProjectGroupFor groupName={}", groupName);
		BusinessGroup projectGroup = businessGroupService.createBusinessGroup(identity, groupName, groupDescription, BusinessGroup.BUSINESS_TYPE,//TODO group type
				-1, -1, false, false, re);
		// projectGroup could be null when a group with name already exists
		int counter = 2;
		while (projectGroup == null) {
			// name already exist try another one
			String newGroupName = groupName + " _" + counter ;
			projectGroup = businessGroupService.createBusinessGroup(identity, newGroupName, groupDescription, BusinessGroup.BUSINESS_TYPE,//TODO gorup type
					-1, -1, false, false, re);
			counter++;
		}
		log.debug("Created a new projectGroup={}", projectGroup);
		return projectGroup;
	}

	@Override
	public void deleteProjectGroupFor(Project project) {
		businessGroupService.deleteBusinessGroup(project.getProjectGroup());
	}
	
	/**
	 * Change group-name and description. Check if new group-name does not already exist in the course-group-context.
	 * If the goup-name already exist, it will be automatically try another one with suffix e.g. ' _2'
	 * @see org.olat.course.nodes.projectbroker.service.ProjectGroupManager#changeProjectGroupName(org.olat.group.BusinessGroup, java.lang.String, java.lang.String)
	 */
	@Override
	public BusinessGroup changeProjectGroupName(Identity ureqIdentity, BusinessGroup projectGroup, String groupName, String groupDescription, OLATResource courseResource) {
		BusinessGroup reloadedBusinessGroup = businessGroupService.loadBusinessGroup(projectGroup);
		return businessGroupService.updateBusinessGroup(ureqIdentity, reloadedBusinessGroup, groupName, groupDescription,
				reloadedBusinessGroup.getExternalId(), reloadedBusinessGroup.getManagedFlagsString(),
				reloadedBusinessGroup.getMinParticipants(), reloadedBusinessGroup.getMaxParticipants());
	}

	@Override
	public List<Identity> addCandidates(final List<Identity> addIdentities, final Project project) {
		List<Identity> addedIdentities = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<List<Identity>>(){
			@Override
			public List<Identity> execute() {
				List<Identity> addedIdentityList = new ArrayList<>();
				for (Identity identity : addIdentities) {
					if (!securityGroupDao.isIdentityInSecurityGroup(identity, project.getCandidateGroup()) ) {
						securityGroupDao.addIdentityToSecurityGroup(identity, project.getCandidateGroup());
						addedIdentityList.add(identity);
						log.info(Tracing.M_AUDIT, "ProjectBroker: Add user as candidate, identity={}", identity);
					}
					// fireEvents ?
				}
				return addedIdentityList;
			}
		});// end of doInSync
		return addedIdentities;
	}

	@Override
	public void removeCandidates(final List<Identity> addIdentities, final Project project) {
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<Boolean>(){
			@Override
			public Boolean execute() {
				Project reloadedProject = (Project) dbInstance.loadObject(project, true);
				for (Identity identity : addIdentities) {
					securityGroupDao.removeIdentityFromSecurityGroup(identity, reloadedProject.getCandidateGroup());
					log.info(Tracing.M_AUDIT, "ProjectBroker: Remove user as candidate, identity={}", identity);
					// fireEvents ?
				}
				return Boolean.TRUE;
			}
		});// end of doInSync
	}

	@Override
	public BusinessGroupAddResponse acceptCandidates(final List<Identity> identities, final Project project, final Identity actionIdentity, final boolean autoSignOut, final boolean isAcceptSelectionManually) {
		final Project reloadedProject = projectDao.loadProject(project.getKey());
		final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		BusinessGroupAddResponse state = businessGroupService.addParticipants(actionIdentity, null, identities, reloadedProject.getProjectGroup(), null);
		response.getAddedIdentities().addAll(state.getAddedIdentities());
		response.getIdentitiesAlreadyInGroup().addAll(state.getIdentitiesAlreadyInGroup());
		
		Boolean result = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<Boolean>(){
			@Override
			public Boolean execute() {
				for (final Identity identity : identities) {
					if (businessGroupService.hasRoles(identity, reloadedProject.getProjectGroup(), GroupRoles.participant.name())) {
						securityGroupDao.removeIdentityFromSecurityGroup(identity, reloadedProject.getCandidateGroup());
						log.info(Tracing.M_AUDIT, "ProjectBroker: Accept candidate, identity=" + identity + " project=" + reloadedProject);
					}		
				}
				return Boolean.TRUE;
			}
		});// end of doInSync
		
		if (autoSignOut && result.booleanValue()) {
			projectBrokerManager.signOutFormAllCandidateList(response.getAddedIdentities(), reloadedProject.getProjectBroker().getKey());
		}
		if (isAcceptSelectionManually && (reloadedProject.getMaxMembers() != Project.MAX_MEMBERS_UNLIMITED) 
				&& reloadedProject.getSelectedPlaces() >= reloadedProject.getMaxMembers()) {
			projectBrokerManager.setProjectState(reloadedProject, Project.STATE_ASSIGNED);
			log.info("ProjectBroker: Accept candidate, change project-state=" + Project.STATE_ASSIGNED);
		}
		return response;
	}

	@Override
	public void sendGroupChangeEvent(Project project, Long courseResourceableId, Identity identity) {
		ICourse course = CourseFactory.loadCourse(courseResourceableId);
		RepositoryEntry ores = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		MultiUserEvent modifiedEvent = new BusinessGroupModifiedEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, project.getProjectGroup(), identity, null);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, ores);
	}

	@Override
	public boolean isProjectManager(Identity identity, Project project) {
		return businessGroupService.hasRoles(identity, project.getProjectGroup(), GroupRoles.coach.name());
	}

	@Override
	public boolean isProjectManagerOrAdministrator(UserRequest ureq, UserCourseEnvironment userCourseEnv, Project project) {
		return userCourseEnv.isAdmin() || isProjectManager(ureq.getIdentity(), project);
	}

	@Override
	public boolean isProjectParticipant(Identity identity, Project project) {
		return businessGroupService.hasRoles(identity, project.getProjectGroup(), GroupRoles.participant.name());
	}

	@Override
	public boolean isProjectCandidate(Identity identity, Project project) {
		return securityGroupDao.isIdentityInSecurityGroup(identity, project.getCandidateGroup());
	}

	@Override
	public BusinessGroup setProjectGroupMaxMembers(Identity ureqIdentity, BusinessGroup projectGroup, int maxMembers ) {
  	 BusinessGroupService bgs = businessGroupService;
  	 BusinessGroup reloadedBusinessGroup = bgs.loadBusinessGroup(projectGroup);
  	 log.debug("ProjectGroup.name=" + reloadedBusinessGroup.getName() + " setMaxParticipants=" + maxMembers);
  	 return bgs.updateBusinessGroup(ureqIdentity, reloadedBusinessGroup, reloadedBusinessGroup.getName(), 
  			 reloadedBusinessGroup.getDescription(), reloadedBusinessGroup.getExternalId(), reloadedBusinessGroup.getManagedFlagsString(),
  			 reloadedBusinessGroup.getMinParticipants(), maxMembers);
	}

	///////////////////
	// PRIVATE METHODS
	///////////////////

	private boolean isAccountManager(Identity identity, BusinessGroup businessGroup, UserCourseEnvironment userCourseEnv) {
		if (userCourseEnv.isAdmin()) {
			return true;
		}
		if (businessGroup == null) {
			return false;
		}
		if (userCourseEnv.isCoach()) {
			return businessGroupService.hasRoles(identity, businessGroup, GroupRoles.coach.name());
		}
		if (userCourseEnv.isParticipant()) {
			return businessGroupService.hasRoles(identity, businessGroup, GroupRoles.participant.name());
		}
		
		return false;
	}

	@Override
	public void acceptAllCandidates(Long projectBrokerId, Identity actionIdentity, boolean autoSignOut, boolean isAcceptSelectionManually) {
		// loop over all project
		List<Project> projectList = projectBrokerManager.getProjectListBy(projectBrokerId);
		for (Iterator<Project> iterator = projectList.iterator(); iterator.hasNext();) {
			Project project = iterator.next();
			List<Identity> candidates = securityGroupDao.getIdentitiesOfSecurityGroup(project.getCandidateGroup());
			if (!candidates.isEmpty()) {
				log.info(Tracing.M_AUDIT, "ProjectBroker: Accept ALL candidates, project={}", project);
				acceptCandidates(candidates, project, actionIdentity, autoSignOut, isAcceptSelectionManually);
			}
		}	
		
	}

	@Override
	public boolean hasProjectBrokerAnyCandidates(Long projectBrokerId) {
		List<Project> projectList = projectBrokerManager.getProjectListBy(projectBrokerId);
		for (Iterator<Project> iterator = projectList.iterator(); iterator.hasNext();) {
			Project project = iterator.next();
			List<Identity> candidates = securityGroupDao.getIdentitiesOfSecurityGroup(project.getCandidateGroup());
			if (!candidates.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isCandidateListEmpty(SecurityGroup candidateGroup) {
		List<Identity> candidates = securityGroupDao.getIdentitiesOfSecurityGroup(candidateGroup);
		return candidates.isEmpty();
	}
	
	@Override
	public boolean isDeselectionAllowed(Project project){
		try {
			return project.getProjectGroup().isAllowToLeave();
		} catch (LazyInitializationException e) {
			return projectDao.loadProject(project.getKey()).getProjectGroup().isAllowToLeave();
		}
	}
	
	@Override
	public void setDeselectionAllowed(Project project, boolean allow){
		project.getProjectGroup().setAllowToLeave(allow);
		businessGroupService.updateAllowToLeaveBusinessGroup(project.getProjectGroup(), allow);
	}

}
