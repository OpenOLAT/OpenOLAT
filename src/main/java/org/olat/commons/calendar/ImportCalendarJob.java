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
package org.olat.commons.calendar;

import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.manager.ImportToCalendarManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * <p>
 * Initial Date:  21 feb. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@DisallowConcurrentExecution
public class ImportCalendarJob extends JobWithDB {

	private static final Logger log = Tracing.createLoggerFor(ImportCalendarJob.class);
	private static final Random random = new Random();
	
	@Override
	public void executeWithDB(JobExecutionContext context) {
		try {
			jitter();
			CoreSpringFactory.getImpl(ImportToCalendarManager.class).updateCalendarIn();
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private void jitter() {
		try {
			double millis = random.nextDouble() * 180000.0d;
			long wait = Math.round(millis);
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			log.error("", e);
		}
	}
}
