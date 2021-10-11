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
import org.olat.core.util.nodes.INode;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentHandler;
import org.olat.course.assessment.handler.NonAssessmentConfig;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.config.CompletionType;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.st.STIdentityListCourseNodeController;
import org.olat.course.run.scoring.AccountingEvaluators;
import org.olat.course.run.scoring.AccountingEvaluatorsBuilder;
import org.olat.course.run.scoring.AverageCompletionEvaluator;
import org.olat.course.run.scoring.BlockerEvaluator;
import org.olat.course.run.scoring.CompletionEvaluator;
import org.olat.course.run.scoring.FullyAssessedEvaluator;
import org.olat.course.run.scoring.LastModificationsEvaluator;
import org.olat.course.run.scoring.ObligationEvaluator;
import org.olat.course.run.scoring.PassedEvaluator;
import org.olat.course.run.scoring.RootPassedEvaluator;
import org.olat.course.run.scoring.ScoreEvaluator;
import org.olat.course.run.scoring.StatusEvaluator;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
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
	
	private static final BlockerEvaluator SEQUENTIAL_BLOCKER_EVALUATOR = new STSequentialBlockerEvaluator();
	private static final BlockerEvaluator WITHOUT_SEQUENCE_BLOCKER_EVALUATOR = new STWithoutSequenceBlockerEvaluator();
	private static final ObligationEvaluator OBLIGATION_EVALUATOR = new STObligationEvaluator();
	private static final CumulatingDurationEvaluator CUMULATION_DURATION_EVALUATOR = new CumulatingDurationEvaluator();
	private static final ScoreEvaluator CONDITION_SCORE_EVALUATOR = new ConditionScoreEvaluator();
	private static final ScoreEvaluator SUM_SCORE_EVALUATOR = new CumulatingScoreEvaluator(false);
	private static final ScoreEvaluator AVG_SCORE_EVALUATOR = new CumulatingScoreEvaluator(true);
	private static final PassedEvaluator CONDITION_PASSED_EVALUATOR = new ConditionPassedEvaluator();
	private static final RootPassedEvaluator ROOT_PASSED_EVALUATOR = new STRootPassedEvaluator();
	private static final StatusEvaluator SCORE_STATUS_EVALUATOR = new STConditionStatusEvaluator();
	private static final StatusEvaluator LEARNING_PATH_STATUS_EVALUATOR = new STLearningPathStatusEvaluator();
	private static final FullyAssessedEvaluator FULLY_ASSESSED_EVALUATOR = new STFullyAssessedEvaluator();
	private static final LastModificationsEvaluator LAST_MODIFICATION_EVALUATOR = new STLastModificationsEvaluator();
	
	@Override
	public String acceptCourseNodeType() {
		return STCourseNode.TYPE;
	}

	@Override
	public AssessmentConfig getAssessmentConfig(CourseNode courseNode) {
		if (courseNode instanceof STCourseNode) {
			STCourseNode stCourseNode = (STCourseNode) courseNode;
			STCourseNode root = getRoot(courseNode);
			boolean isRoot = courseNode.getIdent().equals(root.getIdent());
			return new STAssessmentConfig(stCourseNode, isRoot, root.getModuleConfiguration());
		}
		return NonAssessmentConfig.create();
	}
	
	private STCourseNode getRoot(INode node) {
		STCourseNode root = null;
		if (node instanceof STCourseNode) {
			root = (STCourseNode)node;
		}
		
		INode parent = node.getParent();
		if (parent != null) {
			STCourseNode parentRoot = getRoot(parent);
			if (parentRoot != null) {
				root = parentRoot;
			}
		}
		
		return root;
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
					.withObligationEvaluator(OBLIGATION_EVALUATOR)
					.withDurationEvaluator(CUMULATION_DURATION_EVALUATOR)
					.withStatusEvaluator(LEARNING_PATH_STATUS_EVALUATOR)
					.withFullyAssessedEvaluator(FULLY_ASSESSED_EVALUATOR)
					.withLastModificationsEvaluator(LAST_MODIFICATION_EVALUATOR)
					.withRootPassedEvaluator(ROOT_PASSED_EVALUATOR);
			CompletionEvaluator completionEvaluator = CompletionType.duration.equals(courseConfig.getCompletionType())
					? new AverageCompletionEvaluator(DURATION_WEIGHTED)
					: new AverageCompletionEvaluator(UNWEIGHTED);
			builder.withCompletionEvaluator(completionEvaluator);
			
			ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
			String sequenceKey = moduleConfig.getStringValue(STLearningPathConfigs.CONFIG_LP_SEQUENCE_KEY, STLearningPathConfigs.CONFIG_LP_SEQUENCE_DEFAULT);
			if (STLearningPathConfigs.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL.equals(sequenceKey)) {
				builder.withBlockerEvaluator(SEQUENTIAL_BLOCKER_EVALUATOR);
			} else {
				builder.withBlockerEvaluator(WITHOUT_SEQUENCE_BLOCKER_EVALUATOR);
			}
			
			ModuleConfiguration rootConfig = getRoot(courseNode).getModuleConfiguration();
			if (rootConfig.has(STCourseNode.CONFIG_SCORE_KEY)) {
				String scoreKey = rootConfig.getStringValue(STCourseNode.CONFIG_SCORE_KEY);
				if (STCourseNode.CONFIG_SCORE_VALUE_SUM.equals(scoreKey)) {
					builder.withScoreEvaluator(SUM_SCORE_EVALUATOR);
				} else if (STCourseNode.CONFIG_SCORE_VALUE_AVG.equals(scoreKey)) {
					builder.withScoreEvaluator(AVG_SCORE_EVALUATOR);
				}
			} else {
				builder.withNullScoreEvaluator();
			}
			return builder.build();
		}
		return AccountingEvaluatorsBuilder.builder()
				.withScoreEvaluator(CONDITION_SCORE_EVALUATOR)
				.withPassedEvaluator(CONDITION_PASSED_EVALUATOR)
				.withCompletionEvaluator(new ConventionalSTCompletionEvaluator())
				.withStatusEvaluator(SCORE_STATUS_EVALUATOR)
				.withLastModificationsEvaluator(LAST_MODIFICATION_EVALUATOR)
				.build();
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
			TooledStackedPanel stackPanel, CourseNode courseNode, RepositoryEntry courseEntry,
			UserCourseEnvironment coachCourseEnv, AssessmentToolContainer toolContainer,
			AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		return new STIdentityListCourseNodeController(ureq, wControl, stackPanel, courseEntry, courseNode,
				coachCourseEnv, toolContainer, assessmentCallback, showTitle);
	}

}
