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

package org.olat.course.editor;

import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.AssertException;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.NoEvaluationAccounting;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.CourseReadOnlyDetails;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;

/**
 * This is the user course environment implementation used
 * within the course editor.
 * 
 * <P>
 * Initial Date:  Jul 6, 2005 <br>
 * @author patrick
 */
public class EditorUserCourseEnvironmentImpl implements UserCourseEnvironment {

	private CourseEditorEnv courseEditorEnv;
	private ConditionInterpreter ci;
	private ScoreAccounting sa;
	private RepositoryEntryLifecycle lifecycle;
	private final WindowControl windowControl;
	private final CourseEnvironment courseEnvironment;

	EditorUserCourseEnvironmentImpl(CourseEditorEnv courseEditorEnv, WindowControl windowControl) {
		this.courseEditorEnv = courseEditorEnv;
		this.windowControl = windowControl;
		ci = new ConditionInterpreter(this);
		courseEditorEnv.setConditionInterpreter(ci);
		sa = new NoEvaluationAccounting();
		courseEnvironment = new EditorCourseEnvironmentImpl(courseEditorEnv);
	}

	@Override
	public CourseEnvironment getCourseEnvironment() {
		return courseEnvironment;
	}

	@Override
	public CourseEditorEnv getCourseEditorEnv() {
		return courseEditorEnv;
	}

	@Override
	public WindowControl getWindowControl() {
		return windowControl;
	}

	@Override
	public ConditionInterpreter getConditionInterpreter() {
		return ci;
	}

	@Override
	public IdentityEnvironment getIdentityEnvironment() {
		throw new AssertException("should never be called since it is the EDITOR user course environment");
	}

	@Override
	public ScoreAccounting getScoreAccounting() {
		return sa;
	}

	@Override
	public boolean isIdentityInCourseGroup(Long groupKey) {
		return false;
	}

	@Override
	public boolean isInOrganisation(String organisationIdentifier, OrganisationRoles... roles) {
		return false;
	}

	@Override
	public boolean isAdmin() {
		return false;
	}

	@Override
	public boolean isCoach() {
		return false;
	}

	@Override
	public List<BusinessGroup> getParticipatingGroups() {
		return Collections.emptyList();
	}

	@Override
	public List<BusinessGroup> getWaitingLists() {
		return Collections.emptyList();
	}

	@Override
	public List<BusinessGroup> getCoachedGroups() {
		return Collections.emptyList();
	}

	@Override
	public List<CurriculumElement> getCoachedCurriculumElements() {
		return Collections.emptyList();
	}

	@Override
	public boolean isParticipant() {
		return false;
	}

	@Override
	public boolean isMemberParticipant() {
		return false;
	}

	@Override
	public boolean isAdministratorOfAnyCourse() {
		return false;
	}

	@Override
	public boolean isCoachOfAnyCourse() {
		return false;
	}

	@Override
	public boolean isParticipantOfAnyCourse() {
		return false;
	}

	@Override
	public boolean isCourseReadOnly() {
		return false;
	}
	
	@Override
	public CourseReadOnlyDetails getCourseReadOnlyDetails() {
		return new CourseReadOnlyDetails(Boolean.FALSE, Boolean.FALSE);
	}

	@Override
	public boolean hasEfficiencyStatementOrCertificate(boolean update) {
		return false;
	}

	@Override
	public List<String> getUsernames() {
		return List.of();
	}

	@Override
	public RepositoryEntryLifecycle getLifecycle() {
		if(lifecycle == null) {
			CourseGroupManager cgm = courseEditorEnv.getCourseGroupManager();
			OLATResource courseResource = cgm.getCourseResource();
			RepositoryEntry re = RepositoryManager.getInstance().lookupRepositoryEntry(courseResource, false);
			if(re != null) {
				lifecycle = re.getLifecycle();
			}
		}
		return lifecycle;
	}
}
