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
package org.olat.core.commons.services.vfs.manager;

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.commons.services.vfs.VFSRepositoryModule;
import org.olat.core.util.DateUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 10 April 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class VFSDeletePermanentlyJob extends JobWithDB {
	
	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		int trashRetentionDays = CoreSpringFactory.getImpl(VFSRepositoryModule.class).getTrashRetentionDays();
		if (trashRetentionDays >= 0) {
			VFSRepositoryServiceImpl repositoryService = CoreSpringFactory.getImpl(VFSRepositoryServiceImpl.class);
			Date deletionDateBefore = DateUtils.addDays(new Date(), -trashRetentionDays);
			repositoryService.deleteRetentionExceededPermanently(deletionDateBefore);
		}
	}

}
