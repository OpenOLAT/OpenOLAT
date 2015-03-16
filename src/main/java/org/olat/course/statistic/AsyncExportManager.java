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

import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.manager.BasicManager;

/**
 * Handles asynchronous aspects of log export - including limiting the
 * number of concurrent exports
 * <P>
 * Initial Date:  13.01.2010 <br>
 * @author Stefan
 */
public class AsyncExportManager extends BasicManager {

	/** the logging object used in this class **/
	private static final OLog log_ = Tracing.createLoggerFor(AsyncExportManager.class);

	/** AsyncExportManager is a singleton, configured by spring **/
	private static AsyncExportManager INSTANCE;

	/** set via spring **/
	private int concurrentExportsPerNode_ = 1;
	
	private TaskExecutorManager taskExecutorManager;

	/** the identities currently executing an export **/
	private final Set<Identity> identitiesOfJobsCurrentlyRunning_ = new HashSet<Identity>();

	/** DEBUG ONLY: how many runnables are queued up - are we overloading the ThreadPoolTaskExecutor ? **/
	private int waitingCnt_ = 0;
	
	/** created via spring **/
	private AsyncExportManager() {
		// this empty constructor is okay
		INSTANCE = this;
	}
	
	/**
	 * [used by Spring]
	 * @param taskExecutorManager
	 */
	public void setTaskExecutorManager(TaskExecutorManager taskExecutorManager) {
		this.taskExecutorManager = taskExecutorManager;
	}

	/**
	 * @return Singleton.
	 */
	public static final AsyncExportManager getInstance() {
		if (INSTANCE==null) {
			throw new IllegalStateException("AsyncExportManager bean not created via spring. Configuration error!");
		}
		return INSTANCE;
	}
	
	/** called via spring, sets the number of concurrent exports within one node, default is 2 **/
	public void setConcurrentExportsPerNode(int concurrentExportsPerNode) {
		this.concurrentExportsPerNode_ = concurrentExportsPerNode;
	}
	
	public void asyncArchiveCourseLogFiles(final Identity identity, final Runnable callback,
			final Long oresID, final String exportDir, final Date begin, final Date end, final boolean adminLog, final boolean userLog, final boolean statisticLog, final String charset, final Locale locale, final String email){
		// argument checks
		if (identity==null) {
			throw new IllegalArgumentException("identity must not be null");
		}
		if (callback==null) {
			throw new IllegalArgumentException("callback must not be null");
		}
		
		// DEBUG ONLY
		synchronized(this) {
			log_.info("asyncArchiveCourseLogFiles: user "+identity.getName()+" wants to archive a course log. Already pending jobs: "+waitingCnt_);
		}
		
		CourseLogRunnable run = new CourseLogRunnable(identity, callback, oresID, exportDir, begin, end, adminLog, userLog, statisticLog, charset, locale, email);
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
		private final String charset;
		private final Locale locale;
		private final String email;
		
		public CourseLogRunnable(Identity identity, Runnable callback, Long oresID, String exportDir, Date begin, Date end,
				boolean adminLog, boolean userLog, boolean statisticLog, String charset, Locale locale, String email) {
			this.identity = identity;
			this.callback = callback;
			this.oresID = oresID;
			this.exportDir = exportDir;
			this.begin = begin;
			this.end = end;
			this.adminLog = adminLog;
			this.userLog = userLog;
			this.statisticLog = statisticLog;
			this.charset = charset;
			this.locale = locale;
			this.email = email;
		}

		@Override
		public void run() {
			try{
				log_.info("asyncArchiveCourseLogFiles: user "+identity.getName()+" aquires lock for archiving course log");
				AsyncExportManager.getInstance().waitForSlot(identity);
				log_.info("asyncArchiveCourseLogFiles: user "+identity.getName()+" starts archiving...");
				ExportManager.getInstance().archiveCourseLogFiles(oresID, exportDir, begin, end, adminLog, userLog, statisticLog, charset, locale, email);
				log_.info("asyncArchiveCourseLogFiles: user "+identity.getName()+" finished archiving...");
			} finally {
				AsyncExportManager.getInstance().returnSlot(identity);
				log_.info("asyncArchiveCourseLogFiles: user "+identity.getName()+" releases lock for archiving course log");
				callback.run();
			}
		}
	}
	
	public synchronized boolean asyncArchiveCourseLogOngoingFor(Identity identity) {
		return identitiesOfJobsCurrentlyRunning_.contains(identity);
	}

	/** internal counter method **/
	private synchronized void waitForSlot(Identity identity) {
		waitingCnt_++;
		while(identitiesOfJobsCurrentlyRunning_.size()>concurrentExportsPerNode_ || identitiesOfJobsCurrentlyRunning_.contains(identity)) {
			try{
				log_.info("waitForSlot: user "+identity.getName()+" wants to archive a course log, but the queue is full. Running count: "+identitiesOfJobsCurrentlyRunning_.size()+". Total pending jobs: "+waitingCnt_);
				wait();
			} catch(InterruptedException ie) {
				// this empty catch is ok
			}
		}
		waitingCnt_--;
		identitiesOfJobsCurrentlyRunning_.add(identity);
	}
	
	/** internal counter method **/
	private synchronized void returnSlot(Identity identity) {
		identitiesOfJobsCurrentlyRunning_.remove(identity);
		log_.info("returnSlot: user "+identity.getName()+" returns a slot. Running count: "+identitiesOfJobsCurrentlyRunning_.size()+", Total pending jobs: "+waitingCnt_);
		notifyAll();
	}
}
