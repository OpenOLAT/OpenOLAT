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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskReviewAssignment;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.ui.GTAParticipantController;
import org.olat.modules.assessment.Role;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmCloseReviewsController extends FormBasicController {
	
	private Task task;
	private final GTACourseNode gtaNode;
	private List<TaskReviewAssignment> assignments;
	
	@Autowired
	private GTAManager gtaManager;
	
	public ConfirmCloseReviewsController(UserRequest ureq, WindowControl wControl,
			List<TaskReviewAssignment> assignments, Task assignedTask, GTACourseNode gtaNode) {
		super(ureq, wControl, "confirm_close_peerreview", Util.createPackageTranslator(GTAParticipantController.class, ureq.getLocale()));
		this.assignments = assignments;
		this.task = assignedTask;
		this.gtaNode = gtaNode;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			int numOfReviews = 0;
			int numOfReviewsDone = 0;
			for(TaskReviewAssignment assignment:assignments) {
				numOfReviews++;
				if(assignment.getStatus() == TaskReviewAssignmentStatus.done
						|| assignment.getStatus() == TaskReviewAssignmentStatus.invalidate) {
					// Invalidate but not accepted from coach
					numOfReviewsDone++;
				}
			}
			String msg = translate("confirm.close.reviews.desc", Integer.toString(numOfReviewsDone), Integer.toString(numOfReviews));
			layoutCont.contextPut("msg", msg);
		}
		uifactory.addFormSubmitButton("close.peer.reviews", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		task = gtaManager.submitReviews(task, gtaNode, getIdentity(), Role.user);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
