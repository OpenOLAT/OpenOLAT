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

import java.io.File;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.ms.DocumentsMapper;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Nov 2021<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentViewController extends BasicController {

	private final VelocityContainer mainVC;
	private Link reopenLink;
	private Link userVisibilityVisibleLink;
	private Link userVisibilityHiddenLink;
	
	private AssessmentParticipantViewController assessmentParticipantViewCtrl;

	private final CourseNode courseNode;
	private final UserCourseEnvironment coachCourseEnv;
	private final UserCourseEnvironment assessedUserCourseEnv;
	private final AssessmentConfig assessmentConfig;
	private AssessmentEntry assessmentEntry;

	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeModule gradeModule;

	protected AssessmentViewController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.coachCourseEnv = coachCourseEnv;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		setTranslator(Util.createPackageTranslator(MSCourseNodeRunController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
		
		mainVC = createVelocityContainer("assessment_view");
		
		reopenLink = LinkFactory.createButton("reopen", mainVC, this);
		reopenLink.setElementCssClass("o_sel_assessment_form_reopen");
		reopenLink.setIconLeftCSS("o_icon o_icon_status_in_review");
		updateUserVisibilityUI();
		
		putConfigToVC();
		putAssessmentDataToVC(ureq);
		putParticipantViewToVC(ureq);
		putInitialPanel(mainVC);
	}
	
	private void updateUserVisibilityUI() {
		boolean canChangeUserVisibility = coachCourseEnv.isAdmin()
				|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		
		mainVC.remove("user.visibility.set.hidden");
		mainVC.remove("user.visibility.set.visible");
		if (canChangeUserVisibility) {
			if (assessmentEntry.getUserVisibility() != null && assessmentEntry.getUserVisibility().booleanValue()) {
				userVisibilityHiddenLink = LinkFactory.createButton("user.visibility.set.hidden", mainVC, this);
				userVisibilityHiddenLink.setIconLeftCSS("o_icon o_icon_results_hidden");
				userVisibilityHiddenLink.setElementCssClass("o_button_results_hidden");
			} else {
				userVisibilityVisibleLink = LinkFactory.createButton("user.visibility.set.visible", mainVC, this);
				userVisibilityVisibleLink.setIconLeftCSS("o_icon o_icon_results_visible");
				userVisibilityVisibleLink.setElementCssClass("o_button_results_visible");
			}
		}
		mainVC.contextPut("userVisibility", new UserVisibilityCellRenderer(true).render(assessmentEntry.getUserVisibility(), getTranslator()));
	}

	private void putConfigToVC() {
		mainVC.contextPut("hasAttemptsField", Boolean.valueOf(assessmentConfig.hasAttempts()));
		mainVC.contextPut("hasMaxAttemptsField", Boolean.valueOf(assessmentConfig.hasMaxAttempts()));
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		mainVC.contextPut("hasScoreField", Boolean.valueOf(hasScore));
		if (hasScore) {
			String scoreMinMax = AssessmentHelper.getMinMax(getTranslator(), assessmentConfig.getMinScore(), assessmentConfig.getMaxScore());
			if (scoreMinMax != null) {
				mainVC.contextPut("scoreMinMax", scoreMinMax);
			}
		}
		boolean hasGrade = hasScore && assessmentConfig.hasGrade() && gradeModule.isEnabled();
		mainVC.contextPut("hasGradeField", Boolean.valueOf(hasGrade));
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		mainVC.contextPut("hasPassedField", Boolean.valueOf(hasPassed));
		if (hasPassed && !hasGrade) {
			mainVC.contextPut("passedCutValue", AssessmentHelper.getRoundedScore(assessmentConfig.getCutValue()));
		}
		mainVC.contextPut("hasCommentField", assessmentConfig.hasComment());
		mainVC.contextPut("hasDocumentField", assessmentConfig.hasIndividualAsssessmentDocuments());
	}

	private void putAssessmentDataToVC(UserRequest ureq) {
		Integer attemptsValue = courseAssessmentService.getAttempts(courseNode, assessedUserCourseEnv);
		mainVC.contextPut("attempts", attemptsValue == null ? 0 : attemptsValue.intValue());
		if (assessmentConfig.hasMaxAttempts()) {
			mainVC.contextPut("maxAttempts", assessmentConfig.getMaxAttempts());
		}
		
		mainVC.contextPut("score", AssessmentHelper.getRoundedScore(assessmentEntry.getScore()));
		mainVC.contextPut("grade", GradeUIFactory.translatePerformanceClass(getTranslator(),
				assessmentEntry.getPerformanceClassIdent(), assessmentEntry.getGrade(), assessmentEntry.getGradeSystemIdent()));
		mainVC.contextPut("hasPassedValue", (assessmentEntry.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
		mainVC.contextPut("passed", assessmentEntry.getPassed());
		mainVC.contextPut("inReview", Boolean.valueOf(AssessmentEntryStatus.inReview == assessmentEntry.getAssessmentStatus()));
		
		mainVC.contextPut("status", new AssessmentStatusCellRenderer(getTranslator(), true).render(assessmentEntry.getAssessmentStatus()));
		UserVisibilityCellRenderer userVisibilityCellRenderer = new UserVisibilityCellRenderer(true);
		mainVC.contextPut("userVisibility", userVisibilityCellRenderer.render(assessmentEntry.getUserVisibility(), getTranslator()));
		// Hack to avoid change of the column width when switching user visibility
		Boolean userVisibilityInverted = assessmentEntry.getUserVisibility() != null && assessmentEntry.getUserVisibility().booleanValue()? Boolean.FALSE: Boolean.TRUE;
		mainVC.contextPut("userVisibilityInverted", new UserVisibilityCellRenderer(true).render(userVisibilityInverted, getTranslator()));

		String rawComment = assessmentEntry.getComment();
		if (assessmentConfig.hasComment()) {
			StringBuilder comment = Formatter.stripTabsAndReturns(rawComment);
			mainVC.contextPut("comment", StringHelper.xssScan(comment));
		}
		
		String rawCoachComment = assessmentEntry.getCoachComment();
		StringBuilder coachComment = Formatter.stripTabsAndReturns(rawCoachComment);
		mainVC.contextPut("coachComment", StringHelper.xssScan(coachComment));

		if (assessmentConfig.hasIndividualAsssessmentDocuments()) {
			List<File> docs = courseAssessmentService.getIndividualAssessmentDocuments(courseNode, assessedUserCourseEnv);
			String mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(docs));
			mainVC.contextPut("docsMapperUri", mapperUri);
			mainVC.contextPut("docs", docs);
			DisplayOrDownloadComponent download = new DisplayOrDownloadComponent("", null);
			mainVC.put("download", download);
		}
	}

	private void putParticipantViewToVC(UserRequest ureq) {
		removeAsListenerAndDispose(assessmentParticipantViewCtrl);
		assessmentParticipantViewCtrl = new AssessmentParticipantViewController(ureq, getWindowControl(),
				AssessmentEvaluation.toAssessmentEvaluation(assessmentEntry, assessmentConfig), assessmentConfig);
		listenTo(assessmentParticipantViewCtrl);
		mainVC.put("participantView", assessmentParticipantViewCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == userVisibilityVisibleLink) {
			doSetUserVisibility(ureq, Boolean.TRUE);
		} else if (source == userVisibilityHiddenLink) {
			doSetUserVisibility(ureq, Boolean.FALSE);
		} else if (source == reopenLink) {
			doReopen(ureq);
		}
	}

	private void doSetUserVisibility(UserRequest ureq, Boolean userVisibility) {
		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		ScoreEvaluation eval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(),
				scoreEval.getAssessmentStatus(), userVisibility, scoreEval.getCurrentRunStartDate(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(courseNode, eval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
		assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
		
		updateUserVisibilityUI();
		putParticipantViewToVC(ureq);
		
		fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_CHANGED, false));
	}

	private void doReopen(UserRequest ureq) {
		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		ScoreEvaluation eval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getGradeSystemIdent(), scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(),
				AssessmentEntryStatus.inReview, scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		
		courseAssessmentService.updateScoreEvaluation(courseNode, eval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
		fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_REOPEN, false));
	}

}
