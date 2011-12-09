/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.onyx.plugin.run;

import org.olat.core.id.Identity;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * @author Ingmar Kroll
 */
public class OnyxSession {
	private String assessmenttype;
	private CourseNode node;
	private Identity identity;
	private UserCourseEnvironment userCourseEnvironment;

	/**
	 * @return Returns the userCourseEnvironment.
	 */
	public UserCourseEnvironment getUserCourseEnvironment() {
		return userCourseEnvironment;
	}

	/**
	 * @param userCourseEnvironment The userCourseEnvironment to set.
	 */
	public void setUserCourseEnvironment(UserCourseEnvironment userCourseEnvironment) {
		this.userCourseEnvironment = userCourseEnvironment;
	}

	/**
	 * @return Returns the identity.
	 */
	public Identity getIdentity() {
		return identity;
	}

	/**
	 * The Identity of the user who attends the test.
	 * @param identity The identity to set.
	 */
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}

	public void setAssessmenttype(String assessmenttype) {
		this.assessmenttype = assessmenttype;
	}

	public String getAssessmenttype() {
		return assessmenttype;
	}

	public void setNode(CourseNode node) {
		this.node = node;
	}

	public CourseNode getNode() {
		return node;
	}

}
