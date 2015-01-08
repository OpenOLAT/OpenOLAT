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

import org.olat.core.commons.persistence.DB;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentModeCoordinationServiceImpl implements AssessmentModeCoordinationService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	
	protected void beat() {
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
			sendEvent(currentMode, now);
		}
	}
	
	protected AssessmentMode syncManuallySetStatus(AssessmentMode mode) {
		return sendEvent(mode, new Date());
	}
	
	protected AssessmentMode syncAutomicallySetStatus(AssessmentMode mode) {
		return sendEvent(mode, new Date());
	}

	private AssessmentMode sendEvent(AssessmentMode mode, Date now) {
		TransientAssessmentMode transientMode = new TransientAssessmentMode(mode);
		if(mode.getBeginWithLeadTime().compareTo(now) <= 0 && mode.getBegin().compareTo(now) >= 0) {
			mode = ensureStatusOfMode(mode, Status.leadtime);
			sendEvent(AssessmentModeNotificationEvent.LEADTIME, transientMode,
					assessmentModeManager.getAssessedIdentityKeys(mode));
		} else if(!mode.isManualBeginEnd()) {
			if(mode.getBegin().compareTo(now) <= 0 && mode.getEnd().compareTo(now) >= 0) {
				mode = ensureStatusOfMode(mode, Status.assessment);
				sendEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, transientMode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
				
				//message 5 minutes before end
				Calendar cal = Calendar.getInstance();
				cal.setTime(mode.getEnd());
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.MINUTE, -60);
				if(now.after(cal.getTime())) {
					sendEvent(AssessmentModeNotificationEvent.STOP_WARNING, transientMode, null);
				}
			} else if(mode.getEnd().compareTo(now) <= 0 && mode.getEndWithFollowupTime().compareTo(now) >= 0) {
				if(mode.getLeadTime() > 0) {
					mode = ensureStatusOfMode(mode, Status.followup);
					sendEvent(AssessmentModeNotificationEvent.STOP_ASSESSMENT, transientMode,
							assessmentModeManager.getAssessedIdentityKeys(mode));
				} else {
					mode = ensureStatusOfMode(mode, Status.end);
					sendEvent(AssessmentModeNotificationEvent.END, transientMode,
							assessmentModeManager.getAssessedIdentityKeys(mode));
				}
			} else if(mode.getEndWithFollowupTime().compareTo(now) < 0) {
				mode = ensureStatusOfMode(mode, Status.end);
				sendEvent(AssessmentModeNotificationEvent.END, transientMode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
			}
		}
		return mode;
	}
	
	private AssessmentMode ensureStatusOfMode(AssessmentMode mode, Status status) {
		Status currentStatus = mode.getStatus();
		if(currentStatus == null || currentStatus != status) {
			mode.setStatus(status);
			mode = dbInstance.getCurrentEntityManager().merge(mode);
			dbInstance.commit();
		}
		return mode;
	}
	
	private void sendEvent(String cmd, TransientAssessmentMode mode, Set<Long> assessedIdentityKeys) {
		AssessmentModeNotificationEvent event = new AssessmentModeNotificationEvent(cmd, mode, assessedIdentityKeys);
		coordinatorManager.getCoordinator().getEventBus()
			.fireEventToListenersOf(event, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}

	@Override
	public AssessmentMode startAssessment(AssessmentMode mode) {
		mode = ensureStatusOfMode(mode, Status.assessment);
		TransientAssessmentMode transientMode = new TransientAssessmentMode(mode);
		Set<Long> assessedIdentityKeys = assessmentModeManager.getAssessedIdentityKeys(mode);
		sendEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, transientMode, assessedIdentityKeys);
		return mode;
	}

	@Override
	public AssessmentMode stopAssessment(AssessmentMode mode) {
		TransientAssessmentMode transientMode = new TransientAssessmentMode(mode);
		Set<Long> assessedIdentityKeys = assessmentModeManager.getAssessedIdentityKeys(mode);
		if(mode.getLeadTime() > 0) {
			mode = ensureStatusOfMode(mode, Status.leadtime);
			sendEvent(AssessmentModeNotificationEvent.STOP_ASSESSMENT, transientMode, assessedIdentityKeys);
		} else {
			mode = ensureStatusOfMode(mode, Status.end);
			sendEvent(AssessmentModeNotificationEvent.END, transientMode, assessedIdentityKeys);
		}
		return mode;
	}
}
