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
package org.olat.course.assessment.ui.tool;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentParticipantViewController extends BasicController {

	private final VelocityContainer mainVC;
	
	private final AssessmentEvaluation assessmentEval;
	private final AssessmentConfig assessmentConfig;
	
	@Autowired
	private GradeModule gradeModule;

	public AssessmentParticipantViewController(UserRequest ureq, WindowControl wControl, AssessmentEvaluation assessmentEval, AssessmentConfig assessmentConfig) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MSCourseNodeRunController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.assessmentEval = assessmentEval;
		this.assessmentConfig = assessmentConfig;
		
		mainVC = createVelocityContainer("participant_view");
		
		// We only show the performance data as a first shot.
		// Feel free to add user comment, docs, ...
		exposeToVC();
		
		putInitialPanel(mainVC);
	}
	
	private void exposeToVC() {
		boolean resultsVisible = assessmentEval.getUserVisible() != null && assessmentEval.getUserVisible().booleanValue();
		mainVC.contextPut("resultsVisible", resultsVisible);
		
		// Attempts
		boolean hasAttempts = assessmentConfig.hasAttempts();
		mainVC.contextPut("hasAttemptsField", Boolean.valueOf(hasAttempts));
		if (hasAttempts) {
			Integer attempts = assessmentEval.getAttempts();
			if (attempts == null) {
				attempts = Integer.valueOf(0);
			}
			mainVC.contextPut("attempts", attempts);
			if (assessmentConfig.hasMaxAttempts()) {
				Integer maxAttempts = assessmentConfig.getMaxAttempts();
				mainVC.contextPut("maxAttempts", maxAttempts);
			}
		}
		
		// Score
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		mainVC.contextPut("hasScoreField", Boolean.valueOf(hasScore));
		if (hasScore) {
			Float minScore = assessmentConfig.getMinScore();
			String scoreMinMax = AssessmentHelper.getMinMax(getTranslator(), minScore, assessmentConfig.getMaxScore());
			if (scoreMinMax != null) {
				mainVC.contextPut("scoreMinMax", scoreMinMax);
			}
			mainVC.contextPut("score", AssessmentHelper.getRoundedScore(assessmentEval.getScore()));
		}
		
		// Grade
		boolean hasGrade = hasScore && assessmentConfig.hasGrade() && gradeModule.isEnabled();
		mainVC.contextPut("hasGradeField", Boolean.valueOf(hasGrade));
		if (hasGrade) {
			mainVC.contextPut("grade", GradeUIFactory.translatePerformanceClass(getTranslator(),
					assessmentEval.getPerformanceClassIdent(), assessmentEval.getGrade()));
		}
		
		// Passed
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		mainVC.contextPut("hasPassedField", Boolean.valueOf(hasPassed));
		if (hasPassed) {
			mainVC.contextPut("hasPassedValue", (assessmentEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
			mainVC.contextPut("passed", assessmentEval.getPassed());
			if (!hasGrade) {
				mainVC.contextPut("passedCutValue", AssessmentHelper.getRoundedScore(assessmentConfig.getCutValue()));
			}
		}
		
		// Status
		boolean hasStatus = AssessmentEntryStatus.inReview == assessmentEval.getAssessmentStatus()
				|| AssessmentEntryStatus.done == assessmentEval.getAssessmentStatus();
		mainVC.contextPut("hasStatusField", Boolean.valueOf(hasStatus));
		if (hasStatus) {
			String statusText = null;
			String statusIconCss = null;
			String statusLabelCss = null;
			if (AssessmentEntryStatus.done == assessmentEval.getAssessmentStatus()) {
				if (resultsVisible) {
					statusText = translate("assessment.status.done");
					statusIconCss = "o_icon_status_done";
					statusLabelCss = "o_results_visible";
				} else {
					statusText = translate("in.release");
					statusIconCss = "o_icon_status_in_review";
					statusLabelCss = "o_results_hidden";
				}
			} else {
				statusText = translate("in.review");
				statusIconCss = "o_icon_status_in_review";
				statusLabelCss = "o_results_hidden";
			}
			mainVC.contextPut("statusText", statusText);
			mainVC.contextPut("statusIconCss", statusIconCss);
			mainVC.contextPut("statusLabelCss", statusLabelCss);
		}
	}
	
	public void setCustomFields(Component customFields) {
		if (customFields != null) {
			mainVC.put("customFields", customFields);
		} else {
			mainVC.remove("customFields");
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
