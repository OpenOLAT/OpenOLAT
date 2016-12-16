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

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.area.BGAreaManager;

/**
 * Initial Date:  Jun 16, 2004
 * @author gnaegi
 */
public class InLearningAreaFunction extends AbstractFunction {
	
	public static final String name = "inLearningArea";

	/**
	 * @param userCourseEnv
	 */
	public InLearningAreaFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	public Object call(Object[] inStack) {
		/*
		 * argument check
		 */
		if (inStack.length > 1) {
			return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.provideone.areaname"));
		} else if (inStack.length < 1) { return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
				"error.moreargs", "solution.provideone.areaname")); }
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
				"error.argtype.areanameexpected", "solution.example.name.infunction"));
		String areaName = (String)inStack[0];
		areaName = areaName != null ? areaName.trim() : areaName;
		/*
		 * check reference integrity
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			if (!cev.existsArea(areaName)) { 
				return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name, areaName,
						"error.notfound.name", "solution.checkgroupmanagement"));
			}
			// remember the reference to the node id for this condtion
			cev.addSoftReference("areaId", areaName, false);
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}

		//The real function evaluation which is used during run time
		Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
		CourseGroupManager cgm = getUserCourseEnv().getCourseEnvironment().getCourseGroupManager();
		if(StringHelper.isLong(areaName)) {
			Long areaKey = new Long(areaName);
			return cgm.isIdentityInLearningArea(ident,areaKey) ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
		}
		
		List<Long> areaKeys = CoreSpringFactory.getImpl(BGAreaManager.class).toAreaKeys(areaName, cgm.getCourseResource());
		if(!areaKeys.isEmpty()) {
			return cgm.isIdentityInLearningArea(ident, areaKeys.get(0)) ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
		}
		return ConditionInterpreter.INT_FALSE;
	}

	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}
}
