/**
 *
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 *
 * Copyright (c) 2005-2008 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 *
 * All rights reserved.
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
