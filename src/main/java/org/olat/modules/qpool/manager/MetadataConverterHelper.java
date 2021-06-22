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
package org.olat.modules.qpool.manager;

import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.model.LOMDuration;

/**
 * 
 * Some utilities to convert LOM specific date format
 * 
 * Initial date: 11.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MetadataConverterHelper {

	/**
	 * P[yY][mM][dD][T[hH][nM][s[.s]S]] where:<br>
	 * y = number of years (integer, > 0, not restricted)<br>
	 * m = number of months (integer, > 0, not restricted, e.g., > 12 is acceptable)<br>
	 * d = number of days (integer, > 0, not restricted, e.g., > 31 is acceptable)<br>
	 * h = number of hours (integer, > 0, not restricted, e.g., > 23 is acceptable)<br>
	 * n = number of minutes (integer, > 0, not restricted, e.g., > 59 is acceptable)<br>
	 * s = number of seconds or fraction of seconds (integer, > 0, not restricted, e.g., > 59 is acceptable)<br>
	 * 
	 * The character literal designators "P", "Y", "M", "D", "T", "H", "M", "S" must appear if the corresponding nonzero
	 * value is present. If the value of years, months, days, hours, minutes or seconds is zero, the value and
	 * corresponding designation (e.g., "M") may be omitted, but at least one designator and value must always be present.
	 * The designator "P" is always present. The designator "T" shall be omitted if all of the time (hours/minutes/seconds)
	 * are zero. Negative durations are not supported. NOTE 1:--This value space is based on ISO8601:2000.
	 * (see also http://www.w3.org/TR/xmlschema-2/#duration)
	 * PT1H30M, PT1M45S
	 * @return
	 */
	public static String convertDuration(int day, int hour, int minute, int seconds) {
		StringBuilder sb = new StringBuilder();
		boolean hasD = day > 0;
		boolean hasT = (hour > 0 || minute > 0 || seconds > 0);
		if(hasD || hasT) {
			sb.append("P");
			if(hasD) {
				sb.append(day).append("D");
			}
			if(hasT) {
				sb.append("T");
				if(hour > 0) {
					sb.append(hour).append("H");
				}
				if(minute > 0) {
					sb.append(minute).append("M");
				}
				if(seconds > 0) {
					sb.append(seconds).append("S");
				}
			}
		}
		return sb.toString();
	}
	
	public static LOMDuration convertDuration(String durationStr) {
		LOMDuration duration = new LOMDuration();
		if(StringHelper.containsNonWhitespace(durationStr) && durationStr.startsWith("P")) {
			//remove p
			durationStr = durationStr.substring(1, durationStr.length());
			int indexT = durationStr.indexOf('T');
			if(indexT < 0) {
				convertDurationP(durationStr, duration);
			} else {
				String pDurationStr = durationStr.substring(0, indexT);
				convertDurationP(pDurationStr, duration);
				String tDurationStr = durationStr.substring(indexT + 1, durationStr.length());
				convertDurationT(tDurationStr, duration);
			}
		}
		return duration;
	}
	
	private static void convertDurationP(String durationStr, LOMDuration duration) {
		int indexY = durationStr.indexOf('Y');
		if(indexY >= 0) {
			duration.setYear(extractValueFromDuration(durationStr, indexY));
			durationStr = durationStr.substring(indexY + 1, durationStr.length());
		}
		int indexM = durationStr.indexOf('M');
		if(indexM >= 0) {
			duration.setMonth(extractValueFromDuration(durationStr, indexM));
			durationStr = durationStr.substring(indexM + 1, durationStr.length());
		}
		int indexD = durationStr.indexOf('D');
		if(indexD >= 0) {
			duration.setDay(extractValueFromDuration(durationStr, indexD));
			durationStr = durationStr.substring(indexD + 1, durationStr.length());
		}
	}
	
	public static long convertToSeconds(LOMDuration duration) {
		long time = duration.getSeconds();
		time += (duration.getMinute() * 60);
		time += (duration.getHour() * (60 * 60));
		time += (duration.getDay() * (60 * 60 * 24));
		time += (duration.getMonth() * (60 * 60 * 24 * 30));
		time += (duration.getYear() * (60 * 60 * 24 * 30 * 365));
		return time;
	}
	
	private static void convertDurationT(String durationStr, LOMDuration duration) {
		int indexH = durationStr.indexOf('H');
		if(indexH >= 0) {
			duration.setHour(extractValueFromDuration(durationStr, indexH));
			durationStr = durationStr.substring(indexH + 1, durationStr.length());
		}
		int indexMin = durationStr.indexOf('M');
		if(indexMin >= 0) {
			duration.setMinute(extractValueFromDuration(durationStr, indexMin));
			durationStr = durationStr.substring(indexMin + 1, durationStr.length());
		}
		int indexS = durationStr.indexOf('S');
		if(indexS >= 0) {
			duration.setSeconds(extractValueFromDuration(durationStr, indexS));
			durationStr = durationStr.substring(indexS + 1, durationStr.length());
		}
	}
	
	private static int extractValueFromDuration(String durationStr, int index)
	throws NumberFormatException {
		if(index <= 0) return 0;
		String intVal = durationStr.substring(0, index);
		return Integer.parseInt(intVal);
	}
}
