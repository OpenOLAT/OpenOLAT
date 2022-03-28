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
package org.olat.course.assessment.ui.tool.tools;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.assessment.ui.tool.event.ShowDetailsEvent;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Events:
 * <ul>
 * 	<li>CLOSE_EVENT: want to close the callout
 * 	<li>CHANGED_EVENT: process finish with changes
 *  <li>DONE_EVENT: process finish with changes
 * </ul>
 * 
 * Initial date: 22 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractToolsController extends BasicController {
	
	private Link detailsLink, applyGradeLink, setDoneLink, reopenLink, visibleLink, notVisibleLink;
	private Link resetAttemptsButton;
	private final VelocityContainer mainVC;
	private final List<String> links = new ArrayList<>();

	private CloseableModalController cmc;
	private DialogBoxController applyGradeCtrl;
	private ResetAttemptsConfirmationController resetAttemptsConfirmationCtrl;
	
	private final boolean courseReadonly;
	protected final Identity assessedIdentity;
	private final UserCourseEnvironment coachCourseEnv;
	private final RepositoryEntry courseEntry;
	private final AssessmentEvaluation scoreEval;
	protected final CourseNode courseNode;
	protected final UserCourseEnvironment assessedUserCourseEnv;
	protected final AssessmentConfig assessmentConfig;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	
	public AbstractToolsController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			Identity assessedIdentity, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(IdentityListCourseNodeController.class, getLocale(), getTranslator()));
		
		String velocityRoot = Util.getPackageVelocityRoot(AbstractToolsController.class);
		String page = velocityRoot + "/tools.html";
		mainVC = new VelocityContainer("tools", "vc_tools", page, getTranslator(), this);
		
		this.courseNode = courseNode;
		this.assessedIdentity = assessedIdentity;
		this.coachCourseEnv = coachCourseEnv;
		courseReadonly = coachCourseEnv.isCourseReadOnly();
		courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();

		assessedUserCourseEnv = AssessmentHelper
				.createAndInitUserCourseEnvironment(assessedIdentity, coachCourseEnv.getCourseEnvironment());
		scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
	}
	
	public boolean isCourseReadonly() {
		return courseReadonly;
	}
	
	public RepositoryEntry getCourseRepositoryEntry() {
		return courseEntry;
	}
	
	protected final void initTools() {
		initDetails();
		initApplyGrade();
		initStatus();
		addSeparator();
		initResetAttempts();
		
		//clean up separators
		String lastLink = null;
		for(Iterator<String> linkIt=links.iterator(); linkIt.hasNext(); ) {
			String link = linkIt.next();
			if("-".equals(link) && "-".equals(lastLink)) {
				linkIt.remove();
				continue;
			}
			lastLink = link;	
		}
		if(!links.isEmpty() && "-".equals(links.get(links.size() - 1))) {
			links.remove(links.size() -1);//no trailing separator
		}

		mainVC.contextPut("links", links);
		putInitialPanel(mainVC);
	}
	
	protected void initDetails() {
		detailsLink = addLink("tool.details", "tool.details", "o_icon o_icon-fw o_icon_details");
	}
	
	protected void initApplyGrade() {
		boolean canApplyGrade = gradeModule.isEnabled() 
				&& Mode.none != assessmentConfig.getScoreMode()
				&& assessmentConfig.hasGrade()
				&& !assessmentConfig.isAutoGrade()
				&& !courseReadonly
				&& (coachCourseEnv.isAdmin() || coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_GRADE_APPLY))
				&& scoreEval != null
				&& scoreEval.getScore() != null
				&& !StringHelper.containsNonWhitespace(scoreEval.getGrade());
		if (canApplyGrade) {
			applyGradeLink = addLink("tool.grade.apply", "tool.grade.apply", "o_icon o_icon-fw o_icon_grade");
		}
	}
	
	protected void initStatus() {
		if(scoreEval != null && !courseReadonly) {
			// set status inReview / done
			if(scoreEval.getAssessmentStatus() == AssessmentEntryStatus.done) {
				reopenLink = addLink("tool.reopen", "tool.reopen", "o_icon o_icon-fw o_icon_status_in_review");
			} else {
				setDoneLink = addLink("tool.set.done", "tool.set.done", "o_icon o_icon-fw o_icon_status_done");
			}
			
			// result as visible / not visible
			boolean canChangeUserVisibility = coachCourseEnv.isAdmin()
					|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
			if (canChangeUserVisibility) {
				if(scoreEval.getUserVisible() == null || scoreEval.getUserVisible().booleanValue()) {
					notVisibleLink = addLink("tool.set.hidden", "tool.set.hidden", "o_icon o_icon-fw o_icon_results_hidden");
				} else {
					visibleLink = addLink("tool.set.visible", "tool.set.visible", "o_icon o_icon-fw o_icon_results_visible");
				}
			}
		}
	}
	
	protected void initResetAttempts() {
		if(courseAssessmentService.getAssessmentConfig(courseNode).hasAttempts()) {
			resetAttemptsButton = addLink("tool.reset.attempts", "reset.attempts", "o_icon o_icon-fw o_icon_reset");
		}
	}
	
	protected Link addLink(String name, String cmd, String iconCSS) {
		Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
		if(iconCSS != null) {
			link.setIconLeftCSS(iconCSS);
		}
		mainVC.put(name, link);
		links.add(name);
		return link;
	}
	
	protected void addSeparator() {
		if(links.size() > 0 && !"-".equals(links.get(links.size() - 1))) {
			links.add("-");
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(resetAttemptsButton == source) {
			doResetAttempts(ureq);
		} else if(setDoneLink == source) {
			doSetDone(ureq);
		} else if(reopenLink == source) {
			doReopen(ureq);
		} else if(visibleLink == source) {
			doSetVisibility(ureq, true);
		} else if(notVisibleLink == source) {
			doSetVisibility(ureq, false);
		} else if(applyGradeLink == source) {
			doConfirmApplyGrade(ureq);
		} else if(detailsLink == source) {
			fireEvent(ureq, new ShowDetailsEvent(courseNode, assessedIdentity));
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(resetAttemptsConfirmationCtrl == source) {
			cmc.deactivate();
			cleanUp();
			fireEvent(ureq, event);
		} else if (applyGradeCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doApplyGrade(ureq);
			}
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(resetAttemptsConfirmationCtrl);
		removeAsListenerAndDispose(applyGradeCtrl);
		removeAsListenerAndDispose(cmc);
		resetAttemptsConfirmationCtrl = null;
		applyGradeCtrl = null;
		cmc = null;
	}
	
	private void doResetAttempts(UserRequest ureq) {
		fireEvent(ureq, Event.CLOSE_EVENT);
		
		resetAttemptsConfirmationCtrl = new ResetAttemptsConfirmationController(ureq, getWindowControl(),
				assessedUserCourseEnv.getCourseEnvironment(), courseNode, assessedIdentity);
		listenTo(resetAttemptsConfirmationCtrl);
		
		String title = translate("tool.reset.attempts");
		cmc = new CloseableModalController(getWindowControl(), "close", resetAttemptsConfirmationCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doReopen(UserRequest ureq) {
		if (scoreEval != null) {
			reopenEvaluation();
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	protected void reopenEvaluation() {
		ScoreEvaluation reopenedEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), AssessmentEntryStatus.inReview,
				scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
				AssessmentRunStatus.running, scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(courseNode, reopenedEval, assessedUserCourseEnv,
				getIdentity(), false, Role.coach);
	}
	
	private void doSetDone(UserRequest ureq) {
		if (scoreEval != null) {
			doneEvalution();
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	protected void doneEvalution() {
		ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
				scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), AssessmentEntryStatus.done,
				scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
				scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, assessedUserCourseEnv, getIdentity(),
				false, Role.coach);
	}
	
	private void doSetVisibility(UserRequest ureq, boolean visible) {
		if (scoreEval != null) {
			ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getGrade(),
					scoreEval.getPerformanceClassIdent(), scoreEval.getPassed(), scoreEval.getAssessmentStatus(),
					Boolean.valueOf(visible), scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
					scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, assessedUserCourseEnv,
					getIdentity(), false, Role.coach);
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doConfirmApplyGrade(UserRequest ureq) {
		fireEvent(ureq, Event.CLOSE_EVENT);
		
		if (scoreEval != null && scoreEval.getScore() != null) {
			GradeScale gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
			NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
			GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, scoreEval.getScore());
			String grade = gradeScoreRange.getGrade();
			Boolean passed = Mode.none != assessmentConfig.getPassedMode()
					? Boolean.valueOf(gradeScoreRange.isPassed())
					: null;
			
			String text = null;
			if (passed != null) {
				if (passed.booleanValue()) {
					text = translate("grade.apply.text.passed", grade);
				} else {
					text = translate("grade.apply.text.failed", grade);
				}
			} else {
				text = translate("grade.apply.text", grade);
			}
			String title = translate("grade.apply");
			applyGradeCtrl = activateYesNoDialog(ureq, title, text, applyGradeCtrl);
		}
	}
	
	private void doApplyGrade(UserRequest ureq) {
		if (scoreEval != null && scoreEval.getScore() != null) {
			GradeScale gradeScale = gradeService.getGradeScale(courseEntry, courseNode.getIdent());
			NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, getLocale());
			GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, scoreEval.getScore());
			String grade = gradeScoreRange.getGrade();
			String performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
			Boolean passed = Mode.none != assessmentConfig.getPassedMode()
					? Boolean.valueOf(gradeScoreRange.isPassed())
					: null;
			
			ScoreEvaluation doneEval = new ScoreEvaluation(scoreEval.getScore(), grade,
					performanceClassIdent, passed, scoreEval.getAssessmentStatus(), scoreEval.getUserVisible(),
					scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
					scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
			courseAssessmentService.updateScoreEvaluation(courseNode, doneEval, assessedUserCourseEnv,
					getIdentity(), false, Role.coach);
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
}
