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

import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Description:<br>
 * Function to get the begin date of the course
 * lifecycle.
 * <P>
 * Initial Date:  4.12.2013 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GetCourseBeginDateFunction extends AbstractFunction {

	public static final String name = "getCourseBeginDate";

	/**
	 * Default constructor to use the get initial enrollment date 
	 * @param userCourseEnv
	 */
	public GetCourseBeginDateFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	@Override
	public Object call(Object[] inStack) {
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if(cev != null) {
			return defaultValue();
		}

		RepositoryEntryLifecycle lifecycle = getUserCourseEnv().getLifecycle();
		if (lifecycle != null && lifecycle.getValidFrom() != null) {
			return Double.valueOf(lifecycle.getValidFrom().getTime());
		} else {
			// what to do in case of no date available??? -> return date in the future
			return new Double(Double.POSITIVE_INFINITY);
		}
	}

	@Override
	protected Object defaultValue() {
		return new Double(Double.MIN_VALUE);
	}
}