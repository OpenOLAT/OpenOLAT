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
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.Identity;
import org.olat.core.manager.BasicManager;
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
import org.olat.course.run.environment.CourseEnvironment;
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
public class ProjectGroupManagerImpl extends BasicManager implements ProjectGroupManager {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
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
		Long groupKey = null;
		BusinessGroup accountManagerGroup = null;
		Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
		// Check if account-manager-group-key-property already exist
		if (accountManagerGroupProperty != null) {
			groupKey = accountManagerGroupProperty.getLongValue();
			logDebug("accountManagerGroupProperty=" + accountManagerGroupProperty + "  groupKey=" + groupKey);
		} 
		logDebug("groupKey=" + groupKey);
		if (groupKey != null) {
			accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey);
			logDebug("load businessgroup=" + accountManagerGroup);
			if (accountManagerGroup != null) {
				return accountManagerGroup;
			} else {
				if (accountManagerGroupProperty != null) {
					cpm.deleteProperty(accountManagerGroupProperty);
				}
				groupKey = null;
				logWarn("ProjectBroker: Account-manager does no longer exist, create a new one", null);
			}
		} else {
			logDebug("No group for project-broker exist => create a new one");
			RepositoryEntry re = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			accountManagerGroup = businessGroupService.createBusinessGroup(identity, groupName, groupDescription, -1, -1, false, false, re);
			int i = 2;
			while (accountManagerGroup == null) {
				// group with this name exist already, try another name
				accountManagerGroup = businessGroupService.createBusinessGroup(identity, groupName + " _" + i, groupDescription, -1, -1, false, false, re);
				i++;
			}
			logDebug("createAndPersistBusinessGroup businessgroup=" + accountManagerGroup);			
			
			saveAccountManagerGroupKey(accountManagerGroup.getKey(), cpm, courseNode);
			logDebug("created account-manager default businessgroup=" + accountManagerGroup);
		} 
		return accountManagerGroup;
	}
	

	public void saveAccountManagerGroupKey(Long accountManagerGroupKey, CoursePropertyManager cpm, CourseNode courseNode) {
		Property accountManagerGroupKeyProperty = cpm.createCourseNodePropertyInstance(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY, null, accountManagerGroupKey, null, null);
		cpm.saveProperty(accountManagerGroupKeyProperty);	
		logDebug("saveAccountManagerGroupKey accountManagerGroupKey=" + accountManagerGroupKey);
	}

	public boolean isAccountManager(Identity identity, CoursePropertyManager cpm, CourseNode courseNode) {
		Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
		if (accountManagerGroupProperty != null) {
			Long groupKey = accountManagerGroupProperty.getLongValue();
			BusinessGroup accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey);
			if (accountManagerGroup != null) {
				return isAccountManager(identity,  accountManagerGroup);
			}
		}
		return false;
 	}

	public void deleteAccountManagerGroup( CoursePropertyManager cpm, CourseNode courseNode) {
		logDebug("deleteAccountManagerGroup start...");
  	Property accountManagerGroupProperty = cpm.findCourseNodeProperty(courseNode, null, null, ProjectBrokerCourseNode.CONF_ACCOUNTMANAGER_GROUP_KEY);
  	if (accountManagerGroupProperty != null) {
  		Long groupKey = accountManagerGroupProperty.getLongValue();
  		if (groupKey != null) {
				BusinessGroup accountManagerGroup = businessGroupService.loadBusinessGroup(groupKey);
				if (accountManagerGroup != null) {
					BusinessGroupService bgs = businessGroupService;
					bgs.deleteBusinessGroup(accountManagerGroup);
					logAudit("ProjectBroker: Deleted accountManagerGroup=" + accountManagerGroup);
				} else {
					logDebug("deleteAccountManagerGroup: accountManagerGroup=" + accountManagerGroup + " has already been deleted");
				}
			}
  		cpm.deleteProperty(accountManagerGroupProperty);
			logDebug("deleteAccountManagerGroup: deleted accountManagerGroupProperty=" + accountManagerGroupProperty );
 	} else {
			logDebug("deleteAccountManagerGroup: found no accountManagerGroup-key");
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

		logDebug("createProjectGroupFor groupName=" + groupName);
		BusinessGroup projectGroup = businessGroupService.createBusinessGroup(identity, groupName, groupDescription, -1, -1, false, false, re);
		// projectGroup could be null when a group with name already exists
		int counter = 2;
		while (projectGroup == null) {
			// name already exist try another one
			String newGroupName = groupName + " _" + counter ;
			projectGroup = businessGroupService.createBusinessGroup(identity, newGroupName, groupDescription, -1, -1, false, false, re);
			counter++;
		}
		logDebug("Created a new projectGroup=" + projectGroup);
		return projectGroup;
	}

	@Override
	public void deleteProjectGroupFor(Project project) {
		BusinessGroupService bgs = businessGroupService;
		bgs.deleteBusinessGroup(project.getProjectGroup());
	}
	
	/**
	 * Change group-name and description. Check if new group-name does not already exist in the course-group-context.
	 * If the goup-name already exist, it will be automatically try another one with suffix e.g. ' _2'
	 * @see org.olat.course.nodes.projectbroker.service.ProjectGroupManager#changeProjectGroupName(org.olat.group.BusinessGroup, java.lang.String, java.lang.String)
	 */
	@Override
	public BusinessGroup changeProjectGroupName(Identity ureqIdentity, BusinessGroup projectGroup, String groupName, String groupDescription, OLATResource courseResource) {
		BusinessGroupService bgs = businessGroupService;
		BusinessGroup reloadedBusinessGroup = bgs.loadBusinessGroup(projectGroup);
		return bgs.updateBusinessGroup(ureqIdentity, reloadedBusinessGroup, groupName, groupDescription,
				reloadedBusinessGroup.getExternalId(), reloadedBusinessGroup.getManagedFlagsString(),
				reloadedBusinessGroup.getMinParticipants(), reloadedBusinessGroup.getMaxParticipants());
	}

	public List<Identity> addCandidates(final List<Identity> addIdentities, final Project project) {
	//TODO gsync
		List<Identity> addedIdentities = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<List<Identity>>(){
			public List<Identity> execute() {
				List<Identity> addedIdentityList = new ArrayList<Identity>();
				for (Identity identity : addIdentities) {
					if (!securityManager.isIdentityInSecurityGroup(identity, project.getCandidateGroup()) ) {
						securityManager.addIdentityToSecurityGroup(identity, project.getCandidateGroup());
						addedIdentityList.add(identity);
						logAudit("ProjectBroker: Add user as candidate, identity=" + identity);
					}
					// fireEvents ?
				}
				return addedIdentityList;
			}
		});// end of doInSync
		return addedIdentities;
	}

	public void removeCandidates(final List<Identity> addIdentities, final Project project) {
	//TODO gsync
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<Boolean>(){
			public Boolean execute() {
				Project reloadedProject = (Project) dbInstance.loadObject(project, true);
				for (Identity identity : addIdentities) {
					securityManager.removeIdentityFromSecurityGroup(identity, reloadedProject.getCandidateGroup());
					logAudit("ProjectBroker: Remove user as candidate, identity=" + identity);
					// fireEvents ?
				}
				return Boolean.TRUE;
			}
		});// end of doInSync
	}

	@Override
	public BusinessGroupAddResponse acceptCandidates(final List<Identity> identities, final Project project, final Identity actionIdentity, final boolean autoSignOut, final boolean isAcceptSelectionManually) {
		final Project reloadedProject = (Project) dbInstance.loadObject(project, true);
		final BusinessGroupAddResponse response = new BusinessGroupAddResponse();
		BusinessGroupAddResponse state = businessGroupService.addParticipants(actionIdentity, null, identities, reloadedProject.getProjectGroup(), null);
		response.getAddedIdentities().addAll(state.getAddedIdentities());
		response.getIdentitiesAlreadyInGroup().addAll(state.getIdentitiesAlreadyInGroup());
		
		Boolean result = CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(project.getProjectGroup(), new SyncerCallback<Boolean>(){
			@Override
			public Boolean execute() {
				for (final Identity identity : identities) {
					if (businessGroupService.hasRoles(identity, reloadedProject.getProjectGroup(), GroupRoles.participant.name())) {
						securityManager.removeIdentityFromSecurityGroup(identity, reloadedProject.getCandidateGroup());
						logAudit("ProjectBroker: Accept candidate, identity=" + identity + " project=" + reloadedProject);
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
			logInfo("ProjectBroker: Accept candidate, change project-state=" + Project.STATE_ASSIGNED);
		}
		return response;
	}

	@Override
	public void sendGroupChangeEvent(Project project, Long courseResourceableId, Identity identity) {
		ICourse course = CourseFactory.loadCourse(courseResourceableId);
		RepositoryEntry ores = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		MultiUserEvent modifiedEvent = new BusinessGroupModifiedEvent(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT, project.getProjectGroup(), identity);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(modifiedEvent, ores);
	}

	public boolean isProjectManager(Identity identity, Project project) {
		return businessGroupService.hasRoles(identity, project.getProjectGroup(), GroupRoles.coach.name());
	}

	public boolean isProjectManagerOrAdministrator(UserRequest ureq, CourseEnvironment courseEnv, Project project) {	
		return isProjectManager(ureq.getIdentity(), project)
				   || courseEnv.getCourseGroupManager().isIdentityCourseAdministrator(ureq.getIdentity())
	         || ureq.getUserSession().getRoles().isOLATAdmin();
	}
	
	public boolean isProjectParticipant(Identity identity, Project project) {
		return businessGroupService.hasRoles(identity, project.getProjectGroup(), GroupRoles.participant.name());
	}

	public boolean isProjectCandidate(Identity identity, Project project) {
		return securityManager.isIdentityInSecurityGroup(identity, project.getCandidateGroup());
	}

	@Override
	public BusinessGroup setProjectGroupMaxMembers(Identity ureqIdentity, BusinessGroup projectGroup, int maxMembers ) {
  	 BusinessGroupService bgs = businessGroupService;
  	 BusinessGroup reloadedBusinessGroup = bgs.loadBusinessGroup(projectGroup);
  	 logDebug("ProjectGroup.name=" + reloadedBusinessGroup.getName() + " setMaxParticipants=" + maxMembers);
  	 return bgs.updateBusinessGroup(ureqIdentity, reloadedBusinessGroup, reloadedBusinessGroup.getName(), 
  			 reloadedBusinessGroup.getDescription(), reloadedBusinessGroup.getExternalId(), reloadedBusinessGroup.getManagedFlagsString(),
  			 reloadedBusinessGroup.getMinParticipants(), maxMembers);
	}

	///////////////////
	// PRIVATE METHODS
	///////////////////

	private boolean isAccountManager(Identity identity, BusinessGroup businessGroup) {
		if (businessGroup == null) {
			return false;
		}
		return businessGroupService.hasRoles(identity, businessGroup, GroupRoles.participant.name())
				   || businessGroupService.hasRoles(identity, businessGroup, GroupRoles.coach.name());
	}

	@Override
	public void acceptAllCandidates(Long projectBrokerId, Identity actionIdentity, boolean autoSignOut, boolean isAcceptSelectionManually) {
		// loop over all project
		List<Project> projectList = projectBrokerManager.getProjectListBy(projectBrokerId);
		for (Iterator<Project> iterator = projectList.iterator(); iterator.hasNext();) {
			Project project = iterator.next();
			List<Identity> candidates = securityManager.getIdentitiesOfSecurityGroup(project.getCandidateGroup());
			if (!candidates.isEmpty()) {
				logAudit("ProjectBroker: Accept ALL candidates, project=" + project);
				acceptCandidates(candidates, project, actionIdentity, autoSignOut, isAcceptSelectionManually);
			}
		}	
		
	}

	@Override
	public boolean hasProjectBrokerAnyCandidates(Long projectBrokerId) {
		List<Project> projectList = projectBrokerManager.getProjectListBy(projectBrokerId);
		for (Iterator<Project> iterator = projectList.iterator(); iterator.hasNext();) {
			Project project = iterator.next();
			List<Identity> candidates = securityManager.getIdentitiesOfSecurityGroup(project.getCandidateGroup());
			if (!candidates.isEmpty()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isCandidateListEmpty(SecurityGroup candidateGroup) {
		List<Identity> candidates = securityManager.getIdentitiesOfSecurityGroup(candidateGroup);
		return candidates.isEmpty();
	}
	
	@Override
	public boolean isDeselectionAllowed(Project project){
		return project.getProjectGroup().isAllowToLeave();
	}
	
	@Override
	public void setDeselectionAllowed(Project project, boolean allow){
		project.getProjectGroup().setAllowToLeave(allow);
		businessGroupService.updateAllowToLeaveBusinessGroup(project.getProjectGroup(), allow);
	}

}
