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

import org.olat.course.condition.interpreter.AbstractFunction;
import org.olat.course.condition.interpreter.ArgumentParseException;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Description:<br>
 * 
 * @author Felix Jost
 */
public class GetPassedFunction extends AbstractFunction {
	public static final String name = "getPassed";

	/**
	 * Default constructor to use the current date
	 * 
	 * @param userCourseEnv
	 */
	public GetPassedFunction(UserCourseEnvironment userCourseEnv) {
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
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.provideone.nodereference"));
		} else if (inStack.length < 1) { return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
				"error.moreargs", "solution.provideone.nodereference")); }
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
				"error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
		String childId = (String) inStack[0];
		/*
		 * check reference integrity
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			if (!cev.existsNode(childId)) { return handleException( new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name, childId,
					"error.notfound.coursenodeid", "solution.copypastenodeid")); }
			if (!cev.isAssessable(childId)) { return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name,
					childId, "error.notassessable.coursenodid", "solution.takeassessablenode")); }
			// Remember the reference to the node id for this condition for cycle testing. 
			// Allow testing against own passed (self-referencing) except for ST
			// course nodes as passed is calculated on these node. Do not allow
			// dependencies to parents as they create cycles.
			if (!childId.equals(cev.getCurrentCourseNodeId()) || cev.getNode(cev.getCurrentCourseNodeId()) instanceof STCourseNode) {
				cev.addSoftReference("courseNodeId", childId, true);				
			}
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}

		/*
		 * the real function evaluation which is used during run time
		 */
		ScoreAccounting sa = getUserCourseEnv().getScoreAccounting();
		Boolean passed = sa.evalPassedOfCourseNode(childId);
		return (passed.booleanValue() ? ConditionInterpreter.INT_TRUE : ConditionInterpreter.INT_FALSE);
	}

	protected Object defaultValue() {
		return ConditionInterpreter.INT_FALSE;
	}

}