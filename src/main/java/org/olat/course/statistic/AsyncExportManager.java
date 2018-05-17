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
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
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

	private static final OLog log = Tracing.createLoggerFor(AsyncExportManager.class);

	private static final int concurrentExportsPerNode = 2;

	@Autowired
	private ExportManager exportManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private TaskExecutorManager taskExecutorManager;

	/** the identities currently executing an export **/
	private final Set<Identity> identitiesOfJobsCurrentlyRunning = new HashSet<>();

	/** DEBUG ONLY: how many runnables are queued up - are we overloading the ThreadPoolTaskExecutor ? **/
	private int waitingCnt = 0;

	
	public void asyncArchiveCourseLogFiles(Identity identity, Runnable callback, Long oresID, String exportDir,
			Date begin, Date end, boolean adminLog, boolean userLog, boolean statisticLog, Locale locale, String email){
		// argument checks
		if (identity==null) {
			throw new IllegalArgumentException("identity must not be null");
		}
		if (callback==null) {
			throw new IllegalArgumentException("callback must not be null");
		}
		
		// DEBUG ONLY
		log.info("asyncArchiveCourseLogFiles: user " + identity.getKey() + " wants to archive a course log. Already pending jobs: " + waitingCnt);

		Roles roles = securityManager.getRoles(identity);
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		
		CourseLogRunnable run = new CourseLogRunnable(identity, callback, oresID, exportDir, begin, end, adminLog, userLog, statisticLog,
				locale, email, isAdministrativeUser, this, exportManager);
		taskExecutorManager.execute(run);
	}
	
	public static class CourseLogRunnable implements Runnable {
		
		private final Identity identity;
		private final Runnable callback;
		private final Long oresID;
		private final String exportDir;
		private final Date begin;
		private final Date end;
		private final boolean adminLog;
		private final boolean userLog;
		private final boolean statisticLog;
		private final Locale locale;
		private final String email;
		private final boolean isAdministrativeUser;
		private final ExportManager exportManager;
		private final AsyncExportManager asyncExportManager;
		
		public CourseLogRunnable(Identity identity, Runnable callback, Long oresID, String exportDir, Date begin, Date end,
				boolean adminLog, boolean userLog, boolean statisticLog, Locale locale, String email, boolean isAdministrativeUser,
				AsyncExportManager asyncExportManager, ExportManager exportManager) {
			this.identity = identity;
			this.callback = callback;
			this.oresID = oresID;
			this.exportDir = exportDir;
			this.begin = begin;
			this.end = end;
			this.adminLog = adminLog;
			this.userLog = userLog;
			this.statisticLog = statisticLog;
			this.locale = locale;
			this.email = email;
			this.isAdministrativeUser = isAdministrativeUser;
			this.asyncExportManager = asyncExportManager;
			this.exportManager = exportManager;
		}

		@Override
		public void run() {
			try{
				log.info("asyncArchiveCourseLogFiles: user " + identity.getKey() + " aquires lock for archiving course log");
				asyncExportManager.waitForSlot(identity);
				log.info("asyncArchiveCourseLogFiles: user " + identity.getKey() + " starts archiving...");
				exportManager.archiveCourseLogFiles(oresID, exportDir, begin, end, adminLog, userLog, statisticLog, locale, email, isAdministrativeUser);
				log.info("asyncArchiveCourseLogFiles: user " + identity.getKey() + " finished archiving...");
			} finally {
				asyncExportManager.returnSlot(identity);
				log.info("asyncArchiveCourseLogFiles: user " + identity.getKey() + " releases lock for archiving course log");
				callback.run();
			}
		}
	}
	
	public synchronized boolean asyncArchiveCourseLogOngoingFor(Identity identity) {
		return identitiesOfJobsCurrentlyRunning.contains(identity);
	}

	/** internal counter method **/
	private synchronized void waitForSlot(Identity identity) {
		waitingCnt++;
		while(identitiesOfJobsCurrentlyRunning.size()>concurrentExportsPerNode || identitiesOfJobsCurrentlyRunning.contains(identity)) {
			try{
				log.info("waitForSlot: user "+identity.getName()+" wants to archive a course log, but the queue is full. Running count: "+identitiesOfJobsCurrentlyRunning.size()+". Total pending jobs: "+waitingCnt);
				wait();
			} catch(InterruptedException ie) {
				// this empty catch is ok
			}
		}
		waitingCnt--;
		identitiesOfJobsCurrentlyRunning.add(identity);
	}
	
	/** internal counter method **/
	private synchronized void returnSlot(Identity identity) {
		identitiesOfJobsCurrentlyRunning.remove(identity);
		log.info("returnSlot: user "+identity.getName()+" returns a slot. Running count: "+identitiesOfJobsCurrentlyRunning.size()+", Total pending jobs: "+waitingCnt);
		notifyAll();
	}
}
