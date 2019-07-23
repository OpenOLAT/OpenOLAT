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
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.properties.Property;

/**
 * Description:<BR>
 * Function to get the users recent enrollment date for this course node.
 * If no enrollment has taken place so far, the date will have a future date
 * value.
 * <P>
 * Initial Date:  Oct 26, 2004
 * @author gnaegi
 */
public class GetRecentEnrollmentDateFunction extends AbstractFunction {

	public static final String name = "getRecentEnrollmentDate";

	/**
	 * Default constructor to use the get initial enrollment date 
	 * @param userCourseEnv
	 */
	public GetRecentEnrollmentDateFunction(UserCourseEnvironment userCourseEnv) {
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
					"solution.provideone.nodereference"));
		} else if (inStack.length < 1) { return handleException( new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
				"error.moreargs", "solution.provideone.nodereference")); }
		/*
		 * argument type check
		 */
		if (!(inStack[0] instanceof String)) return handleException( new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
				"error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
		String nodeId = (String) inStack[0];
		/*
		 * check reference integrity
		 */
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			if (!cev.existsNode(nodeId)) {
				return handleException( new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name, nodeId,
					"error.notfound.coursenodeid", "solution.copypastenodeid"));
			}
			if (!cev.isEnrollmentNode(nodeId)) {
				return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name,
					nodeId, "error.notenrollment.coursenodeid", "solution.chooseenrollment"));
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
		CoursePropertyManager pm = getUserCourseEnv().getCourseEnvironment().getCoursePropertyManager();
		Identity identity = getUserCourseEnv().getIdentityEnvironment().getIdentity();
		
		Property recentTime = pm.findCourseNodeProperty(node, identity, null, ENCourseNode.PROPERTY_RECENT_ENROLLMENT_DATE);

		if (recentTime != null) {
			String firstTimeMillis = recentTime.getStringValue();
			return Double.valueOf(firstTimeMillis);
		} else {
			// what to do in case of no date available??? -> return date in the future
			return Double.valueOf(Double.POSITIVE_INFINITY);
		}
	}

	@Override
	protected Object defaultValue() {
		return Double.valueOf(Double.MIN_VALUE);
	}

}