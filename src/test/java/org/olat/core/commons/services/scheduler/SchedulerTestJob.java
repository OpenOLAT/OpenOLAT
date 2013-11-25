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
package org.olat.core.commons.services.scheduler;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * <h3>Description:</h3>
 * Test job used by SchedulerTest.
 * <p>
 * Please note that almost all olat jobs will have implicit database calls! To
 * be save, always use the JobWithDB instead!!
 * <p>
 * Initial Date: 21.03.2007 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 * @author guido
 */
public class SchedulerTestJob extends QuartzJobBean {

	@Override
	public void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		String testValueFromSchedulerTest = arg0.getMergedJobDataMap().getString("testValue");
		arg0.setResult(testValueFromSchedulerTest);
		System.out.println("SchedulerTestJob data: "+testValueFromSchedulerTest);
	}

}