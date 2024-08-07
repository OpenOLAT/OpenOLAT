/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.condition.interpreter.score;

import java.util.ArrayList;
import java.util.List;

import org.olat.course.condition.interpreter.AbstractFunction;
import org.olat.course.condition.interpreter.ArgumentParseException;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * getPassedNodes("node1","node2","node3", "2"), the last one is the number of course
 * elements which need to be passed.
 * 
 * Initial date: 7 août 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GetPassedNodesFunction extends AbstractFunction {
	public static final String name = "getPassedNodes";

	public GetPassedNodesFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {
		if (inStack.length < 1) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
					"error.moreargs", "solution.provideone.nodereference"));
		}
		
		if (!(inStack[0] instanceof String)) {
			return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
					"error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
		}
		
		List<String> childIds = new ArrayList<>();
		for(int i=0; i<inStack.length - 1; i++) {
			childIds.add((String)inStack[i]);
		}
		
		long numOfNodesToPass = Long.decode((String)inStack[inStack.length - 1]) ;
		
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			for(String childId : childIds) {
				if (!cev.existsNode(childId)) {
					return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name,
							childId, "error.notfound.coursenodeid", "solution.copypastenodeid"));
				}
				if (!cev.isAssessable(childId)) {
					return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name,
							childId, "error.notassessable.coursenodid", "solution.takeassessablenode"));
				}
				// Remember the reference to the node id for this condition for cycle testing.
				// Allow testing against own passed (self-referencing) except for ST
				// course nodes as passed is calculated on these node. Do not allow
				// dependencies to parents as they create cycles.
				if (!childId.equals(cev.getCurrentCourseNodeId())
						|| cev.getNode(cev.getCurrentCourseNodeId()) instanceof STCourseNode) {
					cev.addSoftReference("courseNodeId", childId, true);
				}
				// return a valid value to continue with condition evaluation test
				return defaultValue();
			}
		}

		long nodesPassed = 0l;
		for(String childId : childIds) {
			Boolean passed = evalPassedOfCourseNode(childId);
			if(passed.booleanValue()) {
				nodesPassed++;
			}
		}
		return (nodesPassed >= numOfNodesToPass ? ConditionInterpreter.INT_TRUE : ConditionInterpreter.INT_FALSE);
	}
	
	/**
	 * Evaluate the passed / failed state of a course element. The method
	 * takes the visibility of the results in account and will return false
	 * if the results are not visible.
	 * 
	 * @param childId The specified course element ident
	 * @return true/false never null
	 */
	private Boolean evalPassedOfCourseNode(String childId) {
		ScoreAccounting sa = getUserCourseEnv().getScoreAccounting();
		CourseNode foundNode = getUserCourseEnv().getCourseEnvironment().getRunStructure().getNode(childId);
		
		if (foundNode == null) {
			return Boolean.FALSE;
		}
		ScoreEvaluation se = sa.evalCourseNode(foundNode);
		if (se == null) {
			return Boolean.FALSE;
		}
		// check if the results are visible
		if(se.getUserVisible() == null || !se.getUserVisible().booleanValue()) {
			return Boolean.FALSE;
		}
		Boolean passed = se.getPassed();
		if (passed == null) { // a child has no "Passed" yet
			passed = Boolean.FALSE;
		}
		return passed;
	}

	@Override
	protected Object defaultValue() {
		return ConditionInterpreter.INT_FALSE;
	}

}