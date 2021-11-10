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
import org.olat.core.id.Identity;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.course.run.scoring.EndDateEvaluator;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 27. Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigEndDateEvaluator implements EndDateEvaluator {

	private LearningPathService learningPathService;
	private DueDateService dueDateService;

	@Override
	public Overridable<Date> getEndDate(AssessmentEvaluation currentEvaluation, CourseNode courseNode,
			RepositoryEntry courseEntry, Identity identity, Blocker blocker) {
		Overridable<Date> endDate = currentEvaluation.getEndDate().clone();
		if (AssessmentObligation.mandatory == currentEvaluation.getObligation().getCurrent()) {
			DueDateConfig endDateConfig = getLearningPathService().getConfigs(courseNode).getEndDateConfig();
			Date configEndDate = getDueDateService().getDueDate(endDateConfig, courseEntry, identity);
			if (configEndDate == null) {
				// If end date is deleted in config, it can not be overridden.
				endDate.reset();
			}
			endDate.setCurrent(configEndDate);
		} else {
			endDate = Overridable.empty();
		}
		evaluateBlocker(currentEvaluation.getFullyAssessed(), endDate.getCurrent(), blocker);
		return endDate;
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
	
	private LearningPathService getLearningPathService() {
		if (learningPathService == null) {
			learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
		}
		return learningPathService;
	}
	
	/**
	 * For Testing only!
	 *
	 * @param learningPathService
	 */
	protected void setLearningPathService(LearningPathService learningPathService) {
		this.learningPathService = learningPathService;
	}
	
	private DueDateService getDueDateService() {
		if (dueDateService == null) {
			dueDateService = CoreSpringFactory.getImpl(DueDateService.class);
		}
		return dueDateService;
	}
	
	/**
	 * For Testing only!
	 *
	 * @param dueDateService
	 */
	protected void setDueDateService(DueDateService dueDateService) {
		this.dueDateService = dueDateService;
	}

}
