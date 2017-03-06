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

package org.olat.course.condition.interpreter.score;

import org.olat.core.CoreSpringFactory;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.condition.interpreter.AbstractFunction;
import org.olat.course.condition.interpreter.ArgumentParseException;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.model.RepositoryEntryRefImpl;

/**
 * Description:<br>
 * Get the passed of a node in using the efficiency statement from another course
 * 
 * <P>
 * Initial Date:  11.08.2005 <br>
 * @author gnaegi
 */
public class GetPassedWithCourseIdFunction extends AbstractFunction {
	public static final String name = "getPassedWithCourseId";

	/**
	 * Default constructor to use the current date
	 * 
	 * @param userCourseEnv
	 */
	public GetPassedWithCourseIdFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	public Object call(Object[] inStack) {
		/*
		 * argument check
		 */
		if (inStack.length > 2) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.provideone.nodereference"));
		} else if (inStack.length < 1) { return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
				"error.moreargs", "solution.provideone.nodereference")); }
		/*
		 * argument type check
		 */
		Long courseRepoEntryKey;
		try{
			Object arg = inStack[0];
			if(arg instanceof Number) {
				courseRepoEntryKey = new Long(((Number)arg).longValue());
			} else if(arg instanceof String) {
				courseRepoEntryKey = Long.decode((String)arg) ;
			} else {
				courseRepoEntryKey = null;
			}
		}catch(NumberFormatException nfe) {
			return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
					"error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
		}

		/*
		 * no integrity check can be done - other course might not exist anymore
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) { return defaultValue(); }

		/*
		 * the real function evaluation which is used during run time
		 */
		EfficiencyStatementManager esm = CoreSpringFactory.getImpl(EfficiencyStatementManager.class);
		RepositoryEntryRef courseRef = new RepositoryEntryRefImpl(courseRepoEntryKey);
		UserEfficiencyStatement es = esm.getUserEfficiencyStatementLightByRepositoryEntry(courseRef, getUserCourseEnv().getIdentityEnvironment().getIdentity());
		if (es == null) return defaultValue();
		Boolean passed = es.getPassed();
		if (passed == null) {
			return defaultValue();
		}
		// finally check existing value
		return passed.booleanValue() ? ConditionInterpreter.INT_TRUE : ConditionInterpreter.INT_FALSE;
		
	}

	/**
	 * @see org.olat.course.condition.interpreter.AbstractFunction#defaultValue()
	 */
	protected Object defaultValue() {
		return ConditionInterpreter.INT_FALSE;
	}

}