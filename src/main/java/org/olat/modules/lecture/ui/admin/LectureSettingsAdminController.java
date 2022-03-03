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
package org.olat.modules.lecture.ui.admin;

import java.util.Set;

import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.lecture.DailyRollCall;
import org.olat.modules.lecture.LectureBlockStatus;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
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
	private static final String[] yesNoKeys = new String[] { "yes", "no" };
	private static final String[] showKeys = new String[] { "all", "mine" };
	private static final String[] dailyBatchKeys = new String[] { DailyRollCall.startOfLectureBlock.name(), DailyRollCall.daily.name() };

	private SingleSelection showAllTeachersLecturesEl;
	private SingleSelection canOverrideStandardConfigEl;
	private TextElement appealPeriodEl;
	private TextElement reminderPeriodEl;
	private TextElement attendanceRateEl;
	private TextElement autoClosePeriodEl;
	private TextElement assessmentIpsEl;
	private TextElement assessmentLeadTimeEl;
	private TextElement assessmentFollowupTimeEl;
	private TextElement assessmentSafeExamBrowserEl;
	private TextElement defaultPlannedLecturesEl;
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement enableAbsenceNoticeEl;
	private MultipleSelectionElement enableAssessmentModeEl;
	private MultipleSelectionElement calculateAttendanceRateEnableEl;
	private MultipleSelectionElement appealAbsenceEnableEl;
	private MultipleSelectionElement statusEnabledEl;
	private MultipleSelectionElement partiallyDoneEnabledEl;
	private MultipleSelectionElement authorizedAbsenceEnableEl;
	private MultipleSelectionElement absenceDefaultAuthorizedEl;
	private MultipleSelectionElement countDispensationAsAttendantEl;
	private MultipleSelectionElement countAuthorizedAbsenceAsAttendantEl;
	private MultipleSelectionElement syncTeachersCalendarEnableEl;
	private MultipleSelectionElement syncCourseCalendarEnableEl;
	private MultipleSelectionElement courseOwnersCanViewAllCoursesInCurriculumEl;
	private MultipleSelectionElement reminderEnableEl;
	private MultipleSelectionElement rollCallEnableEl;
	private SingleSelection dayBatchRollCallEnableEl;
	private FormLayoutContainer globalCont;
	
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private UserToolsModule userToolsModule;
	
	public LectureSettingsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin_settings", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		initForm(ureq);
		initializeValues();
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// configuration which can be overriden in course
		FormLayoutContainer courseCont = FormLayoutContainer.createDefaultFormLayout("override_course", getTranslator());
		courseCont.setFormTitle(translate("lecture.admin.course.override.title"));
		courseCont.setFormContextHelp("Lecture and roll call management");
		courseCont.setRootForm(mainForm);
		formLayout.add("course", courseCont);

		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("lecture.admin.enabled", courseCont, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		
		enableAbsenceNoticeEl = uifactory.addCheckboxesHorizontal("lecture.absence.notice.enabled", courseCont, onKeys, onValues);
		
		String[] yesNoValues = new String[]{ translate("yes"), translate("no") };
		canOverrideStandardConfigEl = uifactory.addRadiosHorizontal("lecture.can.override.standard.configuration", courseCont, yesNoKeys, yesNoValues);

		// roll call enabled
		rollCallEnableEl = uifactory.addCheckboxesHorizontal("lecture.rollcall.default.enabled", courseCont, onKeys, onValues);

		// calculate attendance
		calculateAttendanceRateEnableEl = uifactory.addCheckboxesHorizontal("lecture.calculate.attendance.rate.default.enabled", courseCont, onKeys, onValues);

		attendanceRateEl = uifactory.addTextElement("lecture.attendance.rate.default", "lecture.attendance.rate.default", 2, "", courseCont);
		attendanceRateEl.setMandatory(true);
		attendanceRateEl.setDisplaySize(2);

		// sync calendars
		syncTeachersCalendarEnableEl = uifactory.addCheckboxesHorizontal("sync.teachers.calendar.enabled", courseCont, onKeys, onValues);
		syncCourseCalendarEnableEl = uifactory.addCheckboxesHorizontal("sync.course.calendar.enabled", courseCont, onKeys, onValues);
		
		// assessment mode
		enableAssessmentModeEl = uifactory.addCheckboxesHorizontal("lecture.assessment.mode.enabled", courseCont, onKeys, onValues);
		enableAssessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		
		assessmentLeadTimeEl = uifactory.addTextElement("lecture.assessment.mode.leading.time", "lecture.assessment.mode.leading.time", 8, "", courseCont);
		assessmentFollowupTimeEl = uifactory.addTextElement("lecture.assessment.mode.followup.time", "lecture.assessment.mode.followup.time", 8, "", courseCont);
		assessmentIpsEl = uifactory.addTextElement("lecture.assessment.mode.ips", "lecture.assessment.mode.ips", 8, "", courseCont);
		assessmentSafeExamBrowserEl = uifactory.addTextElement("lecture.assessment.mode.seb", "lecture.assessment.mode.seb", 8, "", courseCont);

		//global configuration
		globalCont = FormLayoutContainer.createDefaultFormLayout("global", getTranslator());
		globalCont.setFormTitle(translate("lecture.admin.global.title"));
		globalCont.setRootForm(mainForm);
		formLayout.add("global", globalCont);
		
		String[] dailyBatchValues = new String[] {
			translate("lecture.daily.batch.absence.start"), translate("lecture.daily.batch.absence.day")
		};
		dayBatchRollCallEnableEl = uifactory.addRadiosVertical("lecture.daily.batch.absence", globalCont, dailyBatchKeys, dailyBatchValues);

		partiallyDoneEnabledEl = uifactory.addCheckboxesVertical("lecture.status.partially.done.enabled", globalCont, onKeys, onValues, 1);
		partiallyDoneEnabledEl.setElementCssClass("o_sel_lecture_status_partially_done");
		
		String[] statusKeys = new String[]{ LectureBlockStatus.cancelled.name() };
		String[] statusValues = new String[]{ translate(LectureBlockStatus.cancelled.name()) };
		statusEnabledEl = uifactory.addCheckboxesVertical("lecture.status.enabled", globalCont, statusKeys, statusValues, 1);
		statusEnabledEl.setElementCssClass("o_sel_lecture_status_cancelled");
		
		String plannedLectures = Integer.toString(lectureModule.getDefaultPlannedLectures());
		defaultPlannedLecturesEl = uifactory.addTextElement("lecture.def.planned.lectures", 4, plannedLectures, globalCont);
		defaultPlannedLecturesEl.setMandatory(true);
		
		// reminder enabled
		reminderEnableEl = uifactory.addCheckboxesHorizontal("lecture.reminder.enabled", globalCont, onKeys, onValues);
		reminderEnableEl.addActionListener(FormEvent.ONCHANGE);

		reminderPeriodEl = uifactory.addTextElement("lecture.reminder.period", "lecture.reminder.period", 16, "", globalCont);
		reminderPeriodEl.setMandatory(true);

		// auto close period
		autoClosePeriodEl = uifactory.addTextElement("lecture.auto.close.period", "lecture.auto.close.period", 16, "", globalCont);
		autoClosePeriodEl.setMandatory(true);

		authorizedAbsenceEnableEl = uifactory.addCheckboxesHorizontal("lecture.authorized.absence.enabled", globalCont, onKeys, onValues);
		authorizedAbsenceEnableEl.setElementCssClass("o_sel_lecture_autorized_absence");
		authorizedAbsenceEnableEl.addActionListener(FormEvent.ONCHANGE);
		countAuthorizedAbsenceAsAttendantEl = uifactory.addCheckboxesHorizontal("lecture.count.authorized.absence.attendant", globalCont, onKeys, onValues);
		countAuthorizedAbsenceAsAttendantEl.addActionListener(FormEvent.ONCHANGE);
		countDispensationAsAttendantEl = uifactory.addCheckboxesHorizontal("lecture.count.dispensation.attendant", globalCont, onKeys, onValues);

		absenceDefaultAuthorizedEl = uifactory.addCheckboxesHorizontal("lecture.absence.default.authorized", globalCont, onKeys, onValues);
		courseOwnersCanViewAllCoursesInCurriculumEl = uifactory.addCheckboxesHorizontal("lecture.owner.can.view.all.curriculum.elements", globalCont, onKeys, onValues);
		
		// appeal enabled
		appealAbsenceEnableEl = uifactory.addCheckboxesHorizontal("lecture.appeal.absence.enabled", globalCont, onKeys, onValues);
		appealAbsenceEnableEl.addActionListener(FormEvent.ONCHANGE);
		appealPeriodEl = uifactory.addTextElement("lecture.appeal.absence.period", "lecture.appeal.absence.period", 16, "", globalCont);
		appealPeriodEl.setMandatory(true);
		
		String[] showValues = new String[] {
				translate("lecture.show.all.teachers.all"), translate("lecture.show.all.teachers.mine")
		};
		showAllTeachersLecturesEl = uifactory.addRadiosVertical("lecture.show.all.teachers", "lecture.show.all.teachers",
				globalCont, showKeys, showValues);
		
		//buttons
		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("global", getTranslator());
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add("buttonsWrapper", buttonsWrapperCont);
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsWrapperCont.add(buttonsCont);
		buttonsCont.setElementCssClass("o_sel_lecture_save_settings");
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void initializeValues() {
		if(lectureModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		if(lectureModule.isAbsenceNoticeEnabled()) {
			enableAbsenceNoticeEl.select(onKeys[0], true);
		}
		
		if(lectureModule.isCanOverrideStandardConfiguration()) {
			canOverrideStandardConfigEl.select(yesNoKeys[0], true);
		} else {
			canOverrideStandardConfigEl.select(yesNoKeys[1], true);
		}
		
		if(lectureModule.isRollCallDefaultEnabled()) {
			rollCallEnableEl.select(onKeys[0], true);
		} else {
			rollCallEnableEl.uncheckAll();
		}
		
		if(lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled()) {
			calculateAttendanceRateEnableEl.select(onKeys[0], true);
		} else {
			calculateAttendanceRateEnableEl.uncheckAll();
		}
		
		long attendanceRate = Math.round(lectureModule.getRequiredAttendanceRateDefault() * 100.0d);
		attendanceRateEl.setValue(Long.toString(attendanceRate));
		
		if(lectureModule.isTeacherCalendarSyncEnabledDefault()) {
			syncTeachersCalendarEnableEl.select(onKeys[0], true);
		} else {
			syncCourseCalendarEnableEl.uncheckAll();
		}
		if(lectureModule.isCourseCalendarSyncEnabledDefault()) {
			syncCourseCalendarEnableEl.select(onKeys[0], true);
		} else {
			syncCourseCalendarEnableEl.uncheckAll();
		}
		
		if(lectureModule.isStatusPartiallyDoneEnabled()) {
			partiallyDoneEnabledEl.select(onKeys[0], true);
		} else {
			partiallyDoneEnabledEl.uncheckAll();
		}
		
		if(lectureModule.isStatusCancelledEnabled()) {
			statusEnabledEl.select(LectureBlockStatus.cancelled.name(), true);
		} else {
			statusEnabledEl.uncheckAll();
		}
		
		if(lectureModule.isRollCallReminderEnabled()) {
			reminderEnableEl.select(onKeys[0], true);
		} else {
			reminderEnableEl.uncheckAll();
		}
		
		String reminderPeriod = "";
		if(lectureModule.getRollCallReminderPeriod() > 0) {
			reminderPeriod = Integer.toString(lectureModule.getRollCallReminderPeriod());
		}
		reminderPeriodEl.setValue(reminderPeriod);
		
		String autoClosePeriod = "";
		if(lectureModule.getRollCallAutoClosePeriod() > 0) {
			autoClosePeriod = Integer.toString(lectureModule.getRollCallAutoClosePeriod());
		}
		autoClosePeriodEl.setValue(autoClosePeriod);
		
		if(lectureModule.isAuthorizedAbsenceEnabled()) {
			authorizedAbsenceEnableEl.select(onKeys[0], true);
		} else {
			authorizedAbsenceEnableEl.uncheckAll();	
		}
		if(lectureModule.isCountAuthorizedAbsenceAsAttendant()) {
			countAuthorizedAbsenceAsAttendantEl.select(onKeys[0], true);
		} else {
			countAuthorizedAbsenceAsAttendantEl.uncheckAll();
		}
		if(lectureModule.isCountDispensationAsAttendant()) {
			countDispensationAsAttendantEl.select(onKeys[0], true);
		} else {
			countDispensationAsAttendantEl.uncheckAll();
		}
		
		if(lectureModule.isAbsenceDefaultAuthorized()) {
			absenceDefaultAuthorizedEl.select(onKeys[0], true);
		} else {
			absenceDefaultAuthorizedEl.uncheckAll();
		}
		
		if(lectureModule.isOwnerCanViewAllCoursesInCurriculum()) {
			courseOwnersCanViewAllCoursesInCurriculumEl.select(onKeys[0], true);
		} else {
			courseOwnersCanViewAllCoursesInCurriculumEl.uncheckAll();
		}
		
		if(lectureModule.isAbsenceAppealEnabled()) {
			appealAbsenceEnableEl.select(onKeys[0], true);
		} else {
			appealAbsenceEnableEl.uncheckAll();
		}
		
		if(lectureModule.isShowLectureBlocksAllTeachersDefault()) {
			showAllTeachersLecturesEl.select(showKeys[0], true);
		} else {
			showAllTeachersLecturesEl.select(showKeys[1], true);
		}
		
		String appealPeriod = "";
		if(lectureModule.getAbsenceAppealPeriod() > 0) {
			appealPeriod = Integer.toString(lectureModule.getAbsenceAppealPeriod());
		}
		appealPeriodEl.setValue(appealPeriod);
		
		if(lectureModule.isAssessmentModeEnabledDefault()) {
			enableAssessmentModeEl.select(onKeys[0], true);
		} else {
			enableAssessmentModeEl.uncheckAll();
		}
		
		assessmentIpsEl.setValue(lectureModule.getAssessmentModeAdmissibleIps());
		if(lectureModule.getAssessmentModeLeadTime() >= 0) {
			assessmentLeadTimeEl.setValue(Integer.toString(lectureModule.getAssessmentModeLeadTime()));
		} else {
			assessmentLeadTimeEl.setValue("");
		}
		if(lectureModule.getAssessmentModeFollowupTime() >= 0) {
			assessmentFollowupTimeEl.setValue(Integer.toString(lectureModule.getAssessmentModeFollowupTime()));
		} else {
			assessmentFollowupTimeEl.setValue("");
		}
		assessmentSafeExamBrowserEl.setValue(lectureModule.getAssessmentModeSebKeys());
		
		dayBatchRollCallEnableEl.select(lectureModule.getDailyRollCall().name(), true);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		enableAbsenceNoticeEl.setVisible(enabled);
		canOverrideStandardConfigEl.setVisible(enabled);
		authorizedAbsenceEnableEl.setVisible(enabled);
		attendanceRateEl.setVisible(enabled);
		appealAbsenceEnableEl.setVisible(enabled);
		reminderEnableEl.setVisible(enabled);
		syncTeachersCalendarEnableEl.setVisible(enabled);
		syncCourseCalendarEnableEl.setVisible(enabled);
		enableAssessmentModeEl.setVisible(enabled);
		
		boolean assessmentModeEnabled = enableAssessmentModeEl.isVisible() && enableAssessmentModeEl.isAtLeastSelected(1);
		assessmentLeadTimeEl.setVisible(assessmentModeEnabled);
		assessmentFollowupTimeEl.setVisible(assessmentModeEnabled);
		assessmentIpsEl.setVisible(assessmentModeEnabled);
		assessmentSafeExamBrowserEl.setVisible(assessmentModeEnabled);
		
		globalCont.setVisible(enabled);
		autoClosePeriodEl.setVisible(enabled);
		statusEnabledEl.setVisible(enabled);
		partiallyDoneEnabledEl.setVisible(enabled);
		absenceDefaultAuthorizedEl.setVisible(enabled);
		rollCallEnableEl.setVisible(enabled);
		calculateAttendanceRateEnableEl.setVisible(enabled);
		
		defaultPlannedLecturesEl.setVisible(enabled);
		
		appealPeriodEl.setVisible(appealAbsenceEnableEl.isVisible() && appealAbsenceEnableEl.isAtLeastSelected(1));
		reminderPeriodEl.setVisible(reminderEnableEl.isVisible() && reminderEnableEl.isAtLeastSelected(1));
		
		countAuthorizedAbsenceAsAttendantEl.setVisible(authorizedAbsenceEnableEl.isVisible() && authorizedAbsenceEnableEl.isAtLeastSelected(1));
		countDispensationAsAttendantEl.setVisible(countAuthorizedAbsenceAsAttendantEl.isVisible() && !countAuthorizedAbsenceAsAttendantEl.isAtLeastSelected(1));
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
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
		allOk &= validateInt(assessmentLeadTimeEl);
		allOk &= validateInt(assessmentFollowupTimeEl);
		
		appealPeriodEl.clearError();
		if(appealAbsenceEnableEl.isVisible() && appealAbsenceEnableEl.isAtLeastSelected(1)) {
			allOk &= validateInt(appealPeriodEl);
		}
		
		reminderPeriodEl.clearError();
		if(reminderEnableEl.isVisible() && reminderEnableEl.isAtLeastSelected(1)) {
			allOk &= validateInt(reminderPeriodEl);
		}
		
		boolean validatePlannedLectures = validateInt(defaultPlannedLecturesEl);
		if(validatePlannedLectures && Integer.parseInt(defaultPlannedLecturesEl.getValue()) > 12) {
			defaultPlannedLecturesEl.setErrorKey("lecture.def.planned.lectures.max", null);
			validatePlannedLectures = false;
		}
		allOk &= validatePlannedLectures;

		return allOk;
	}
	
	private boolean validateInt(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(!el.isVisible()) {
			// OK
		} else if(StringHelper.containsNonWhitespace(el.getValue())) {
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
		if(enableEl == source) {
			if(enableEl.isAtLeastSelected(1)) {
				initializeValues();
			}
			updateUI();
		} else if(appealAbsenceEnableEl == source || reminderEnableEl == source
				|| authorizedAbsenceEnableEl == source || enableAssessmentModeEl == source
				|| countAuthorizedAbsenceAsAttendantEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		lectureModule.setEnabled(enabled);
		boolean assessmentModeEnabled = enabled && enableAssessmentModeEl.isAtLeastSelected(1);
		lectureModule.setAssessmentModeEnabledDefault(assessmentModeEnabled);
		
		if(enabled) {
			lectureModule.setCanOverrideStandardConfiguration(canOverrideStandardConfigEl.isSelected(0));
			lectureModule.setAbsenceNoticeEnabled(enableAbsenceNoticeEl.isSelected(0));

			//enabled user tool
			Set<String> availableTools = userToolsModule.getAvailableUserToolSet();
			if(!availableTools.isEmpty()) {
				if(!availableTools.contains("org.olat.home.HomeMainController:org.olat.modules.lecture.ui.LecturesToolController")) {
					availableTools.add("org.olat.home.HomeMainController:org.olat.modules.lecture.ui.LecturesToolController");
				}
				
				StringBuilder aTools = new StringBuilder();
				for(String selectedKey:availableTools) {
					if(aTools.length() > 0) aTools.append(",");
					aTools.append(selectedKey);
				}
				userToolsModule.setAvailableUserTools(aTools.toString());
			}
			
			lectureModule.setRollCallDefaultEnabled(rollCallEnableEl.isAtLeastSelected(1));
			
			int autoClosePeriod = Integer.parseInt(autoClosePeriodEl.getValue());
			lectureModule.setRollCallAutoClosePeriod(autoClosePeriod);
			
			lectureModule.setStatusPartiallyDoneEnabled(partiallyDoneEnabledEl.isAtLeastSelected(1));
			lectureModule.setStatusCancelledEnabled(statusEnabledEl.isAtLeastSelected(1));
			lectureModule.setDefaultPlannedLectures(Integer.parseInt(defaultPlannedLecturesEl.getValue()));	
			
			boolean authorizedAbsenceEnabled = authorizedAbsenceEnableEl.isAtLeastSelected(1);
			lectureModule.setAuthorizedAbsenceEnabled(authorizedAbsenceEnabled);
			boolean countAuthorizedAbsenceAsAttendant = authorizedAbsenceEnabled && countAuthorizedAbsenceAsAttendantEl.isAtLeastSelected(1);
			lectureModule.setCountAuthorizedAbsenceAsAttendant(countAuthorizedAbsenceAsAttendant);
			lectureModule.setCountDispensationAsAttendant(!countAuthorizedAbsenceAsAttendant && countDispensationAsAttendantEl.isAtLeastSelected(1));
		
			lectureModule.setOwnerCanViewAllCoursesInCurriculum(courseOwnersCanViewAllCoursesInCurriculumEl.isAtLeastSelected(1));
			
			lectureModule.setAbsenceAppealEnabled(appealAbsenceEnableEl.isAtLeastSelected(1));
			if(appealAbsenceEnableEl.isAtLeastSelected(1)) {
				int period = Integer.parseInt(appealPeriodEl.getValue());
				lectureModule.setAbsenceAppealPeriod(period);
			}
			lectureModule.setAbsenceDefaultAuthorized(absenceDefaultAuthorizedEl.isAtLeastSelected(1));
			
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
			lectureModule.setCourseCalendarSyncEnabledDefault(syncCourseCalendarEnableEl.isAtLeastSelected(1));
		
			lectureModule.setShowLectureBlocksAllTeachersDefault(showAllTeachersLecturesEl.isSelected(0));
			lectureModule.setDailyRollCall(DailyRollCall.valueOf(dayBatchRollCallEnableEl.getSelectedKey()));
			
		}
		
		if(assessmentModeEnabled) {
			lectureModule.setAssessmentModeAdmissibleIps(assessmentIpsEl.getValue());
			lectureModule.setAssessmentModeSebKeys(assessmentSafeExamBrowserEl.getValue());
			lectureModule.setAssessmentModeLeadTime(Integer.parseInt(assessmentLeadTimeEl.getValue()));
			lectureModule.setAssessmentModeFollowupTime(Integer.parseInt(assessmentFollowupTimeEl.getValue()));
		} else {
			lectureModule.setAssessmentModeSebKeys("");
			lectureModule.setAssessmentModeAdmissibleIps("");
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}