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
package org.olat.course.learningpath.evaluation;

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.course.run.scoring.EndDateEvaluator;

/**
 * 
 * Initial date: 27. Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigEndDateEvaluator implements EndDateEvaluator {

	@Override
	public Date getEndDate(AssessmentEvaluation currentEvaluation, CourseNode courseNode, Blocker blocker) {
		Date configEndDate = getConfigEndDate(courseNode);
		evaluateBlocker(currentEvaluation.getFullyAssessed(), configEndDate, blocker);
		return configEndDate;
	}

	private Date getConfigEndDate(CourseNode courseNode) {
		LearningPathService learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
		return learningPathService.getConfigs(courseNode).getEndDate();
	}

	void evaluateBlocker(Boolean fullyAssessed, Date configEndDate, Blocker blocker) {
		Date now = new Date();
		if (configEndDate != null && configEndDate.before(now) && isNotFullyAssessed(fullyAssessed)) {
			blocker.block();
		}
	}

	private boolean isNotFullyAssessed(Boolean fullyAssessed) {
		return fullyAssessed == null || !fullyAssessed.booleanValue();
	}

}
