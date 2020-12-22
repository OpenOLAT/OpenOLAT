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
package org.olat.modules.appointments.ui;

import java.util.Date;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.Appointment;

/**
 * 
 * Initial date: 24 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentsUIFactory {
	
	public static String getDisplayLocation(Translator translator, Appointment appointement) {
		StringBuilder sb = new StringBuilder();
		
		boolean hasLocation = StringHelper.containsNonWhitespace(appointement.getLocation());
		boolean hasMeeting = appointement.getBBBMeeting() != null || appointement.getTeamsMeeting() != null;
		if (hasLocation) {
			sb.append(appointement.getLocation());
		}
		if (hasLocation && hasMeeting) {
			sb.append(" / ");
		}
		if (hasMeeting) {
			sb.append(translator.translate("appointment.location.bbb"));
		}
		
		return sb.length() > 0? sb.toString(): null;
	}
	
	public static boolean isEndInFuture(Appointment appointment, Date now) {
		Date end = appointment.getEnd();
		end = DateUtils.isSameDate(end, appointment.getStart())
				? DateUtils.setTime(end, 23, 59, 59)
				: end;
		return now.before(end);
	}
	
	public static boolean isEndInPast(Appointment appointment, Date now) {
		return !isEndInFuture(appointment, now);
	}
	
}
