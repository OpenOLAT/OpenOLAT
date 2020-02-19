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
package org.olat.user.manager;

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.user.AbsenceLeave;

/**
 * 
 * Initial date: 14 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AbsenceLeaveHelper {
	
	
	public static boolean isOnLeave(Date date, AbsenceLeave absenceLeave, OLATResourceable resource, String subIdent) {
		return isOnLeave(absenceLeave, resource, subIdent) && isOnLeave(absenceLeave, date);
	}
	
	private static boolean isOnLeave(AbsenceLeave absenceLeave, OLATResourceable resource, String subIdent) {
		if(!StringHelper.containsNonWhitespace(absenceLeave.getResName())) {
			return true;
		}
		if(resource != null
				&& StringHelper.containsNonWhitespace(resource.getResourceableTypeName()) && resource.getResourceableTypeName().equals(absenceLeave.getResName())
				&& resource.getResourceableId() != null && resource.getResourceableId().equals(absenceLeave.getResId())
				&& ((!StringHelper.containsNonWhitespace(subIdent) && !StringHelper.containsNonWhitespace(absenceLeave.getSubIdent()))
						|| (subIdent != null && subIdent.equals(absenceLeave.getSubIdent())))) {
			return true;
		}
		return false;
	}
	
	private static boolean isOnLeave(AbsenceLeave absenceLeave, Date date) {
		if(absenceLeave.getAbsentFrom() == null && absenceLeave.getAbsentTo() == null) {
			return true;
		}
		if(absenceLeave.getAbsentFrom() == null && date.before(CalendarUtils.endOfDay(absenceLeave.getAbsentTo()))) {
			return true;
		}
		if(absenceLeave.getAbsentTo() == null && date.after(CalendarUtils.startOfDay(absenceLeave.getAbsentFrom()))) {
			return true;
		}
		if(date.after(CalendarUtils.startOfDay(absenceLeave.getAbsentFrom())) && date.before(CalendarUtils.endOfDay(absenceLeave.getAbsentTo()))) {
			return true;
		}
		return false;
	}

}
