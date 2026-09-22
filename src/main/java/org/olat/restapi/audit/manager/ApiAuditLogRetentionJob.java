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
package org.olat.restapi.audit.manager;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.restapi.RestModule;
import org.olat.restapi.audit.ApiAuditLogService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;

/**
 * Deletes the rows of the API audit log which are older than the retention
 * period of the REST module.
 * 
 * Initial date: 17 sept. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
@DisallowConcurrentExecution
public class ApiAuditLogRetentionJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(ApiAuditLogRetentionJob.class);
	
	@Override
	public void executeWithDB(JobExecutionContext context) {
		try {
			int days = CoreSpringFactory.getImpl(RestModule.class).getAuditLogRetentionDays();
			if(days <= 0) {
				log.debug("Retention of the API audit log is disabled, no row deleted");
				return;
			}
			
			int deleted = CoreSpringFactory.getImpl(ApiAuditLogService.class).deleteOlderThan(days);
			if(deleted > 0) {
				log.info(Tracing.M_AUDIT, "Deleted {} rows of the API audit log older than {} days", 
						Integer.valueOf(deleted), Integer.valueOf(days));
			}
		} catch (Exception e) {
			log.error("Cannot delete the old rows of the API audit log", e);
		}
	}
}
