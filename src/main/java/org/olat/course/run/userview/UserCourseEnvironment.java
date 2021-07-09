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

package org.olat.course.run.userview;

import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.IdentityEnvironment;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * @author Felix Jost
 *
 */
public interface UserCourseEnvironment {
	/**
	 * @return Returns the courseEnvironment.
	 */
	public CourseEnvironment getCourseEnvironment();
	/**
	 * 
	 * @return returns a view to the course in the editor
	 */
	public CourseEditorEnv getCourseEditorEnv();
	
	public ConditionInterpreter getConditionInterpreter();
	
	public IdentityEnvironment getIdentityEnvironment();
	
	/**
	 * Return a value only if the user has opened the course in
	 * the GUI. Return null otherwise.
	 * @return
	 */
	public WindowControl getWindowControl();
	
	public ScoreAccounting getScoreAccounting();
	
	/**
	 * Is admin of a course an administrator, learn resource manager or principal
	 * of an organization linked to the course or an owner of the course.
	 * @return
	 */
	public boolean isAdmin();
	
	/**
	 * Is a coach of the course, within the course as in relation of a group.
	 * @return
	 */
	public boolean isCoach();

	/**
	 * Is a participant of the course, within the course as in relation of a group.
	 * Users in a open course are participants as well.
	 * 
	 * @return
	 */
	public boolean isParticipant();
	
	/**
	 * Is the identity a member with the role participant of the course?
	 * 
	 * @return
	 */
	public boolean isMemberParticipant();
	
	public boolean isIdentityInCourseGroup(Long groupKey);
	
	
	public boolean isInOrganisation(String organisationIdentifier, OrganisationRoles... roles);
	
	public List<BusinessGroup>  getParticipatingGroups();
	
	public List<BusinessGroup>  getWaitingLists();
	
	public List<BusinessGroup> getCoachedGroups();
	
	public List<CurriculumElement> getCoachedCurriculumElements();
	
	
	/**
	 * Is administrator of some courses (as owner, OpenOLAT administrator or institutional resource manager).
	 * @return
	 */
	public boolean isAdministratorOfAnyCourse();
	
	/**
	 * Is coach of some course.
	 * @return
	 */
	public boolean isCoachOfAnyCourse();

	/**
	 * Is participant of some course.
	 * @return
	 */
	public boolean isParticipantOfAnyCourse();
	
	
	public RepositoryEntryLifecycle getLifecycle();
	
	public boolean isCourseReadOnly();
	
	public CourseReadOnlyDetails getCourseReadOnlyDetails();
	
	/**
	 * Check if the user has an efficiency statement or a certificate. The method
	 * doesn't check if the efficiency statement or the certificate are configured
	 * for the course. It's a database check only.
	 * 
	 * @param update
	 * @return
	 */
	public boolean hasEfficiencyStatementOrCertificate(boolean update);
	
	/**
	 * 
	 * @return
	 */
	public List<String> getUsernames();
	

}