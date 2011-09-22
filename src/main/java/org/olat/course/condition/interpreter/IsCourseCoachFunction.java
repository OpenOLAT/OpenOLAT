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
* <p>
*/ 

package org.olat.course.condition.interpreter;

import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Initial Date:  Jun 16, 2004
 * @author Felix Jost
 * Description: A user is course coach if he/she is in at least one owner group of a learning group of the learning group context of the course
 */
public class IsCourseCoachFunction extends AbstractFunction {
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
	public Object call(Object[] inStack) {
		/*
		 * expression check only if cev != null
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}

		Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
		CourseGroupManager cgm = getUserCourseEnv().getCourseEnvironment().getCourseGroupManager();
		boolean isCourseCoach = cgm.isIdentityCourseCoach(ident);
		if (Tracing.isDebugEnabled(IsCourseCoachFunction.class)) {
			Tracing.logDebug("identity "+ident.getName()+", coursecoach:"+isCourseCoach+", in course "+getUserCourseEnv().getCourseEnvironment().getCourseResourceableId(), IsCourseCoachFunction.class);
		}
		
		return isCourseCoach ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
	}

	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}

}
