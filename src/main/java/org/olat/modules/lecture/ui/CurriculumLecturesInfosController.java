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
package org.olat.modules.lecture.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.ui.ParticipantLecturesDataModel.LecturesCols;
import org.olat.modules.lecture.ui.component.LectureStatisticsCellRenderer;
import org.olat.modules.lecture.ui.component.RateWarningCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumLecturesInfosController extends FormBasicController {
	
	private FormLink moreLink;
	private FormLink lessLink;

	private final boolean absenceNoticeEnabled;
	private final boolean authorizedAbsenceEnabled;
	
	@Autowired
	private LectureModule lectureModule;
	
	public CurriculumLecturesInfosController(UserRequest ureq, WindowControl wControl, Form rootForm) {
		super(ureq, wControl, LAYOUT_CUSTOM, "curriculum_lectures_overview", rootForm);
		
		absenceNoticeEnabled = lectureModule.isAbsenceNoticeEnabled();
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		
		initForm(ureq);
	}
	
	public void setStatistics(LectureBlockIdentityStatistics statistics) {
		// Reset
		flc.contextRemove(LecturesCols.unauthorizedAbsentLectures.name());
		flc.contextRemove(LecturesCols.authorizedAbsentLectures.name());
		flc.contextRemove(LecturesCols.dispensedLectures.name());
		flc.contextRemove(LecturesCols.absentLectures.name());
		flc.contextRemove("rate");
		flc.contextRemove("warning");
		flc.contextRemove("progress");
		flc.contextRemove("statistics");
		
		// New Data
		long lectures = statistics.getTotalPersonalPlannedLectures();
		if(lectures > 0l) {
			long rounded = Math.round(statistics.getAttendanceRate() * 100.0d);
			flc.contextPut("rate", Long.valueOf(rounded));
			if(authorizedAbsenceEnabled) {
				flc.contextPut(LecturesCols.unauthorizedAbsentLectures.name(), positive(statistics.getTotalAbsentLectures()));
				flc.contextPut(LecturesCols.authorizedAbsentLectures.name(), positive(statistics.getTotalAuthorizedAbsentLectures()));
				if(absenceNoticeEnabled) {
					flc.contextPut(LecturesCols.dispensedLectures.name(), positive(statistics.getTotalDispensationLectures()));
				}
			} else {
				flc.contextPut(LecturesCols.absentLectures.name(), positive(statistics.getTotalAbsentLectures()));
			}
			flc.contextPut("statistics", statistics);
			
			String warning = new RateWarningCellRenderer(getTranslator()).render(statistics);
			flc.contextPut("warning", warning);
			String progress = new LectureStatisticsCellRenderer().render(statistics);
			flc.contextPut("progress", progress);
		}
	}
	
	private Long positive(long val) {
		return val < 0 ? Long.valueOf(0l) : Long.valueOf(val);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("more", Boolean.FALSE);
		}
		
		moreLink = uifactory.addFormLink("more", "more", "...", formLayout, Link.BUTTON | Link.NONTRANSLATED);
		moreLink.setIconLeftCSS("o_icon o_icon_lg o_icon_details_expand");
		moreLink.setElementCssClass("o_button_details");
		lessLink = uifactory.addFormLink("less", "less", "...", formLayout, Link.BUTTON | Link.NONTRANSLATED);
		lessLink.setIconLeftCSS("o_icon o_icon_lg o_icon_details_collaps");
		lessLink.setElementCssClass("o_button_details");
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(moreLink == source) {
			flc.contextPut("more", Boolean.TRUE);
		} else if(lessLink == source) {
			flc.contextPut("more", Boolean.FALSE);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
