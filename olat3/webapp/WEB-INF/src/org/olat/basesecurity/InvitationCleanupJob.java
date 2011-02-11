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
 * Copyright (c) 2008 frentix GmbH, Switzerland<br>
 * http://www.frentix.com
 * <p>
 */
package org.olat.basesecurity;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.scheduler.JobWithDB;
import org.quartz.JobExecutionContext;

/**
 * Description:<br>
 * A job to remove invitation without policies.
 * 
 * <P>
 * Initial Date:  11 nov. 2010 <br>
 * @author srosse
 */
public class InvitationCleanupJob extends JobWithDB {

	/**
	 * @see org.olat.core.commons.scheduler.JobWithDB#executeWithDB(org.quartz.JobExecutionContext)
	 */
	public void executeWithDB(JobExecutionContext context) {
		try {
			log.info("Starting invitation clean up job");
			BaseSecurity securityManager = (BaseSecurity)CoreSpringFactory.getBean("baseSecurityManager");
			securityManager.cleanUpInvitations();
		} catch (Exception e) {
			// ups, something went completely wrong! We log this but continue next time
			log.error("Error while cleaning up invitation", e);
		}
		// db closed by JobWithDB class		
	}
}
