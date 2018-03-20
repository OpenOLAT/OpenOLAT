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

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 20 mars 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IsInOrganisationFunction extends AbstractFunction {
	public static final String name = "isInOrganisation";

	/**
	 * Constructor
	 * @param userCourseEnv
	 */
	public IsInOrganisationFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

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
		
		boolean isInOrganisation = false;
		if(inStack != null && inStack.length > 1 && inStack[0] instanceof String && inStack[1] instanceof String) {
			OrganisationRoles[] roles;
			if("*".equals(inStack[1])) {
				roles = OrganisationRoles.values();
			} else {
				List<OrganisationRoles> roleList = new ArrayList<>(inStack.length);
				for(int i=1; i<inStack.length; i++) {
					if(OrganisationRoles.valid((String)inStack[i])) {
						roleList.add(OrganisationRoles.valueOf((String)inStack[i]));
					}
				}
				roles = roleList.toArray(new OrganisationRoles[roleList.size()]);
			}
			isInOrganisation = getUserCourseEnv().isInOrganisation((String)inStack[0], roles);
		}
		return isInOrganisation ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}
}
