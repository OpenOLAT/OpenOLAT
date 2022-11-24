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
package org.olat.modules.quality.manager;

import java.util.Date;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.modules.quality.QualityModule;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 11.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class QualityJob extends JobWithDB {

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		QualityModule qualityModule = CoreSpringFactory.getImpl(QualityModule.class);
		if (!qualityModule.isEnabled()) return;
		
		Date until = new Date();
		QualityGeneratorService generatorService = CoreSpringFactory.getImpl(QualityGeneratorService.class);
		generatorService.generateDataCollections();
		
		QualityService qualityService = CoreSpringFactory.getImpl(QualityService.class);
		qualityService.startDataCollection(until);
		qualityService.stopDataCollections(until);
		DBFactory.getInstance().commitAndCloseSession();
		qualityService.sendReminders(until);
	}

}
