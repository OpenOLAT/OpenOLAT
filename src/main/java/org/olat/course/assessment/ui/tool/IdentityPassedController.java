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

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.Overridable;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 Mar 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IdentityPassedController extends BasicController {

	private final VelocityContainer mainVC;
	private final Link passLink;
	private final Link failLink;
	private final Link resetLink;
	private final ProgressBar completionItem;

	private final UserCourseEnvironment assessedUserCourseEnv;
	private final boolean readOnly;
	private final boolean canModify;

	@Autowired
	private UserManager userManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	protected IdentityPassedController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment coachCourseEnv,
			UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl);
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.readOnly = coachCourseEnv.isCourseReadOnly();
		
		boolean passedManually = coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode()
				.getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_PASSED_MANUALLY);
		canModify = (passedManually || coachCourseEnv.isAdmin());
		
		mainVC = createVelocityContainer("passed");

		passLink = LinkFactory.createLink("passed.manually.pass", "assed.manually.pass", getTranslator(), mainVC, this, Link.BUTTON);
		passLink.setIconLeftCSS("o_icon o_icon_passed");
		failLink = LinkFactory.createLink("passed.manually.fail", "assed.manually.fail", getTranslator(), mainVC, this, Link.BUTTON);
		failLink.setIconLeftCSS("o_icon o_icon_failed");
		resetLink = LinkFactory.createLink("passed.manually.reset", "assed.manually.reset", getTranslator(), mainVC, this, Link.BUTTON);
		resetLink.setIconLeftCSS("o_icon o_icon_reset_data");
		
		completionItem = new ProgressBar("completion", 100, 0, Float.valueOf(100), "%");
		completionItem.setWidthInPercent(true);
		completionItem.setLabelAlignment(LabelAlignment.none);
		completionItem.setLabelMaxEnabled(false);
		completionItem.setRenderStyle(RenderStyle.radial);
		completionItem.setRenderSize(RenderSize.small);
		mainVC.put("completion", completionItem);
		
		refresh();
		putInitialPanel(mainVC);
	}
	
	void refresh() {
		Overridable<Boolean> passedOverridable = courseAssessmentService.getRootPassed(assessedUserCourseEnv);
		updateUI(passedOverridable);
	}

	private void updateUI(Overridable<Boolean> passedOverridable) {
		mainVC.contextPut("message", getMessage(passedOverridable));
		
		Boolean current = passedOverridable.getCurrent();
		boolean passed = current != null && current.booleanValue();
		boolean failed = current != null && !current.booleanValue();
		passLink.setVisible(!readOnly && canModify && !passedOverridable.isOverridden() && !passed);
		failLink.setVisible(!readOnly && canModify && !passedOverridable.isOverridden() && !failed);
		resetLink.setVisible(!readOnly && canModify && passedOverridable.isOverridden());
		
		CourseNode rootNode = assessedUserCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		AssessmentEvaluation assessmentEvaluation = assessedUserCourseEnv.getScoreAccounting().getScoreEvaluation(rootNode);
		Double completion = assessmentEvaluation.getCompletion();
		if(completion != null) {
			BarColor barColor = failed ? BarColor.danger : BarColor.success;
			completionItem.setBarColor(barColor);
			completionItem.setActual(completion.floatValue() * 100f);
		}
		
		if(passed) {
			mainVC.contextPut("completionPassed", Boolean.TRUE);
		} else if(failed) {
			mainVC.contextPut("completionPassed", Boolean.FALSE);
		} else {
			mainVC.contextRemove("completionPassed");
		}
			
		Float score = assessmentEvaluation.getScore();
		Float maxScore = assessmentEvaluation.getMaxScore();
		if (score != null && score.floatValue() > 0.0f) {
			String scoreStr = Integer.toString(Math.round(score.floatValue())); 
			if(maxScore != null && maxScore.floatValue() > 0.0f) {
				String maxScoreStr = Integer.toString(Math.round(maxScore.floatValue()));
				completionItem.setInfo(translate("progress.score.w.max", scoreStr, maxScoreStr));
			} else {
				completionItem.setInfo(translate("progress.score", scoreStr));
			}
		}
		completionItem.setVisible(completion != null);
	}

	private String getMessage(Overridable<Boolean> passedOverridable) {
		String message = null;

		Formatter formatter = Formatter.getInstance(getLocale());
		if (passedOverridable.isOverridden()) {
			String messageOriginal;
			if (passedOverridable.getOriginal() == null) {
				messageOriginal = translate("passed.manually.message.original.null");
			} else if (passedOverridable.getOriginal().booleanValue()) {
				messageOriginal = translate("passed.manually.message.original.passed");
			} else {
				messageOriginal = translate("passed.manually.message.original.failed");
			}
			
			String[] args = new String[] {
					userManager.getUserDisplayName(passedOverridable.getModBy()),
					formatter.formatDateAndTime(passedOverridable.getModDate()),
					messageOriginal
			};
			message = passedOverridable.getCurrent().booleanValue()
					? translate("passed.manually.message.overriden.passed", args)
					: translate("passed.manually.message.overriden.failed", args);
		} else {
			// Not overridden
			if (passedOverridable.getCurrent() == null) {
				message = translate("passed.manually.message.null");
			} else if (passedOverridable.getCurrent().booleanValue()) {
				Date passedDate = passedOverridable.getDate();
				if(passedDate != null) {
					message = translate("passed.manually.message.passed.date",
							formatter.formatDateAndTime(passedOverridable.getDate()));
				} else {
					message = translate("passed.manually.message.passed");
				}
			} else {
				message = translate("passed.manually.message.failed");
			}
		}
		return message;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == passLink) {
			doPass(ureq);
		} else if (source == failLink) {
			doFail(ureq);
		} else if (source == resetLink) {
			doReset(ureq);
		}
	}

	private void doPass(UserRequest ureq) {
		doOverride(ureq, Boolean.TRUE);
	}

	private void doFail(UserRequest ureq) {
		doOverride(ureq, Boolean.FALSE);
	}

	private void doOverride(UserRequest ureq, Boolean passed) {
		Overridable<Boolean> passedOverridable = courseAssessmentService.overrideRootPassed(getIdentity(),
				assessedUserCourseEnv, passed);
		updateUI(passedOverridable);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private void doReset(UserRequest ureq) {
		Overridable<Boolean> passedOverridable = courseAssessmentService.resetRootPassed(getIdentity(),
				assessedUserCourseEnv);
		updateUI(passedOverridable);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}
