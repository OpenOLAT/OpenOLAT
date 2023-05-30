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

import java.util.NavigableSet;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.modules.assessment.Role;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IdentityConditionalScoreController extends BasicController {

	private final VelocityContainer mainVC;
	private final Link gradeApplyLink;
	private final Link efficiencyLink;

	private final UserCourseEnvironment assessedUserCourseEnv;
	private final boolean readOnly;
	private final RepositoryEntry courseEntry;
	private final CourseNode courseNode;
	private final AssessmentConfig assessmentConfig;
	private final String gradeSystemLabel;
	
	private CloseableModalController cmc;
	private CertificateAndEfficiencyStatementController certificateAndEfficiencyStatementCtrl;
	private DialogBoxController applyGradeCtrl;
	
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private EfficiencyStatementManager efficiencyStatementMgr;

	public IdentityConditionalScoreController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironmentImpl assessedUserCourseEnv,
			boolean certificateAndEfficiencyLink) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.readOnly = coachCourseEnv.isCourseReadOnly();
		courseNode = assessedUserCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		assessmentConfig = courseAssessmentService.getAssessmentConfig(
				courseEntry, courseNode);
		
		mainVC = createVelocityContainer("conditional_score");
		putInitialPanel(mainVC);
		
		AssessmentEvaluation assessmentEvaluation = assessedUserCourseEnv.getScoreAccounting().getScoreEvaluation(courseNode);
		String gradeSystemident = StringHelper.containsNonWhitespace(assessmentEvaluation.getGradeSystemIdent())
				? assessmentEvaluation.getGradeSystemIdent()
				: gradeService.getGradeSystem(courseEntry, courseNode.getIdent()).toString();
		gradeSystemLabel = GradeUIFactory.translateGradeSystemLabel(getTranslator(), gradeSystemident);
		
		gradeApplyLink = LinkFactory.createCustomLink("grade.apply.button", "grade", "", Link.BUTTON + Link.NONTRANSLATED, mainVC, this);
		gradeApplyLink.setCustomDisplayText(translate("grade.apply.label", gradeSystemLabel));
		gradeApplyLink.setIconLeftCSS("o_icon o_icon_grade");
		gradeApplyLink.setElementCssClass("a_button_bottom");
		efficiencyLink = LinkFactory.createLink("show.efficency.statement", "show.efficency.statement", getTranslator(), mainVC, this, Link.BUTTON);
		efficiencyLink.setIconLeftCSS("o_icon o_icon_preview");
		efficiencyLink.setElementCssClass("a_button_bottom");
		efficiencyLink.setVisible(certificateAndEfficiencyLink);
		
		reload();
	}

	public void reload() {
		AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode() && assessmentEvaluation.getScore() != null;
		boolean gradeApplied = StringHelper.containsNonWhitespace(assessmentEvaluation.getGrade());
		gradeApplyLink.setVisible(!readOnly && !gradeApplied && gradeModule.isEnabled() && hasScore);
		
		if (hasScore) {
			String score = AssessmentHelper.getRoundedScore(assessmentEvaluation.getScore());
			mainVC.contextPut("score", translate("score.value", score));
		}
		
		if (assessmentConfig.hasGrade() && gradeApplied && gradeModule.isEnabled()) {
			mainVC.contextPut("grade", translate("grade.value", gradeSystemLabel, assessmentEvaluation.getGrade()));
		}
		
		if (Mode.none != assessmentConfig.getPassedMode() && assessmentEvaluation.getPassed() != null) {
			mainVC.contextPut("passed", assessmentEvaluation.getPassed());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(certificateAndEfficiencyStatementCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if (applyGradeCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doApplyGrade(ureq);
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(certificateAndEfficiencyStatementCtrl);
		removeAsListenerAndDispose(applyGradeCtrl);
		removeAsListenerAndDispose(cmc);
		certificateAndEfficiencyStatementCtrl = null;
		applyGradeCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == gradeApplyLink) {
			doConfirmApplyGrade(ureq);
		} else if(source == efficiencyLink) {
			doViewEfficiencyStatement(ureq);
		}
	}
	
	private void doConfirmApplyGrade(UserRequest ureq) {
		AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		
		if (scoreEval != null && scoreEval.getScore() != null) {
			GradeScale gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
			NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
			GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, scoreEval.getScore());
			String grade = gradeScoreRange.getGrade();
			String gradeSystemLabel = GradeUIFactory.translateGradeSystemLabel(getTranslator(), gradeScoreRange.getGradeSystemIdent());
			Boolean passed = Mode.none != courseAssessmentService.getAssessmentConfig(courseEntry, courseNode).getPassedMode()
					? gradeScoreRange.getPassed()
					: null;
			
			String text = null;
			if (passed != null) {
				if (passed.booleanValue()) {
					text = translate("grade.apply.text.passed", grade, gradeSystemLabel);
				} else {
					text = translate("grade.apply.text.failed", grade, gradeSystemLabel);
				}
			} else {
				text = translate("grade.apply.text", grade, gradeSystemLabel);
			}
			String title = translate("grade.apply.label", gradeSystemLabel);
			applyGradeCtrl = activateYesNoDialog(ureq, title, text, applyGradeCtrl);
		}
	}
	
	private void doApplyGrade(UserRequest ureq) {
		AssessmentEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		if (scoreEval != null && scoreEval.getScore() != null) {
			GradeScale gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
			NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
			GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, scoreEval.getScore());
			String grade = gradeScoreRange.getGrade();
			String gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
			String performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
			Boolean passed = Mode.none != courseAssessmentService.getAssessmentConfig(courseEntry, courseNode).getPassedMode()
					? gradeScoreRange.getPassed()
					: null;
			
			ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), grade,
					gradeSystemIdent, performanceClassIdent, passed, scoreEval.getAssessmentStatus(),
					scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(),
					scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, assessedUserCourseEnv,
					getIdentity(), false, Role.coach);
			
			reload();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void doViewEfficiencyStatement(UserRequest ureq) {
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		RepositoryEntry entry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		Certificate lastCertificate = certificatesManager.getLastCertificate(assessedIdentity, entry.getOlatResource().getKey());
		EfficiencyStatement efficiencyStatement = efficiencyStatementMgr.getUserEfficiencyStatementByCourseRepositoryEntry(entry, assessedIdentity);
		certificateAndEfficiencyStatementCtrl = new CertificateAndEfficiencyStatementController(getWindowControl(), ureq,
				assessedIdentity, null, entry.getOlatResource().getKey(), entry, efficiencyStatement, lastCertificate,
				false, false, true, false, false);
		listenTo(certificateAndEfficiencyStatementCtrl);
		
		String title = translate("show.efficency.statement.title");
		cmc = new CloseableModalController(getWindowControl(), "close", certificateAndEfficiencyStatementCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}


}
