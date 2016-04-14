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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */

package org.olat.course.condition.interpreter;

import org.olat.core.CoreSpringFactory;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Description:<br>
 * Function to get the users recent launch date for this course.
 * If no launch has taken place so far, the date will have a future date
 * <P>
 * Initial Date:  12 jan. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com
 */
public class GetRecentCourseLaunchDateFunction extends AbstractFunction {

	public static final String name = "getRecentCourseLaunchDate";

	/**
	 * Default constructor to use the get initial enrollment date 
	 * @param userCourseEnv
	 */
	public GetRecentCourseLaunchDateFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	public Object call(Object[] inStack) {
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if(cev != null) {
			return defaultValue();
		}

		UserCourseInformationsManager mgr = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
		UserCourseInformations infos = mgr.getUserCourseInformations(getUserCourseEnv().getCourseEnvironment().getCourseGroupManager().getCourseResource(), getUserCourseEnv().getIdentityEnvironment().getIdentity());
		if (infos != null) {
			return Double.valueOf(infos.getRecentLaunch().getTime());
		} else {
			// what to do in case of no date available??? -> return date in the future
			return new Double(Double.POSITIVE_INFINITY);
		}
	}

	protected Object defaultValue() {
		return new Double(Double.MIN_VALUE);
	}
}