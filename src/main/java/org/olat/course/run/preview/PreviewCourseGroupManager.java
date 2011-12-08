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
* <p>
*/ 

package org.olat.course.run.preview;

import java.io.File;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.manager.BasicManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;

/**
 * Initial Date:  08.02.2005
 *
 * @author Mike Stock
 */
final class PreviewCourseGroupManager extends BasicManager implements CourseGroupManager {

	private List groups;
	private List areas;
	private boolean isCoach, isCourseAdmin;
	
	/**
	 * @param groups
	 * @param areas
	 * @param isCoach
	 * @param isCourseAdmin
	 */
	public PreviewCourseGroupManager(List groups, List areas, boolean isCoach, boolean isCourseAdmin) {
		this.groups = groups;
		this.areas = areas;
		this.isCourseAdmin = isCourseAdmin;
		this.isCoach = isCoach;
	}
	
	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#initGroupContextsList()
	 */
	public void initGroupContextsList() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#hasRight(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean hasRight(Identity identity, String courseRight) {
		if (courseRight.equals(CourseRights.RIGHT_COURSEEDITOR)) {
			return false;
		}
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#hasRight(org.olat.core.id.Identity, java.lang.String, java.lang.String)
	 */
	public boolean hasRight(Identity identity, String courseRight, String groupContextName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningGroup(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean isIdentityInLearningGroup(Identity identity, String groupName) {
		return groups.contains(groupName);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningGroup(org.olat.core.id.Identity, java.lang.String, java.lang.String)
	 */
	public boolean isIdentityInLearningGroup(Identity identity, String groupName, String groupContextName) {
		return groups.contains(groupName);
	}
	
	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isLearningGroupFull(java.lang.String)
	 */
	public boolean isLearningGroupFull(String groupName){
		return groups.contains(groupName);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInRightGroup(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean isIdentityInRightGroup(Identity identity, String groupName) {
		return groups.contains(groupName);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInRightGroup(org.olat.core.id.Identity, java.lang.String, java.lang.String)
	 */
	public boolean isIdentityInRightGroup(Identity identity, String groupName, String groupContextName) {
		return groups.contains(groupName);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningArea(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean isIdentityInLearningArea(Identity identity, String areaName) {
		return areas.contains(areaName);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningArea(org.olat.core.id.Identity, java.lang.String, java.lang.String)
	 */
	public boolean isIdentityInLearningArea(Identity identity, String areaName, String groupContextName) {
		return areas.contains(areaName);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInGroupContext(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean isIdentityInGroupContext(Identity identity, String groupContextName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityCourseCoach(org.olat.core.id.Identity)
	 */
	public boolean isIdentityCourseCoach(Identity identity) {
		return isCoach;
	}

	@Override
	public boolean isIdentityCourseParticipant(Identity identity) {
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityCourseAdministrator(org.olat.core.id.Identity)
	 */
	public boolean isIdentityCourseAdministrator(Identity identity) {
		return isCourseAdmin;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityParticipantInAnyRightGroup(org.olat.core.id.Identity)
	 */
	public boolean isIdentityParticipantInAnyRightGroup(Identity identity) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityParticipantInAnyLearningGroup(org.olat.core.id.Identity)
	 */
	public boolean isIdentityParticipantInAnyLearningGroup(Identity identity) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupContexts()
	 */
	public List getLearningGroupContexts() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getRightGroupContexts()
	 */
	public List getRightGroupContexts() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllLearningGroupsFromAllContexts()
	 */
	public List getAllLearningGroupsFromAllContexts() {
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupsFromAllContexts(java.lang.String)
	 */
	public List getLearningGroupsFromAllContexts(String groupName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupsInAreaFromAllContexts(java.lang.String)
	 */
	public List getLearningGroupsInAreaFromAllContexts(String areaName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsFromAllContexts(org.olat.core.id.Identity, java.lang.String)
	 */
	public List getParticipatingLearningGroupsFromAllContexts(Identity identity, String groupName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsInAreaFromAllContexts(org.olat.core.id.Identity, java.lang.String)
	 */
	public List getParticipatingLearningGroupsInAreaFromAllContexts(Identity identity, String araName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getOwnedLearningGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List getOwnedLearningGroupsFromAllContexts(Identity identity) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List getParticipatingLearningGroupsFromAllContexts(Identity identity) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingRightGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List getParticipatingRightGroupsFromAllContexts(Identity identity) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllRightGroupsFromAllContexts()
	 */
	public List getAllRightGroupsFromAllContexts() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllAreasFromAllContexts()
	 */
	public List getAllAreasFromAllContexts() {
		return areas;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#deleteCourseGroupmanagement()
	 */
	public void deleteCourseGroupmanagement() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#createCourseGroupmanagement(java.lang.String)
	 */
	public void createCourseGroupmanagement(String courseTitle) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#createCourseGroupmanagementAsCopy(org.olat.course.groupsandrights.CourseGroupManager, java.lang.String)
	 */
	public void createCourseGroupmanagementAsCopy(CourseGroupManager originalCourseGroupManager, String courseTitle) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getNumberOfMembersFromGroups(java.util.List)
	 */
	public List getNumberOfMembersFromGroups(List groupList) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueLearningGroupNamesFromAllContexts()
	 */
	public List getUniqueLearningGroupNamesFromAllContexts() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueAreaNamesFromAllContexts()
	 */
	public List getUniqueAreaNamesFromAllContexts() {
		throw new AssertException("unsupported");
	}
	
	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningAreasOfGroupFromAllContexts(java.lang.String)
	 */
  public List getLearningAreasOfGroupFromAllContexts(String groupName) {
		throw new AssertException("unsupported");
  }

	public List getCoachesFromLearningGroup(String groupName) {
		throw new AssertException("unsupported");
	}

	public List getCoachesFromArea(String areaName) {
		throw new AssertException("unsupported");
	}

	public List getParticipantsFromLearningGroup(String groupName) {
		throw new AssertException("unsupported");
	}

	public List getParticipantsFromArea(String areaName) {
		throw new AssertException("unsupported");
	}
	
	//fxdiff VCRP-1,2: access control of resources
	@Override
	public List<Identity> getCoaches() {
		throw new AssertException("unsupported");
	}

	@Override
	public List<Identity> getParticipants() {
		throw new AssertException("unsupported");
	}

	public List getRightGroupsFromAllContexts(String groupName) {
		throw new AssertException("unsupported");
	}

	public void exportCourseRightGroups(File fExportDirectory) {
		throw new AssertException("unsupported");
	}

	public void importCourseRightGroups(File fImportDirectory) {
		throw new AssertException("unsupported");
	}

	public void exportCourseLeaningGroups(File fExportDirectory) {
		throw new AssertException("unsupported");
	}

	public void importCourseLearningGroups(File fImportDirectory) {
		throw new AssertException("unsupported");
	}

	public List getWaitingListGroupsFromAllContexts(Identity identity) {
		throw new AssertException("unsupported");
	}

}