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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskDueDate;
import org.olat.course.nodes.gta.TaskHelper;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 août 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRevisionsController extends FormBasicController {
	
	private DateChooser revisionDueDateEl;
	
	private Task assignedTask;
	private final GTACourseNode gtaNode;
	private final Identity assessedIdentity;
	private final BusinessGroup assessedGroup;
	private final CourseEnvironment courseEnv;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GTAManager gtaManager;
	
	public ConfirmRevisionsController(UserRequest ureq, WindowControl wControl, Task assignedTask,
			Identity assessedIdentity, BusinessGroup assessedGroup,
			GTACourseNode gtaNode, CourseEnvironment courseEnv) {
		super(ureq, wControl);
		this.gtaNode = gtaNode;
		this.courseEnv = courseEnv;
		this.assignedTask = assignedTask;
		this.assessedGroup = assessedGroup;
		this.assessedIdentity = assessedIdentity;
		initForm(ureq);
	}
	
	public Task getTask() {
		return assignedTask;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		File documentsDir;
		int iteration = assignedTask == null ? 0 : assignedTask.getRevisionLoop();
		if(assessedGroup != null) {
			documentsDir = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, iteration, assessedGroup);
		} else {
			documentsDir = gtaManager.getRevisedDocumentsCorrectionsDirectory(courseEnv, gtaNode, iteration, assessedIdentity);
		}
		
		boolean hasDocument = TaskHelper.hasDocuments(documentsDir);
		if(!hasDocument) {
			setFormWarning("coach.revisions.confirm.text.warn");
		}
		
		setFormDescription("coach.revisions.confirm.text");
		
		Date revisionsDueDate = assignedTask == null ? null : assignedTask.getRevisionsDueDate();
		revisionDueDateEl = uifactory.addDateChooser("revisions.duedate", revisionsDueDate, formLayout);
		revisionDueDateEl.setDateChooserTimeEnabled(true);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", buttonsCont);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// here special cases if optional with only feedback enabled
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		assignedTask = gtaManager.ensureTaskExists(assignedTask, assessedGroup, assessedIdentity, courseEntry, gtaNode);

		TaskDueDate dueDates = gtaManager.getDueDatesTask(assignedTask);
		dueDates.setRevisionsDueDate(revisionDueDateEl.getDate());
		gtaManager.updateTaskDueDate(dueDates);
		dbInstance.commit();
		// make sure the task is up to date
		assignedTask = gtaManager.getTask(dueDates);
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}