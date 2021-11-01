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
package org.olat.group.manager;

import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupModule;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 10 sept. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class BusinessGroupLifecycleJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(BusinessGroupLifecycleJob.class);

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		BusinessGroupModule businessGroupModule = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		if(businessGroupModule.getGroupLifecycleTypeEnumsList().isEmpty()) {
			return;
		}
		
		log.info("Start group lifecycle job");
		
		BusinessGroupLifecycleManager lifecycleManager = CoreSpringFactory.getImpl(BusinessGroupLifecycleManager.class);
		
		Set<BusinessGroup> vetoed = new HashSet<>();// only one action at once
		if(businessGroupModule.isAutomaticGroupInactivationEnabled()) {
			lifecycleManager.inactivateAutomaticallyBusinessGroups(vetoed);
		} else if(businessGroupModule.getNumberOfDayBeforeDeactivationMail() > 0) {
			lifecycleManager.inactivateBusinessGroupsAfterResponseTime(vetoed);
		}
		
		if(businessGroupModule.isAutomaticGroupSoftDeleteEnabled()) {
			lifecycleManager.softDeleteAutomaticallyBusinessGroups(vetoed);
		} else if(businessGroupModule.getNumberOfDayBeforeSoftDeleteMail() > 0) {
			lifecycleManager.softDeleteBusinessGroupsAfterResponseTime(vetoed);
		}

		if(businessGroupModule.isAutomaticGroupDefinitivelyDeleteEnabled()) {
			lifecycleManager.definitivelyDeleteBusinessGroups(vetoed);
		}
		
		log.info("End group lifecycle job");
	}
}
