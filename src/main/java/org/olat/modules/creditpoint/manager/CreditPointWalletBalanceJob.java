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
package org.olat.modules.creditpoint.manager;

import java.util.Date;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.modules.creditpoint.CreditPointModule;
import org.olat.modules.creditpoint.CreditPointService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 21 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class CreditPointWalletBalanceJob extends JobWithDB {

	private static final Logger logger = Tracing.createLoggerFor(CreditPointWalletBalanceJob.class);
	
	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		try {
			if(CoreSpringFactory.getImpl(CreditPointModule.class).isEnabled()) {
				Date now = new Date();
				CoreSpringFactory.getImpl(CreditPointService.class).calculateBalance(now);
			}
		} catch (Exception e) {
			logger.error("", e);
		}
	}
}
