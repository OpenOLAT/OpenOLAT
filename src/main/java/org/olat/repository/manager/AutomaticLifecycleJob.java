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
package org.olat.repository.manager;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.repository.AutomaticLifecycleService;
import org.olat.repository.RepositoryModule;
import org.olat.repository.model.AutomaticLifecycleInfos;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

/**
 * 
 * Initial date: 1 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class AutomaticLifecycleJob extends JobWithDB implements InterruptableJob, AutomaticLifecycleJobState {
	
	private static final Logger logger = Tracing.createLoggerFor(AutomaticLifecycleJob.class);
	
	public static final String PROCESSED_INFOS = "processed";

	private AtomicBoolean interrupted = new AtomicBoolean(false);
	private AutomaticLifecycleInfos processedInfos = new AutomaticLifecycleInfos();
	
	@Override
	public void interrupt() throws UnableToInterruptJobException {
		interrupted.set(true);
	}

	@Override
	public boolean isInterrupted() {
		return interrupted.get();
	}
	
	@Override
	public void updateClosedCourse(int num, int total) {
		processedInfos.setClosed(num);
		processedInfos.setTotalToClose(total);
	}

	@Override
	public void updateDeletedCourse(int num, int total) {
		processedInfos.setDeleted(num);
		processedInfos.setTotalToDelete(total);
	}

	@Override
	public void updateDefinitivelyDeletedCourse(int num, int total) {
		processedInfos.setDefinitivelyDeleted(num);
		processedInfos.setTotalToDefinitivelyDelete(total);
	}

	@Override
	public void executeWithDB(JobExecutionContext context)
	throws JobExecutionException {
		try {
			logger.info(Tracing.M_AUDIT, "Start repository automatic lifecycle");
			RepositoryModule repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
			AutomaticLifecycleService lifecycleService = CoreSpringFactory.getImpl(AutomaticLifecycleService.class);
			AutomaticLifecycleInfos infos = lifecycleService.getLifecycleInfos(new Date());
			processedInfos = new AutomaticLifecycleInfos(0, infos.getTotalToClose(), 0, infos.getTotalToDelete(), 0, infos.getTotalToDefinitivelyDelete());
			context.put(PROCESSED_INFOS, processedInfos);
			
			if(!isInterrupted() && repositoryModule.isLifecycleAutoCloseEnabled()) {
				lifecycleService.close(this);
			}
			if(!isInterrupted() && repositoryModule.isLifecycleAutoDeleteEnabled()) {
				lifecycleService.delete(this);
			}
			if(!isInterrupted() && repositoryModule.isLifecycleAutoDefinitivelyDeleteEnabled()) {
				lifecycleService.definitivelyDelete(this);
			}
			logger.info(Tracing.M_AUDIT, "End repository automatic lifecycle");
		} catch (Exception e) {
			logger.error("", e);
		}
	}
}
