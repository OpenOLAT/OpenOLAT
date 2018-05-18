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

import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Initial Date:  Jun 16, 2004
 * @author Felix Jost
 * Description: A user is course coach if he/she is in at least one owner group of a learning group of the learning group context of the course
 */
public class IsCourseCoachFunction extends AbstractFunction {
	private static final OLog log = Tracing.createLoggerFor(IsCourseCoachFunction.class);
	public static final String name = "isCourseCoach";
	
	/**
	 * @param userCourseEnv
	 */
	public IsCourseCoachFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	@Override
	public Object call(Object[] inStack) {
		/*
		 * expression check only if cev != null
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}
		boolean isCourseCoach;
		if(inStack != null && inStack.length > 0
				&& inStack[0] instanceof String
				&& AnyCourseVariable.name.equalsIgnoreCase((String)inStack[0])) {
			//administrator of any course
			isCourseCoach = getUserCourseEnv().isCoachOfAnyCourse();
		} else {
			isCourseCoach = getUserCourseEnv().isCoach();
		}
		if (log.isDebug()) {
			Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
			log.debug("identity "+ident.getKey()+", coursecoach:"+isCourseCoach+", in course "+getUserCourseEnv().getCourseEnvironment().getCourseResourceableId());
		}
		return isCourseCoach ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}
}
