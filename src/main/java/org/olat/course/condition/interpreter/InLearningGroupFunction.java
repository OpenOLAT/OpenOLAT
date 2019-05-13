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
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;

/**
 * @author Felix Jost
 *
 */
public class InLearningGroupFunction extends AbstractFunction {
	
	private static final Logger log = Tracing.createLoggerFor(InLearningGroupFunction.class);
	
	public final String name;

	/**
	 * @param userCourseEnv
	 */
	public InLearningGroupFunction(UserCourseEnvironment userCourseEnv, String fnName) {
		super(userCourseEnv);
		this.name = fnName;
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	@Override
	public Object call(Object[] inStack) {		/*
		 * argument check
		 */
		if (inStack.length > 1) {
			return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.provideone.groupname"));
		} else if (inStack.length < 1) { return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
				"error.moreargs", "solution.provideone.groupname")); }
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
				"error.argtype.groupnameexpected", "solution.example.name.infunction"));
		String groupName = (String)inStack[0];
		groupName = groupName != null ? groupName.trim() : null;
		/*
		 * check reference integrity
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			if (!cev.existsGroup(groupName)) { return handleException( new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name, groupName,
					"error.notfound.name", "solution.checkgroupmanagement")); }
			// remember the reference to the node id for this condtion
			cev.addSoftReference("groupId", groupName, false);
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}

		//the real function evaluation which is used during run time
		if(StringHelper.isLong(groupName)) {
			try {
				Long groupKey = Long.parseLong(groupName);
				return getUserCourseEnv().isIdentityInCourseGroup(groupKey) ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
			} catch (NumberFormatException e) {
				log.error("" + groupName, e);
			}
		}

		CourseGroupManager cgm = getUserCourseEnv().getCourseEnvironment().getCourseGroupManager();
		List<Long> groupKeys = CoreSpringFactory.getImpl(BusinessGroupService.class).toGroupKeys(groupName, cgm.getCourseEntry());
		if(!groupKeys.isEmpty()) {
			Identity ident = getUserCourseEnv().getIdentityEnvironment().getIdentity();
			return cgm.isIdentityInGroup(ident, groupKeys.get(0)) ? ConditionInterpreter.INT_TRUE: ConditionInterpreter.INT_FALSE;
		}
		return ConditionInterpreter.INT_FALSE;
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_TRUE;
	}
}
