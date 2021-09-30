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
package org.olat.course.run.scoring;

import org.olat.course.nodes.CourseNode;

/**
 * This class does nothing. It is supposed to make sure that course member with
 * (currently) no participant role does not load the assessment entries.
 * 
 * Initial date: 8 Oct 2019<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NoEvaluationAccounting implements ScoreAccounting {

	@Override
	public void setObligationContext(ObligationContext obligationContext) {
		//
	}
	
	@Override
	public void evaluateAll() {
		//
	}

	@Override
	public boolean evaluateAll(boolean update) {
		return false;
	}

	@Override
	public AssessmentEvaluation getScoreEvaluation(CourseNode courseNode) {
		return AssessmentEvaluation.EMPTY_EVAL;
	}

	@Override
	public AssessmentEvaluation evalCourseNode(CourseNode courseNode) {
		return AssessmentEvaluation.EMPTY_EVAL;
	}

}
