/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.peerreview;

import static org.olat.modules.forms.EvaluationFormSessionStatus.done;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.GTAScores;
import org.olat.course.nodes.ms.MSService;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAPeerReviewDetailsScoreController extends FormBasicController {
	
	private final GTACourseNode courseNode;
	private final Identity assessedIdentity;
	private final UserCourseEnvironment assessedUserCourseEnv;
	
	@Autowired
	private MSService msService;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private GTAPeerReviewManager peerReviewManager;
	
	public GTAPeerReviewDetailsScoreController(UserRequest ureq, WindowControl wControl, Form rootForm,
			GTACourseNode node, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, LAYOUT_CUSTOM, "score_details", rootForm);
		setTranslator(Util.createPackageTranslator(GTACoachController.class, getLocale(), getTranslator()));
		
		this.courseNode = node;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			EvaluationFormSession session = msService.getSession(courseEntry, courseNode.getIdent(),
					GTACourseNode.getEvaluationFormProvider(), assessedIdentity, done);
			
			AssessmentEvaluation currentEval = assessedUserCourseEnv.getScoreAccounting().evalCourseNode(courseNode);
			GTAScores scores = courseNode.getScore(msService, gtaManager, peerReviewManager, assessedUserCourseEnv, currentEval, session);
			String scoreParts = courseNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_SCORE_PARTS, "");
			if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_EVALUATION_FORM)) {
				setScore(scores.evaluationFormScore(), "evaluationFormScore", layoutCont);
			}
			if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_PEER_REVIEW)) {
				setScore(scores.peerReviewScore(), "peerReviewScore", layoutCont);
			}
			if(scoreParts.contains(GTACourseNode.GTASK_SCORE_PARTS_REVIEW_SUBMITTED)) {
				setScore(scores.awardedReviewScore(), "awardedReviewsScore", layoutCont);
			}

			Float total = scores.totalScore();
			Float maxTotal = scores.minMax() == null ? null : scores.minMax().getMax();
			String totalLabel = translate("score.details.total", AssessmentHelper.getRoundedScore(maxTotal));
			layoutCont.contextPut("totalLabel", totalLabel);
			String totalAsString = getRoundedScore(total);
			layoutCont.contextPut("totalScore", totalAsString);
		}
	}
	
	private String getRoundedScore(Float score) {
		return score == null ? "-" : AssessmentHelper.getRoundedScore(score);
	}
	
	private void setScore(Float score, String key, FormLayoutContainer layoutCont) {
		String scoreAsString = getRoundedScore(score);
		layoutCont.contextPut(key, scoreAsString);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
