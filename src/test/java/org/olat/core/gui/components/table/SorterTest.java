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
package org.olat.core.gui.components.table;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

/**
 * 
 * Initial date: 23.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class SorterTest {

	@Test
	public void testDateAndTimeStampsSort() {
		//use the comparator method of the default column descriptor
		final DefaultColumnDescriptor cd = new DefaultColumnDescriptor("test", 1, "test", Locale.ENGLISH);
		Comparator<Date> comparator = new Comparator<Date>() {
			@Override
			public int compare(Date o1, Date o2) {
				return cd.compareDateAndTimestamps(o1, o2);
			}
		};
		
		//make a list of timestamps and dates
		List<Date> dates = new ArrayList<>();
		Calendar cal = Calendar.getInstance();
		for(int i=0; i<100; i++) {
			cal.add(Calendar.MINUTE, 2);
			if(i % 2 == 0) {
				Timestamp t = new Timestamp(cal.getTimeInMillis());
				dates.add(t);
			} else {
				Date d = cal.getTime();
				dates.add(d);
			}
		}
		//something a little bit random
		Collections.shuffle(dates);
		
		//sort
		Collections.sort(dates, comparator);
		
		//check
		Date current = null;
		for(Date date:dates) {
			if(current == null) {
				current = date;
			} else {
				boolean before = current.before(date);
				Assert.assertTrue(before);
				current = date;
			}	
		}
	}

}
