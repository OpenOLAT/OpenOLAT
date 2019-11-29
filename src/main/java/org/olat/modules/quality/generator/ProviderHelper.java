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
package org.olat.modules.quality.generator;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 29.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProviderHelper {

	private static final String DELIMITER = ",";
	
	public static Date subtractDays(Date date, String daysToAdd) {
		int days = Integer.parseInt(daysToAdd);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, -days);
		return c.getTime();
	}

	public static Date addDays(Date date, String daysToAdd) {
		int days = Integer.parseInt(daysToAdd);
		return addDays(date, days);
	}

	public static Date addDays(Date date, int days) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DATE, days);
		return c.getTime();
	}
	
	public static Date addHours(Date date, String hoursToAdd) {
		int hours = Integer.parseInt(hoursToAdd);
		return addHours(date, hours);
	}
	
	public static Date addHours(Date date, int hours) {
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.HOUR, hours);
		return c.getTime();
	}
	
	public static Date addMinutes(Date date, String minutesToAdd) {
		int minutes = Integer.parseInt(minutesToAdd);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.MINUTE, minutes);
		return c.getTime();
	}
	
	public static Double toDouble(String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				return Double.valueOf(value);
			} catch (NumberFormatException e) {
				// 
			}
		}
		return null;
	}
	
	public static int toIntOrZero(String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}
	
	public static long toLongOrZero(String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException e) {
				return 0;
			}
		}
		return 0;
	}
	
	public static String concatDaysOfWeek(List<DayOfWeek> daysOfWeek) {
		if (daysOfWeek == null || daysOfWeek.isEmpty()) return null;
		
		return daysOfWeek.stream().map(DayOfWeek::name).collect(joining(DELIMITER));
		
	}
	
	public static List<DayOfWeek> splitDaysOfWeek(String config) {
		if (!StringHelper.containsNonWhitespace(config)) return new ArrayList<>(0);
		
		return Arrays.stream(config.split(DELIMITER)).map(DayOfWeek::valueOf).collect(toList());
	}
	

	public static List<LocalDate> generateDaysInRange(LocalDate startDate, LocalDate endDate) {
		if (startDate == null || endDate == null) return new ArrayList<>(0);
		
		List<LocalDate> datesInRange = new ArrayList<>();
		while (!startDate.isAfter(endDate)) {
			datesInRange.add(startDate);
			startDate = startDate.plusDays(1);
		}
		return datesInRange;
	}
	
}
