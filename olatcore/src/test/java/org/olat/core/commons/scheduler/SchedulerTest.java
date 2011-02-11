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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.commons.scheduler;

import static org.junit.Assert.*;

import java.util.Date;

import org.junit.Test;
import org.olat.core.test.MockServletContextWebContextLoader;
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
	"classpath:org/olat/core/commons/scheduler/scheduler.xml"})
	
public class SchedulerTest extends AbstractJUnit4SpringContextTests {

	@Test public void testSimpleTrigger() {
		Date start = new Date();
		JobDetailBean job = (JobDetailBean)applicationContext.getBean("schedulerTestJobSimple");
		assertNotNull(job);
		//wait until the job is executed
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		SimpleTriggerBean trigger = (SimpleTriggerBean) applicationContext.getBean("schedulerTestJobSimpleTrigger");
		Date end = new Date();
		assertEquals(5, trigger.computeNumTimesFiredBetween(start, end));
	}
	
}
