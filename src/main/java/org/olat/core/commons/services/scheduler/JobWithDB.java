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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.logging.Tracing;
import org.olat.core.logging.activity.ThreadLocalUserActivityLoggerInstaller;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 * <h3>Description:</h3>
 * This abstract class implements the regular Quartz job interface and allows
 * the user to implement an alternative execute method. This abstract class
 * should be used whenever the job does some hibernate calls using the
 * DBFactory.
 * <p>
 * This abstract class guarantees that after executing the job the database
 * handle is being released properly.
 * <p>
 * If unsure, use this Job! Even if you don't use extra DB calls, some of the
 * manager you call in your code are likely to do some database calls!
 * <p>
 * Initial Date: 27.04.2007 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public abstract class JobWithDB extends QuartzJobBean {
	// A logger instantiated for the immplementing class of this abstract class
	private static final Logger log = Tracing.createLoggerFor(JobWithDB.class);
	
	@Override
	protected final void executeInternal(JobExecutionContext arg0)
			throws JobExecutionException {
		boolean success = false;
		try {
			//init logging framework
			ThreadLocalUserActivityLoggerInstaller.initEmptyUserActivityLogger();
			
			executeWithDB(arg0);
			DBFactory.getInstance().commitAndCloseSession();
			success = true;
		} catch(JobExecutionException e) {
			// for documentation purpose only
			log.error("", e);
			throw e;
		} finally {
			//clean up logging
			ThreadLocalUserActivityLoggerInstaller.resetUserActivityLogger();
			if (!success) {
				DBFactory.getInstance().rollbackAndCloseSession();
			}

		}
		
	}

	/**
	 * Implement this execute method instead of the regular execute method if your
	 * job does some database stuff using the database factory.
	 * 
	 * @param arg0 The JobExecutionContext
	 * @throws JobExecutionException
	 */
	abstract public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException;

}
