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
import org.olat.core.util.DateUtils;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.course.run.scoring.StartDateEvaluator;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 4 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigStartDateEvaluator implements StartDateEvaluator {
	
	private LearningPathService learningPathService;

	@Override
	public Date evaluate(AssessmentEvaluation currentEvaluation, CourseNode courseNode, Blocker blocker) {
		Date configStartDate = getLearningPathService().getConfigs(courseNode).getStartDate();
		AssessmentObligation obligation = currentEvaluation.getObligation().getCurrent();
		return evaluateDate(configStartDate, obligation, blocker);
	}

	Date evaluateDate(Date configStartDate, AssessmentObligation obligation, Blocker blocker) {
		Date now = new Date();
		Date later = blocker.getStartDate();
		if (configStartDate != null && configStartDate.after(now)) {
			later = DateUtils.getLater(configStartDate, blocker.getStartDate());
			if (AssessmentObligation.mandatory == obligation) {
				blocker.block(later);
			} else {
				blocker.blockNoPassThrough();
			}
		}
		return later;
	}
	
	private LearningPathService getLearningPathService() {
		if (learningPathService == null) {
			learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
		}
		return learningPathService;
	}

}
