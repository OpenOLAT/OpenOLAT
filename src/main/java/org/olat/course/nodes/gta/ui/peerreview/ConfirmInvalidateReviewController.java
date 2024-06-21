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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.assessment.Role;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmInvalidateReviewController extends FormBasicController {
	
	private final GTACourseNode gtaNode;
	private TaskReviewAssignment assignment;
	private final CourseEnvironment courseEnv;

	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private GTAPeerReviewManager peerReviewManager;
	
	public ConfirmInvalidateReviewController(UserRequest ureq, WindowControl wControl,
			TaskReviewAssignment assignment, CourseEnvironment courseEnv, GTACourseNode gtaNode) {
		super(ureq, wControl, "confirm_invalidate_review", Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
		this.assignment = assignment;
		this.courseEnv = courseEnv;
		this.gtaNode = gtaNode;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			String reviewer = userManager.getUserDisplayName(assignment.getAssignee());
			layoutCont.contextPut("msg", translate("confirm.invalidate.review", reviewer));
		}
		
		uifactory.addFormSubmitButton("invalidate.review", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		assignment = peerReviewManager.invalidateAssignment(assignment, gtaNode, getIdentity());
		Identity assessedIdentity = assignment.getTask().getIdentity();
		gtaManager.log("Peer review", "invalidate review", assignment.getTask(), getIdentity(), assessedIdentity, null, courseEnv, gtaNode, Role.coach);
		
		// Recalculate score of both assessed and assigned user
		gtaNode.recalculateAndUpdateScore(courseEnv, assessedIdentity, getIdentity(), getLocale());
		gtaNode.recalculateAndUpdateScore(courseEnv, assignment.getAssignee(), getIdentity(), getLocale());
		fireEvent(ureq, Event.DONE_EVENT);
	}
}