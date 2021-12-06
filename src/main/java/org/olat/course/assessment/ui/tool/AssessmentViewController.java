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
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
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
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
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

	private final CourseNode courseNode;
	private final UserCourseEnvironment coachCourseEnv;
	private final UserCourseEnvironment assessedUserCourseEnv;
	private final AssessmentConfig assessmentConfig;

	@Autowired
	private CourseAssessmentService courseAssessmentService;

	protected AssessmentViewController(UserRequest ureq, WindowControl wControl, CourseNode courseNode,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.coachCourseEnv = coachCourseEnv;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(MSCourseNodeRunController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
		assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		

		mainVC = createVelocityContainer("assessment_view");
		
		reopenLink = LinkFactory.createButton("reopen", mainVC, this);
		reopenLink.setElementCssClass("o_sel_assessment_form_reopen");
		reopenLink.setIconLeftCSS("o_icon o_icon_status_in_review");
		updateUserVisibilityUI();
		
		putConfigToVC();
		putAssessmentDataToVC(ureq);
		putInitialPanel(mainVC);
	}

	private void updateUserVisibilityUI() {
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
		
		boolean canChangeUserVisibility = coachCourseEnv.isAdmin()
				|| coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_COACH_USER_VISIBILITY);
		
		if (canChangeUserVisibility) {
			if (assessmentEntry.getUserVisibility() == null || assessmentEntry.getUserVisibility().booleanValue()) {
				Dropdown userVisibility = new Dropdown("user.visibility", "user.visibility.visible", false, getTranslator());
				userVisibility.setIconCSS("o_icon o_icon_results_visible");
				userVisibility.setElementCssClass("o_button_results_visible");
				userVisibility.setOrientation(DropdownOrientation.right);
				userVisibility.setEmbbeded(true);
				userVisibility.setButton(true);
				
				userVisibilityHiddenLink = LinkFactory.createToolLink("user.visibility.hidden", translate("user.visibility.hidden"), this);
				userVisibilityHiddenLink.setIconLeftCSS("o_icon o_icon_results_hidden");
				userVisibilityHiddenLink.setElementCssClass("o_button_results_hidden");
				userVisibility.addComponent(userVisibilityHiddenLink);
				mainVC.put("user.visibility", userVisibility);
			} else {
				Dropdown userVisibility = new Dropdown("user.visibility", "user.visibility.hidden", false, getTranslator());
				userVisibility.setIconCSS("o_icon o_icon_results_hidden");
				userVisibility.setElementCssClass("o_button_results_hidden");
				userVisibility.setOrientation(DropdownOrientation.right);
				userVisibility.setEmbbeded(true);
				userVisibility.setButton(true);
				
				userVisibilityVisibleLink = LinkFactory.createToolLink("user.visibility.visible", translate("user.visibility.visible"), this);
				userVisibilityVisibleLink.setIconLeftCSS("o_icon o_icon_results_visible");
				userVisibilityVisibleLink.setElementCssClass("o_button_results_visible");
				userVisibility.addComponent(userVisibilityVisibleLink);
				mainVC.put("user.visibility", userVisibility);
			}
		} else {
			if (assessmentEntry.getUserVisibility() == null || assessmentEntry.getUserVisibility().booleanValue()) {
				userVisibilityVisibleLink = LinkFactory.createLink("user.visibility", "user.visibility", "vis", "user.visibility.visible", getTranslator(), mainVC, this, Link.BUTTON);
				userVisibilityVisibleLink.setIconLeftCSS("o_icon o_icon_results_visible");
				userVisibilityVisibleLink.setElementCssClass("o_button_results_visible");
				userVisibilityVisibleLink.setEnabled(false);
			} else {
				userVisibilityHiddenLink = LinkFactory.createLink("user.visibility", "user.visibility", "vis", "user.visibility.hidden", getTranslator(), mainVC, this, Link.BUTTON);
				userVisibilityHiddenLink.setIconLeftCSS("o_icon o_icon_results_hidden");
				userVisibilityHiddenLink.setElementCssClass("o_button_results_hidden");
				userVisibilityHiddenLink.setEnabled(false);
			}
		}
	}

	private void putConfigToVC() {
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		mainVC.contextPut("hasScoreField", Boolean.valueOf(hasScore));
		if (hasScore) {
			mainVC.contextPut("scoreMin", AssessmentHelper.getRoundedScore(assessmentConfig.getMinScore()));
			mainVC.contextPut("scoreMax", AssessmentHelper.getRoundedScore(assessmentConfig.getMaxScore()));
		}
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		mainVC.contextPut("hasPassedField", Boolean.valueOf(hasPassed));
		if (hasPassed) {
			mainVC.contextPut("passedCutValue", AssessmentHelper.getRoundedScore(assessmentConfig.getCutValue()));
		}
		mainVC.contextPut("hasCommentField", assessmentConfig.hasComment());
		mainVC.contextPut("hasDocumentField", assessmentConfig.hasIndividualAsssessmentDocuments());
	}

	private void putAssessmentDataToVC(UserRequest ureq) {
		AssessmentEntry assessmentEntry = courseAssessmentService.getAssessmentEntry(courseNode, assessedUserCourseEnv);
		
		mainVC.contextPut("score", AssessmentHelper.getRoundedScore(assessmentEntry.getScore()));
		mainVC.contextPut("hasPassedValue", (assessmentEntry.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
		mainVC.contextPut("passed", assessmentEntry.getPassed());
		mainVC.contextPut("inReview",
				Boolean.valueOf(AssessmentEntryStatus.inReview == assessmentEntry.getAssessmentStatus()));

		String rawComment = assessmentEntry.getComment();
		if (assessmentConfig.hasComment() && StringHelper.containsNonWhitespace(rawComment)) {
			StringBuilder comment = Formatter.stripTabsAndReturns(rawComment);
			mainVC.contextPut("comment", StringHelper.xssScan(comment));
		}

		if (assessmentConfig.hasIndividualAsssessmentDocuments()) {
			List<File> docs = courseAssessmentService.getIndividualAssessmentDocuments(courseNode, assessedUserCourseEnv);
			String mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(docs));
			mainVC.contextPut("docsMapperUri", mapperUri);
			mainVC.contextPut("docs", docs);
			DisplayOrDownloadComponent download = new DisplayOrDownloadComponent("", null);
			mainVC.put("download", download);
		}
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
		ScoreEvaluation eval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getPassed(),
				scoreEval.getAssessmentStatus(), userVisibility,
				scoreEval.getCurrentRunStartDate(), scoreEval.getCurrentRunCompletion(),
				scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		courseAssessmentService.updateScoreEvaluation(courseNode, eval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
		
		updateUserVisibilityUI();
		fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_CHANGED, false));
	}

	private void doReopen(UserRequest ureq) {
		ScoreEvaluation scoreEval = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		ScoreEvaluation eval = new ScoreEvaluation(scoreEval.getScore(), scoreEval.getPassed(),
				AssessmentEntryStatus.inReview, scoreEval.getUserVisible(), scoreEval.getCurrentRunStartDate(),
				scoreEval.getCurrentRunCompletion(), scoreEval.getCurrentRunStatus(), scoreEval.getAssessmentID());
		
		courseAssessmentService.updateScoreEvaluation(courseNode, eval, assessedUserCourseEnv, getIdentity(), false, Role.coach);
		fireEvent(ureq, new AssessmentFormEvent(AssessmentFormEvent.ASSESSMENT_REOPEN, false));
	}

}
