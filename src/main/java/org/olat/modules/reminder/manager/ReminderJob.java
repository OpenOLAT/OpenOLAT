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
package org.olat.modules.reminder.manager;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class ReminderJob extends JobWithDB implements InterruptableJob {
	
	private static final Logger log = Tracing.createLoggerFor(ReminderJob.class);
	
	private boolean cancel = false;

	@Override
	public void executeWithDB(JobExecutionContext context)
	throws JobExecutionException {
		ReminderModule reminderModule = CoreSpringFactory.getImpl(ReminderModule.class);
		if(reminderModule.isEnabled()) {
			ReminderService reminderService = CoreSpringFactory.getImpl(ReminderService.class);
			
			log.info("Start sending reminders");
			Date now = new Date();
			List<Reminder> reminders = reminderService.getReminders(now);
			for(Reminder reminder:reminders) {
				if(cancel) {
					log.info("Reminders sending cancelled");
					break;
				}
				reminderService.sendReminder(reminder);
			}
			log.info("Reminders sent");
		}
	}

	@Override
	public void interrupt() {
		log.info("Reminders interrupted");
		cancel = true;
	}
}