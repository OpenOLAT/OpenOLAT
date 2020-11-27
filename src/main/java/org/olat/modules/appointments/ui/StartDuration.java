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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 26 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class StartDuration {
	
	private static final StartDuration NONE = of(null, null);
	
	private final Date start;
	private final Integer duration;
	
	private StartDuration(Date start, Integer duration) {
		this.start = start;
		this.duration = duration;
	}

	public Date getStart() {
		return start;
	}

	public Integer getDuration() {
		return duration;
	}
	
	public static final StartDuration none() {
		return NONE;
	}
	
	public static final StartDuration of(Date start, Integer duration) {
		return new StartDuration(start, duration);
	}
	
	public static final StartDuration ofString(Date start, String duration) {
		return of(start, integerOrNull(duration));
	}
	
	private static Integer integerOrNull(String value) {
		if (StringHelper.containsNonWhitespace(value)) {
			try {
				return Integer.valueOf(value);
			} catch (Exception e) {
				// fall through
			}
		}
		return null;
	}
	
	public static final StartDuration next(StartDuration previous, StartDuration previous2) {
		if (previous != null) {
			if (previous.getStart() != null) {
				if (previous2 != null && previous2.getStart() != null && previous2.getDuration() != null) {
					Date previous2End = DateUtils.addMinutes(previous2.getStart(), previous2.getDuration().intValue());
					long pauseMillis = previous.getStart().getTime() - previous2End.getTime();
					Integer duration = previous.getDuration() != null? previous.getDuration(): previous2.getDuration();
					Date nextStart = DateUtils.addMinutes(previous.getStart(), duration.intValue());
					nextStart = new Date(nextStart.getTime() + pauseMillis);
					return of(nextStart, previous.getDuration());
				} else if (previous.getDuration() != null) {
					GregorianCalendar previousCal = new GregorianCalendar();
					previousCal.setTime(previous.getStart());
					Date nextStart = DateUtils.addMinutes(previous.getStart(), previous.getDuration().intValue());
					GregorianCalendar nextCal = new GregorianCalendar();
					nextCal.setTime(nextStart);
					if (nextCal.get(Calendar.MINUTE) > previousCal.get(Calendar.MINUTE)) {
						nextCal.set(Calendar.MINUTE, previousCal.get(Calendar.MINUTE));
						nextCal.set(Calendar.HOUR, nextCal.get(Calendar.HOUR) + 1);
					} else if (nextCal.get(Calendar.MINUTE) < previousCal.get(Calendar.MINUTE)) {
						nextCal.set(Calendar.MINUTE, previousCal.get(Calendar.MINUTE));
					}
					return of(nextCal.getTime(), previous.getDuration());	
				} else {
					return of(previous.getStart(), null);
				}
			} 
			
		}
		return none();
	}
	
	public static Date getEnd(StartDuration startDuration) {
		if (startDuration != null && startDuration.getStart() != null && startDuration.getDuration() != null) {
			return DateUtils.addMinutes(startDuration.getStart(), startDuration.getDuration().intValue());
		}
		return null;
	}

}
