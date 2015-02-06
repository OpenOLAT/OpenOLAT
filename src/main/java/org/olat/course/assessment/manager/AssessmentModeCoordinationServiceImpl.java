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
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.model.AssessmentModeImpl;
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
	private AssessmentModule assessmentModule;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private AssessmentModeManagerImpl assessmentModeManager;
	
	protected void beat() {
		if(assessmentModule.isAssessmentModeEnabled()) {
			Date now = now();
			List<AssessmentMode> currentModes = assessmentModeManager.getAssessmentModes(now);
			for(AssessmentMode currentMode:currentModes) {
				sendEvent(currentMode, now, false);
			}
		}
	}
	
	private Date now() {
		Calendar cal = Calendar.getInstance();
		//round to minute
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		return cal.getTime();
	}
	
	protected AssessmentMode syncManuallySetStatus(AssessmentMode mode, boolean forceStatus) {
		return sendEvent(mode, now(), forceStatus);
	}
	
	protected AssessmentMode syncAutomicallySetStatus(AssessmentMode mode) {
		return sendEvent(mode, now(), true);
	}
	
	public Status evaluateStatus(Date begin, int leadtime, Date end, int followup) {
		Status status;
		Date now = now();
		
		Date beginWithLeadTime = assessmentModeManager.evaluateLeadTime(begin, leadtime);
		Date endWithFollowupTime = assessmentModeManager.evaluateFollowupTime(end, followup);
		if(beginWithLeadTime.compareTo(now) > 0) {
			status = Status.none;
		} else if(beginWithLeadTime.compareTo(now) <= 0 && begin.compareTo(now) >= 0 && !beginWithLeadTime.equals(begin)) {
			status = Status.leadtime;
		} else if(begin.compareTo(now) <= 0 && end.compareTo(now) >= 0) {
			status = Status.assessment;
		} else if(end.compareTo(now) <= 0 && endWithFollowupTime.compareTo(now) >= 0) {
			if(followup > 0) {
				status = Status.followup;
			} else {
				status = Status.end;
			}
		} else if(endWithFollowupTime.compareTo(now) < 0) {
			status = Status.end;
		} else {
			status = null;
		}

		return status;
	}

	private AssessmentMode sendEvent(AssessmentMode mode, Date now, boolean forceStatus) {
		if(mode.getBeginWithLeadTime().compareTo(now) > 0) {
			mode = ensureStatusOfMode(mode, Status.none);
			sendEvent(AssessmentModeNotificationEvent.BEFORE, mode,
					assessmentModeManager.getAssessedIdentityKeys(mode));
		} else if(mode.getBeginWithLeadTime().compareTo(now) <= 0 && mode.getBegin().compareTo(now) >= 0
				&& mode.getBeginWithLeadTime().compareTo(mode.getBegin()) != 0) {
			mode = ensureStatusOfMode(mode, Status.leadtime);
			sendEvent(AssessmentModeNotificationEvent.LEADTIME, mode,
					assessmentModeManager.getAssessedIdentityKeys(mode));
		} else if(mode.isManualBeginEnd() && !forceStatus) {
			//what to do in manual mode
			if(mode.getStatus() == Status.followup) {
				if(mode.getEndWithFollowupTime().compareTo(now) < 0) {
					mode = ensureStatusOfMode(mode, Status.end);
					sendEvent(AssessmentModeNotificationEvent.END, mode,
							assessmentModeManager.getAssessedIdentityKeys(mode));
				}
			}
		} else {
			if(mode.getBegin().compareTo(now) <= 0 && mode.getEnd().compareTo(now) >= 0) {
				mode = ensureStatusOfMode(mode, Status.assessment);
				sendEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, mode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
				
				//message 5 minutes before end
				Calendar cal = Calendar.getInstance();
				cal.setTime(mode.getEnd());
				cal.set(Calendar.SECOND, 0);
				cal.set(Calendar.MILLISECOND, 0);
				cal.add(Calendar.MINUTE, -6);
				if(now.after(cal.getTime())) {
					sendEvent(AssessmentModeNotificationEvent.STOP_WARNING, mode, null);
				}
			} else if(mode.getEnd().compareTo(now) <= 0 && mode.getEndWithFollowupTime().compareTo(now) >= 0) {
				if(mode.getFollowupTime() > 0) {
					mode = ensureStatusOfMode(mode, Status.followup);
					sendEvent(AssessmentModeNotificationEvent.STOP_ASSESSMENT, mode,
							assessmentModeManager.getAssessedIdentityKeys(mode));
				} else {
					mode = ensureStatusOfMode(mode, Status.end);
					sendEvent(AssessmentModeNotificationEvent.END, mode,
							assessmentModeManager.getAssessedIdentityKeys(mode));
				}
			} else if(mode.getEndWithFollowupTime().compareTo(now) < 0) {
				mode = ensureStatusOfMode(mode, Status.end);
				sendEvent(AssessmentModeNotificationEvent.END, mode,
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
	
	private void sendEvent(String cmd, AssessmentMode mode, Set<Long> assessedIdentityKeys) {
		TransientAssessmentMode transientMode = new TransientAssessmentMode(mode);
		AssessmentModeNotificationEvent event = new AssessmentModeNotificationEvent(cmd, transientMode, assessedIdentityKeys);
		coordinatorManager.getCoordinator().getEventBus()
			.fireEventToListenersOf(event, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}

	@Override
	public boolean canStart(AssessmentMode assessmentMode) {
		boolean canStart;
		Status status = assessmentMode.getStatus();
		if(status == Status.end || status == Status.assessment) {
			canStart = false;
		} else {
			canStart = true;
		}
		return canStart;
	}

	@Override
	public boolean canStop(AssessmentMode assessmentMode) {
		boolean canStop;
		Status status = assessmentMode.getStatus();
		if(status == Status.leadtime || status == Status.assessment) {
			canStop = true;
		} else {
			canStop = false;
		}
		return canStop;
	}

	@Override
	public AssessmentMode startAssessment(AssessmentMode mode) {
		mode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		mode = ensureStatusOfMode(mode, Status.assessment);
		Set<Long> assessedIdentityKeys = assessmentModeManager.getAssessedIdentityKeys(mode);
		sendEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, mode, assessedIdentityKeys);
		return mode;
	}

	@Override
	public AssessmentMode stopAssessment(AssessmentMode mode) {
		mode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		Set<Long> assessedIdentityKeys = assessmentModeManager.getAssessedIdentityKeys(mode);
		if(mode.getFollowupTime() > 0) {
			Date followupTime = assessmentModeManager.evaluateFollowupTime(now(), mode.getFollowupTime());
			((AssessmentModeImpl)mode).setEndWithFollowupTime(followupTime);
			mode.setStatus(Status.followup);
			mode = dbInstance.getCurrentEntityManager().merge(mode);
			dbInstance.commit();
			sendEvent(AssessmentModeNotificationEvent.STOP_ASSESSMENT, mode, assessedIdentityKeys);
		} else {
			mode = ensureStatusOfMode(mode, Status.end);
			sendEvent(AssessmentModeNotificationEvent.END, mode, assessedIdentityKeys);
		}
		return mode;
	}
}