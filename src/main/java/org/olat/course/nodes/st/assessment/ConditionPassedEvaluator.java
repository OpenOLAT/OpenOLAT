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

import java.util.Date;

import org.hibernate.LazyInitializationException;
import org.olat.core.CoreSpringFactory;
import org.olat.core.util.StringHelper;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.FailedEvaluationType;
import org.olat.course.run.scoring.PassedEvaluator;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.modules.assessment.Overridable;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryLifecycle;

/**
 * 
 * Initial date: 20 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConditionPassedEvaluator implements PassedEvaluator {

	@Override
	public Overridable<Boolean> getPassed(AssessmentEvaluation currentEvaluation, CourseNode courseNode, RepositoryEntry courseEntry,
			ConditionInterpreter conditionInterpreter) {
		Boolean passed = null;
		ScoreCalculator scoreCalculator = getScoreCalculator(courseNode);
		if (scoreCalculator != null) {
			String passedExpression = scoreCalculator.getPassedExpression();
			if (StringHelper.containsNonWhitespace(passedExpression)) {
				boolean hasPassed = conditionInterpreter.evaluateCondition(passedExpression);
				if(hasPassed) {
					passed = Boolean.TRUE;
				} else {
					//some rules to set -> failed
					FailedEvaluationType failedType = scoreCalculator.getFailedType();
					if(failedType == null || failedType == FailedEvaluationType.failedAsNotPassed) {
						passed = Boolean.FALSE;
					} else if(failedType == FailedEvaluationType.failedAsNotPassedAfterEndDate) {
						RepositoryEntryLifecycle lifecycle = getRepositoryEntryLifecycle(courseEntry);
						if(lifecycle != null && lifecycle.getValidTo() != null && lifecycle.getValidTo().compareTo(new Date()) < 0) {
							passed = Boolean.FALSE;
						}
					}
				}
			}
		}
		return Overridable.of(passed);
	}
	
	private ScoreCalculator getScoreCalculator(CourseNode courseNode) {
		if (courseNode instanceof STCourseNode) {
			STCourseNode stCourseNode = (STCourseNode) courseNode;
			return stCourseNode.getScoreCalculator();
		}
		return null;
	}
	
	private RepositoryEntryLifecycle getRepositoryEntryLifecycle(RepositoryEntry courseEntry) {
		try {
			RepositoryEntryLifecycle lifecycle = courseEntry.getLifecycle();
			if(lifecycle != null) {
				lifecycle.getValidTo();
			}
			return lifecycle;
		} catch (LazyInitializationException e) {
			RepositoryEntry reloadedEntry = CoreSpringFactory.getImpl(RepositoryService.class)
					.loadByKey(courseEntry.getKey());
			return reloadedEntry.getLifecycle();
		}
	}

}
