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
package org.olat.modules.quality.analysis;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 15 Jan 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TemporalKey implements Comparable<TemporalKey> {
	
	public static final String DELIMITER = "-";
	public static final int NO_VALUE = -1;
	
	private static final TemporalKey NONE = new TemporalKey(NO_VALUE, NO_VALUE);
	
	private final int year;
	private final int yearPart;

	
	public static TemporalKey of(int year, int yearPart) {
		return new TemporalKey(year, yearPart);
	}
	
	public static TemporalKey of(int year) {
		return of(year, NO_VALUE);
	}
	
	public static TemporalKey none() {
		return NONE;
	}
	
	public static TemporalKey parse(String temporalKey) {
		if (StringHelper.containsNonWhitespace(temporalKey)) {
			String[] split = temporalKey.split(DELIMITER);
			int year = getIntOrNoValue(split, 0);
			int yearPart = getIntOrNoValue(split, 1);
			return of(year, yearPart);
		}
		return none();
	}
	
	private static int getIntOrNoValue(String[] split, int index) {
		if (split.length > index) {
			String value = split[index];
			if (StringHelper.containsNonWhitespace(value)) {
				try {
					return Integer.parseInt(value);
				} catch (NumberFormatException e) {
					//
				}
			}
		}
		return NO_VALUE;
	}
	
	private TemporalKey(int year, int yearPart) {
		this.year = year;
		this.yearPart = yearPart;
	}

	public int getYear() {
		return year;
	}

	public int getYearPart() {
		return yearPart;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TemporalKey [year=");
		builder.append(year);
		builder.append(", yearPart=");
		builder.append(yearPart);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + year;
		result = prime * result + yearPart;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TemporalKey other = (TemporalKey) obj;
		if (year != other.year)
			return false;
		if (yearPart != other.yearPart)
			return false;
		return true;
	}

	@Override
	public int compareTo(TemporalKey o) {
		int compare = Integer.compare(this.year, o.year);
		if (compare == 0) {
			compare = Integer.compare(this.yearPart, o.yearPart);
		}
		return compare;
	}

}
