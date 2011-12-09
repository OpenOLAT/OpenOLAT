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

package org.olat.course.condition.interpreter;

import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

import com.neemsoft.jmep.FunctionCB;

/**
 * Initial Date: Feb 6, 2004
 * Description:<br>
 * the abstract class for all functions
 * 
 * @author Felix Jost
 */
public abstract class AbstractFunction extends FunctionCB{

	private UserCourseEnvironment userCourseEnv;

	/**
	 * @param userCourseEnv
	 */
	public AbstractFunction(UserCourseEnvironment userCourseEnv) {
		this.userCourseEnv = userCourseEnv;
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	public abstract Object call(Object[] inStack);

	/**
	 * @return user course environment
	 */
	public UserCourseEnvironment getUserCourseEnv() {
		return userCourseEnv;
	}

	/**
	 * the exception handle method for each function. It checks the condition
	 * interpreters mode, e.g. if it is used in the editor environment or in the
	 * run.
	 * 
	 * @param e
	 * @return default value of function
	 */
	protected Object handleException(Exception e){
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			cev.pushError(e);
		}
		return defaultValue();
	}
	
	protected abstract Object defaultValue();
}
