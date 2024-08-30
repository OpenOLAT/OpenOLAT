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
package org.olat.course.nodes.gta.ui.workflow;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.AssignmentType;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAPeerReviewManager;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * Initial date: 25 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmAutomaticAssignmentController extends FormBasicController {
	
	private final TaskList taskList;
	private final GTACourseNode gtaNode;
	private final RepositoryEntry courseEntry;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private GTAPeerReviewManager peerReviewManager;
	
	public ConfirmAutomaticAssignmentController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, TaskList taskList, GTACourseNode gtaNode) {
		super(ureq, wControl, "confirm_assignment", Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
		this.gtaNode = gtaNode;
		this.taskList = taskList;
		this.courseEntry = courseEntry;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		FormSubmit assignButton = uifactory.addFormSubmitButton("automatic.assignment", formLayout);
		assignButton.setIconLeftCSS("o_icon o_icon-fw o_icon_mix");
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		final String typeOfAssignment = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT,
				GTACourseNode.GTASK_PEER_REVIEW_ASSIGNMENT_DEFAULT);
		AssignmentType assignmentType = AssignmentType.keyOf(typeOfAssignment);
		if(assignmentType == AssignmentType.RANDOM && !gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			// Generate missing tasks
			gtaManager.ensureTasksExist(taskList, courseEntry, gtaNode);
			dbInstance.commitAndCloseSession();
		}
		peerReviewManager.assign(courseEntry, taskList, gtaNode);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
