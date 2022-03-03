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
package org.olat.modules.lecture;

import org.olat.NewControllerFactory;
import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.modules.lecture.site.LecturesManagementContextEntryControllerCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LectureModule extends AbstractSpringModule implements ConfigOnOff {
	
	private static final String LECTURE_ENABLED = "lecture.enabled";
	private static final String LECTURE_MANAGED = "lecture.managed";
	private static final String LECTURE_ABSENCE_NOTICE_ENABLED = "lecture.absence.notice.enabled";
	
	private static final String ASSESSMENT_MODE_ENABLED = "lecture.assessment.mode.enabled";
	private static final String ASSESSMENT_MODE_LEAD_TIME = "lecture.assessment.mode.lead.time";
	private static final String ASSESSMENT_MODE_FOLLOWUP_TIME = "lecture.assessment.mode.followup.time";
	private static final String ASSESSMENT_MODE_ADMISSIBLE_IPS = "lecture.assessment.mode.admissible.ips";
	private static final String ASSESSMENT_MODE_SEB_KEYS = "lecture.assessment.mode.seb.keys";

	private static final String CAN_OVERRIDE_STANDARD_CONFIGURATION = "lecture.can.override.standard.configuration";
	private static final String STATUS_PARTIALLY_DONE_ENABLED = "lecture.status.partially.done.enabled";
	private static final String STATUS_CANCELLED_ENABLED = "lecture.status.cancelled.enabled";
	private static final String AUTHORIZED_ABSENCE_ENABLED = "lecture.authorized.absence.enabled";
	private static final String AUTHORIZED_ABSENCE_ATTENDANT_ENABLED = "lecture.authorized.absence.as.attendant";
	private static final String DISPENSATION_ATTENDANT_ENABLED = "lecture.dispensation.as.attendant";
	
	private static final String TEACHER_CAN_AUTHORIZED_ABSENCE = "teacher.can.authorized.absence";
	private static final String TEACHER_CAN_SEE_APPEAL = "teacher.can.see.appeal";
	private static final String TEACHER_CAN_AUTHORIZED_APPEAL = "teacher.can.authorized.appeal";
	private static final String TEACHER_CAN_RECORD_NOTICE = "teacher.can.record.notice";
	
	private static final String MASTERCOACH_CAN_SEE_ABSENCE = "mastercoach.can.see.absence";
	private static final String MASTERCOACH_CAN_RECORD_NOTICE = "mastercoach.can.record.notice";
	private static final String MASTERCOACH_CAN_AUTHORIZED_ABSENCE = "mastercoach.can.authorized.absence";
	private static final String MASTERCOACH_CAN_SEE_APPEAL = "mastercoach.can.see.appeal";
	private static final String MASTERCOACH_CAN_AUTHORIZED_APPEAL = "mastercoach.can.authorized.appeal";
	private static final String MASTERCOACH_CAN_REOPEN_LECTURE_BLOCKS = "mastercoach.can.reopen.lecture.blocks";

	private static final String PARTICIPANT_CAN_NOTICE = "participant.can.notice";
	
	private static final String DAILY_ROLL_CALL = "daily.rollcall";
	
	private static final String OWNER_CAN_VIEW_ALL_COURSES_IN_CURRICULUM = "lecture.owner.can.view.all.courses.curriculum";
	private static final String ROLLCALL_REMINDER_ENABLED = "lecture.rollcall.reminder.enabled";
	private static final String ROLLCALL_REMINDER_PERIOD = "lecture.rollcall.reminder.period";
	private static final String ROLLCALL_AUTOCLOSE_PERIOD = "lecture.rollcall.autoclose.period";
	private static final String ABSENCE_APPEAL_ENABLED = "lecture.absence.appeal.enabled";
	private static final String ABSENCE_APPEAL_PERIOD = "lecture.absence.appeal.period";
	private static final String ROLLCALL_ENABLED = "lecture.rollcall.default.enabled";
	private static final String CALCULATE_ATTENDANCE_RATE_DEFAULT_ENABLED = "lecture.calculate.attendance.rate.default.enabled";
	private static final String REQUIRED_ATTENDANCE_RATE_DEFAULT = "lecture.required.attendance.rate.default";
	private static final String TEACHER_CALENDAR_SYNC_DEFAULT_ENABLED = "lecture.teacher.calendar.sync.default.enabled";
	private static final String PARTICIPANT_CALENDAR_SYNC_DEFAULT_ENABLED = "lecture.participant.calendar.sync.default.enabed";
	private static final String COURSE_CALENDAR_SYNC_DEFAULT_ENABLED = "lecture.course.calendar.sync.default.enabed";
	private static final String ABSENCE_DEFAULT_AUTHORIZED = "lecture.absence.default.authorized";
	private static final String COURSE_SHOW_ALL_TEACHERS = "lecture.course.show.all.teachers";
	
	private static final String DEFAULT_PLANNED_LECTURES = "lecture.default.planned.lectures";
	
	@Value("${lecture.enabled:false}")
	private boolean enabled;
	@Value("${lecture.managed:true}")
	private boolean lecturesManaged;
	@Value("${lecture.can.override.standard.configuration:false}")
	private boolean canOverrideStandardConfiguration;
	@Value("${lecture.absence.notice.enabled:false}")
	private boolean absenceNoticeEnabled;
	
	@Value("${lecture.status.partially.done.enabled:true}")
	private boolean statusPartiallyDoneEnabled;
	@Value("${lecture.status.cancelled.enabled:true}")
	private boolean statusCancelledEnabled;

	@Value("${lecture.authorized.absence.enabled:true}")
	private boolean authorizedAbsenceEnabled;
	@Value("${lecture.authorized.absence.as.attendant:false}")
	private boolean countAuthorizedAbsenceAsAttendant;
	@Value("${lecture.dispensation.as.attendant:false}")
	private boolean countDispensationAsAttendant;
	@Value("${lecture.absence.default.authorized:false}")
	private boolean absenceDefaultAuthorized;
	
	@Value("${lecture.teacher.can.authorized.absence:true}")
	private boolean teacherCanAuthorizedAbsence;
	@Value("${lecture.teacher.can.see.appeal:true}")
	private boolean teacherCanSeeAppeal;
	@Value("${lecture.teacher.can.authorized.appeal:false}")
	private boolean teacherCanAuthorizedAppeal;
	@Value("${lecture.teacher.can.record.notice:true}")
	private boolean teacherCanRecordNotice;
	
	@Value("${lecture.mastercoach.can.see.absence:true}")
	private boolean masterCoachCanSeeAbsence;
	@Value("${lecture.mastercoach.can.record.notice:true}")
	private boolean masterCoachCanRecordNotice;
	@Value("${lecture.mastercoach.can.authorized.absence:true}")
	private boolean masterCoachCanAuthorizedAbsence;
	@Value("${lecture.mastercoach.can.see.appeal:true}")
	private boolean masterCoachCanSeeAppeal;
	@Value("${lecture.mastercoach.can.authorized.appeal:true}")
	private boolean masterCoachCanAuthorizedAppeal;
	@Value("${lecture.mastercoach.can.reopen.lecture.blocks:true}")
	private boolean masterCoachCanReopenLectureBlocks;
	
	

	@Value("${lecture.participant.can.notice:false}")
	private boolean participantCanNotice;
	
	@Value("${lecture.assessment.mode.enabled:true}")
	private boolean assessmentModeEnabled;
	@Value("${lecture.assessment.mode.lead.time:5}")
	private int assessmentModeLeadTime;
	@Value("${lecture.assessment.mode.followup.time:5}")
	private int assessmentModeFollowupTime;
	@Value("${lecture.assessment.mode.admissible.ips}")
	private String assessmentModeAdmissibleIps;
	@Value("${lecture.assessment.mode.seb.keys}")
	private String assessmentModeSebKeys;
	
	@Value("${lecture.owner.can.view.all.courses.curriculum:true}")
	private boolean ownerCanViewAllCoursesInCurriculum;

	@Value("${lecture.rollcall.reminder.enabled:true}")
	private boolean rollCallReminderEnabled;
	@Value("${lecture.rollcall.reminder.period:3}")
	private int rollCallReminderPeriod;
	@Value("${lecture.rollcall.autoclose.period:5}")
	private int rollCallAutoClosePeriod;

	@Value("${lecture.absence.appeal.enabled:true}")
	private boolean absenceAppealEnabled;
	@Value("${lecture.absence.appeal.period:15}")
	private int absenceAppealPeriod;

	@Value("${lecture.rollcall.default.enabled:true}")
	private boolean rollCallDefaultEnabled;
	@Value("${lecture.calculate.attendance.rate.default.enabled:true}")
	private boolean rollCallCalculateAttendanceRateDefaultEnabled;
	@Value("${lecture.required.attendance.rate.default:0.8}")
	private double requiredAttendanceRateDefault;

	@Value("${lecture.teacher.calendar.sync.default.enabled:true}")
	private boolean teacherCalendarSyncEnabledDefault;
	@Value("${lecture.participant.calendar.sync.default.enabled:true}")
	private boolean participantCalendarSyncEnabledDefault;
	@Value("${lecture.course.calendar.sync.default.enabled:true}")
	private boolean courseCalendarSyncEnabledDefault;

	@Value("${lecture.course.show.all.teachers:false}")
	private boolean showLectureBlocksAllTeachersDefault;
	
	@Value("${lecture.daily.rollcall:start}")
	private String dailyRollCall;
	
	@Value("${lecture.default.planned.lectures:4}")
	private int defaultPlannedLectures;
	
	@Autowired
	public LectureModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		// Add controller factory extension point to launch groups
		NewControllerFactory.getInstance().addContextEntryControllerCreator("Lectures",
				new LecturesManagementContextEntryControllerCreator());
		NewControllerFactory.getInstance().addContextEntryControllerCreator("LecturesManagementSite",
				new LecturesManagementContextEntryControllerCreator());
		
		updateProperties();
	}
	
	@Override
	protected void initFromChangedProperties() {
		updateProperties();
	}
	
	private void updateProperties() {
		//module enabled/disabled
		String enabledObj = getStringPropertyValue(LECTURE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String managedObj = getStringPropertyValue(LECTURE_MANAGED, true);
		if(StringHelper.containsNonWhitespace(managedObj)) {
			lecturesManaged = "true".equals(managedObj);
		}
		
		String absenceNoticeEnabledObj = getStringPropertyValue(LECTURE_ABSENCE_NOTICE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(absenceNoticeEnabledObj)) {
			absenceNoticeEnabled = "true".equals(absenceNoticeEnabledObj);
		}
		
		String canOverrideSStandardConfigurationObj = getStringPropertyValue(CAN_OVERRIDE_STANDARD_CONFIGURATION, true);
		if(StringHelper.containsNonWhitespace(canOverrideSStandardConfigurationObj)) {
			canOverrideStandardConfiguration = "true".equals(canOverrideSStandardConfigurationObj);
		}
		
		String statusPartiallyDoneEnabledObj = getStringPropertyValue(STATUS_PARTIALLY_DONE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(statusPartiallyDoneEnabledObj)) {
			statusPartiallyDoneEnabled = "true".equals(statusPartiallyDoneEnabledObj);
		}
		String statusCancelledEnabledObj = getStringPropertyValue(STATUS_CANCELLED_ENABLED, true);
		if(StringHelper.containsNonWhitespace(statusCancelledEnabledObj)) {
			statusCancelledEnabled = "true".equals(statusCancelledEnabledObj);
		}
		
		String authorizedAbsenceEnabledObj = getStringPropertyValue(AUTHORIZED_ABSENCE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(authorizedAbsenceEnabledObj)) {
			authorizedAbsenceEnabled = "true".equals(authorizedAbsenceEnabledObj);
		}
		
		String authorizedAbsenceAttendantEnabledObj = getStringPropertyValue(AUTHORIZED_ABSENCE_ATTENDANT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(authorizedAbsenceAttendantEnabledObj)) {
			countAuthorizedAbsenceAsAttendant = "true".equals(authorizedAbsenceAttendantEnabledObj);
		}
		
		String dispensationAttendantEnabledObj = getStringPropertyValue(DISPENSATION_ATTENDANT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(dispensationAttendantEnabledObj)) {
			countDispensationAsAttendant = "true".equals(dispensationAttendantEnabledObj);
		}

		String absenceDefaultAuthorizedObj = getStringPropertyValue(ABSENCE_DEFAULT_AUTHORIZED, true);
		if(StringHelper.containsNonWhitespace(absenceDefaultAuthorizedObj)) {
			absenceDefaultAuthorized = "true".equals(absenceDefaultAuthorizedObj);
		}
		
		String teacherCanAuthorizedAbsenceObj = getStringPropertyValue(TEACHER_CAN_AUTHORIZED_ABSENCE, true);
		if(StringHelper.containsNonWhitespace(teacherCanAuthorizedAbsenceObj)) {
			teacherCanAuthorizedAbsence = "true".equals(teacherCanAuthorizedAbsenceObj);
		}
		
		String teacherCanSeeAppealObj = getStringPropertyValue(TEACHER_CAN_SEE_APPEAL, true);
		if(StringHelper.containsNonWhitespace(teacherCanSeeAppealObj)) {
			teacherCanSeeAppeal = "true".equals(teacherCanSeeAppealObj);
		}
		
		String teacherCanAuthorizedAppealObj = getStringPropertyValue(TEACHER_CAN_AUTHORIZED_APPEAL, true);
		if(StringHelper.containsNonWhitespace(teacherCanAuthorizedAppealObj)) {
			teacherCanAuthorizedAppeal = "true".equals(teacherCanAuthorizedAppealObj);
		}
		
		String teacherCanRecordNoticeObj = getStringPropertyValue(TEACHER_CAN_RECORD_NOTICE, true);
		if(StringHelper.containsNonWhitespace(teacherCanRecordNoticeObj)) {
			teacherCanRecordNotice = "true".equals(teacherCanRecordNoticeObj);
		}
		
		String masterCoachCanSeeAbsenceObj = getStringPropertyValue(MASTERCOACH_CAN_SEE_ABSENCE, true);
		if(StringHelper.containsNonWhitespace(masterCoachCanSeeAbsenceObj)) {
			masterCoachCanSeeAbsence = "true".equals(masterCoachCanSeeAbsenceObj);
		}
		
		String masterCoachCanRecordNoticeObj = getStringPropertyValue(MASTERCOACH_CAN_RECORD_NOTICE, true);
		if(StringHelper.containsNonWhitespace(masterCoachCanRecordNoticeObj)) {
			masterCoachCanRecordNotice = "true".equals(masterCoachCanRecordNoticeObj);
		}
		
		String masterCoachCanAuthorizedAbsenceObj = getStringPropertyValue(MASTERCOACH_CAN_AUTHORIZED_ABSENCE, true);
		if(StringHelper.containsNonWhitespace(masterCoachCanAuthorizedAbsenceObj)) {
			masterCoachCanAuthorizedAbsence = "true".equals(masterCoachCanAuthorizedAbsenceObj);
		}

		String masterCoachCanSeeAppealObj = getStringPropertyValue(MASTERCOACH_CAN_SEE_APPEAL, true);
		if(StringHelper.containsNonWhitespace(masterCoachCanSeeAppealObj)) {
			masterCoachCanSeeAppeal = "true".equals(masterCoachCanSeeAppealObj);
		}
		
		String masterCoachCanAuthorizedAppealObj = getStringPropertyValue(MASTERCOACH_CAN_AUTHORIZED_APPEAL, true);
		if(StringHelper.containsNonWhitespace(masterCoachCanAuthorizedAppealObj)) {
			masterCoachCanAuthorizedAppeal = "true".equals(masterCoachCanAuthorizedAppealObj);
		}
		
		String masterCoachCanReopenLectureBlocksObj = getStringPropertyValue(MASTERCOACH_CAN_REOPEN_LECTURE_BLOCKS, true);
		if(StringHelper.containsNonWhitespace(masterCoachCanReopenLectureBlocksObj)) {
			masterCoachCanReopenLectureBlocks = "true".equals(masterCoachCanReopenLectureBlocksObj);
		}
		
		String participantCanNoticeObj = getStringPropertyValue(PARTICIPANT_CAN_NOTICE, true);
		if(StringHelper.containsNonWhitespace(participantCanNoticeObj)) {
			participantCanNotice = "true".equals(participantCanNoticeObj);
		}

		String ownerCanViewAllCoursesInCurriculumObj = getStringPropertyValue(OWNER_CAN_VIEW_ALL_COURSES_IN_CURRICULUM, true);
		if(StringHelper.containsNonWhitespace(ownerCanViewAllCoursesInCurriculumObj)) {
			ownerCanViewAllCoursesInCurriculum = "true".equals(ownerCanViewAllCoursesInCurriculumObj);
		}
		
		String rollCallReminderEnabledObj = getStringPropertyValue(ROLLCALL_REMINDER_ENABLED, true);
		if(StringHelper.containsNonWhitespace(rollCallReminderEnabledObj)) {
			rollCallReminderEnabled = "true".equals(rollCallReminderEnabledObj);
		}
		
		String rollcallReminderPeriodObj = getStringPropertyValue(ROLLCALL_REMINDER_PERIOD, true);
		if(StringHelper.containsNonWhitespace(rollcallReminderPeriodObj)) {
			rollCallReminderPeriod = Integer.parseInt(rollcallReminderPeriodObj);
		}
		
		String rollcallAutoClosePeriodObj = getStringPropertyValue(ROLLCALL_AUTOCLOSE_PERIOD, true);
		if(StringHelper.containsNonWhitespace(rollcallAutoClosePeriodObj)) {
			rollCallAutoClosePeriod = Integer.parseInt(rollcallAutoClosePeriodObj);
		}
		
		String absenceAppealEnabledObj = getStringPropertyValue(ABSENCE_APPEAL_ENABLED, true);
		if(StringHelper.containsNonWhitespace(absenceAppealEnabledObj)) {
			absenceAppealEnabled = "true".equals(absenceAppealEnabledObj);
		}
		
		String absenceAppealPeriodObj = getStringPropertyValue(ABSENCE_APPEAL_PERIOD, true);
		if(StringHelper.containsNonWhitespace(absenceAppealPeriodObj)) {
			absenceAppealPeriod = Integer.parseInt(absenceAppealPeriodObj);
		}
		
		String rollcallDefaultEnabledObj = getStringPropertyValue(ROLLCALL_ENABLED, true);
		if(StringHelper.containsNonWhitespace(rollcallDefaultEnabledObj)) {
			rollCallDefaultEnabled = "true".equals(rollcallDefaultEnabledObj);
		}
		
		String calculateAttendanceRateDefaultEnabledObj = getStringPropertyValue(CALCULATE_ATTENDANCE_RATE_DEFAULT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(calculateAttendanceRateDefaultEnabledObj)) {
			rollCallCalculateAttendanceRateDefaultEnabled = "true".equals(calculateAttendanceRateDefaultEnabledObj);
		}
		
		String requiredAttendanceRateDefaultObj = getStringPropertyValue(REQUIRED_ATTENDANCE_RATE_DEFAULT, true);
		if(StringHelper.containsNonWhitespace(requiredAttendanceRateDefaultObj)) {
			requiredAttendanceRateDefault = Double.parseDouble(requiredAttendanceRateDefaultObj);
		}
		
		String teacherCalendarSyncDefaultEnabledObj = getStringPropertyValue(TEACHER_CALENDAR_SYNC_DEFAULT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(teacherCalendarSyncDefaultEnabledObj)) {
			teacherCalendarSyncEnabledDefault = "true".equals(teacherCalendarSyncDefaultEnabledObj);
		}
		
		String participantCalendarSyncDefaultEnabledObj = getStringPropertyValue(PARTICIPANT_CALENDAR_SYNC_DEFAULT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(participantCalendarSyncDefaultEnabledObj)) {
			participantCalendarSyncEnabledDefault = "true".equals(participantCalendarSyncDefaultEnabledObj);
		}
		
		String courseCalendarSyncDefaultEnabledObj = getStringPropertyValue(COURSE_CALENDAR_SYNC_DEFAULT_ENABLED, true);
		if(StringHelper.containsNonWhitespace(courseCalendarSyncDefaultEnabledObj)) {
			courseCalendarSyncEnabledDefault = "true".equals(courseCalendarSyncDefaultEnabledObj);
		}
		
		String showAllTeachersEnabledObj = getStringPropertyValue(COURSE_SHOW_ALL_TEACHERS, true);
		if(StringHelper.containsNonWhitespace(showAllTeachersEnabledObj)) {
			showLectureBlocksAllTeachersDefault = "true".equals(showAllTeachersEnabledObj);
		}
		
		String assessmentModeEnabledObj = getStringPropertyValue(ASSESSMENT_MODE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(assessmentModeEnabledObj)) {
			assessmentModeEnabled = "true".equals(assessmentModeEnabledObj);
		}
		
		String leadTimeObj = getStringPropertyValue(ASSESSMENT_MODE_LEAD_TIME, true);
		if(StringHelper.containsNonWhitespace(leadTimeObj)) {
			assessmentModeLeadTime = Integer.parseInt(leadTimeObj);
		}
		
		String followupTimeObj = getStringPropertyValue(ASSESSMENT_MODE_FOLLOWUP_TIME, true);
		if(StringHelper.containsNonWhitespace(followupTimeObj)) {
			assessmentModeFollowupTime = Integer.parseInt(followupTimeObj);
		}
		
		String dailyRollCallObj = getStringPropertyValue(DAILY_ROLL_CALL, true);
		if(StringHelper.containsNonWhitespace(dailyRollCallObj)) {
			dailyRollCall = dailyRollCallObj;
		}
		
		String plannedLecturesObj = getStringPropertyValue(DEFAULT_PLANNED_LECTURES, true);
		if(StringHelper.isLong(plannedLecturesObj)) {
			defaultPlannedLectures = Integer.parseInt(plannedLecturesObj);
		}

		assessmentModeAdmissibleIps = getStringPropertyValue(ASSESSMENT_MODE_ADMISSIBLE_IPS, assessmentModeAdmissibleIps);
		assessmentModeSebKeys = getStringPropertyValue(ASSESSMENT_MODE_SEB_KEYS, assessmentModeSebKeys);
	}


	@Override
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
		setStringProperty(LECTURE_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isCanOverrideStandardConfiguration() {
		return canOverrideStandardConfiguration;
	}

	public void setCanOverrideStandardConfiguration(boolean enable) {
		canOverrideStandardConfiguration = enable;
		setStringProperty(CAN_OVERRIDE_STANDARD_CONFIGURATION, Boolean.toString(enable), true);
	}

	public boolean isLecturesManaged() {
		return lecturesManaged;
	}

	public void setLecturesManaged(boolean lecturesManaged) {
		this.lecturesManaged = lecturesManaged;
		setStringProperty(LECTURE_MANAGED, Boolean.toString(lecturesManaged), true);
	}
	
	public boolean isAbsenceNoticeEnabled() {
		return absenceNoticeEnabled;
	}

	public void setAbsenceNoticeEnabled(boolean enabled) {
		this.absenceNoticeEnabled = enabled;
		setStringProperty(LECTURE_ABSENCE_NOTICE_ENABLED, Boolean.toString(enabled), true);
	}

	public boolean isAuthorizedAbsenceEnabled() {
		return authorizedAbsenceEnabled;
	}

	public void setAuthorizedAbsenceEnabled(boolean enable) {
		this.authorizedAbsenceEnabled = enable;
		setStringProperty(AUTHORIZED_ABSENCE_ENABLED, Boolean.toString(enable), true);
	}

	public boolean isStatusPartiallyDoneEnabled() {
		return statusPartiallyDoneEnabled;
	}

	public void setStatusPartiallyDoneEnabled(boolean enable) {
		this.statusPartiallyDoneEnabled = enable;
		setStringProperty(STATUS_PARTIALLY_DONE_ENABLED, Boolean.toString(enable), true);
	}

	public boolean isStatusCancelledEnabled() {
		return statusCancelledEnabled;
	}

	public void setStatusCancelledEnabled(boolean enable) {
		this.statusCancelledEnabled = enable;
		setStringProperty(STATUS_CANCELLED_ENABLED, Boolean.toString(enable), true);
	}

	public boolean isCountAuthorizedAbsenceAsAttendant() {
		return countAuthorizedAbsenceAsAttendant;
	}

	public void setCountAuthorizedAbsenceAsAttendant(boolean enable) {
		this.countAuthorizedAbsenceAsAttendant = enable;
		setStringProperty(AUTHORIZED_ABSENCE_ATTENDANT_ENABLED, Boolean.toString(enable), true);
	}
	
	public boolean isCountDispensationAsAttendant() {
		return countDispensationAsAttendant;
	}

	public void setCountDispensationAsAttendant(boolean enable) {
		this.countDispensationAsAttendant = enable;
		setStringProperty(DISPENSATION_ATTENDANT_ENABLED, Boolean.toString(enable), true);
	}

	public boolean isAbsenceDefaultAuthorized() {
		return absenceDefaultAuthorized;
	}

	public void setAbsenceDefaultAuthorized(boolean absenceDefaultAuthorized) {
		this.absenceDefaultAuthorized = absenceDefaultAuthorized;
		setStringProperty(ABSENCE_DEFAULT_AUTHORIZED, Boolean.toString(absenceDefaultAuthorized), true);
	}

	public boolean isTeacherCanAuthorizedAbsence() {
		return teacherCanAuthorizedAbsence;
	}

	public void setTeacherCanAuthorizedAbsence(boolean enable) {
		this.teacherCanAuthorizedAbsence = enable;
		setStringProperty(TEACHER_CAN_AUTHORIZED_ABSENCE, Boolean.toString(enable), true);
	}

	public boolean isTeacherCanSeeAppeal() {
		return teacherCanSeeAppeal;
	}

	public void setTeacherCanSeeAppeal(boolean enable) {
		this.teacherCanSeeAppeal = enable;
		setStringProperty(TEACHER_CAN_SEE_APPEAL, Boolean.toString(enable), true);
	}

	public boolean isTeacherCanAuthorizedAppeal() {
		return teacherCanAuthorizedAppeal;
	}

	public void setTeacherCanAuthorizedAppeal(boolean enable) {
		this.teacherCanAuthorizedAppeal = enable;
		setStringProperty(TEACHER_CAN_AUTHORIZED_APPEAL, Boolean.toString(enable), true);
	}
	
	public boolean isTeacherCanRecordNotice() {
		return teacherCanRecordNotice;
	}

	public void setTeacherCanRecordNotice(boolean enable) {
		this.teacherCanRecordNotice = enable;
		setStringProperty(TEACHER_CAN_RECORD_NOTICE, Boolean.toString(enable), true);
	}

	public boolean isMasterCoachCanSeeAbsence() {
		return masterCoachCanSeeAbsence;
	}

	public void setMasterCoachCanSeeAbsence(boolean enable) {
		this.masterCoachCanSeeAbsence = enable;
		setStringProperty(MASTERCOACH_CAN_SEE_ABSENCE, Boolean.toString(enable), true);
	}

	public boolean isMasterCoachCanRecordNotice() {
		return masterCoachCanRecordNotice;
	}

	public void setMasterCoachCanRecordNotice(boolean enable) {
		this.masterCoachCanRecordNotice = enable;
		setStringProperty(MASTERCOACH_CAN_RECORD_NOTICE, Boolean.toString(enable), true);
	}

	public boolean isMasterCoachCanAuthorizedAbsence() {
		return masterCoachCanAuthorizedAbsence;
	}

	public void setMasterCoachCanAuthorizedAbsence(boolean enable) {
		this.masterCoachCanAuthorizedAbsence = enable;
		setStringProperty(MASTERCOACH_CAN_AUTHORIZED_ABSENCE, Boolean.toString(enable), true);
	}

	public boolean isMasterCoachCanSeeAppeal() {
		return masterCoachCanSeeAppeal;
	}

	public void setMasterCoachCanSeeAppeal(boolean enable) {
		this.masterCoachCanSeeAppeal = enable;
		setStringProperty(MASTERCOACH_CAN_SEE_APPEAL, Boolean.toString(enable), true);
	}

	public boolean isMasterCoachCanAuthorizedAppeal() {
		return masterCoachCanAuthorizedAppeal;
	}

	public void setMasterCoachCanAuthorizedAppeal(boolean enable) {
		this.masterCoachCanAuthorizedAppeal = enable;
		setStringProperty(MASTERCOACH_CAN_AUTHORIZED_APPEAL, Boolean.toString(enable), true);
	}
	
	public boolean isMasterCoachCanReopenLectureBlocks() {
		return masterCoachCanReopenLectureBlocks;
	}

	public void setMasterCoachCanReopenLectureBlocks(boolean enable) {
		this.masterCoachCanReopenLectureBlocks = enable;
		setStringProperty(MASTERCOACH_CAN_REOPEN_LECTURE_BLOCKS, Boolean.toString(enable), true);
	}

	public boolean isParticipantCanNotice() {
		return participantCanNotice;
	}

	public void setParticipantCanNotice(boolean enable) {
		this.participantCanNotice = enable;
		setStringProperty(PARTICIPANT_CAN_NOTICE, Boolean.toString(enable), true);
	}

	public boolean isAssessmentModeEnabledDefault() {
		return assessmentModeEnabled;
	}

	public void setAssessmentModeEnabledDefault(boolean enable) {
		this.assessmentModeEnabled = enable;
		setStringProperty(ASSESSMENT_MODE_ENABLED, Boolean.toString(enable), true);
	}

	public int getAssessmentModeLeadTime() {
		return assessmentModeLeadTime;
	}

	public void setAssessmentModeLeadTime(int assessmentModeLeadTime) {
		this.assessmentModeLeadTime = assessmentModeLeadTime;
		setStringProperty(ASSESSMENT_MODE_LEAD_TIME, Integer.toString(assessmentModeLeadTime), true);
	}

	public int getAssessmentModeFollowupTime() {
		return assessmentModeFollowupTime;
	}

	public void setAssessmentModeFollowupTime(int assessmentModeFollowupTime) {
		this.assessmentModeFollowupTime = assessmentModeFollowupTime;
		setStringProperty(ASSESSMENT_MODE_FOLLOWUP_TIME, Integer.toString(assessmentModeFollowupTime), true);
	}

	public String getAssessmentModeAdmissibleIps() {
		return assessmentModeAdmissibleIps;
	}

	public void setAssessmentModeAdmissibleIps(String assessmentModeAdmissibleIps) {
		this.assessmentModeAdmissibleIps = assessmentModeAdmissibleIps;
		setStringProperty(ASSESSMENT_MODE_ADMISSIBLE_IPS, assessmentModeAdmissibleIps, true);
	}

	public String getAssessmentModeSebKeys() {
		return assessmentModeSebKeys;
	}

	public void setAssessmentModeSebKeys(String assessmentModeSebKeys) {
		this.assessmentModeSebKeys = assessmentModeSebKeys;
		setStringProperty(ASSESSMENT_MODE_SEB_KEYS, assessmentModeSebKeys, true);
	}
	
	public boolean isOwnerCanViewAllCoursesInCurriculum() {
		return ownerCanViewAllCoursesInCurriculum;
	}

	public void setOwnerCanViewAllCoursesInCurriculum(boolean enable) {
		this.ownerCanViewAllCoursesInCurriculum = enable;
		setStringProperty(OWNER_CAN_VIEW_ALL_COURSES_IN_CURRICULUM, Boolean.toString(enable), true);
	}

	public boolean isRollCallReminderEnabled() {
		return rollCallReminderEnabled;
	}

	public void setRollCallReminderEnabled(boolean enable) {
		this.rollCallReminderEnabled = enable;
		setStringProperty(ROLLCALL_REMINDER_ENABLED, Boolean.toString(enable), true);
	}

	public int getRollCallReminderPeriod() {
		return rollCallReminderPeriod;
	}

	public void setRollCallReminderPeriod(int period) {
		this.rollCallReminderPeriod = period;
		setIntProperty(ROLLCALL_REMINDER_PERIOD, period, true);
	}

	public int getRollCallAutoClosePeriod() {
		return rollCallAutoClosePeriod;
	}

	public void setRollCallAutoClosePeriod(int period) {
		this.rollCallAutoClosePeriod = period;
		setIntProperty(ROLLCALL_AUTOCLOSE_PERIOD, period, true);
	}

	public boolean isAbsenceAppealEnabled() {
		return absenceAppealEnabled;
	}

	public void setAbsenceAppealEnabled(boolean enable) {
		this.absenceAppealEnabled = enable;
		setStringProperty(ABSENCE_APPEAL_ENABLED, Boolean.toString(enable), true);
	}

	public int getAbsenceAppealPeriod() {
		return absenceAppealPeriod;
	}

	public void setAbsenceAppealPeriod(int period) {
		this.absenceAppealPeriod = period;
		setIntProperty(ABSENCE_APPEAL_PERIOD, period, true);
	}

	public boolean isRollCallDefaultEnabled() {
		return rollCallDefaultEnabled;
	}

	public void setRollCallDefaultEnabled(boolean enable) {
		this.rollCallDefaultEnabled = enable;
		setStringProperty(ROLLCALL_ENABLED, Boolean.toString(enable), true);
	}

	public boolean isRollCallCalculateAttendanceRateDefaultEnabled() {
		return rollCallCalculateAttendanceRateDefaultEnabled;
	}

	public void setRollCallCalculateAttendanceRateDefaultEnabled(boolean enable) {
		rollCallCalculateAttendanceRateDefaultEnabled = enable;
		setStringProperty(CALCULATE_ATTENDANCE_RATE_DEFAULT_ENABLED, Boolean.toString(enable), true);
	}

	public double getRequiredAttendanceRateDefault() {
		return requiredAttendanceRateDefault;
	}

	public void setRequiredAttendanceRateDefault(double rate) {
		requiredAttendanceRateDefault = rate;
		setStringProperty(REQUIRED_ATTENDANCE_RATE_DEFAULT, Double.toString(rate), true);
	}

	public boolean isTeacherCalendarSyncEnabledDefault() {
		return teacherCalendarSyncEnabledDefault;
	}

	public void setTeacherCalendarSyncEnabledDefault(boolean enable) {
		teacherCalendarSyncEnabledDefault = enable;
		setStringProperty(TEACHER_CALENDAR_SYNC_DEFAULT_ENABLED, Boolean.toString(enable), true);
	}

	public boolean isParticipantCalendarSyncEnabledDefault() {
		return participantCalendarSyncEnabledDefault;
	}

	public void setParticipantCalendarSyncEnabledDefault(boolean enable) {
		participantCalendarSyncEnabledDefault = enable;
		setStringProperty(PARTICIPANT_CALENDAR_SYNC_DEFAULT_ENABLED, Boolean.toString(enable), true);
	}

	public boolean isCourseCalendarSyncEnabledDefault() {
		return courseCalendarSyncEnabledDefault;
	}

	public void setCourseCalendarSyncEnabledDefault(boolean enable) {
		courseCalendarSyncEnabledDefault = enable;
		setStringProperty(COURSE_CALENDAR_SYNC_DEFAULT_ENABLED, Boolean.toString(enable), true);
	}

	public boolean isShowLectureBlocksAllTeachersDefault() {
		return showLectureBlocksAllTeachersDefault;
	}

	public void setShowLectureBlocksAllTeachersDefault(boolean enable) {
		showLectureBlocksAllTeachersDefault = enable;
		setStringProperty(COURSE_SHOW_ALL_TEACHERS, Boolean.toString(enable), true);
	}

	public DailyRollCall getDailyRollCall() {
		return DailyRollCall.relaxedValue(dailyRollCall);
	}

	public void setDailyRollCall(DailyRollCall enable) {
		this.dailyRollCall = enable.name();
		setStringProperty(DAILY_ROLL_CALL, dailyRollCall, true);
	}

	public int getDefaultPlannedLectures() {
		return defaultPlannedLectures;
	}

	public void setDefaultPlannedLectures(int defaultPlannedLectures) {
		this.defaultPlannedLectures = defaultPlannedLectures;
		setStringProperty(DEFAULT_PLANNED_LECTURES, Integer.toString(defaultPlannedLectures), true);
	}
}
