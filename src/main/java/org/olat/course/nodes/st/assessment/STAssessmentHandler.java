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

import static org.olat.course.run.scoring.AverageCompletionEvaluator.DURATION_WEIGHTED;
import static org.olat.course.run.scoring.AverageCompletionEvaluator.UNWEIGHTED;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentHandler;
import org.olat.course.assessment.handler.NonAssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.config.CompletionType;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.STIdentityListCourseNodeController;
import org.olat.course.run.scoring.AccountingEvaluators;
import org.olat.course.run.scoring.AccountingEvaluatorsBuilder;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.AverageCompletionEvaluator;
import org.olat.course.run.scoring.CompletionEvaluator;
import org.olat.course.run.scoring.FullyAssessedEvaluator;
import org.olat.course.run.scoring.LastModificationsEvaluator;
import org.olat.course.run.scoring.ObligationEvaluator;
import org.olat.course.run.scoring.PassedEvaluator;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.run.scoring.ScoreEvaluator;
import org.olat.course.run.scoring.StatusEvaluator;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class STAssessmentHandler implements AssessmentHandler {
	
	private static final ObligationEvaluator MANDATORY_OBLIGATION_EVALUATOR = new MandatoryObligationEvaluator();
	private static final CumulatingDurationEvaluator CUMULATION_DURATION_EVALUATOR = new CumulatingDurationEvaluator();
	private static final ScoreEvaluator CONDITION_SCORE_EVALUATOR = new ConditionScoreEvaluator();
	private static final PassedEvaluator CONDITION_PASSED_EVALUATOR = new ConditionPassedEvaluator();
	private static final StatusEvaluator SCORE_STATUS_EVALUATOR = new ScoreStatusEvaluator();
	private static final StatusEvaluator LEARNING_PATH_STATUS_EVALUATOR = new STLinearStatusEvaluator();
	private static final FullyAssessedEvaluator FULLY_ASSESSED_EVALUATOR = new STFullyAssessedEvaluator();
	private static final LastModificationsEvaluator LAST_MODIFICATION_EVALUATOR = new STLastModificationsEvaluator();
	private static final AccountingEvaluators CONVENTIONAL_EVALUATORS = AccountingEvaluatorsBuilder.builder()
			.withScoreEvaluator(CONDITION_SCORE_EVALUATOR)
			.withPassedEvaluator(CONDITION_PASSED_EVALUATOR)
			.withStatusEvaluator(SCORE_STATUS_EVALUATOR)
			.withLastModificationsEvaluator(LAST_MODIFICATION_EVALUATOR)
			.build();
	
	@Override
	public String acceptCourseNodeType() {
		return STCourseNode.TYPE;
	}

	@Override
	public AssessmentConfig getAssessmentConfig(CourseNode courseNode) {
		if (courseNode instanceof STCourseNode) {
			STCourseNode stCourseNode = (STCourseNode) courseNode;
			return new STAssessmentConfig(stCourseNode.getScoreCalculator());
		}
		return NonAssessmentConfig.create();
	}

	@Override
	public AssessmentEntry getAssessmentEntry(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getAssessmentEntry(courseNode, assessedIdentity);
	}

	@Override
	public AccountingEvaluators getEvaluators(CourseNode courseNode, CourseConfig courseConfig) {
		if (LearningPathNodeAccessProvider.TYPE.equals(courseConfig.getNodeAccessType().getType())) {
			AccountingEvaluatorsBuilder builder = AccountingEvaluatorsBuilder.builder()
					.withObligationEvaluator(MANDATORY_OBLIGATION_EVALUATOR)
					.withDurationEvaluator(CUMULATION_DURATION_EVALUATOR)
					.withScoreEvaluator(CONDITION_SCORE_EVALUATOR)
					.withPassedEvaluator(CONDITION_PASSED_EVALUATOR)
					.withStatusEvaluator(LEARNING_PATH_STATUS_EVALUATOR)
					.withFullyAssessedEvaluator(FULLY_ASSESSED_EVALUATOR)
					.withLastModificationsEvaluator(LAST_MODIFICATION_EVALUATOR);
			CompletionEvaluator completionEvaluator = CompletionType.duration.equals(courseConfig.getCompletionType())
					? new AverageCompletionEvaluator(DURATION_WEIGHTED)
					: new AverageCompletionEvaluator(UNWEIGHTED);
			builder.withCompletionEvaluator(completionEvaluator);
			return builder.build();
		}
		return CONVENTIONAL_EVALUATORS;
	}

	@Override
	public AssessmentEvaluation getCalculatedScoreEvaluation(CourseNode courseNode, UserCourseEnvironment userCourseEnvironment) {
		ScoreCalculator scoreCalculator = getScoreCalculator(courseNode);
		if (scoreCalculator == null) { 
			// this is a not-computable course node at the moment (no scoring/passing rules defined)
			return null; 
		}
		
		Float score = null;
		Boolean passed = null;

		String scoreExpressionStr = scoreCalculator.getScoreExpression();
		String passedExpressionStr = scoreCalculator.getPassedExpression();

		ConditionInterpreter ci = userCourseEnvironment.getConditionInterpreter();
		if (scoreExpressionStr != null) {
			score = new Float(ci.evaluateCalculation(scoreExpressionStr));
		}
		if (passedExpressionStr != null) {
			passed = new Boolean(ci.evaluateCondition(passedExpressionStr));
		}
		return new AssessmentEvaluation(score, passed);
	}

	@Override
	public ScoreCalculator getScoreCalculator(CourseNode courseNode) {
		if (courseNode instanceof STCourseNode) {
			STCourseNode stCourseNode = (STCourseNode) courseNode;
			return stCourseNode.getScoreCalculator();
		}
		return null;
	}

	@Override
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			CourseNode courseNode, UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		return null;
	}

	@Override
	public boolean hasCustomIdentityList() {
		return true;
	}
	
	@Override
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CourseNode courseNode, RepositoryEntry courseEntry, BusinessGroup group,
			UserCourseEnvironment coachCourseEnv, AssessmentToolContainer toolContainer,
			AssessmentToolSecurityCallback assessmentCallback) {
		return new STIdentityListCourseNodeController(ureq, wControl, stackPanel, courseEntry, group, courseNode,
				coachCourseEnv, toolContainer, assessmentCallback);
	}

}
