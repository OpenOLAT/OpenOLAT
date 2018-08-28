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

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
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
	private static final String CAN_OVERRIDE_STANDARD_CONFIGURATION = "lecture.can.override.standard.configuration";
	private static final String STATUS_PARTIALLY_DONE_ENABLED = "lecture.status.partially.done.enabled";
	private static final String STATUS_CANCELLED_ENABLED = "lecture.status.cancelled.enabled";
	private static final String AUTHORIZED_ABSENCE_ENABLED = "lecture.authorized.absence.enabled";
	private static final String AUTHORIZED_ABSENCE_ATTENDANT_ENABLED = "lecture.authorized.absence.as.attendant";
	private static final String TEACHER_CAN_AUTHORIZED_ABSENCE = "teacher.can.authorized.absence";
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
	
	@Value("${lecture.enabled:false}")
	private boolean enabled;
	@Value("${lecture.managed:true}")
	private boolean lecturesManaged;
	@Value("${lecture.can.override.standard.configuration:false}")
	private boolean canOverrideStandardConfiguration;
	
	@Value("${lecture.status.partially.done.enabled:true}")
	private boolean statusPartiallyDoneEnabled;
	@Value("${lecture.status.cancelled.enabled:true}")
	private boolean statusCancelledEnabled;

	@Value("${lecture.authorized.absence.enabled:true}")
	private boolean authorizedAbsenceEnabled;
	@Value("${lecture.authorized.absence.as.attendant:false}")
	private boolean countAuthorizedAbsenceAsAttendant;
	@Value("${lecture.absence.default.authorized:false}")
	private boolean absenceDefaultAuthorized;
	@Value("${lecture.teacher.can.authorized.absence:true}")
	private boolean teacherCanAuthorizedAbsence;

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
	
	@Autowired
	public LectureModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		//module enabled/disabled
		String enabledObj = getStringPropertyValue(LECTURE_ENABLED, true);
		if(StringHelper.containsNonWhitespace(enabledObj)) {
			enabled = "true".equals(enabledObj);
		}
		
		String managedObj = getStringPropertyValue(LECTURE_MANAGED, true);
		if(StringHelper.containsNonWhitespace(managedObj)) {
			lecturesManaged = "true".equals(managedObj);
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

		String absenceDefaultAuthorizedObj = getStringPropertyValue(ABSENCE_DEFAULT_AUTHORIZED, true);
		if(StringHelper.containsNonWhitespace(absenceDefaultAuthorizedObj)) {
			absenceDefaultAuthorized = "true".equals(absenceDefaultAuthorizedObj);
		}
		
		String teacherCanAuthorizedAbsenceObj = getStringPropertyValue(TEACHER_CAN_AUTHORIZED_ABSENCE, true);
		if(StringHelper.containsNonWhitespace(teacherCanAuthorizedAbsenceObj)) {
			teacherCanAuthorizedAbsence = "true".equals(teacherCanAuthorizedAbsenceObj);
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
	}

	@Override
	protected void initFromChangedProperties() {
		init();
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
}
