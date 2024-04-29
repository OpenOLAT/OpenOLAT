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
package org.olat.course.assessment.ui.inspection.elements;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionRow;

/**
 * 
 * Initial date: 5 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InspectionStatusCellRenderer implements FlexiCellRenderer {
	
	private Date now;
	private final Translator translator;
	
	public InspectionStatusCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator transl) {
		if(row == 0 || now == null) {
			now = new Date();
		}
		
		if(cellValue instanceof AssessmentInspectionRow inspectionRow) {
			AssessmentInspectionStatusEnum status = inspectionRow.getInspectionStatus();
			if(status == AssessmentInspectionStatusEnum.scheduled) {
				Date start = inspectionRow.getFromDate();
				Date end = inspectionRow.getToDate();
				if(end != null && end.before(now)) {
					render(target, AssessmentInspectionStatusEnum.noShow.name());
				} else if(start != null && start.before(now)) {
					render(target, "active");
				} else {
					render(target, status.name());
				}
			} else if(status == AssessmentInspectionStatusEnum.inProgress) {
				Date end = inspectionRow.getEndTime();
				Date to = inspectionRow.getToDate();
				if((end != null && end.before(now)) || (to != null && to.before(now))) {
					render(target, AssessmentInspectionStatusEnum.carriedOut.name());
				} else {
					render(target, status.name());
				}
			} else {
				render(target, status.name());
			}
		} else if(cellValue instanceof AssessmentInspectionStatusEnum status) {
			render(target, status.name());
		}
	}
	
	private void render(StringOutput target, String status) {
		String statusLC = status.toLowerCase();
		target.append("<span class='o_labeled_light o_assessment_inspection_").append(statusLC).append("'>")
		      .append("<i class='o_icon o_icon-fw o_icon_assessment_inspection_").append(statusLC).append("'> </i> ")
		      .append(translator.translate("inspection.status.".concat(status)))
		      .append("</span>");
	}
}
