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

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.projectbroker.datamodel.Project;
import org.olat.course.nodes.projectbroker.datamodel.ProjectBroker;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;

public interface ProjectBrokerManager {
	public static final String CUSTOMFIELD_LIST_DELIMITER = "\n;";

	/**
	 * Returns a list of projects for certain project-broker.
	 * @param projectBrokerId
	 * @return
	 */
	public List<Project> getProjectListBy(Long projectBrokerId);

	public List<Project> getProjectsWith(BusinessGroup group);


	/**
	 * Returns a project-broker object for certain project-broker-ID.
	 * @param projectBrokerId
	 * @return
	 */
	public ProjectBroker getProjectBroker(Long projectBrokerId);

	/**
	 * Creates a new project-broker and save it.
	 * @return
	 */
	public ProjectBroker createAndSaveProjectBroker();

	/**
	 * Update and save an existing project.
	 * @param project
	 */
	public void updateProject(Project project);

	/**
	 * Create and save a new project.
	 * @param projectTitle
	 * @param description
	 * @param projectBrokerId
	 * @param projectGroup
	 * @return
	 */
	public Project createAndSaveProjectFor(String projectTitle, String description, Long projectBrokerId, BusinessGroup projectGroup);

	/**
	 * Enroll certain identity as participant or candidate of a project (depending on project-broker configuration).
	 * @param identity
	 * @param projectAt
	 * @param moduleConfig
	 * @param nbrSelectedProjects
	 * @param isParticipantInAnyProject
	 * @return
	 */
	public boolean enrollProjectParticipant(Identity identity, Project projectAt, ProjectBrokerModuleConfiguration moduleConfig, int nbrSelectedProjects, boolean isParticipantInAnyProject);

	/**
	 * Remove certain identity as participant or candidate of a project (depending on project-broker configuration).
	 * @param identity
	 * @param projectAt
	 * @param moduleConfig
	 * @return
	 */
	public boolean cancelProjectEnrollmentOf(Identity identity, Project projectAt, ProjectBrokerModuleConfiguration moduleConfig);

	/**
	 * Delete a project.
	 * @param project
	 * @param deleteGroup true: project group will be deleted too
	 * @param courseEnv
	 * @param cNode
	 */
	public void deleteProject(Project project, boolean deleteGroup, CourseEnvironment courseEnv, CourseNode cNode);

	/**
	 * Get the number of total selected project for certain identity.
	 * @param identity
	 * @param projectList
	 * @return
	 */
	public int getNbrSelectedProjects(Identity identity, List<Project> projectList);

	/**
	 * Return true when the project can be selected by an identity.
	 * Depends on enrollment-date, number of already enrolled projects and project-broker configuration.
	 * @param identity
	 * @param project
	 * @param moduleConfig
	 * @param nbrSelectedProjects
	 * @param isParticipantInAnyProject
	 * @return
	 */
	public boolean canBeProjectSelectedBy(Identity identity, Project project,  ProjectBrokerModuleConfiguration moduleConfig, int nbrSelectedProjects, boolean isParticipantInAnyProject);

	/**
	 * Return true when the project can be de-selected by an identity.
	 * @param identity
	 * @param project
	 * @param moduleConfig
	 * @return
	 */
	public boolean canBeCancelEnrollmentBy(Identity identity, Project project,  ProjectBrokerModuleConfiguration moduleConfig);

	/**
	 * Remove identities from all projects as candidates.
	 * @param chosenIdentities
	 * @param projectBrokerId
	 */
	public void signOutFormAllCandidateList(List<Identity> chosenIdentities, Long projectBrokerId);

