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

import java.util.Locale;

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.nodes.gta.TaskReviewAssignmentStatus;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.peerreview.CoachPeerReviewRow;
import org.olat.course.nodes.gta.ui.peerreview.ParticipantPeerReviewAssignmentRow;

/**
 * 
 * Initial date: 8 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaskReviewAssignmentStatusCellRenderer extends LabelCellRenderer {

	private final boolean light;
	private final Translator trans;
	
	public TaskReviewAssignmentStatusCellRenderer(Locale locale, boolean light) {
		this.light = light;
		trans = Util.createPackageTranslator(AssessmentStatusCellRenderer.class, locale,
				Util.createPackageTranslator(GTACoachController.class, locale));
	}

	@Override
	protected boolean isLabelLight() {
		return light;
	}

	@Override
	protected boolean isNullRendered() {
		return true;
	}
	
	public String render(TaskReviewAssignmentStatus status) {
		StringOutput stringOutput = new StringOutput();
		render(stringOutput, trans, status);
		return stringOutput.toString();
	}

	public static TaskReviewAssignmentStatus getStatus(Object val) {
		if(val instanceof ParticipantPeerReviewAssignmentRow assignment) {
			return assignment.getStatus();
		}
		if(val instanceof CoachPeerReviewRow peerReviewRow) {
			if(peerReviewRow.getParent() != null) {
				return peerReviewRow.getAssignmentStatus();
			}
			
			if(peerReviewRow.getChildrenRows() != null && !peerReviewRow.getChildrenRows().isEmpty()) {
				boolean onlyOpen = true;
				boolean onlyDone = true;
				for(CoachPeerReviewRow childRow:peerReviewRow.getChildrenRows()) {
					if(childRow.getAssignmentStatus() == TaskReviewAssignmentStatus.invalidate
							|| childRow.getAssignmentStatus() == TaskReviewAssignmentStatus.disabled) {
						continue;
					}
					
					if(childRow.getAssignmentStatus() != null && childRow.getAssignmentStatus() != TaskReviewAssignmentStatus.open) {
						onlyOpen &= false;
					}
					if(childRow.getAssignmentStatus() != null && childRow.getAssignmentStatus() != TaskReviewAssignmentStatus.done) {
						onlyDone &= false;
					}
				}
				if(onlyDone) {
					return TaskReviewAssignmentStatus.done;
				}
				return onlyOpen ? TaskReviewAssignmentStatus.open : TaskReviewAssignmentStatus.inProgress;
			}
		}
		return null;
	}

	@Override
	protected String getIconCssClass(Object val) {
		return null;
	}

	@Override
	protected String getCellValue(Object val, Translator translator) {
		return getLabel(val);
	}
	
	private String getLabel(Object val) {
		TaskReviewAssignmentStatus status = getStatus(val);
		if(status == TaskReviewAssignmentStatus.inProgress) {
			return trans.translate("assessment.evaluation.status.inProgress");
		} else if(status == TaskReviewAssignmentStatus.done) {
			return trans.translate("task.review.status.done");	
		} else if(status == TaskReviewAssignmentStatus.invalidate) {
			return trans.translate("assessment.evaluation.status.invalid");	
		}
		return trans.translate("assessment.evaluation.status.open");
	}
	
	@Override
	protected String getElementCssClass(Object val) {
		TaskReviewAssignmentStatus status = getStatus(val);
		if(status == null) {
			return "o_evaluation_open";
		}
		return switch(status) {
			case inProgress -> "o_evaluation_in_progress";
			case done -> "o_evaluation_done";
			case invalidate, disabled -> "o_evaluation_invalid";
			default -> "o_evaluation_open";
		};
	}
}
