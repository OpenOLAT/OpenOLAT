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
package org.olat.repository;

import java.util.Calendar;
import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 2 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryLifeCycleValue implements Comparable<RepositoryEntryLifeCycleValue> {
	
	private static final Logger log = Tracing.createLoggerFor(RepositoryEntryLifeCycleValue.class);
	
	private final int value;
	private final RepositoryEntryLifeCycleUnit unit;
	
	public RepositoryEntryLifeCycleValue(int value, RepositoryEntryLifeCycleUnit unit) {
		this.value = value;
		this.unit = unit;
	}
	
	public int getValue() {
		return value;
	}

	public RepositoryEntryLifeCycleUnit getUnit() {
		return unit;
	}
	
	@Override
	public String toString() {
		return value + unit.name();
	}
	
	/**
	 * Calculate at the end of the day.
	 * 
	 * @param ref
	 * @return
	 */
	public Date toDate(Date ref) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(ref);
		switch(unit) {
			case day: cal.add(Calendar.DATE, value); break;
			case week: cal.add(Calendar.DATE, value * 7); break;
			case month: cal.add(Calendar.MONTH, value); break;
			case year: cal.add(Calendar.YEAR, value); break;
		}
		
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public Date limitDate(Date ref) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(ref);
		switch(unit) {
			case day: cal.add(Calendar.DATE, -value); break;
			case week: cal.add(Calendar.DATE, -(value * 7)); break;
			case month: cal.add(Calendar.MONTH, -value); break;
			case year: cal.add(Calendar.YEAR, -value); break;
		}
		
		cal.set(Calendar.HOUR_OF_DAY, 23);
		cal.set(Calendar.MINUTE, 59);
		cal.set(Calendar.SECOND, 59);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	@Override
	public int compareTo(RepositoryEntryLifeCycleValue o) {
		Date now = new Date();
		Date d1 = toDate(now);
		Date d2 = o.toDate(now);
		return d1.compareTo(d2);
	}

	public static RepositoryEntryLifeCycleValue parse(String string) {
		RepositoryEntryLifeCycleValue val = null;
		if(StringHelper.containsNonWhitespace(string)) {
			char lastCh = string.charAt(string.length() - 1);
			switch(lastCh) {
				case 'y': val = parse(string, RepositoryEntryLifeCycleUnit.day); break;//day
				case 'k': val = parse(string, RepositoryEntryLifeCycleUnit.week); break;//week
				case 'h': val = parse(string, RepositoryEntryLifeCycleUnit.month); break;//month
				case 'r': val = parse(string, RepositoryEntryLifeCycleUnit.year); break;//year
			}
		}
		return val;
	}
	
	private static final RepositoryEntryLifeCycleValue parse(String string, RepositoryEntryLifeCycleUnit unit) {
		RepositoryEntryLifeCycleValue val = null;
		if(string.endsWith(unit.name())) {
			try {
				String valueString = string.substring(0, string.length() - unit.name().length());
				int value = Integer.parseInt(valueString);
				val = new RepositoryEntryLifeCycleValue(value, unit);
			} catch (NumberFormatException e) {
				log.error("", e);
			}
		}
		return val;
	}

	public enum RepositoryEntryLifeCycleUnit {
		day,
		week,
		month,
		year
	}
}
