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
package org.olat.modules.grading.ui.confirmation;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.grading.GradingAssignment;
import org.olat.modules.grading.GradingService;
import org.olat.modules.grading.ui.GradingRepositoryOverviewController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmReopenAssignmentController extends FormBasicController {
	
	private final GradingAssignment assignment;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private GradingService gradingService;
	
	public ConfirmReopenAssignmentController(UserRequest ureq, WindowControl wControl, GradingAssignment assignment) {
		super(ureq, wControl, "confirm_reopen", Util.createPackageTranslator(GradingRepositoryOverviewController.class, ureq.getLocale()));
		this.assignment = assignment;
		initForm(ureq);
	}
	
	public GradingAssignment getAssignment() {
		return assignment;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			Identity grader = assignment.getGrader().getIdentity();
			String graderFullname = userManager.getUserDisplayName(grader);
			String msg = translate("confirm.reopen.assignment", new String[] { graderFullname });
			layoutCont.contextPut("msg", msg);	
		}

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("tool.reopen.assignment", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		gradingService.reopenAssignment(assignment, null);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
