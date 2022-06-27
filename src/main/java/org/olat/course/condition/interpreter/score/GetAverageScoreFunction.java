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
package org.olat.course.condition.interpreter.score;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.course.Structure;
import org.olat.course.condition.interpreter.AbstractFunction;
import org.olat.course.condition.interpreter.ArgumentParseException;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Initial date: 29 Jul 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GetAverageScoreFunction extends AbstractFunction {
	
	public static final String NAME = "getAverageScore";

	public GetAverageScoreFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}
	
	@Override
	public Object call(Object[] inStack) {
		if (inStack.length < 1) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, NAME, "",
					"error.moreargs", "solution.provideone.nodereference"));
		}
		
		for (Object object : inStack) {
			if (!(object instanceof String)) {
				return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT, NAME, "",
					"error.argtype.coursnodeidexpeted", "solution.example.node.infunction")); 
			}
		}
		
		List<String> childIds = Arrays.stream(inStack)
				.map(o -> (String) o)
				.collect(Collectors.toList());
		
		// Editor mode
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			for (String childId: childIds) {
				if (!cev.existsNode(childId)) {
					return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, NAME,
							childId, "error.notfound.coursenodeid", "solution.copypastenodeid"));
				}
				if (!cev.isAssessable(childId)) {
					return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, NAME,
							childId, "error.notassessable.coursenodid", "solution.takeassessablenode"));
				}
				
				// Remember the reference to the node id for this condition for cycle testing. 
				// Allow testing against own score (self-referencing) except for ST
				// course nodes as score is calculated on these node. Do not allow
				// dependencies to parents as they create cycles.
				if (!childId.equals(cev.getCurrentCourseNodeId()) || cev.getNode(cev.getCurrentCourseNodeId()) instanceof STCourseNode) {
					cev.addSoftReference("courseNodeId", childId, true);
				}
			}
			// return a valid value to continue with condition evaluation test
			return defaultValue();
		}
		
		Float score = evalAverageScore(childIds);
		return new Double(score);
	}
	
	/**
	 * Evaluate the average score of the course element. The method
	 * takes the visibility of the results in account.
	 * 
	 * @param childIds The specified course element idents
	 * @return A float (never null)
	 */
	private Float evalAverageScore(Collection<String> childIds) {
		ScoreAccounting sa = getUserCourseEnv().getScoreAccounting();
		Structure structure = getUserCourseEnv().getCourseEnvironment().getRunStructure();
		
		int count = 0;
		float sum = 0.0f;
		
		for (String childId : childIds) {
			CourseNode foundNode = structure.getNode(childId);
			Float score = null;
			if (foundNode != null) {
				ScoreEvaluation se = sa.evalCourseNode(foundNode);
				if(se != null) {
					// the node could not provide any sensible information on scoring. e.g. a STNode with no calculating rules
					if(se.getUserVisible() != null && se.getUserVisible().booleanValue()) {
						score = se.getScore();
						if (score != null) {
							count++;
							sum += score.floatValue();
						}
					}
				}
			}
		}
		
		// Calculate the average only if at least one score is available.
		return count > 0? Float.valueOf(sum / count): Float.valueOf(0.0f);
	}

	@Override
	protected Object defaultValue() {
		return new Double(Double.MIN_VALUE);
	}

}
