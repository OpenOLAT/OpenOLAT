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
package org.olat.modules.reminder;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.quartz.CronExpression;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReminderModuleTest extends OlatTestCase {
	
	@Autowired
	private ReminderModule reminderModule;
	
	
	@Test
	public void testCronJob_everyTwoHours() throws ParseException {
		reminderModule.setScheduler("2", "9:30");
		String cron = reminderModule.getCronExpression();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 5);
		
		CronExpression cronExpression = new CronExpression(cron);
		Date d1 = cronExpression.getNextValidTimeAfter(cal.getTime());
		
		Calendar cal1 = Calendar.getInstance();
		cal1.setTime(d1);
		Assert.assertEquals(1, cal1.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(30, cal1.get(Calendar.MINUTE));	
	}
	
	@Test
	public void testCronJob_everyHeightHours() throws ParseException {
		reminderModule.setScheduler("8", "6:30");
		sleep(1000);
		
		String cron = reminderModule.getCronExpression();
		
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 5);
		
		CronExpression cronExpression = new CronExpression(cron);
		Date d1 = cronExpression.getNextValidTimeAfter(cal.getTime());
		
		Calendar triggerCal = Calendar.getInstance();
		triggerCal.setTime(d1);
		Assert.assertEquals(6, triggerCal.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(30, triggerCal.get(Calendar.MINUTE));

		Date d2 = cronExpression.getNextValidTimeAfter(d1);
		triggerCal.setTime(d2);
		Assert.assertEquals(14, triggerCal.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(30, triggerCal.get(Calendar.MINUTE));

		Date d3 = cronExpression.getNextValidTimeAfter(d2);
		triggerCal.setTime(d3);
		Assert.assertEquals(22, triggerCal.get(Calendar.HOUR_OF_DAY));
		Assert.assertEquals(30, triggerCal.get(Calendar.MINUTE));
	}
}