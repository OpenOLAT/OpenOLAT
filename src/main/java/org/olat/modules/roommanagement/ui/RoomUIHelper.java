/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.roommanagement.ui;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.olat.modules.roommanagement.RoomBooking;

/**
 * Initial date: 19 Jun 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
class RoomUIHelper {

	static String formatNextEvent(RoomBooking booking, Locale locale) {
		Date startDate = booking.getStartDate();
		Date endDate = booking.getEndDate();

		String dayOfWeek = new SimpleDateFormat("EEE", locale).format(startDate);
		String date = DateFormat.getDateInstance(DateFormat.SHORT, locale).format(startDate);
		String time = DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(startDate);

		long durationMinutes = (endDate.getTime() - startDate.getTime()) / 60000L;
		String duration = formatDuration(durationMinutes);

		return dayOfWeek + " " + date + ", " + time + " " + duration;
	}

	static String formatDuration(long minutes) {
		long hours = minutes / 60;
		long mins = minutes % 60;
		if (hours > 0 && mins > 0) {
			return hours + "h " + mins + "m";
		} else if (hours > 0) {
			return hours + "h";
		} else {
			return mins + "m";
		}
	}
}
