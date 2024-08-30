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
package org.olat.course.nodes.gta.ui.component;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantStatus;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 1 juil. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskStepStatusCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	private final GTACourseNode gtaNode;
	private final GTAManager gtaManager;
	private final RepositoryEntry courseEntry;
	
	public TaskStepStatusCellRenderer(RepositoryEntry courseEntry, GTACourseNode gtaNode, GTAManager gtaManager, Translator translator) {
		this.translator = translator;
		this.gtaManager = gtaManager;
		this.gtaNode = gtaNode;
		this.courseEntry = courseEntry;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(cellValue instanceof CoachedParticipantStatus status) {
			render(target, status.i18nKey(), status.cssClass());
		}
	}
	
	private void render(StringOutput target, String i18nKey, String cssClass) {
		target.append("<span class='o_labeled_light ").append(cssClass).append("'>").append(translator.translate(i18nKey)).append("</span>");
	}
	
	
	public final CoachedParticipantStatus calculateSubmissionStatus(Identity taskIdentity, Task assignedTask) {
		//calculate state
		CoachedParticipantStatus state = null;
		if(gtaNode.getModuleConfiguration().getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment) {
				state = CoachedParticipantStatus.notAvailable;
			} else if (assignedTask.getTaskStatus() == TaskProcess.submit) {
				DueDate submissionDueDate = gtaManager.getSubmissionDueDate(assignedTask, taskIdentity, null, gtaNode, courseEntry, true);
				DueDate lateSubmissionDueDate = gtaManager.getLateSubmissionDueDate(assignedTask, taskIdentity, null, gtaNode, courseEntry, true);
				if(isSubmissionLate(submissionDueDate, lateSubmissionDueDate)) {
					state = CoachedParticipantStatus.late;
				} else {
					state = CoachedParticipantStatus.waiting;
				}
			} else {
				state = CoachedParticipantStatus.done;
			}	
		} else if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.submit) {
			DueDate submissionDueDate = gtaManager.getSubmissionDueDate(assignedTask, taskIdentity, null, gtaNode, courseEntry, true);
			DueDate lateSubmissionDueDate = gtaManager.getLateSubmissionDueDate(assignedTask, taskIdentity, null, gtaNode, courseEntry, true);
			if(isSubmissionLate(submissionDueDate, lateSubmissionDueDate)) {
				state = CoachedParticipantStatus.late;
			} else {
				state = CoachedParticipantStatus.open;
			}
		} else {
			state = CoachedParticipantStatus.done;
		}
		return state;
	}
	
	public final boolean isSubmissionLate(DueDate dueDate, DueDate lateDueDate) {
		if(dueDate == null || dueDate.getDueDate() == null
				|| lateDueDate == null || lateDueDate.getDueDate() == null) {
			return false;
		}
		Date refDate = dueDate.getDueDate();
		return new Date().after(refDate);
	}
}
