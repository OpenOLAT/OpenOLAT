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
package org.olat.admin.sysinfo;

import org.olat.core.CoreSpringFactory;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.springframework.scheduling.quartz.QuartzJobBean;

/**
 *
 * Job to update the maintenance message in the GUI of all logged in users. Does
 * not need access to DB, it's all in memory.
 * 
 * Initial date: 18 March. 2022<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */

@DisallowConcurrentExecution
public class MaintenanceMessageJob extends QuartzJobBean {

	@Override
	protected final void executeInternal(JobExecutionContext arg0) {
		CoreSpringFactory.getImpl(InfoMessageManager.class).updateMaintenanceMessageFromJob();
	}
}
