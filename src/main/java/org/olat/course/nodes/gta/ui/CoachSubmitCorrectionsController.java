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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.TaskRevision;
import org.olat.course.nodes.gta.ui.events.NeedRevisionEvent;
import org.olat.course.nodes.gta.ui.events.ReviewedEvent;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoachSubmitCorrectionsController extends SubmitDocumentsController {
	
	private TextElement commentEl;
	private FormLink reviewedButton;
	private FormLink needRevisionsButton;
	
	private TaskRevision taskRevision;
	private final Identity assessedIdentity;
	private final BusinessGroup assessedGroup;

	@Autowired
	private GTAManager gtaManager;
	
	public CoachSubmitCorrectionsController(UserRequest ureq, WindowControl wControl, Task assignedTask,
			TaskRevision taskRevision, Identity assessedIdentity, BusinessGroup assessedGroup, File documentsDir,
			VFSContainer documentsContainer, GTACourseNode cNode, CourseEnvironment courseEnv, boolean readOnly,
			Date deadline, String docI18nKey, VFSContainer submitContainer, String copyEnding, String copyI18nKey) {
		super(ureq, wControl, assignedTask, documentsDir, documentsContainer, -1, -1, cNode, courseEnv, readOnly,
				deadline, docI18nKey, submitContainer, copyEnding, copyI18nKey);
		this.assignedTask = assignedTask;
		this.taskRevision = taskRevision;
		this.assessedGroup = assessedGroup;
		this.assessedIdentity = assessedIdentity;
		if(taskRevision != null) {
			commentEl.setValue(taskRevision.getComment());
		}
	}
	
	public TaskRevision getTaskRevision() {
		return taskRevision;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		commentEl = uifactory.addTextAreaElement("corrections.comment", null, 32000, 4, 60, true, false, "", formLayout);

		reviewedButton = uifactory.addFormLink("coach.reviewed.button", "reviewed", "coach.reviewed.button", null, formLayout, Link.BUTTON);
		reviewedButton.setElementCssClass("o_sel_course_gta_reviewed");
		reviewedButton.setIconLeftCSS("o_icon o_icon_accepted");
		reviewedButton.setPrimary(true);
		reviewedButton.setVisible(!isReadOnly());
		if(config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
			needRevisionsButton = uifactory.addFormLink("coach.need.revision.button", "need-revision", "coach.need.revision.button", null, formLayout, Link.BUTTON);
			needRevisionsButton.setElementCssClass("o_sel_course_gta_need_revision");
			needRevisionsButton.setPrimary(true);
			needRevisionsButton.setVisible(!isReadOnly());
			needRevisionsButton.setIconLeftCSS("o_icon o_icon_rejected");
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			commitComment();
		}
		if(reviewedButton == source) {
			fireEvent(ureq, new ReviewedEvent());
		} else if(needRevisionsButton == source) {
			fireEvent(ureq, new NeedRevisionEvent());
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void commitComment() {
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		assignedTask = gtaManager.ensureTaskExists(assignedTask, assessedGroup, assessedIdentity, courseEntry, gtaNode);
		String comment = commentEl.getValue();
		taskRevision = gtaManager.updateTaskRevisionComment(getAssignedTask(), TaskProcess.correction, 0, comment, getIdentity());
	}
}
