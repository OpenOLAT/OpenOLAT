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
package org.olat.modules.lecture.ui.coach;

import java.util.Calendar;
import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.modules.lecture.AbsenceNotice;

/**
 * 
 * Initial date: 3 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceNoticeHelper {
	
	public static boolean isWholeDay(Date startDate, Date endDate) {
		return CalendarUtils.isSameDay(startDate, endDate)
				&& isStartOfWholeDay(startDate)
				&& isEndOfWholeDay(endDate);
	}
	
	public static boolean isStartOfWholeDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY) == 0
				&& cal.get(Calendar.MINUTE) == 0
				&& cal.get(Calendar.SECOND) == 0;
	}
	
	public static boolean isEndOfWholeDay(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		return cal.get(Calendar.HOUR_OF_DAY) == 23
				&& cal.get(Calendar.MINUTE) == 59
				&& cal.get(Calendar.SECOND) == 59;
	}
	
	public static String getEditKey(AbsenceNotice notice) {
		switch(notice.getNoticeType()) {
			case absence: return "edit.type.absence";
			case notified: return "edit.type.notice.absence";
			case dispensation: return "edit.type.dispensation";
			default: return "edit.type.absence";
		}
	}
	
	public static AbsenceNoticeCSS valueOf(AbsenceNotice notice) {
		String cssClass = null;
		String i18nKey = null;
		
		Boolean absenceAuthorized = notice.getAbsenceAuthorized();
		boolean authorized = absenceAuthorized != null && absenceAuthorized.booleanValue();
		
		switch(notice.getNoticeType()) {
			case absence:
				cssClass = authorized ? "o_icon_absence_authorized" : "o_icon_absence_unauthorized";
				i18nKey = authorized ? "noticed.absence.authorized" : "noticed.absence.unauthorized";
				break;
			case notified:
				cssClass = authorized ? "o_icon_notice_authorized" : "o_icon_notice_unauthorized";
				i18nKey = "noticed.notice.absence";
				break;
			case dispensation:
				cssClass = authorized ? "o_icon_dispensation_authorized" : "o_icon_dispensation_unauthorized";
				i18nKey = "noticed.dispensation";
				break;
		}
		
		return new AbsenceNoticeCSS(cssClass, i18nKey);
	}
	
	public static class AbsenceNoticeCSS {
		
		private final String cssClass;
		private final String i18nKey;
		
		public AbsenceNoticeCSS(String cssClass, String i18nKey) {
			this.cssClass = cssClass;
			this.i18nKey = i18nKey;
		}

		public String getCssClass() {
			return cssClass;
		}

		public String getI18nKey() {
			return i18nKey;
		}
	}
}
