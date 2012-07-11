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

package org.olat.course.run.preview;

import java.io.File;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.manager.BasicManager;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.group.BusinessGroup;
import org.olat.group.area.BGArea;
import org.olat.resource.OLATResource;

/**
 * Initial Date:  08.02.2005
 *
 * @author Mike Stock
 */
final class PreviewCourseGroupManager extends BasicManager implements CourseGroupManager {

	private List<BusinessGroup> groups;
	private List<BGArea> areas;
	private boolean isCoach, isCourseAdmin;
	
	/**
	 * @param groups
	 * @param areas
	 * @param isCoach
	 * @param isCourseAdmin
	 */
	public PreviewCourseGroupManager(List<BusinessGroup> groups, List<BGArea> areas, boolean isCoach, boolean isCourseAdmin) {
		this.groups = groups;
		this.areas = areas;
		this.isCourseAdmin = isCourseAdmin;
		this.isCoach = isCoach;
	}

	@Override
	public OLATResource getCourseResource() {
		return null;
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
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningGroup(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean isIdentityInGroup(Identity identity, String groupName) {
		return groups.contains(groupName);
	}
	
	@Override
	public boolean isIdentityInGroup(Identity identity, Long groupKey) {
		for(BusinessGroup group:groups) {
			if(groupKey.equals(group.getKey())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isLearningGroupFull(java.lang.String)
	 */
	public boolean isLearningGroupFull(String groupName){
		return groups.contains(groupName);
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#isIdentityInLearningArea(org.olat.core.id.Identity, java.lang.String)
	 */
	public boolean isIdentityInLearningArea(Identity identity, String areaName) {
		return areas.contains(areaName);
	}

	@Override
	public boolean isIdentityInLearningArea(Identity identity, Long areaKey) {
		for(BGArea area:areas) {
			if(areaKey.equals(area.getKey())) {
				return true;
			}
		}
		return false;
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
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllLearningGroupsFromAllContexts()
	 */
	public List<BusinessGroup> getAllLearningGroupsFromAllContexts() {
		return groups;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupsFromAllContexts(java.lang.String)
	 */
	public List<BusinessGroup> getLearningGroupsFromAllContexts(String groupName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningGroupsInAreaFromAllContexts(java.lang.String)
	 */
	public List<BusinessGroup> getLearningGroupsInAreaFromAllContexts(String areaName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsFromAllContexts(org.olat.core.id.Identity, java.lang.String)
	 */
	public List<BusinessGroup> getParticipatingLearningGroupsFromAllContexts(Identity identity, String groupName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsInAreaFromAllContexts(org.olat.core.id.Identity, java.lang.String)
	 */
	public List<BusinessGroup> getParticipatingLearningGroupsInAreaFromAllContexts(Identity identity, String araName) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getOwnedLearningGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List<BusinessGroup> getOwnedLearningGroupsFromAllContexts(Identity identity) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingLearningGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List<BusinessGroup> getParticipatingLearningGroupsFromAllContexts(Identity identity) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getParticipatingRightGroupsFromAllContexts(org.olat.core.id.Identity)
	 */
	public List<BusinessGroup> getParticipatingRightGroupsFromAllContexts(Identity identity) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllRightGroupsFromAllContexts()
	 */
	public List<BusinessGroup> getAllRightGroupsFromAllContexts() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getAllAreasFromAllContexts()
	 */
	public List<BGArea> getAllAreasFromAllContexts() {
		return areas;
	}

	@Override
	public List<BGArea> getAreasFromContext(String areaname) {
		return null;
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#deleteCourseGroupmanagement()
	 */
	public void deleteCourseGroupmanagement() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getNumberOfMembersFromGroups(java.util.List)
	 */
	public List<Integer> getNumberOfMembersFromGroups(List<BusinessGroup> groupList) {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueLearningGroupNamesFromAllContexts()
	 */
	public List<String> getUniqueLearningGroupNamesFromAllContexts() {
		throw new AssertException("unsupported");
	}

	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getUniqueAreaNamesFromAllContexts()
	 */
	public List<String> getUniqueAreaNamesFromAllContexts() {
		throw new AssertException("unsupported");
	}
	
	/**
	 * @see org.olat.course.groupsandrights.CourseGroupManager#getLearningAreasOfGroupFromAllContexts(java.lang.String)
	 */
  public List<BGArea> getLearningAreasOfGroupFromAllContexts(String groupName) {
		throw new AssertException("unsupported");
  }

	public List<Identity> getCoachesFromLearningGroup(String groupName) {
		throw new AssertException("unsupported");
	}

	public List<Identity> getCoachesFromArea(String areaName) {
		throw new AssertException("unsupported");
	}

	public List<Identity> getParticipantsFromLearningGroup(String groupName) {
		throw new AssertException("unsupported");
	}

	public List<Identity> getParticipantsFromArea(String areaName) {
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

	public List<BusinessGroup> getRightGroupsFromAllContexts(String groupName) {
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

	public List<BusinessGroup> getWaitingListGroupsFromAllContexts(Identity identity) {
		throw new AssertException("unsupported");
	}

}