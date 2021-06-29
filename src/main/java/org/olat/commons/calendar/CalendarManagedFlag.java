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
package org.olat.commons.calendar;

import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 31.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CalendarManagedFlag {
	
	all,
		details(all),
		subject(details, all),
		description(details, all),
		location(details, all),
		color(details, all),
		dates(details, all),
		classification(all),
		links(all),
		liveStreamUrl(details, all)
	;
	
	private CalendarManagedFlag[] parents;
	private static final Logger log = Tracing.createLoggerFor(CalendarManagedFlag.class);
	public static final CalendarManagedFlag[] EMPTY_ARRAY = new CalendarManagedFlag[0];
	
	private static CalendarModule calendarModule;
	
	private CalendarManagedFlag() {
		//
	}
	
	private CalendarManagedFlag(CalendarManagedFlag... parents) {
		if(parents == null) {
			this.parents = new CalendarManagedFlag[0];
		} else {
			this.parents = parents;
		}
	}
	
	public static String toString(CalendarManagedFlag[] flags) {
		if(flags == null || flags.length == 0) return "";
		
		StringBuilder sb = new StringBuilder();
		for(CalendarManagedFlag flag:flags) {
			if(sb.length() > 0) sb.append(",");
			sb.append(flag.name());
		}
		return sb.toString();
	}
	
	public static CalendarManagedFlag[] toEnum(String flags) {
		if(StringHelper.containsNonWhitespace(flags)) {
			String[] flagArr = flags.split("[\\\\,]");
			CalendarManagedFlag[] flagEnums = new CalendarManagedFlag[flagArr.length];
	
			int count = 0;
			for(String flag:flagArr) {
				if(StringHelper.containsNonWhitespace(flag)) {
					try {
						CalendarManagedFlag flagEnum = valueOf(flag);
						flagEnums[count++] = flagEnum;
					} catch (Exception e) {
						log.warn("Cannot parse this managed flag: {}", flag, e);
					}
				}
			}
			
			if(count != flagEnums.length) {
				flagEnums = Arrays.copyOf(flagEnums, count);
			}
			return flagEnums;
		} else {
			return EMPTY_ARRAY;
		}
	}
	
	public static boolean isManaged(KalendarEvent event, CalendarManagedFlag marker) {
		if(calendarModule == null) {
			calendarModule = CoreSpringFactory.getImpl(CalendarModule.class);
		}
		if(!calendarModule.isManagedCalendars()) {
			return false;
		}
		
		if(event != null && (contains(event, marker) || contains(event, marker.parents))) {
			return true;
		}
		return false;
	}
	
	private static boolean contains(KalendarEvent event, CalendarManagedFlag... markers) {
		if(event == null || markers == null) return false;
		CalendarManagedFlag[] flags = event.getManagedFlags();
		return contains(flags, markers);
	}

	private static boolean contains(CalendarManagedFlag[] flags, CalendarManagedFlag... markers) {
		if(flags == null || flags.length == 0) return false;

		for(CalendarManagedFlag flag:flags) {
			for(CalendarManagedFlag marker:markers) {
				if(flag.equals(marker)) {
					return true;
				}
			}
		}
		return false;
	}

}
