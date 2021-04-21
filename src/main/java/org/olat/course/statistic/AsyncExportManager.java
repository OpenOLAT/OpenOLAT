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

import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Handles asynchronous aspects of log export - including limiting the
 * number of concurrent exports
 * <P>
 * Initial Date:  13.01.2010 <br>
 * @author Stefan
 */
@Service
public class AsyncExportManager {

	private static final Logger log = Tracing.createLoggerFor(AsyncExportManager.class);
	
	public static final OLATResourceable LOG_EVENT_ORES = OresHelper.createOLATResourceableInstance(CourseLogRunnable.class, 0l);

	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private TaskExecutorManager taskExecutorManager;

	/** the identities currently executing an export **/
	private final Set<Identity> identitiesOfJobsCurrentlyRunning = ConcurrentHashMap.newKeySet();
	
	public void asyncArchiveCourseLogFiles(Identity identity, Long oresID, String exportDir,
			Date begin, Date end, boolean adminLog, boolean userLog, boolean statisticLog, Locale locale, String email){
		// argument checks
		if (identity==null) {
			throw new IllegalArgumentException("identity must not be null");
		}
		
		log.info("asyncArchiveCourseLogFiles: user {} wants to archive a course log. Already pending jobs: {}",
				identity.getKey(), identitiesOfJobsCurrentlyRunning.size());

		Roles roles = securityManager.getRoles(identity);
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		
		CourseLogRunnable run = new CourseLogRunnable(identity, oresID, exportDir, begin, end,
				adminLog, userLog, statisticLog, locale, email, isAdministrativeUser);
		taskExecutorManager.execute(run);
	}
	
	public boolean asyncArchiveCourseLogOngoingFor(Identity identity) {
		return identitiesOfJobsCurrentlyRunning.contains(identity);
	}

	/** internal counter method **/
	protected void register(Identity identity) {
		identitiesOfJobsCurrentlyRunning.add(identity);
	}
	
	/** internal counter method **/
	protected void deregister(Identity identity) {
		identitiesOfJobsCurrentlyRunning.remove(identity);
	}
}
