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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.scheduler.JobWithDB;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * job that updates all the statistics wrapped in DBjob to close DB sessions properly
 * <P>
 * Initial Date:  12.02.2010 <br>
 * @author Stefan
 */
public class UpdateStatisticsJob extends JobWithDB {

	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(UpdateStatisticsJob.class);

	
	/**
	 * 
	 * @see org.olat.core.commons.scheduler.JobWithDB#executeWithDB(org.quartz.JobExecutionContext)
	 */
	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		StatisticUpdateManager statisticManager = (StatisticUpdateManager) CoreSpringFactory.getBean("org.olat.course.statistic.StatisticUpdateManager");
		if (statisticManager==null) {
			log_.error("executeWithDB: UpdateStatisticsJob configured, but no StatisticUpdateManager available");
		} else {
			if (!statisticManager.updateStatistics(false, null)) {
				log_.warn("executeWithDB: UpdateStatisticsJob could not trigger updateStatistics - must be already running");
			}
		}
	}

}
