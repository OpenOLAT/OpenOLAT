/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.commons.services.scheduler;

import static org.quartz.JobBuilder.newJob;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import org.junit.Assert;
import org.junit.Test;
import org.olat.test.OlatTestCase;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.TriggerUtils;
import org.quartz.spi.OperableTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.SimpleTriggerFactoryBean;

/**
 * Description:<br>
 * <P>
 * Initial Date: 03.03.2008 <br>
 * 
 * @author guido
 */	
public class SchedulerTest extends OlatTestCase {

	@Autowired
	private Scheduler scheduler;
	
	@Test
	public void testSimpleTrigger() throws SchedulerException, ParseException {
		JobDetail job = newJob(SchedulerTestJob.class)
				.withIdentity("schedulerTestJobSimpleTrigger", Scheduler.DEFAULT_GROUP)
				.build();

		SimpleTriggerFactoryBean trigger = new SimpleTriggerFactoryBean();
		trigger.setName("Test scheduler trigger");
		trigger.setStartDelay(0);
		trigger.setRepeatInterval(1000);
		trigger.setRepeatCount(10);
		trigger.setJobDetail(job);
		trigger.afterPropertiesSet();
		// Schedule job now
		scheduler.scheduleJob(job, trigger.getObject());
		
		sleep(20);//because of cal.add(Calendar.MILLISECOND, 11);

		//check number of calls to the job
		org.quartz.Calendar quartzCal = scheduler.getCalendar(trigger.getObject().getCalendarName());
		Calendar cal = Calendar.getInstance();
		Date start = cal.getTime();
		cal.add(Calendar.SECOND, 5);
		cal.add(Calendar.MILLISECOND, 11);
		Date end = cal.getTime();
		Assert.assertEquals(5, TriggerUtils.computeFireTimesBetween((OperableTrigger)trigger.getObject(), quartzCal, start, end).size());
	}
}
