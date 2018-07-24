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

package org.olat.course.groupsandrights;

import java.io.File;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.id.Identity;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * Description:<BR/> The course group manager provides methods to access
 * groups, areas and contexts. For many functionality the BusinessGroupManager,
 * BGAreaManager or the BGContextManager must be used. <P/>
 * 
 * Initial Date: Aug 25, 2004
 * @author gnaegi
 */
public interface CourseGroupManager {

	/** default course group contexts prefix used for learning and right groups * */
	static final String DEFAULT_CONTEXT_PREFIX = "default::";
	/** default course group context name for learning groups */
	static final String DEFAULT_NAME_LC_PREFIX = DEFAULT_CONTEXT_PREFIX + "learninggroups::";
	/** default course group context name for right groups */
	static final String DEFAULT_NAME_RC_PREFIX = DEFAULT_CONTEXT_PREFIX + "rightgroups::";

	
	public OLATResource getCourseResource();
	
	public RepositoryEntry getCourseEntry();
	
	/**
	 * @return true if the status of the course allow notifications
	 */
	public boolean isNotificationsAllowed();

	/**
	 * Checks users course rights in any of the available right group context of
	 * this course
	 * 
	 * @param identity
	 * @param courseRight
	 * @return true if user has course right, false otherwise
	 */
	public boolean hasRight(Identity identity, String courseRight);
	
	/**
	 * Return the users course rights in any of the available right group context of
	 * this course
	 * @param identity
	 * @return
	 */
	public List<String> getRights(Identity identity);

	/**
	 * Checks if an identity is in a learning group with the given name in any
	 * contexts of this course, either as owner or as participant
	 * 
	 * @param identity
	 * @param groupKey
	 * @return true if user is in learning group, false otherwise
	 */
	public boolean isIdentityInGroup(Identity identity, Long groupKey);

	/**
	 * Checks whether a set of learning groups with an identical name are full or not.
	 * 
	 * @param groupKey the name of groups
	 * 
	 * @return true means all learning groups are full
	 */
	public boolean isBusinessGroupFull(Long groupKey);


	/**
	 * Checks if an identity is in any learning areas with the given name in any
	 * of the courses group contexts
	 * 
	 * @param identity
	 * @param areaKey
	 * @return true if user is in such an area, false otherwise
	 */
	public boolean isIdentityInLearningArea(Identity identity, Long areaKey);

	/**
	 * Checks if user is coach in the course (of the repository entry or of a business group)
	 * 
	 * @param identity
	 * @return true if user is coach
	 */
	public boolean isIdentityCourseCoach(Identity identity);

	/**
	 * Checks if user is course administrator (is owner, learning resource
	 * manager or administrator of repository entry)
	 * 
	 * @param identity The identity to check
	 * @return boolean true if the specified is administrator, learn resource manager or owner
	 * 			of the course.
	 */
	public boolean isIdentityCourseAdministrator(Identity identity);
	
	/**
	 * Checks if user is course participant
	 * 
	 * @param identity
	 * @return boolean
	 */
	public boolean isIdentityCourseParticipant(Identity identity);
	
	
	/**
	 * Checks if user is coach in any course.
	 * 
	 * @param identity
	 * @return true if user is coach
	 */
	public boolean isIdentityAnyCourseCoach(Identity identity);

	/**
	 * Checks if user is course administrator in any course.
	 * 
	 * @param identity
	 * @return boolean
	 */
	public boolean isIdentityAnyCourseAdministrator(Identity identity);
	
	/**
	 * Checks if user is participant in any course.
	 * 
	 * @param identity
	 * @return boolean
	 */
	public boolean isIdentityAnyCourseParticipant(Identity identity);
	
	/**
	 * Check if the identity has one of the specified roles in the organisation
	 * 
	 * @param identity The identity
	 * @param organisationIdentifier The organisation identifier
	 * @param roles The roles
	 * @return true if a role match
	 */
	public boolean isIdentityInOrganisation(IdentityRef identity, String organisationIdentifier, OrganisationRoles... roles);
	
	/**
	 * @return True if there are some business groups linked to this resource
	 */
	public boolean hasBusinessGroups();

	/**
	 * @return A list of all learning group from all learning group contexts of
	 *         this course
	 */
	public List<BusinessGroup> getAllBusinessGroups();

	public boolean existGroup(String nameOrKey);

