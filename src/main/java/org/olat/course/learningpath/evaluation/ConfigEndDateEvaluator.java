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
import java.util.function.Function;

import org.olat.core.CoreSpringFactory;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.Blocker;
import org.olat.course.run.scoring.EndDateEvaluator;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 27. Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConfigEndDateEvaluator implements EndDateEvaluator {

	/* Service is wrapped with a function to 
	 * - use the class as a static member (Service can not be autowired)
	 * - mock the service in tests
	 */
	private final Function<CourseNode, Date> configDateFunction;
	
	public ConfigEndDateEvaluator() {
		this.configDateFunction = new LearningPathServiceSupplier();
	}
	
	ConfigEndDateEvaluator(Function<CourseNode, Date> configDateFunction) {
		this.configDateFunction = configDateFunction;
	}

	@Override
	public Overridable<Date> getEndDate(AssessmentEvaluation currentEvaluation, CourseNode courseNode, Blocker blocker) {
		Overridable<Date> endDate = currentEvaluation.getEndDate().clone();
		if (AssessmentObligation.mandatory == currentEvaluation.getObligation().getCurrent()) {
			Date configEndDate = configDateFunction.apply(courseNode);
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
	
	private static final class LearningPathServiceSupplier implements Function<CourseNode, Date> {
		
		private LearningPathService learningPathService;

		@Override
		public Date apply(CourseNode courseNode) {
			if (learningPathService == null) {
				learningPathService = CoreSpringFactory.getImpl(LearningPathService.class);
			}
			return learningPathService.getConfigs(courseNode).getEndDate();
		}

	

	}


}
