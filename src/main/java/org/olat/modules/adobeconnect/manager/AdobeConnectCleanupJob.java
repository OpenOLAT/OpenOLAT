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
package org.olat.modules.adobeconnect.manager;

import java.util.Calendar;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.modules.adobeconnect.AdobeConnectManager;
import org.olat.modules.adobeconnect.AdobeConnectMeeting;
import org.olat.modules.adobeconnect.AdobeConnectModule;
import org.olat.modules.adobeconnect.model.AdobeConnectErrors;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 4 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectCleanupJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(AdobeConnectCleanupJob.class);
	
	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		AdobeConnectModule adobeConnectModule = CoreSpringFactory.getImpl(AdobeConnectModule.class);
		if(adobeConnectModule.isCleanupMeetings() && adobeConnectModule.getDaysToKeep() > 0) {
			cleanUp(adobeConnectModule.getDaysToKeep());
		}
	}
	
	private void cleanUp(long days) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -(int)days);
		CalendarUtils.getStartOfDay(cal);

		AdobeConnectManager adobeConnectManager = CoreSpringFactory.getImpl(AdobeConnectManager.class);
		List<AdobeConnectMeeting> oldMeetings = adobeConnectManager.getMeetingsBefore(cal.getTime());
		for(AdobeConnectMeeting oldMeeting:oldMeetings) {
			try {
				AdobeConnectErrors errors = new AdobeConnectErrors();
				adobeConnectManager.deleteMeeting(oldMeeting, errors);
				if(errors.hasErrors()) {
					log.error("Error trying to delete adobe connect meeting with id {} with message: {}",
							(oldMeeting != null && oldMeeting.getKey() != null ? oldMeeting.getKey() : "NULL"),
							errors.getErrorMessages());
				}
			} catch (Exception e) {
				log.error("", e);
			}
		}
	}

}
