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
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * Description:<br>
 * @author Felix Jost
 */
public class GetScoreFunction extends AbstractFunction {

	public static final String name = "getScore";

	public GetScoreFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	@Override
	public Object call(Object[] inStack) {
		if (inStack.length > 1) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "",
					"error.fewerargs", "solution.provideone.nodereference"));
		} else if (inStack.length < 1) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name, "",
					"error.moreargs", "solution.provideone.nodereference"));
		}
		
		if (!(inStack[0] instanceof String)) {
			return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, name, "",
					"error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
		}
		
		String childId = (String) inStack[0];
		
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			if (!cev.existsNode(childId)) {
				return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name,
						childId, "error.notfound.coursenodeid", "solution.copypastenodeid"));
			}
			if (!cev.isAssessable(childId)) {
				return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name,
						childId, "error.notassessable.coursenodid", "solution.takeassessablenode"));
			}
			// Remember the reference to the node id for this condition for cycle testing.
			// Allow testing against own score (self-referencing) except for ST
			// course nodes as score is calculated on these node. Do not allow
			// dependencies to parents as they create cycles.
			if (!childId.equals(cev.getCurrentCourseNodeId())
					|| cev.getNode(cev.getCurrentCourseNodeId()) instanceof STCourseNode) {
				cev.addSoftReference("courseNodeId", childId, true);
			}
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}

		Float score = evalScoreOfCourseNode(childId);
		return Double.valueOf(score.doubleValue());
	}

	/**
	 * Evaluate the score of the course element. The method
	 * takes the visibility of the results in account and will
	 * return 0.0 if the results are not visible.
	 * 
	 * @param childId The specified course element ident
	 * @return A float (never null)
	 */
	private Float evalScoreOfCourseNode(String childId) {
		ScoreAccounting sa = getUserCourseEnv().getScoreAccounting();
		CourseNode foundNode = getUserCourseEnv().getCourseEnvironment().getRunStructure().getNode(childId);
		
		Float score = null;
		if (foundNode != null) {
			ScoreEvaluation se = sa.evalCourseNode(foundNode);
			if(se != null) {
				// the node could not provide any sensible information on scoring. e.g. a STNode with no calculating rules
				if(se.getUserVisible() != null && se.getUserVisible().booleanValue()) {
					score = se.getScore();
				} else {
					score = Float.valueOf(0.0f);
				}
			}
			if (score == null) { // a child has no score yet
				score = Float.valueOf(0.0f); // default to 0.0, so that the condition can be evaluated (zero points makes also the most sense for "no results yet", if to be expressed in a number)
			}
		} else {
			score = Float.valueOf(0.0f);
		}
		
		return score;
	}

	@Override
	protected Object defaultValue() {
		return Double.valueOf(Double.MIN_VALUE);
	}

}