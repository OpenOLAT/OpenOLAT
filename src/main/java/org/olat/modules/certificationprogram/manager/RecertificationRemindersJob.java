/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.manager;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 19 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RecertificationRemindersJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(RecertificationRemindersJob.class);

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		CertificationCoordinator certificationCoordinator = CoreSpringFactory.getImpl(CertificationCoordinator.class);
		DB dbInstance = CoreSpringFactory.getImpl(DB.class);
		
		log.info(Tracing.M_AUDIT, "Send reminders for certification programs");
		
		Date now = new Date();
		// Send notifications
		certificationCoordinator.sendExpiredNotifications(now);
		dbInstance.commitAndCloseSession();
		certificationCoordinator.sendRemovedNotifications(now);
		dbInstance.commitAndCloseSession();
		certificationCoordinator.sendUpcomingReminders(now);
		dbInstance.commitAndCloseSession();
		certificationCoordinator.sendOverdueReminders(now);
		dbInstance.commitAndCloseSession();
		
		log.info(Tracing.M_AUDIT, "Set activity log for expired certificates and removed memberships for certification programs");
		
		certificationCoordinator.logExpiredMemberships(now);
		dbInstance.commitAndCloseSession();
		certificationCoordinator.logRemovedMemberships(now);
		dbInstance.commitAndCloseSession();
	}
}
