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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * The Year model
 * 
 * <P>
 * Initial Date: Aug 13, 2009 <br>
 * 
 * @author gwassmann
 */
public class Year {

	private int year;
	private SortedMap<Integer, Month> months;

	/**
	 * Constructor
	 * 
	 * @param year
	 */
	Year(int year) {
		this.year = year;
		months = new TreeMap<>(Collections.reverseOrder());
	}

	/**
	 * Adds an item (Dated object) to this Year
	 * 
	 * @param item
	 */
	void add(Dated item) {
		Date date = item.getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int m = cal.get(Calendar.MONTH);
		if (months.containsKey(m)) {
			Month month = months.get(m);
			month.add(item);
		} else {
			Month month = new Month(m);
			month.add(item);
			months.put(m, month);
		}
	}

	/**
	 * Removes the item from this year
	 * 
	 * @param item
	 */
	void remove(Dated item) {
		Date date = item.getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int m = cal.get(Calendar.MONTH);
		if (months.containsKey(m)) {
			Month month = months.get(m);
			month.remove(item);
			if (month.itemsCount() == 0) {
				// remove month
				months.remove(m);
			}
		}
	}

	/**
	 * @return The months of this year
	 */
	public Collection<Month> getMonths() {
		return months.values();
	}

	/**
	 * @return The number of months in this year
	 */
	int monthsCount() {
		return months.size();
	}

	/**
	 * @return The items of this year
	 */
	public List<? extends Dated> getItems() {
		List<Dated> items = new ArrayList<>();
		for (Month month : months.values()) {
			items.addAll(month.getItems());
		}
		return items;
	}

	/**
	 * @return The name of this year, e.g. 2009
	 */
	public String getName() {
		return Integer.toString(year);
	}

	/**
	 * @return The year
	 */
	int getYear() {
		return year;
	}
}