	/**
	 * @param identity
	 * @return A list of all learning groups where this identity is owner
	 */
	public List<BusinessGroup> getOwnedBusinessGroups(Identity identity);

	/**
	 * @param identity
	 * @return A list of all learning groups where this identity is participant
	 */
	public List<BusinessGroup> getParticipatingBusinessGroups(Identity identity);
	
	/**
	 * @return A list of curriculum elements linked to this course.
	 */
	public List<CurriculumElement> getAllCurriculumElements();
	
	/**
	 * Returns the list of curriculum elements where the specified
	 * identity has the role "coach".
	 * 
	 * @param identity The coach
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getCoachedCurriculumElements(Identity identity);
	
	/**
	 * @return True if the course has some areas configured.
	 */
	public boolean hasAreas();

	/**
	 * @return A list of all group areas from this course
	 */
	public List<BGArea> getAllAreas();

	public boolean existArea(String nameOrKey);

	/**
	 * Deletes the course group management. This will unlink all group contexts
	 * from this course. When the unlinked contexts are not used in any other
	 * resources then the context itself will be deleted as well. This will delete
	 * all areas, groups, group memberships, group folders, forums etc. Use with
	 * care!
	 */
	public void deleteCourseGroupmanagement();

	/**
	 * Method to count group memberships. Only participants will be counted, no
	 * the owners.
	 * 
	 * @param groups A list of groups
	 * @return A list of Integers that show the number of members in a group for
	 *         each of the group from the groups list.
	 */
	public List<Integer> getNumberOfMembersFromGroups(List<BusinessGroup> groups);

	/**
	 * @return A list with all group names used in all learning group contexts. If
	 *         a group red is in more than one contexts, red will be only once in
	 *         the list
	 */
	public List<String> getUniqueBusinessGroupNames();

	/**
	 * @return A list with all area names used in all learning group contexts. If
	 *         an area red is in more than one contexts, red will be only once in
	 *         the list
	 */
	public List<String> getUniqueAreaNames();

	/**
	 * Export all groups which are course internal to a file for later import.
	 * 
	 * @param fExportDirectory
	 */
	public void exportCourseBusinessGroups(File fExportDirectory, CourseEnvironmentMapper env,
			boolean runtimeDatas, boolean backwardsCompatible);
	
	public CourseEnvironmentMapper getBusinessGroupEnvironment();

	/**
	 * Import course internal groups fa previous export.
	 * 
	 * @param fImportDirectory
	 */
	public CourseEnvironmentMapper importCourseBusinessGroups(File fImportDirectory);
	
	public void archiveCourseGroups(File exportDirectory);

	/**
	 * List with identities being coaches in learning groups of this course. If
	 * the specified name is null, all learning groups are considered.
	 * 
	 * @param groupName
	 * @return a list with all coaches of this course
	 */
	public List<Identity> getCoachesFromBusinessGroups();
	
	public List<Identity> getCoachesFromBusinessGroups(List<Long> groupKeys);

	/**
	 * The coahces in the course and its curriculums.
	 * 
	 * @return A list of identities
	 */
	public List<Identity> getCoaches();

	/**
	 * List with identities being coaches in the areas of this course. If
	 * the specified name is null, all areas are considered.
	 * 
	 * @param areaName
	 * @return a list with all coaches of this course
	 */
	public List<Identity> getCoachesFromAreas();
	
	public List<Identity> getCoachesFromAreas(List<Long> areaKeys);

	/**
	 * List with identities being participants in the learning groups of this course. If
	 * the specified name is null, all learning groups are considered.
	 * 
	 * @param groupName
	 * @return a list with all participants of this course
	 */
	public List<Identity> getParticipantsFromBusinessGroups();
	
	public List<Identity> getParticipantsFromBusinessGroups(List<Long> groupKeys);
	
	/**
	 * The participants in the course and its curriculums.
	 * 
	 * @return A list of identities
	 */
	public List<Identity> getParticipants();

	/**
	 * List with identities being participants in the areas of this course. If
	 * the specified name is null, all areas are considered.
	 * 
	 * @param areaName
	 * @return a list with participants of this course
	 */
	public List<Identity> getParticipantsFromAreas();
	
	public List<Identity> getParticipantsFromAreas(List<Long> areaKeys);

	/**
	 * @param identity
	 * @return A list of all waiting-list groups where this identity is in
	 */
	public List<BusinessGroup> getWaitingListGroups(Identity identity);

}
