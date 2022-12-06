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
package org.olat.modules.lecture.ui.component;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 20 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureBlockRollCallStatusComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		
		LectureBlockRollCallStatusComponent cmp = (LectureBlockRollCallStatusComponent)source;
		String fId = cmp.getFormDispatchId();

		String iconCssClass;
		String title;
		int numOfLectures = cmp.getPlannedLecturesNumber();
		if(cmp.getRollCall() != null && cmp.getRollCall().getAbsenceNotice() != null) {
			iconCssClass = "o_lectures_rollcall_notice";
			switch(cmp.getRollCall().getAbsenceNotice().getNoticeType()) {
				case notified: title = cmp.getTranslator().translate("rollcall.absence.notice"); break;
				case dispensation: title = cmp.getTranslator().translate("rollcall.dispensation"); break;
				case absence:
				default: title = cmp.getTranslator().translate("rollcall.absence"); break;
			}	
		} else if(cmp.getLecturesAttendedNumber() >= numOfLectures) {
			iconCssClass = "o_lectures_rollcall_ok";
			title = cmp.getTranslator().translate("rollcall.tooltip.ok");
		} else if(cmp.getRollCall() == null || cmp.getRollCall().getRollCall() == null) {
			iconCssClass = "o_lectures_rollcall_ok";
			title = cmp.getTranslator().translate("rollcall.tooltip.pending");
		} else if(cmp.isAuthorizedAbsenceEnabled()) {
			if(cmp.isAbsenceDefaultAuthorized()) {
				if(cmp.isLecturesAuthorizedAbsent() || cmp.getRollCall().getRollCall().getAbsenceAuthorized() == null) {
					iconCssClass = "o_lectures_rollcall_warning";
					title = cmp.getTranslator().translate("rollcall.tooltip.authorized.absence");
				} else {
					iconCssClass = "o_lectures_rollcall_danger";
					title = cmp.getTranslator().translate("rollcall.tooltip.unauthorized.absence");
				}
			} else if(cmp.isLecturesAuthorizedAbsent()) {
				iconCssClass = "o_lectures_rollcall_warning";
				title = cmp.getTranslator().translate("rollcall.tooltip.authorized.absence");
			} else {
				iconCssClass = "o_lectures_rollcall_danger";
				title = cmp.getTranslator().translate("rollcall.tooltip.unauthorized.absence");
			}
		} else {
			iconCssClass = "o_lectures_rollcall_danger";
			title = cmp.getTranslator().translate("rollcall.tooltip.absence");
		}
		
		sb.append("<span id='").append(fId).append("' title='")
			.append(title).append("'><i class='o_icon o_icon-lg ")
			.append(iconCssClass).append("'> </i>");
		if(cmp.isWithNumOfLectures()) {
			sb.append(" ").append(cmp.getLecturesAbsentNumber())
			  .append(" / ")
			  .append(numOfLectures);
		}
		if(cmp.isWithExplanation()) {
			sb.append(" ").append(title);
		}
		sb.append("</span>");
	}
}
