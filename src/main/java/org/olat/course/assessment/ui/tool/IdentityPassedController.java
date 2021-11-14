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
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.course.assessment.CourseAssessmentService;
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

	private VelocityContainer mainVC;
	private Link passLink;
	private Link failLink;
	private Link resetLink;

	private final UserCourseEnvironment assessedUserCourseEnv;
	private final boolean readOnly;
	
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private UserManager userManager;

	protected IdentityPassedController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment coachCourseEnv,
			UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl);
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.readOnly = coachCourseEnv.isCourseReadOnly();
		
		mainVC = createVelocityContainer("passed");
		
		passLink = LinkFactory.createLink("passed.manually.pass", "assed.manually.pass", getTranslator(), mainVC, this, Link.BUTTON);
		failLink = LinkFactory.createLink("passed.manually.fail", "assed.manually.fail", getTranslator(), mainVC, this, Link.BUTTON);
		resetLink = LinkFactory.createLink("passed.manually.reset", "assed.manually.reset", getTranslator(), mainVC, this, Link.BUTTON);
		
		Overridable<Boolean> passedOverridable = courseAssessmentService.getRootPassed(assessedUserCourseEnv);
		updateUI(passedOverridable);
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
		boolean faild = current != null && !current.booleanValue();
		passLink.setVisible(!readOnly && !passedOverridable.isOverridden() && !passed);
		failLink.setVisible(!readOnly && !passedOverridable.isOverridden() && !faild);
		resetLink.setVisible(!readOnly && passedOverridable.isOverridden());
	}

	private String getMessage(Overridable<Boolean> passedOverridable) {
		String message = null;
		if (passedOverridable.isOverridden()) {
			String messageOriginal;
			if (passedOverridable.getOriginal() == null) {
				messageOriginal = translate("passed.manually.message.original.null");
			} else if (passedOverridable.getOriginal().booleanValue()) {
				messageOriginal = translate("passed.manually.message.original.passed");
			} else {
				messageOriginal = translate("passed.manually.message.original.failed");
			}
			
			Formatter formatter = Formatter.getInstance(getLocale());
			String[] args = new String[] {
					userManager.getUserDisplayName(passedOverridable.getModBy()),
					formatter.formatDateAndTime(passedOverridable.getModDate()),
					messageOriginal
			};
			message = passedOverridable.getCurrent().booleanValue()
					? translate("passed.manually.message.overriden.passed", args)
					: translate("passed.manually.message.overriden.failed", args);
		} else {
			if (passedOverridable.getCurrent() == null) {
				message = translate("passed.manually.message.null");
			} else if (passedOverridable.getCurrent().booleanValue()) {
				message = translate("passed.manually.message.passed");
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
		fireEvent(ureq, FormEvent.CHANGED_EVENT);
	}

	private void doReset(UserRequest ureq) {
		Overridable<Boolean> passedOverridable = courseAssessmentService.resetRootPassed(getIdentity(),
				assessedUserCourseEnv);
		updateUI(passedOverridable);
		fireEvent(ureq, FormEvent.CHANGED_EVENT);
	}
}
