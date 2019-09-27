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
package org.olat.ldap;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description:<br>
 * This Job Bean synchronizes the LDAP users
 * 
 * <P>
 * Initial Date: 20.08.2008 <br>
 * 
 * @author gnaegi
 */
@DisallowConcurrentExecution
public class LDAPUserSynchronizerJob extends JobWithDB {

	private static final Logger log = Tracing.createLoggerFor(LDAPUserSynchronizerJob.class);

	@Override
	public void executeWithDB(JobExecutionContext arg0)
	throws JobExecutionException {
		try {
			log.info("Starting LDAP user synchronize job");
			LDAPError errors = new LDAPError();
			LDAPLoginManager ldapLoginManager = CoreSpringFactory.getImpl(LDAPLoginManager.class);
			boolean allOk = ldapLoginManager.doBatchSync(errors);
			if(allOk) {
				log.info("LDAP user synchronize job finished successfully");				
			} else {
				log.info("LDAP user synchronize job finished with errors::" + errors.get());				
			}
		} catch (Exception e) {
			// ups, something went completely wrong! We log this but continue next time
			log.error("Erron while synchronizeing LDAP users", e);
		}		
	}
}
