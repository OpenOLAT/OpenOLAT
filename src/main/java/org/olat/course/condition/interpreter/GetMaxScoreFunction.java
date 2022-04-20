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
package org.olat.course.condition.interpreter;

import org.olat.core.CoreSpringFactory;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;

/**
 * 
 * Description:<br>
 * Function to get the max score of the current course node
 * <P>
 * Initial Date:  7.4.2022 <br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 */
public class GetMaxScoreFunction extends AbstractFunction {

	public static final String name = "getMaxScore";

	/**
	 * Default constructor to use the get initial enrollment date 
	 * @param userCourseEnv
	 */
	public GetMaxScoreFunction(UserCourseEnvironment userCourseEnv) {
		super(userCourseEnv);
	}

	/**
	 * @see com.neemsoft.jmep.FunctionCB#call(java.lang.Object[])
	 */
	@Override
	public Object call(Object[] inStack) {
		CourseNode node  = null;
		Double maxScore = null;
		
		// Check that we have exactly one argument, the enode ID
		if (inStack.length > 1) {
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_FEWER_ARGUMENTS, name, "", "error.fewerargs",
					"solution.provideone.nodereference"));
		} else if (inStack.length < 1) { 
			return handleException(new ArgumentParseException(ArgumentParseException.NEEDS_MORE_ARGUMENTS, name,
				"", "error.moreargs", "solution.provideone.nodereference")); 
		}

		// Check that thge arguement is syntactically a node ID
		if (!(inStack[0] instanceof String)) return handleException(new ArgumentParseException(ArgumentParseException.WRONG_ARGUMENT_FORMAT,
				name, "", "error.argtype.coursnodeidexpeted", "solution.example.node.infunction"));
		String nodeId = (String) inStack[0];
		nodeId.trim();

		// Lookup node from editor tree if in editor model...
		CourseEditorEnv cev = getUserCourseEnv().getCourseEditorEnv();
		if (cev != null) {
			if (!cev.existsNode(nodeId)) { 
				return handleException(new ArgumentParseException(ArgumentParseException.REFERENCE_NOT_FOUND, name,
					nodeId, "error.notfound.coursenodeid", "solution.copypastenodeid")); 
			}
			node = cev.getNode(nodeId);
		} else {
			// ... or from the run model
			node = getUserCourseEnv().getCourseEnvironment().getRunStructure().getNode(nodeId);			
		}
		
		// Now lookup max score for this course node
		if (node == null) {
			maxScore = defaultValue();
		} else {			
			// node is known, read max score form assessment config
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(new CourseEntryRef(getUserCourseEnv()), node);
			if (assessmentConfig == null) {
				// should never happen, but to be on the save side
				return defaultValue();			
			}
			// return 0 for course nodes without score
			if (Mode.none.equals(assessmentConfig.getScoreMode())) {
				maxScore = Double.valueOf(0f);
			} else {			
				Float maxConfig = assessmentConfig.getMaxScore();
				if (maxConfig == null) {
					// return infinite for course nodes with score but undefined max score
					maxScore = defaultValue();
				} else {
					maxScore = Double.valueOf(maxConfig.doubleValue());					
				}
			}
		}
		return Double.valueOf(maxScore);
	}

	@Override
	protected Double defaultValue() {
		// if no max value is defined the score can be anything
		return Double.POSITIVE_INFINITY;
	}
}