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

import org.olat.course.run.scoring.AccountingEvaluators;
import org.olat.course.run.scoring.AccountingEvaluatorsBuilder;
import org.olat.course.run.scoring.EndDateEvaluator;
import org.olat.course.run.scoring.StartDateEvaluator;
import org.olat.course.run.scoring.StatusEvaluator;
import org.olat.modules.assessment.model.AssessmentEntryStatus;

/**
 * 
 * Initial date: 17 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathEvaluatorBuilder {
	
	private static final StartDateEvaluator CONFIG_START_DATE_EVALUATOR = new ConfigStartDateEvaluator();
	private static final EndDateEvaluator CONFIG_END_DATE_EVALUATOR = new ConfigEndDateEvaluator();
	private static final ConfigObligationEvaluator CONFIG_OBLIGATION_EVALUATOR = new ConfigObligationEvaluator();
	private static final ConfigDurationEvaluator CONFIG_DURATION_EVALUATOR = new ConfigDurationEvaluator();
	private static final StatusEvaluator STATUS_EVALUATOR = new DefaultLearningPathStatusEvaluator(AssessmentEntryStatus.notStarted);
	private static final AccountingEvaluators DEFAULT = defaults().build();
	
	public static AccountingEvaluators buildDefault() {
		return DEFAULT;
	}
	
	public static AccountingEvaluatorsBuilder defaults() {
		 return AccountingEvaluatorsBuilder
			.builder()
			.withStartDateEvaluator(CONFIG_START_DATE_EVALUATOR)
			.withEndDateEvaluator(CONFIG_END_DATE_EVALUATOR)
			.withObligationEvaluator(CONFIG_OBLIGATION_EVALUATOR)
			.withDurationEvaluator(CONFIG_DURATION_EVALUATOR)
			.withStatusEvaluator(STATUS_EVALUATOR);
	}
	

}
