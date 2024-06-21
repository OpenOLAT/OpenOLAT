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
package org.olat.course.nodes.gta;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.assessment.handler.AssessmentConfig.FormEvaluationScoreMode;
import org.olat.course.assessment.handler.FormEvaluationHandler;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.assessment.ui.tool.AssessmentEvaluationFormExecutionController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.gta.ui.GTAIdentityListCourseNodeController;
import org.olat.course.nodes.gta.ui.peerreview.GTAPeerReviewDetailsScoreController;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.forms.EvaluationFormProvider;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ITAAssessmentHandler extends AbstractGTAAssessmentHandler implements FormEvaluationHandler {

	@Override
	public String acceptCourseNodeType() {
		return GTACourseNode.TYPE_INDIVIDUAL;
	}
	
	@Override
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, CourseNode courseNode, RepositoryEntry courseEntry,
			UserCourseEnvironment coachCourseEnv, AssessmentToolContainer toolContainer,
			AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		return new GTAIdentityListCourseNodeController(ureq, wControl, stackPanel, courseEntry, courseNode,
				coachCourseEnv, toolContainer, assessmentCallback, showTitle);
	}

	@Override
	public EvaluationFormSession getSession(RepositoryEntry courseEntry, CourseNode courseNode, Identity assessedIdentity) {
		MSService msService = CoreSpringFactory.getImpl(MSService.class);
		RepositoryEntry formEntry = MSCourseNode.getEvaluationForm(courseNode.getModuleConfiguration());
		EvaluationFormProvider evaluationFormProvider = GTACourseNode.getEvaluationFormProvider();
		return msService.getSession(formEntry, courseEntry, courseNode.getIdent(), evaluationFormProvider, assessedIdentity);
	}
	
	@Override
	public Float getEvaluationScore(EvaluationFormSession session, FormEvaluationScoreMode scoreMode) {
		MSService msService = CoreSpringFactory.getImpl(MSService.class);
		Float evaluationScore = null;
		if(scoreMode == FormEvaluationScoreMode.avg) {
			evaluationScore = msService.calculateScoreByAvg(session);
		} else if(scoreMode == FormEvaluationScoreMode.sum) {
			evaluationScore = msService.calculateScoreBySum(session);
		}
		return evaluationScore;
	}

	@Override
	public Controller getEvaluationFormController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv,
			boolean edit, boolean reopen) {
		EvaluationFormProvider evaluationFormProvider = GTACourseNode.getEvaluationFormProvider();
		return new AssessmentEvaluationFormExecutionController(ureq, wControl, assessedUserCourseEnv, edit, reopen,
				courseNode, evaluationFormProvider);
	}

	@Override
	public boolean hasDetailsScoreController(CourseNode courseNode) {
		if(courseNode instanceof GTACourseNode gtaNode) {
			int score = 0;
			String scoreParts = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_SCORE_PARTS, "");
			boolean evalFormEnabled = gtaNode.getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_EVAL_FORM_ENABLED);
			if(evalFormEnabled && scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM)) {
				score++;
			}
			if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_PEER_REVIEW)) {
				score++;
			}
			if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_REVIEW_SUBMITTED)) {
				score++;
			}
			return score > 1;
		}
		return false;
	}
	
	@Override
	public FormBasicController getDetailsScoreController(UserRequest ureq, WindowControl wControl, Form rootForm,
			UserCourseEnvironment assessedUserCourseEnvironment, CourseNode courseNode) {
		return new GTAPeerReviewDetailsScoreController(ureq, wControl, rootForm, (GTACourseNode)courseNode, assessedUserCourseEnvironment);
	}
}
