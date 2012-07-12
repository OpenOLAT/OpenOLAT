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

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
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
	
	/**
	 * Initialize the group contexts list.
	 */
	public void initGroupContextsList();

	/**
	 * Checks users course rights in any of the available right group context of
	 * this course
	 * 
	 * @param identity
	 * @param courseRight
	 * @return true if user has course right, false otherwhise
	 */
	public boolean hasRight(Identity identity, String courseRight);

	/**
	 * Checks if an identity is in a learning group with the given name in any
	 * contexts of this course, either as owner or as participant
	 * 
	 * @param identity
	 * @param groupName
	 * @return true if user is in learning group, false otherwhise
	 */
	public boolean isIdentityInGroup(Identity identity, String groupName);
	
	public boolean isIdentityInGroup(Identity identity, Long groupKey);

	/**
	 * Checks whether a set of learning groups with an identical name are full or not.
	 * 
	 * @param groupName the name of groups
	 * 
	 * @return true means all learning groups are full
	 */
	public boolean isLearningGroupFull(String groupName);


	/**
	 * Checks if an identity is in any learning areas with the given name in any
	 * of the courses group contexts
	 * 
	 * @param identity
	 * @param areaName
	 * @return true if user is in such an area, false otherwhise
	 */
	public boolean isIdentityInLearningArea(Identity identity, String areaName);
	
	public boolean isIdentityInLearningArea(Identity identity, Long areaKey);

	/**
	 * Checks if user is coach in any of the courses learning groups
	 * 
	 * @param identity
	 * @return true if user is coach
	 */
	public boolean isIdentityCourseCoach(Identity identity);

	/**
	 * Checks if user is course administrator (is owner of repository entry)
	 * 
	 * @param identity
	 * @return boolean
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
	 * @return A list of all learning group from all learning group contexts of
	 *         this course
	 */
	public List<BusinessGroup> getAllLearningGroupsFromAllContexts();

	/**
	 * @param groupName
	 * @return A list of all learning groups with the given group name from all
	 *         contexts of this course
	 */
	public List<BusinessGroup> getLearningGroupsFromAllContexts(String groupName);
	
	public boolean existGroup(String nameOrKey);

	/**
	 * @param areaName
	 * @return A list of all learning groups from all contexts from this course
	 *         that are in the given group area
	 */
	public List<BusinessGroup> getLearningGroupsInAreaFromAllContexts(String areaName);

	/**
	 * @param groupName
	 * @return A list of all learning areas where the given group takes part. All
	 *         course group contexts are considered in this search.
	 */
	public List<BGArea> getLearningAreasOfGroupFromAllContexts(String groupName);

	/**
	 * @param identity
	 * @param groupName
	 * @return A list of all learning groups with the given name where this
	 *         identity is participant
	 */
	public List<BusinessGroup> getParticipatingLearningGroupsFromAllContexts(Identity identity, String groupName);

	/**
	 * @param identity
	 * @param araName
	 * @return A list of all learning groups within the given group area where
	 *         this identity is participant
	 */
	public List<BusinessGroup> getParticipatingLearningGroupsInAreaFromAllContexts(Identity identity, String araName);

	/**
	 * @param identity
	 * @return A list of all learning groups where this identity is owner
	 */
	public List<BusinessGroup> getOwnedLearningGroupsFromAllContexts(Identity identity);

	/**
	 * @param identity
	 * @return A list of all learning groups where this identity is participant
	 */
	public List<BusinessGroup> getParticipatingLearningGroupsFromAllContexts(Identity identity);

	/**
	 * @return A list of all group areas from this course
	 */
	public List<BGArea> getAllAreasFromAllContexts();
	
	public List<BGArea> getAreasFromContext(String areaname);
	
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
	public List<String> getUniqueLearningGroupNamesFromAllContexts();

	/**
	 * @return A list with all area names used in all learning group contexts. If
	 *         an area red is in more than one contexts, red will be only once in
	 *         the list
	 */
	public List<String> getUniqueAreaNamesFromAllContexts();

	/**
	 * Export all groups which are course internal to a file for later import.
	 * 
	 * @param fExportDirectory
	 */
	public void exportCourseLeaningGroups(File fExportDirectory);

	/**
	 * Import course internal groups fa previous export.
	 * 
	 * @param fImportDirectory
	 */
	public void importCourseLearningGroups(File fImportDirectory);

	/**
	 * Import course internal groups fa previous export.
	 * 
	 * @param fImportDirectory
	 */
	public void importCourseRightGroups(File fImportDirectory);

	/**
	 * List with identities being coaches in learning groups of this course. If
	 * the specified name is null, all learning groups are considered.
	 * 
	 * @param groupName
	 * @return a list with all coaches of this course
	 */
	public List<Identity> getCoachesFromLearningGroup(String groupName);
	
	//fxdiff VCRP-1,2: access control of resources
	public List<Identity> getCoaches();

	/**
	 * List with identities being coaches in the areas of this course. If
	 * the specified name is null, all areas are considered.
	 * 
	 * @param areaName
	 * @return a list with all coaches of this course
	 */
	public List<Identity> getCoachesFromArea(String areaName);

	/**
	 * List with identities being participants in the learning groups of this course. If
	 * the specified name is null, all learning groups are considered.
	 * 
	 * @param groupName
	 * @return a list with all participants of this course
	 */
	public List<Identity> getParticipantsFromLearningGroup(String groupName);
	
	//fxdiff VCRP-1,2: access control of resources
	public List<Identity> getParticipants();

	/**
	 * List with identities being participants in the areas of this course. If
	 * the specified name is null, all areas are considered.
	 * 
	 * @param areaName
	 * @return a list with participants of this course
	 */
	public List<Identity> getParticipantsFromArea(String areaName);

	/**
	 * @param identity
	 * @return A list of all waiting-list groups where this identity is in
	 */
	public List<BusinessGroup> getWaitingListGroupsFromAllContexts(Identity identity);
}
