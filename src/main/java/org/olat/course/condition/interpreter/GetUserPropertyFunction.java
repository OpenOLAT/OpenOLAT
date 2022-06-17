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
import org.olat.core.id.User;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * <h3>Description:</h3>
 * The getUserProperty function provides access to the user properties. Eg. you
 * can ask if the user is from a certain city, has a certain firstname or for a
 * certain institutional id
 * <p>
 * Initial Date: 02.08.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class GetUserPropertyFunction extends AbstractFunction {

	public static final String name = "getUserProperty";

	public GetUserPropertyFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {
		/*
		 * argument check
		 */
		if (inStack.length > 1) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.providetwo.attrvalue"));
		} else if (inStack.length < 1) { 
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name,
				"", "error.moreargs", "solution.providetwo.attrvalue")); 
			}
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) { 
			return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT,
				name, "", "error.argtype.attributename", "solution.example.name.infunction")); 
		}
		
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			// return empty string to continue with condition evaluation test
			return defaultValue();
		}

		Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
		if(ident == null) {
			return defaultValue();
		}
		User user = ident.getUser();
		String propertyName = (String) inStack[0];
		
		String propertyValue = user.getProperty(propertyName, null); // always use default locale
		// Always return a non-null string. No need to distinguish between a user
		// set value "" and "" for a non existing value. This is such a theoretical
		// case that we just ignore it.
		return (propertyValue == null ? defaultValue() : propertyValue);
	}

	@Override
	protected Object defaultValue() {
		return "";
	}
	
}