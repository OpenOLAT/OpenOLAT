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

import org.olat.core.id.Identity;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Description:<BR/>
 * Condition function isUser()
 
 * Initial Date:  Sep 15, 2004
 * @author gnaegi
 */
public class IsUserFunction extends AbstractFunction {
	
	public static final String name = "isUser";

	/**
	 * @param userCourseEnv
	 */
	public IsUserFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	@Override
	public Object call(Object[] inStack) {/*
		 * argument check
		 */
		if (inStack.length > 1) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.provideone.username"));
		} else if (inStack.length < 1) { return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
				"error.moreargs", "solution.provideone.username")); }
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
				"error.argtype.username", "solution.example.name.infunction"));
		String userName = (String)inStack[0];
		/*
		 * expression check only if cev != null
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}
		
		Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
		if(ident == null || ident.getName() == null || userName == null) {
			return ConditionInterpreter.INT_FALSE;
		}
		// compare using lower case as done in login process
		
		if(userName.equalsIgnoreCase(ident.getName()) || userName.equalsIgnoreCase(ident.getUser().getNickName())) {
			return ConditionInterpreter.INT_TRUE;
		}
		
		List<String> usernames = getUserCourseEnv().getUsernames();
		for(String uname:usernames) {
			if(userName.equalsIgnoreCase(uname)) {
				return ConditionInterpreter.INT_TRUE;
			}
		}
		return ConditionInterpreter.INT_FALSE;
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}
}