	/**
	 * Get the state [STATE_ASSIGNED_ACCOUNT_MANAGER | STATE_NOT_ASSIGNED_ACCOUNT_MANAGER |
	 * STATE_NOT_ASSIGNED_ACCOUNT_MANAGER_NO_CANDIDATE | STATE_FINAL_ENROLLED |
	 * STATE_PROV_ENROLLED | STATE_COMPLETE | STATE_NOT_ASSIGNED | STATE_ENROLLED ] of a project
	 * @param project
	 * @param identity
	 * @param moduleConfig
	 * @return
	 */
	public String getStateFor(Project project, Identity identity, ProjectBrokerModuleConfiguration moduleConfig);

	/**
	 * Delete a project-broker and his projects and groups.
	 * @param projectBrokerId
	 * @param courseEnvironment
	 * @param courseNode
	 */
	public void deleteProjectBroker(Long projectBrokerId, CourseEnvironment courseEnvironment, CourseNode courseNode);

	/**
	 * Save attachment file in the attachment-folder of a project.
	 * @param project
	 * @param fileName
	 * @param uploadedItem
	 * @param courseEnv
	 * @param cNode
	 * @param savedBy 
	 */
	public void saveAttachedFile(Project project, String fileName, VFSLeaf uploadedItem, CourseEnvironment courseEnv, CourseNode cNode, Identity savedBy);

	/**
	 * Return true when the custom-field value is one of possible-values (drop-down-mode) or when it could be any value (input field).
	 * @param value
	 * @param string
	 * @return
	 */
	public boolean isCustomFieldValueValid(String value, String string);

	/**
	 * Get attachment-file relative path.
	 * E.g. course/<COURSE_ID>/projectbroker_attach/<COURSE_NODE>/<PROJECT_ID>
	 * @param project
	 * @param courseEnv
	 * @param courseNode
	 * @return
	 */
	public String getAttamchmentRelativeRootPath(Project project, CourseEnvironment courseEnv, CourseNode courseNode);

	/**
	 * Set project-state [STATE_NOT_ASSIGNED | STATE_ASSIGNED].
	 * @param project
	 * @param state
	 */
	public void setProjectState(Project project, String state);

	/**
	 * Return true when an identity is participant in any project of a project-broker.
	 * @param identity
	 * @param projectList
	 * @return
	 */
	public boolean isParticipantInAnyProject(Identity identity, List<Project> projectList);

	/**
	 * Get saved project-broker-id.
	 * @param cpm
	 * @param courseNode
	 * @return
	 */
	public Long getProjectBrokerId(CoursePropertyManager cpm, CourseNode courseNode);

	/**
	 * Save project-broker-id.
	 * @param projectBrokerId
	 * @param cpm
	 * @param courseNode
	 */
	public void saveProjectBrokerId(Long projectBrokerId, CoursePropertyManager cpm, CourseNode courseNode);

	/**
	 * Return true when a project with the same title already exist.
	 * @param projectBrokerId
	 * @param newProjectTitle
	 * @return
	 */
	public boolean existProjectName(Long projectBrokerId, String newProjectTitle);

	/**
	 * Get attachment-folder relative path (without project-id). THis path can be used to delete all
	 * attachment-file for certain project-broker.
	 * E.g. course/<COURSE_ID>/projectbroker_attach/<COURSE_NODE>
	 * @param courseEnvironment
	 * @param courseNode
	 * @return
	 */
	public String getAttachmentBasePathRelToFolderRoot(CourseEnvironment courseEnvironment, CourseNode courseNode);

	/**
	 * Get list of selected projects for certain identity.
	 * @param identity
	 * @param projectBrokerId
	 * @return
	 */
	public List<Project> getProjectsOf(Identity identity, Long projectBrokerId);

	/**
	 * Get certain project by key.
	 * @param projectId
	 * @return
	 */
	public Project getProject(Long projectId);

	/**
	 * Get list of coached projects for certain identity.
	 * @param identity
	 * @param projectBrokerId
	 * @return
	 */
	public List<Project> getCoachedProjectsOf(Identity identity, Long projectBrokerId);

	/**
	 * Check if certain project still exists (on db)
	 * @param projectKey
	 * @return
	 */
	public boolean existsProject(Long projectKey);

}
