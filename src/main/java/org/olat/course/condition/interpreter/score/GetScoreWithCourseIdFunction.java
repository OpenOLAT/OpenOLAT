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
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.model.RepositoryEntryRefImpl;

/**
 * Description:<br>
 * Get the score of a node in using the efficiency statement from another course
 * Initial Date:  11.08.2005 <br>
 * @author gnaegi
 */
public class GetScoreWithCourseIdFunction extends AbstractFunction {
	public static final String name = "getScoreWithCourseId";

	/**
	 * Default constructor to use the current date
	 * 
	 * @param userCourseEnv
	 */
	public GetScoreWithCourseIdFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	public Object call(Object[] inStack) {
		if (inStack.length > 2) {//need > 2 for compatibility reason
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.provideone.nodereference"));
		} else if (inStack.length < 1) { return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
				"error.moreargs", "solution.provideone.nodereference")); }

		Long courseRepoEntryKey;
		try{
			courseRepoEntryKey = Long.decode((String) inStack[0]) ;
		} catch(NumberFormatException nfe) {
			return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
					"error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
		}

		// no integrity check can be done - other course might not exist anymore
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) { return defaultValue(); }

		// the real function evaluation which is used during run time
		EfficiencyStatementManager esm = CoreSpringFactory.getImpl(EfficiencyStatementManager.class);
		RepositoryEntryRef courseRef = new RepositoryEntryRefImpl(courseRepoEntryKey);
		UserEfficiencyStatement es = esm.getUserEfficiencyStatementLightByRepositoryEntry(courseRef, getUserCourseEnv().getIdentityEnvironment().getIdentity());
		if (es == null) return defaultValue();
		Float score = es.getScore();
		if (score == null) return defaultValue();
		// finally check existing value
		return new Double(score.doubleValue());
		
	}

	/**
	 * @see org.olat.course.condition.interpreter.AbstractFunction#defaultValue()
	 */
	protected Object defaultValue() {
		return new Double(Double.MIN_VALUE);
	}

}