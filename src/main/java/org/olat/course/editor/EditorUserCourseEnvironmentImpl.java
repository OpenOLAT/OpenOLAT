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

import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.AssertException;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.resource.OLATResource;

/**
 * Description:<br>
 * TODO: patrick Class Description for EditorUserCourseEnvironment
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

	EditorUserCourseEnvironmentImpl(CourseEditorEnv courseEditorEnv, WindowControl windowControl) {
		this.courseEditorEnv = courseEditorEnv;
		this.windowControl = windowControl;
		ci = new ConditionInterpreter(this);
		courseEditorEnv.setConditionInterpreter(ci);
		sa = new ScoreAccounting(this);
	}
	
	/**
	 * @see org.olat.course.run.userview.UserCourseEnvironment#getCourseEnvironment()
	 */
	public CourseEnvironment getCourseEnvironment() {
		throw new AssertException("should never be called since it is the EDITOR user course environment");
	}

	/**
	 * @see org.olat.course.run.userview.UserCourseEnvironment#getCourseEditorEnv()
	 */
	public CourseEditorEnv getCourseEditorEnv() {
		return courseEditorEnv;
	}

	@Override
	public WindowControl getWindowControl() {
		return windowControl;
	}

	/**
	 * @see org.olat.course.run.userview.UserCourseEnvironment#getConditionInterpreter()
	 */
	public ConditionInterpreter getConditionInterpreter() {
		return ci;
	}

	/**
	 * @see org.olat.course.run.userview.UserCourseEnvironment#getIdentityEnvironment()
	 */
	public IdentityEnvironment getIdentityEnvironment() {
		throw new AssertException("should never be called since it is the EDITOR user course environment");
	}

	/**
	 * @see org.olat.course.run.userview.UserCourseEnvironment#getScoreAccounting()
	 */
	public ScoreAccounting getScoreAccounting() {
		return sa;
	}

	@Override
	public boolean isIdentityInCourseGroup(Long groupKey) {
		//TODO OO-502
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
	public boolean isParticipant() {
		return false;
	}

	@Override
	public boolean isAdminOfAnyCourse() {
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
	public boolean hasEfficiencyStatementOrCertificate(boolean update) {
		return false;
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
