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
package org.olat.modules.grading.manager;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.modules.grading.GradingService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * 
 * Initial date: 31 janv. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class GraderReminderJob extends JobWithDB {

	@Override
	public void executeWithDB(JobExecutionContext arg0) {
		GradingService gradingService = CoreSpringFactory.getImpl(GradingService.class);
		gradingService.sendReminders();
		gradingService.sendGradersAsssignmentsNotification();
		gradingService.graderAbsenceLeavesCheckWorkingDays();
	}
}
