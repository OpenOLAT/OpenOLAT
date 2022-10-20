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
package org.olat.course.nodes.st.assessment;

import org.olat.core.util.StringHelper;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.run.scoring.ScoreEvaluator;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 20 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConditionScoreEvaluator implements ScoreEvaluator {

	@Override
	public Float getScore(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			ScoreAccounting scoreAccounting, RepositoryEntryRef courseEntry, ConditionInterpreter conditionInterpreter) {
		ScoreCalculator scoreCalculator = getScoreCalculator(courseNode);
		if (scoreCalculator != null) {
			String scoreExpression = scoreCalculator.getScoreExpression();
			if (StringHelper.containsNonWhitespace(scoreExpression)) {
				float val = conditionInterpreter.evaluateCalculation(scoreExpression);
				return Float.isNaN(val) ? null : Float.valueOf(val);
			}
		}
		return null;
	}
	
	private ScoreCalculator getScoreCalculator(CourseNode courseNode) {
		if (courseNode instanceof STCourseNode) {
			STCourseNode stCourseNode = (STCourseNode) courseNode;
			return stCourseNode.getScoreCalculator();
		}
		return null;
	}

}
