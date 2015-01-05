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
package org.olat.course.assessment.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeNotificationJob extends JobWithDB {

	@Override
	public void executeWithDB(JobExecutionContext context)
	throws JobExecutionException {
		AssessmentModeManager assessmentModeManager = CoreSpringFactory.getImpl(AssessmentModeManager.class);
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		int second = cal.get(Calendar.SECOND);
		if(second > 30) {
			//round to the next minute
			cal.set(Calendar.SECOND, 0);
			cal.add(Calendar.MINUTE, 1);
		} else {
			cal.set(Calendar.SECOND, 0);
		}
		
		Date now = cal.getTime();
		List<AssessmentMode> currentModes = assessmentModeManager.getAssessmentModes(now);
		for(AssessmentMode currentMode:currentModes) {
			Set<Long> assessedIdentityKeys = assessmentModeManager.getAssessedIdentityKeys(currentMode);
			TransientAssessmentMode transientMode = new TransientAssessmentMode(currentMode);
			if(currentMode.getBeginWithLeadTime().compareTo(now) <= 0 && currentMode.getBegin().compareTo(now) >= 0) {
				sendEvent(AssessmentModeNotificationEvent.PRE_LAUNCH, transientMode, assessedIdentityKeys);
			}

		}
	}
	
	private void sendEvent(String cmd, TransientAssessmentMode mode, Set<Long> assessedIdentityKeys) {
		AssessmentModeNotificationEvent event = new AssessmentModeNotificationEvent(cmd, mode, assessedIdentityKeys);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(event, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
}
