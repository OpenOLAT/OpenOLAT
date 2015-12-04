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

package org.olat.course.condition;

import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.Structure;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.config.CourseConfig;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 * @deprecated
 */
public class NoOpCourseEnvironment implements CourseEnvironment {

	/**
	 * Default constructor for the No Op course environment. This is only used for
	 * for syntax validating
	 */
	public NoOpCourseEnvironment() {
	// nothig special to do
	}

	/**
	 * @see org.olat.course.run.environment.CourseEnvironment#getCurrentTimeMillis()
	 *      This is not implemented in NoOpCourseEnvironment
	 */

	public long getCurrentTimeMillis() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	/**
	 * @see org.olat.course.run.environment.CourseEnvironment#getCourseGroupManager()
	 */
	public CourseGroupManager getCourseGroupManager() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	/**
	 * @see org.olat.course.run.environment.CourseEnvironment#getCourseResourceableId()
	 *      This is not implemented in NoOpCourseEnvironment
	 */
	public Long getCourseResourceableId() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	/**
	 * @see org.olat.course.run.environment.CourseEnvironment#getCoursePropertyManager()
	 *      This is not implemented in NoOpCourseEnvironment
	 */
	public CoursePropertyManager getCoursePropertyManager() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	/**
	 * @see org.olat.course.run.environment.CourseEnvironment#getAssessmentManager()
	 */
	public AssessmentManager getAssessmentManager() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	/**
	 * @see org.olat.course.run.environment.CourseEnvironment#getAuditManager()
	 */
	public UserNodeAuditManager getAuditManager() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	/**
	 * @see org.olat.course.run.environment.CourseEnvironment#getRunStructure()
	 */
	public Structure getRunStructure() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	/**
	 * @see org.olat.course.run.environment.CourseEnvironment#getCourseTitle()
	 */
	public String getCourseTitle() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	public CourseConfig getCourseConfig() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	/**
	 * @see org.olat.course.run.environment.CourseEnvironment#setCourseConfig()
	 */
	public void setCourseConfig(CourseConfig cc) {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	public VFSContainer getCourseFolderContainer() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

	public OlatRootFolderImpl getCourseBaseContainer() {
		throw new UnsupportedOperationException("never to be called in No Op (syntax validating) mode");
	}

}
