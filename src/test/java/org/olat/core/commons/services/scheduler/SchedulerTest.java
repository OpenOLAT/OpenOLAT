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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;
import org.olat.test.MockServletContextWebContextLoader;
import org.springframework.scheduling.quartz.JobDetailBean;
import org.springframework.scheduling.quartz.SimpleTriggerBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Description:<br>
 * <P>
 * Initial Date: 03.03.2008 <br>
 * 
 * @author guido
 */
@ContextConfiguration(loader = MockServletContextWebContextLoader.class, locations = {
	"classpath:org/olat/core/commons/services/scheduler/scheduler.xml"})
	
public class SchedulerTest extends AbstractJUnit4SpringContextTests {

	@Test public void testSimpleTrigger() {
		JobDetailBean job = (JobDetailBean)applicationContext.getBean("schedulerTestJobSimple");
		assertNotNull(job);
		SimpleTriggerBean trigger = (SimpleTriggerBean) applicationContext.getBean("schedulerTestJobSimpleTrigger");
		
		Calendar cal = Calendar.getInstance();
		Date start = cal.getTime();
		cal.add(Calendar.SECOND, 5);
		cal.add(Calendar.MILLISECOND, 011);
		Date end = cal.getTime();
		assertEquals(5, trigger.computeNumTimesFiredBetween(start, end));
	}
	
}
