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

import java.util.Set;

import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configure the lecture module.
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureSettingsAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private TextElement attendanceRateEl, appealPeriodEl, reminderPeriodEl,
		autoClosePeriodEl;
	private MultipleSelectionElement enableEl,  calculateAttendanceRateEnableEl,
		appealAbsenceEnableEl, statusEnabledEl, authorizedAbsenceEnableEl,
		countAuthorizedAbsenceAsAttendantEl, syncTeachersCalendarEnableEl,
		syncParticipantsCalendarEnableEl, teacherCanAuthorizeAbsenceEl,
		reminderEnableEl, rollCallEnableEl;

	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private UserToolsModule userToolsModule;
	
	public LectureSettingsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("lecture.admin.title");
		
		String[] onValues = new String[] { "" };
		enableEl = uifactory.addCheckboxesHorizontal("lecture.admin.enabled", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(lectureModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}

		String[] statusKeys = new String[]{
				LectureBlockStatus.partiallydone.name(), LectureBlockStatus.cancelled.name()
			};
		String[] statusValues = new String[]{
				translate(LectureBlockStatus.partiallydone.name()), translate(LectureBlockStatus.cancelled.name())
			};
		statusEnabledEl = uifactory.addCheckboxesVertical("lecture.status.enabled", formLayout, statusKeys, statusValues, 1);
		if(lectureModule.isStatusPartiallyDoneEnabled()) {
			statusEnabledEl.select(LectureBlockStatus.partiallydone.name(), true);
		}
		if(lectureModule.isStatusCancelledEnabled()) {
			statusEnabledEl.select(LectureBlockStatus.cancelled.name(), true);
		}
		
		// reminder enabled
		reminderEnableEl = uifactory.addCheckboxesHorizontal("lecture.reminder.enabled", formLayout, onKeys, onValues);
		reminderEnableEl.addActionListener(FormEvent.ONCHANGE);
		if(lectureModule.isRollCallReminderEnabled()) {
			reminderEnableEl.select(onKeys[0], true);
		}
		String reminderPeriod = "";
		if(lectureModule.getRollCallReminderPeriod() > 0) {
			reminderPeriod = Integer.toString(lectureModule.getRollCallReminderPeriod());
		}
		reminderPeriodEl = uifactory.addTextElement("lecture.reminder.period", "lecture.reminder.period", 16, reminderPeriod, formLayout);
		reminderPeriodEl.setMandatory(true);

		// auto close period
		String autoClosePeriod = "";
		if(lectureModule.getRollCallAutoClosePeriod() > 0) {
			autoClosePeriod = Integer.toString(lectureModule.getRollCallAutoClosePeriod());
		}
		autoClosePeriodEl = uifactory.addTextElement("lecture.auto.close.period", "lecture.auto.close.period", 16, autoClosePeriod, formLayout);
		autoClosePeriodEl.setMandatory(true);

		// roll call enabled
		rollCallEnableEl = uifactory.addCheckboxesHorizontal("lecture.rollcall.default.enabled", formLayout, onKeys, onValues);
		rollCallEnableEl.addActionListener(FormEvent.ONCHANGE);
		if(lectureModule.isRollCallDefaultEnabled()) {
			rollCallEnableEl.select(onKeys[0], true);
		}

		authorizedAbsenceEnableEl = uifactory.addCheckboxesHorizontal("lecture.authorized.absence.enabled", formLayout, onKeys, onValues);
		authorizedAbsenceEnableEl.addActionListener(FormEvent.ONCHANGE);
		if(lectureModule.isAuthorizedAbsenceEnabled()) {
			authorizedAbsenceEnableEl.select(onKeys[0], true);
		}
		countAuthorizedAbsenceAsAttendantEl = uifactory.addCheckboxesHorizontal("lecture.count.authorized.absence.attendant", formLayout, onKeys, onValues);
		if(lectureModule.isCountAuthorizedAbsenceAsAttendant()) {
			countAuthorizedAbsenceAsAttendantEl.select(onKeys[0], true);
		}
		teacherCanAuthorizeAbsenceEl = uifactory.addCheckboxesHorizontal("lecture.teacher.can.authorize.absence", formLayout, onKeys, onValues);
		if(lectureModule.isTeacherCanAuthorizedAbsence()) {
			teacherCanAuthorizeAbsenceEl.select(onKeys[0], true);
		}
		
		// appeal enabled
		appealAbsenceEnableEl = uifactory.addCheckboxesHorizontal("lecture.appeal.absence.enabled", formLayout, onKeys, onValues);
		appealAbsenceEnableEl.addActionListener(FormEvent.ONCHANGE);
		if(lectureModule.isAbsenceAppealEnabled()) {
			appealAbsenceEnableEl.select(onKeys[0], true);
		}
		String appealPeriod = "";
		if(lectureModule.getAbsenceAppealPeriod() > 0) {
			appealPeriod = Integer.toString(lectureModule.getAbsenceAppealPeriod());
		}
		appealPeriodEl = uifactory.addTextElement("lecture.appeal.absence.period", "lecture.appeal.absence.period", 16, appealPeriod, formLayout);
		appealPeriodEl.setMandatory(true);

		// calculate attendance
		calculateAttendanceRateEnableEl = uifactory.addCheckboxesHorizontal("lecture.calculate.attendance.rate.default.enabled", formLayout, onKeys, onValues);
		if(lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled()) {
			calculateAttendanceRateEnableEl.select(onKeys[0], true);
		}
		long attendanceRate = Math.round(lectureModule.getRequiredAttendanceRateDefault() * 100.0d);
		String attendanceRateStr = Long.toString(attendanceRate);
		attendanceRateEl = uifactory.addTextElement("lecture.attendance.rate.default", "lecture.attendance.rate.default", 2, attendanceRateStr, formLayout);
		attendanceRateEl.setMandatory(true);
		attendanceRateEl.setDisplaySize(2);

		// sync calendars
		syncTeachersCalendarEnableEl = uifactory.addCheckboxesHorizontal("sync.teachers.calendar.enabled", formLayout, onKeys, onValues);
		if(lectureModule.isTeacherCalendarSyncEnabledDefault()) {
			syncTeachersCalendarEnableEl.select(onKeys[0], true);
		}
		syncParticipantsCalendarEnableEl = uifactory.addCheckboxesHorizontal("sync.participants.calendar.enabled", formLayout, onKeys, onValues);
		if(lectureModule.isParticipantCalendarSyncEnabledDefault()) {
			syncParticipantsCalendarEnableEl.select(onKeys[0], true);
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		updateUI();
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		authorizedAbsenceEnableEl.setVisible(enabled);
		attendanceRateEl.setVisible(enabled);
		appealAbsenceEnableEl.setVisible(enabled);
		authorizedAbsenceEnableEl.setVisible(enabled);
		reminderEnableEl.setVisible(enabled);
		syncTeachersCalendarEnableEl.setVisible(enabled);
		syncParticipantsCalendarEnableEl.setVisible(enabled);
		autoClosePeriodEl.setVisible(enabled);
		statusEnabledEl.setVisible(enabled);
		rollCallEnableEl.setVisible(enabled);
		calculateAttendanceRateEnableEl.setVisible(enabled);
		
		appealPeriodEl.setVisible(appealAbsenceEnableEl.isVisible() && appealAbsenceEnableEl.isAtLeastSelected(1));
		reminderPeriodEl.setVisible(reminderEnableEl.isVisible() && reminderEnableEl.isAtLeastSelected(1));
		
		countAuthorizedAbsenceAsAttendantEl.setVisible(authorizedAbsenceEnableEl.isVisible() && authorizedAbsenceEnableEl.isAtLeastSelected(1));
		teacherCanAuthorizeAbsenceEl.setVisible(authorizedAbsenceEnableEl.isVisible() && authorizedAbsenceEnableEl.isAtLeastSelected(1));
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		attendanceRateEl.clearError();
		if(StringHelper.containsNonWhitespace(attendanceRateEl.getValue())) {
			try {
				int val = Integer.parseInt(attendanceRateEl.getValue());
				if(val <= 0 && val > 100) {
					attendanceRateEl.setErrorKey("error.integer.between", new String[] {"1", "100"});
					allOk &= false;
				}
			} catch (Exception e) {
				attendanceRateEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		} else {
			attendanceRateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		allOk &= validateInt(autoClosePeriodEl);
		
		appealPeriodEl.clearError();
		if(appealAbsenceEnableEl.isVisible() && appealAbsenceEnableEl.isAtLeastSelected(1)) {
			allOk &= validateInt(appealPeriodEl);
		}
		
		reminderPeriodEl.clearError();
		if(reminderEnableEl.isVisible() && reminderEnableEl.isAtLeastSelected(1)) {
			allOk &= validateInt(reminderPeriodEl);
		}
		
		return allOk & super.validateFormLogic(ureq);
	}
	
	private boolean validateInt(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())) {
			try {
				int val = Integer.parseInt(el.getValue());
				if(val <= 0) {
					el.setErrorKey("error.integer.positive", null);
					allOk &= false;
				}
			} catch (Exception e) {
				el.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		} else {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source || appealAbsenceEnableEl == source || reminderEnableEl == source
				|| authorizedAbsenceEnableEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		lectureModule.setEnabled(enableEl.isAtLeastSelected(1));
		if(enableEl.isAtLeastSelected(1)) {
			//enabled user tool
			Set<String> availableTools = userToolsModule.getAvailableUserToolSet();
			if(!availableTools.contains("org.olat.home.HomeMainController:org.olat.modules.lecture.ui.ParticipantLecturesOverviewController")) {
				availableTools.add("org.olat.home.HomeMainController:org.olat.modules.lecture.ui.ParticipantLecturesOverviewController");
			}
			
			StringBuilder aTools = new StringBuilder();
			for(String selectedKey:availableTools) {
				if(aTools.length() > 0) aTools.append(",");
				aTools.append(selectedKey);
			}
			userToolsModule.setAvailableUserTools(aTools.toString());
		}
		
		int autoClosePeriod = Integer.parseInt(autoClosePeriodEl.getValue());
		lectureModule.setRollCallAutoClosePeriod(autoClosePeriod);
		
		boolean authorizedAbsenceenabled = authorizedAbsenceEnableEl.isAtLeastSelected(1);
		lectureModule.setAuthorizedAbsenceEnabled(authorizedAbsenceEnableEl.isAtLeastSelected(1));
		lectureModule.setCountAuthorizedAbsenceAsAttendant(authorizedAbsenceenabled && countAuthorizedAbsenceAsAttendantEl.isAtLeastSelected(1));
		lectureModule.setTeacherCanAuthorizedAbsence(authorizedAbsenceenabled && teacherCanAuthorizeAbsenceEl.isAtLeastSelected(1));
		
		lectureModule.setAbsenceAppealEnabled(appealAbsenceEnableEl.isAtLeastSelected(1));
		if(appealAbsenceEnableEl.isAtLeastSelected(1)) {
			int period = Integer.parseInt(appealPeriodEl.getValue());
			lectureModule.setAbsenceAppealPeriod(period);
		}
		
		lectureModule.setRollCallReminderEnabled(reminderEnableEl.isAtLeastSelected(1));
		if(reminderEnableEl.isAtLeastSelected(1)) {
			int period = Integer.parseInt(reminderPeriodEl.getValue());
			lectureModule.setRollCallReminderPeriod(period);
		}
		
		lectureModule.setRollCallCalculateAttendanceRateDefaultEnabled(calculateAttendanceRateEnableEl.isAtLeastSelected(1));
		String attendanceRateInPercent = attendanceRateEl.getValue();
		if(StringHelper.containsNonWhitespace(attendanceRateInPercent)) {
			double val = Double.parseDouble(attendanceRateInPercent) / 100.0d;
			lectureModule.setRequiredAttendanceRateDefault(val);
		}

		lectureModule.setTeacherCalendarSyncEnabledDefault(syncTeachersCalendarEnableEl.isAtLeastSelected(1));
		lectureModule.setParticipantCalendarSyncEnabledDefault(syncParticipantsCalendarEnableEl.isAtLeastSelected(1));
	}
}