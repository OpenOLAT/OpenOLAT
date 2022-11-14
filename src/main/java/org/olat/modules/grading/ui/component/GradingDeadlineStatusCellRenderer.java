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
package org.olat.modules.grading.ui.component;

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.grading.GradingAssignmentStatus;
import org.olat.modules.grading.ui.GradingAssignmentRow;

/**
 * 
 * Initial date: 23 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GradingDeadlineStatusCellRenderer implements FlexiCellRenderer {
	
	private final Date now;
	private final Translator translator;
	
	public GradingDeadlineStatusCellRenderer(Translator translator) {
		this.translator = translator;
		this.now = new Date();
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator trans) {
		if(cellValue instanceof GradingAssignmentStatus) {
			GradingAssignmentStatus status = (GradingAssignmentStatus)cellValue;
			if(status == GradingAssignmentStatus.unassigned || status == GradingAssignmentStatus.done) {
				renderStatusWithIcon(renderer, target, status);
			} else {
				Object rowObject = source.getFormItem().getTableDataModel().getObject(row);
				if(rowObject instanceof GradingAssignmentRow) {
					GradingAssignmentRow assignmentRow = (GradingAssignmentRow)rowObject;
					render(renderer, target, assignmentRow.getDeadline(), assignmentRow.getExtendedDeadline(), status);
				} else {
					renderStatusWithIcon(renderer, target, status);
				}
			}
		}
	}
	
	public void render(Renderer renderer, StringOutput target, Date deadline, Date extendedDeadline, GradingAssignmentStatus status) {
		if(extendedDeadline != null && !(deadline != null && !deadline.before(extendedDeadline))) {
			deadline =extendedDeadline;
		}
		if(deadline != null) {
			deadline = CalendarUtils.endOfDay(deadline);
		}
		if(deadline != null) {
			if(deadline.before(now)) {
				renderStatus(renderer, target, translator.translate("assignment.status.deadlineMissed"), "o_icon_error");
			} else if(CalendarUtils.isSameDay(deadline, now)) {
				renderStatus(renderer, target, translator.translate("assignment.status.deadlineToday"), "o_icon_warn");
			} else {
				long daysToDeadline = CalendarUtils.numOfDays(now, deadline);
				if(daysToDeadline == 1) {
					renderStatus(renderer, target, translator.translate("assignment.status.deadlineInDay"), "o_icon_warn");
				} else if(daysToDeadline <= 5) {
					renderStatus(renderer, target, translator.translate("assignment.status.deadlineInDays", new String[] { Long.toString(daysToDeadline) }), "o_icon_important");
				} else {
					renderStatus(renderer, target, translator.translate("assignment.status.deadlineInDays", new String[] { Long.toString(daysToDeadline) }), "o_grad_assignment_assigned");
				}
			}
		} else {
			renderStatusWithIcon(renderer, target, status);
		}
	}
	
	private void renderStatusWithIcon(Renderer renderer, StringOutput target, GradingAssignmentStatus status) {
		String iconCssClass = status.iconCssClass(); 
		String label = translator.translate("assignment.status.".concat(status.name()));
		renderStatus(renderer, target, label, iconCssClass);
	}
	
	private static void renderStatus(Renderer renderer, StringOutput target, String label, String iconCssClass) {
		if(renderer == null) {
			target.append(label);
		} else {
			target.append("<span><i class='o_icon ")
			      .append(iconCssClass).append("'> </i> ")
			      .append(label).append("</span>");
		}
	}
}
