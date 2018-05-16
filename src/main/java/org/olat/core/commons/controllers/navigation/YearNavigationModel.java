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
package org.olat.core.commons.controllers.navigation;

import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The data model for the YearNavigation.
 * 
 * <P>
 * Initial Date: Aug 13, 2009 <br>
 * 
 * @author gwassmann
 */
public class YearNavigationModel {

	private SortedMap<Integer, Year> years;
	private Calendar cal;
	private DateFormatSymbols symbols;
	private Year currentYear;

	/**
	 * Constructor base on <code>Dated</code> objects.
	 * 
	 * @param datedObjects
	 * @param locale
	 */
	YearNavigationModel(List<? extends Dated> datedObjects, Locale locale, Year currentYear) {
		this.currentYear = currentYear;
		cal = Calendar.getInstance();
		symbols = new DateFormatSymbols(locale);
		initializeYears(datedObjects);
	}

	/**
	 * Initializes the years and months of the model
	 * 
	 * @param datedObjects
	 */
	private void initializeYears(List<? extends Dated> datedObjects) {
		years = new TreeMap<>();
		Collections.sort(datedObjects, new DatedComparator());
		for (Dated item : datedObjects) {
			if(item.getDate() != null) {
				add(item);
			}
		}
		// Display the current year
		Date today = new Date();
		cal.setTime(today);
		int thisYear = currentYear != null? currentYear.getYear(): cal.get(Calendar.YEAR);
		goTo(thisYear);
	}

	/**
	 * Sets the current year to y or the closest existing before that
	 * 
	 * @param y The selected year
	 */
	private void goTo(int y) {
		Year year = years.get(y);
		if (year == null) {
			// get the closest year before date
			int closest = 0;
			Iterator<Integer> it = years.keySet().iterator();
			while (y > closest && it.hasNext()) {
				closest = it.next();
			}
			year = years.get(closest);
		}
		currentYear = year;
	}

	/**
	 * Go to the next year in the model
	 */
	void next() {
		Iterator<Year> it = years.values().iterator();
		while (it.hasNext()) {
			if (currentYear == it.next() && it.hasNext()) {
				// go to the next year
				currentYear = it.next();
				break;
			}
		}
	}

	/**
	 * Go to the previous year in the model
	 */
	void previous() {
		Year previous = null;
		for (Year year : years.values()) {
			if (year == currentYear) break;
			previous = year;
		}
		if (previous != null) {
			currentYear = previous;
		}
	}

	/**
	 * @return true if the model contains a next year
	 */
	boolean hasNext() {
		boolean hasNext = false;
		// if years has no elements, the lastKey method raises an exeption
		if (years.size() > 0 && currentYear != null) hasNext = years.lastKey() > currentYear.getYear();
		return hasNext;
	}

	/**
	 * @return true if the model contains a next year
	 */
	boolean hasPrevious() {
		boolean hasPrevious = false;
		// if years has no elements, the firstKey method raises an exeption
		if (years.size() > 0 && currentYear != null) hasPrevious = years.firstKey() < currentYear.getYear();
		return hasPrevious;
	}

	/**
	 * @param month
	 * @return The internationalized name of month
	 */
	String getMonthName(Month month) {
		// Get an Array of months
		String[] months = symbols.getMonths();
		return months[month.getMonth()];
	}

	/**
	 * @return The current year
	 */
	Year getCurrentYear() {
		return currentYear;
	}

	/**
	 * Adds the item to this model
	 * 
	 * @param item
	 */
	void add(Dated item) {
		if(item.getDate() == null) return;
		cal.setTime(item.getDate());
		int y = cal.get(Calendar.YEAR);
		if (years.containsKey(y)) {
			Year year = years.get(y);
			year.add(item);
		} else {
			Year year = new Year(y);
			year.add(item);
			years.put(y, year);
		}
		if (currentYear == null) {
			goTo(y);
		}
	}

	/**
	 * Removes the item from this model
	 * 
	 * @param item
	 */
	void remove(Dated item) {
		if(item.getDate() == null) return;
		cal.setTime(item.getDate());
		int y = cal.get(Calendar.YEAR);
		if (years.containsKey(y)) {
			Year year = years.get(y);
			year.remove(item);
			if (year.monthsCount() == 0) {
				years.remove(y);
			}
		}
	}
}
