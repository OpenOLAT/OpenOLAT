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
package org.olat.course.certificate;

import java.util.Calendar;
import java.util.Date;

/**
 * 
 * Initial date: 20.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum CertificationTimeUnit {
	day,
	week,
	month,
	year;
	
	public Date toDate(Date reference, int time) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(reference);
		switch(this) {
			case day: cal.add(Calendar.DATE, time); break;
			case week: cal.add(Calendar.DATE, time * 7); break;
			case month: cal.add(Calendar.MONTH, time); break;
			case year: cal.add(Calendar.YEAR, time); break;
		}
		return cal.getTime();
	}
	
	public int toDays(int time) {
		switch(this) {
			case day: return time;
			case week: return time * 7;
			case month: return time * 30;
			case year: return time * 365;
			default: return time;
		}
	}
}