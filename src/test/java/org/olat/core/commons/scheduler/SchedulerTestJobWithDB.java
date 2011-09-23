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
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.core.commons.scheduler;

import java.util.List;

import org.olat.core.commons.persistence.DBFactory;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * <h3>Description:</h3>
 * Test job that makes a database call. Please note that almost all olat jobs
 * will have implicit database calls! To be save, always use the JobWithDB!!
 * <p>
 * Initial Date: 27.04.2007 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class SchedulerTestJobWithDB extends JobWithDB {
	private boolean testValueFromSchedulerTest;

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {

		// try a query without initializing - if this works, then there was already
		// a session in the thread local
		try {
			DBFactory.getInstance(false).find("from org.olat.core.id.Identity");
			testValueFromSchedulerTest = Boolean.FALSE;
			// 
		} catch (Exception e) {
			// as expected
			testValueFromSchedulerTest = Boolean.TRUE;
			// now open a hibernate session
			List resuList = DBFactory.getInstance(true).find("from org.olat.core.id.Identity");
			log.info("Identity count is::" + resuList.size(), null);
		}

	}
	
	public boolean getTestValueFromSchedulerTest() {
		return testValueFromSchedulerTest;
	}

}
