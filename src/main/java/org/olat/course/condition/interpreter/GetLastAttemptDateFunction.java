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
 * 04.10.2013 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.condition.interpreter;

import java.util.Date;

import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Description:<br>
 * Function to get the date of the last attempt a user tried to solve a test, do
 * a questionnaire and alike. Meaning may be different on the various course
 * building blocks.
 * <p>
 * 
 * @author gnaegi
 * 
 *         Initial Date: Oct 04, 2013
 */
public class GetLastAttemptDateFunction extends AbstractFunction {

	public static final String name = "getLastAttemptDate";

	public GetLastAttemptDateFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {
		/*
		 * argument check
		 */
		if (inStack.length > 1) {
			return handleException(new ArgumentParseException(
					ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "",
					"error.fewerargs", "solution.provideone.nodereference"));
		} else if (inStack.length < 1) {
			return handleException(new ArgumentParseException(
					ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
					"error.moreargs", "solution.provideone.nodereference"));
		}
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String))
			return handleException(new ArgumentParseException(
					ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
					"error.argtype.coursnodeidexpeted",
					"solution.example.node.infunction"));
		String nodeId = (String) inStack[0];
		/*
		 * check reference integrity
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			if (!cev.existsNode(nodeId)) {
				return handleException(new ArgumentParseException(
						ArgumentParseException.REFERENCE_NOT_FOUND, name,
						nodeId, "error.notfound.coursenodeid",
						"solution.copypastenodeid"));
			}
			// Remember the reference to the node id for this condition for cycle testing. 
			// Allow self-referencing but do not allow dependencies to parents as they create cycles.
			if (!nodeId.equals(cev.getCurrentCourseNodeId())) {
				cev.addSoftReference("courseNodeId", nodeId, false);				
			}
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}
		
		CourseNode node = getUserCourseEnv().getCourseEnvironment().getRunStructure().getNode(nodeId);
		if (node != null) {
			AssessmentEvaluation assessmentEvaluation = getUserCourseEnv().getScoreAccounting().evalCourseNode(node);
			Integer attempts = assessmentEvaluation.getAttempts();
			Date lastAttempt = assessmentEvaluation.getLastAttempt();
			if (attempts != null && attempts.intValue() > 0 && lastAttempt != null && lastAttempt.getTime() > 0) {
				return Double.valueOf(lastAttempt.getTime());
			}
		}
				
		return Double.POSITIVE_INFINITY; // date in future
	}

	@Override
	protected Object defaultValue() {
		return Double.MIN_VALUE;
	}

}