/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.cns.assessment;

import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.condition.ConditionNodeAccessProvider;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.LearningPathOnlyAssessmentHandler;
import org.olat.course.learningpath.evaluation.LearningPathEvaluatorBuilder;
import org.olat.course.nodes.CNSCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.st.assessment.STWithoutSequenceBlockerEvaluator;
import org.olat.course.run.scoring.AccountingEvaluators;
import org.olat.course.run.scoring.AccountingEvaluatorsBuilder;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CNSAssessmentHandler extends LearningPathOnlyAssessmentHandler {
	
	private static final CNSAssessmentConfig ASSESSMENT_CONFIG = new CNSAssessmentConfig();
	private static final AccountingEvaluators LP_EVALUATORS = LearningPathEvaluatorBuilder.defaults()
			.withBlockerEvaluator(new STWithoutSequenceBlockerEvaluator())
			.withObligationEvaluator(new CNSObligationEvaluator())
			.withCompletionEvaluator(new CNSCompletionEvaluator())
			.withStatusEvaluator(new CNSStatusEvaluator())
			.withFullyAssessedEvaluator(new CNSFullyAssesssedEvaluator())
			.build();

	@Override
	public String acceptCourseNodeType() {
		return CNSCourseNode.TYPE;
	}

	@Override
	public AssessmentConfig getAssessmentConfig(RepositoryEntryRef courseEntry, CourseNode courseNode) {
		return ASSESSMENT_CONFIG;
	}

	@Override
	public AccountingEvaluators getEvaluators(CourseNode courseNode, CourseConfig courseConfig) {
		if (ConditionNodeAccessProvider.TYPE.equals(courseConfig.getNodeAccessType().getType())) {
			return AccountingEvaluatorsBuilder.defaultConventional();
		}
		
		return LP_EVALUATORS;
	}
	
}
