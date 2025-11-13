/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui.component;

import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.course.certificate.model.CertificateImpl;

/**
 * 
 * Initial date: 13 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class NextRecertificationInDaysTest {
	
	@Test
	public void valueOfToday() {
		CertificateImpl certificate = new CertificateImpl();
		Date nextRecertification = CalendarUtils.endOfDay(CalendarUtils.getDate(2025, 11, 13));
		certificate.setNextRecertificationDate(nextRecertification);
		
		Date referenceDate = setTime(CalendarUtils.getDate(2025, 11, 13), 10, 51, 32, 345);
		
		NextRecertificationInDays next = NextRecertificationInDays.valueOf(certificate, referenceDate);
		Assert.assertNotNull(next);
		Assert.assertEquals(Long.valueOf(0l), next.days());
		Assert.assertNull(next.overdueDays());
	}
	
	@Test
	public void valueOfTwoDays() {
		CertificateImpl certificate = new CertificateImpl();
		Date nextRecertification = CalendarUtils.endOfDay(CalendarUtils.getDate(2025, 11, 13));
		certificate.setNextRecertificationDate(nextRecertification);
		
		Date referenceDate = setTime(CalendarUtils.getDate(2025, 11, 11), 10, 51, 32, 345);
		
		NextRecertificationInDays next = NextRecertificationInDays.valueOf(certificate, referenceDate);
		Assert.assertNotNull(next);
		Assert.assertEquals(Long.valueOf(2l), next.days());
		Assert.assertNull(next.overdueDays());
	}
	
	private static final Date setTime(Date date, int hour, int minute, int second, int milliSecond) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, second);
		cal.set(Calendar.MILLISECOND, milliSecond);
		return cal.getTime();
	}
}
