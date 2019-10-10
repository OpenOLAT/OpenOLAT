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
*/
package org.olat.course.statistic;

import java.util.Random;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * job that updates all the statistics wrapped in DBjob to close DB sessions properly
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
@DisallowConcurrentExecution
public class UpdateStatisticsJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(UpdateStatisticsJob.class);

	/** the logging object used in this class **/
	private final Random random = new Random();

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		jitter();// wait 0 - 60 seconds

		StatisticUpdateManager statisticManager = (StatisticUpdateManager) CoreSpringFactory.getBean("org.olat.course.statistic.StatisticUpdateManager");
		if (statisticManager==null) {
			log.error("executeWithDB: UpdateStatisticsJob configured, but no StatisticUpdateManager available");
		} else {
			if (!statisticManager.updateStatistics(false, null)) {
				log.warn("executeWithDB: UpdateStatisticsJob could not trigger updateStatistics - must be already running");
			}
		}
	}
	
	private void jitter() {
		try {
			double millis = random.nextDouble() * 60.0d * 1000.0d;
			long wait = Math.round(millis);
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			log.error("", e);
		}
	}
}
