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

import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.CourseNode;
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

	/**
	 * Default constructor to use the get attempts object
	 * 
	 * @param userCourseEnv
	 */
	public GetLastAttemptDateFunction(UserCourseEnvironment userCourseEnv) {
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

		/*
		 * the real function evaluation which is used during run time
		 */
		CourseNode node = getUserCourseEnv().getCourseEnvironment().getRunStructure().getNode(nodeId);
		AssessmentManager am = getUserCourseEnv().getCourseEnvironment().getAssessmentManager();
		Identity identity = getUserCourseEnv().getIdentityEnvironment().getIdentity();

		// use the last modified date from the course node
		Date lastModified = am.getScoreLastModifiedDate(node, identity);

	    if (lastModified != null && lastModified.getTime() > 0) {
	    	return Double.valueOf(lastModified.getTime());
	    } else {
	    	// what to do in case of no date available??? -> return date in the future
	    	return new Double(Double.POSITIVE_INFINITY);
	    }
	}

	/**
	 * @see org.olat.course.condition.interpreter.AbstractFunction#defaultValue()
	 */
	protected Object defaultValue() {
		return new Double(Double.MIN_VALUE);
	}

}