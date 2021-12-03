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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.course.CourseFactory;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.model.AssessmentModeImpl;
import org.olat.course.assessment.model.AssessmentModeStatistics;
import org.olat.course.assessment.model.CoordinatedAssessmentMode;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.manager.AssessmentTestSessionDAO;
import org.olat.modules.dcompensation.manager.DisadvantageCompensationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.repository.model.RepositoryEntryStatusChangedEvent;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 06.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentModeCoordinationServiceImpl implements AssessmentModeCoordinationService, GenericEventListener, InitializingBean {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentModeCoordinationServiceImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private TaskExecutorManager taskExecutor;
	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private UserSessionManager userSessionManager;
	@Autowired
	private AssessmentModeManagerImpl assessmentModeManager;
	@Autowired
	private AssessmentTestSessionDAO assessmentTestSessionDao;
	@Autowired
	private DisadvantageCompensationDAO disadvantageCompensationDao;
	
	private Map<Long,CoordinatedAssessmentMode> coordinatedModes = new ConcurrentHashMap<>();
	private CacheWrapper<Long,AssessmentModeStatistics> coordinatedModeStatistics;
	
	protected synchronized void beat() {
		if(assessmentModule.isAssessmentModeEnabled()) {
			Date now = now();
			List<Long> currentModeKeys = new ArrayList<>();
			List<AssessmentMode> currentModes = assessmentModeManager.getAssessmentModes(now);
			for(AssessmentMode currentMode:currentModes) {
				try {
					sendEvent(currentMode, now, false);
					currentModeKeys.add(currentMode.getKey());
				} catch (Exception e) {
					log.error("", e);
				}
			}
			
			//remove coordinated mode 
			List<Long> coordinatedModeKeys = new ArrayList<>(coordinatedModes.keySet());
			for(Long coordinatedModeKey:coordinatedModeKeys) {
				if(!currentModeKeys.contains(coordinatedModeKey)) {
					CoordinatedAssessmentMode decoordinatedMode = coordinatedModes.remove(coordinatedModeKey);
					if(decoordinatedMode != null) {
						coordinatorManager.getCoordinator().getEventBus()
							.deregisterFor(this, decoordinatedMode.getListenerRes());
					}
				}
			}
			
			if(coordinatedModes.size() > 250) {
				log.error("Seem to be a leak of coordinated modes");
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
	
	private void manageListenersOfCoordinatedMode(AssessmentMode mode) {
		try {
			Status status = mode.getStatus();
			if(status == Status.leadtime || status == Status.assessment || status == Status.followup) {
				//add listeners
				CoordinatedAssessmentMode coordinateMode = coordinatedModes.get(mode.getKey());
				if(coordinateMode == null) {
					coordinateMode = new CoordinatedAssessmentMode(mode);
					coordinatedModes.put(mode.getKey(), coordinateMode);
				}
				coordinatorManager.getCoordinator().getEventBus()
					.registerFor(this, null, coordinateMode.getListenerRes());
				
			} else if(coordinatedModes.containsKey(mode.getKey())) {
				CoordinatedAssessmentMode decoordinateMode = coordinatedModes.remove(mode.getKey());
				if(decoordinateMode != null) {
					coordinatorManager.getCoordinator().getEventBus()
						.deregisterFor(this, decoordinateMode.getListenerRes());
				}
			}
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, null, OresHelper.lookupType(RepositoryEntry.class));
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, null, OresHelper.lookupType(BusinessGroup.class));
		
		coordinatedModeStatistics = coordinatorManager.getCoordinator().getCacher()
				.getCache(AssessmentModeStatistics.class.getSimpleName(), "waiting");
	}

	@Override
	public void event(Event event) {
		if(event instanceof BusinessGroupModifiedEvent) {
			try {
				BusinessGroupModifiedEvent mod = (BusinessGroupModifiedEvent)event;
				if(BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT.equals(mod.getCommand())
						&& mod.getAffectedRepositoryEntryKey() == null) {
					// all assessment modes are checked, need only to catch the main event (not the course specific ones)
					Long identityKey = mod.getAffectedIdentityKey();
					sendEventAfterMembershipChange(identityKey);
				}
			} catch (Exception e) {
				log.error("", e);
			}
		} else if(event instanceof RepositoryEntryStatusChangedEvent) {
			RepositoryEntryStatusChangedEvent changedEvent = (RepositoryEntryStatusChangedEvent)event;
			RepositoryEntry entry = repositoryEntryDao.loadByKey(changedEvent.getRepositoryEntryKey());
			processRepositoryEntryChangedStatus(entry);
		}
	}
	
	@Override
	public void processRepositoryEntryChangedStatus(RepositoryEntry entry) {
		if(entry != null && (entry.getEntryStatus() == RepositoryEntryStatusEnum.closed
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.trash
				|| entry.getEntryStatus() == RepositoryEntryStatusEnum.deleted)) {
			try {
				List<AssessmentMode> modes = assessmentModeManager.getAssessmentModeFor(entry);
				for(AssessmentMode mode:modes) {
					if(mode.getStatus() == Status.assessment || mode.getStatus() == Status.followup) {
						endAssessment(mode);
					}
				}
			} catch (Exception e) {
				log.error("", e);
				dbInstance.rollbackAndCloseSession();
			}
		}
	}
	
	private void sendEventAfterMembershipChange(final Long identityKey) {
		List<AssessmentMode> modes = assessmentModeManager.getAssessmentModeFor(new IdentityRefImpl(identityKey));
		for(AssessmentMode mode:modes) {
			Status status = mode.getStatus();
			if(status == Status.leadtime ) {
				sendEvent(AssessmentModeNotificationEvent.LEADTIME, mode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
			} else if(status == Status.assessment) {
				sendEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, mode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
			} else if(status == Status.followup) {
				sendEvent(AssessmentModeNotificationEvent.END, mode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
			}
		}
	}

	@Override
	public Status evaluateStatus(Date begin, int leadtime, Date end, int followup) {
		Status status;
		Date now = now();
		
		Date beginWithLeadTime = assessmentModeManager.evaluateLeadTime(begin, leadtime);
		Date endWithFollowupTime = assessmentModeManager.evaluateFollowupTime(end, followup);
		if(beginWithLeadTime.compareTo(now) > 0) {
			status = Status.none;
		} else if(beginWithLeadTime.compareTo(now) <= 0 && begin.compareTo(now) > 0 && !beginWithLeadTime.equals(begin)) {
			status = Status.leadtime;
		} else if(begin.compareTo(now) <= 0 && end.compareTo(now) > 0) {
			status = Status.assessment;
		} else if(end.compareTo(now) <= 0 && endWithFollowupTime.compareTo(now) > 0) {
			if(followup > 0) {
				status = Status.followup;
			} else {
				status = Status.end;
			}
		} else if(endWithFollowupTime.compareTo(now) <= 0) {
			status = Status.end;
		} else {
			status = null;
		}

		return status;
	}

	private AssessmentMode sendEvent(AssessmentMode mode, Date now, boolean forceStatus) {
		if(mode.getBeginWithLeadTime().compareTo(now) > 0) {
			//none
			Status status = mode.getStatus();
			if(status != Status.leadtime && status != Status.assessment && status != Status.followup && status != Status.end) {
				mode = ensureStatusOfMode(mode, Status.none);
				sendEvent(AssessmentModeNotificationEvent.BEFORE, mode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
			} else if(mode.getStatus() == Status.followup && mode.isManualBeginEnd() && !forceStatus) {
				// close manual assessment mode in the follow-up time but started before the begin date
				if(mode.getEndWithFollowupTime().compareTo(now) < 0) {
					mode = ensureStatusOfMode(mode, Status.end);
					sendEvent(AssessmentModeNotificationEvent.END, mode,
							assessmentModeManager.getAssessedIdentityKeys(mode));
				}
			}
		} else if(mode.getBeginWithLeadTime().compareTo(now) <= 0 && mode.getBegin().compareTo(now) > 0
				&& mode.getBeginWithLeadTime().compareTo(mode.getBegin()) != 0) {
			//leading time
			Status status = mode.getStatus();
			if(status != Status.assessment && status != Status.followup && status != Status.end) {
				mode = ensureStatusOfMode(mode, Status.leadtime);
				sendEvent(AssessmentModeNotificationEvent.LEADTIME, mode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
			}
		} else if(mode.isManualBeginEnd() && !forceStatus) {
			sendEventForManualMode(mode, now);
		} else {
			if(mode.getBegin().compareTo(now) <= 0 && mode.getEnd().compareTo(now) > 0) {
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
			} else if(mode.getEnd().compareTo(now) <= 0 && mode.getEndWithFollowupTime().compareTo(now) > 0) {
				if(mode.getFollowupTime() > 0) {
					mode = ensureStatusOfMode(mode, Status.followup);
					sendEvent(AssessmentModeNotificationEvent.STOP_ASSESSMENT, mode,
							assessmentModeManager.getAssessedIdentityKeys(mode));
				} else {
					mode = ensureBothStatusOfMode(mode, Status.end, EndStatus.all);
					sendEvent(AssessmentModeNotificationEvent.END, mode,
							assessmentModeManager.getAssessedIdentityKeys(mode));
				}
			} else if(mode.getEndWithFollowupTime().compareTo(now) <= 0) {
				mode = ensureBothStatusOfMode(mode, Status.end, EndStatus.all);
				sendEvent(AssessmentModeNotificationEvent.END, mode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
			}
		}
		manageListenersOfCoordinatedMode(mode);
		return mode;
	}
	
	private void sendEventForManualMode(AssessmentMode mode, Date now) {
		//what to do in manual mode
		if(mode.getStatus() == Status.assessment) {
			sendEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, mode,
					assessmentModeManager.getAssessedIdentityKeys(mode));
		} else if(mode.getStatus() == Status.followup) {
			// remind user with compensation for disadvantage
			if(mode.getEndStatus() == EndStatus.withoutDisadvantage) {
				Set<Long> keys = getAssessedIdentitiesWithDisadvantageCompensations(mode);
				sendEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, mode, keys);
			}
			
			// follow-up is ended
			if(mode.getEndWithFollowupTime().compareTo(now) < 0) {
				Set<Long> keys = getAssessedIdentitiesWithDisadvantageCompensations(mode);
				if(keys.isEmpty() || mode.getEndStatus() == EndStatus.all) {
					mode = ensureBothStatusOfMode(mode, Status.end, EndStatus.all);
					sendEvent(AssessmentModeNotificationEvent.END, mode,
						assessmentModeManager.getAssessedIdentityKeys(mode));
				} else {
					mode = ensureBothStatusOfMode(mode, Status.end, EndStatus.withoutDisadvantage);
					Set<Long> identitiesToNotify = assessmentModeManager.getAssessedIdentityKeys(mode);
					identitiesToNotify.removeAll(keys);
					sendEvent(AssessmentModeNotificationEvent.END, mode, identitiesToNotify);
				}
			}
		} else if(mode.getStatus() == Status.end && mode.getEndStatus() == EndStatus.withoutDisadvantage) {
			Set<Long> keys = getAssessedIdentitiesWithDisadvantageCompensations(mode);
			sendEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, mode, keys);
		}
	}
	
	private AssessmentMode ensureStatusOfMode(AssessmentMode mode, Status status) {
		Status currentStatus = mode.getStatus();
		if(currentStatus == null || currentStatus != status) {
			mode.setStatus(status);
			if(status == Status.assessment && mode.isManualBeginEnd()) {
				syncBeginEndDate(mode);
			}
			mode = dbInstance.getCurrentEntityManager().merge(mode);
			if(status == Status.leadtime || status == Status.assessment) {
				warmUpAssessment(mode);
			}
			dbInstance.commit();
		}
		return mode;
	}
	
	private AssessmentMode ensureBothStatusOfMode(AssessmentMode mode, Status status, EndStatus endStatus) {
		Status currentStatus = mode.getStatus();
		EndStatus currentEndStatus = mode.getEndStatus();
		if(currentStatus == null || currentStatus != status || currentEndStatus != endStatus) {
			mode.setStatus(status);
			mode.setEndStatus(endStatus);
			if(status == Status.assessment && mode.isManualBeginEnd()) {
				syncBeginEndDate(mode);
			}
			mode = dbInstance.getCurrentEntityManager().merge(mode);
			if(status == Status.leadtime || status == Status.assessment) {
				warmUpAssessment(mode);
			}
			dbInstance.commit();
		}
		return mode;
	}
	
	private void syncBeginEndDate(AssessmentMode mode) {
		Date programmedBegin = mode.getBegin();
		
		Date now = new Date();
		((AssessmentModeImpl)mode).setBegin(now);
		Date beginWithLeadTime = assessmentModeManager.evaluateLeadTime(now, mode.getLeadTime());
		((AssessmentModeImpl)mode).setBeginWithLeadTime(beginWithLeadTime);
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(programmedBegin);
		long programmedBeginInMillis = cal.getTimeInMillis();
		cal.setTime(now);
		long nowInMillis = cal.getTimeInMillis();
		
		Date end = mode.getEnd();
		cal.setTime(end);
		cal.add(Calendar.MILLISECOND, (int)(nowInMillis - programmedBeginInMillis));
		
		Date movedEnd = cal.getTime();
		((AssessmentModeImpl)mode).setEnd(movedEnd);
		Date endWithFollowupTime = assessmentModeManager.evaluateFollowupTime(movedEnd, mode.getFollowupTime());
		((AssessmentModeImpl)mode).setEndWithFollowupTime(endWithFollowupTime);	
	}
	
	private void sendEvent(String cmd, AssessmentMode mode, Set<Long> assessedIdentityKeys) {
		TransientAssessmentMode transientMode = new TransientAssessmentMode(mode);
		AssessmentModeNotificationEvent event = new AssessmentModeNotificationEvent(cmd, transientMode, assessedIdentityKeys);
		coordinatorManager.getCoordinator().getEventBus()
			.fireEventToListenersOf(event, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
		
		// update the status
		AssessmentModeStatistics statistics = getStatistics(mode.getKey(), assessedIdentityKeys);
		statistics.setStatus(mode.getStatus());
	}

	@Override
	public boolean canStart(AssessmentMode assessmentMode) {
		boolean canStart;
		Status status = assessmentMode.getStatus();
		if(status == Status.assessment || status == Status.followup || status == Status.end) {
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
		EndStatus endStatus = assessmentMode.getEndStatus();
		if(status == Status.leadtime || status == Status.assessment
				|| (endStatus == EndStatus.withoutDisadvantage && (status == Status.followup || status == Status.end))) {
			canStop = true;
		} else {
			canStop = false;
		}
		return canStop;
	}

	@Override
	public boolean isDisadvantageCompensationExtensionTime(AssessmentMode assessmentMode) {
		Status status = assessmentMode.getStatus();
		EndStatus endStatus = assessmentMode.getEndStatus();
		return endStatus == EndStatus.withoutDisadvantage && (status == Status.followup || status == Status.end);
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
	public AssessmentMode stopAssessment(AssessmentMode mode, boolean pullTestSessions, boolean withDisadvantaged, Identity doer) {
		mode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		
		EndStatus endStatus;
		Set<Long> assessedIdentityKeys;
		if(isDisadvantageCompensationExtensionTime(mode)) {
			endStatus = withDisadvantaged ?  EndStatus.all : EndStatus.withoutDisadvantage;
			assessedIdentityKeys = getAssessedIdentitiesWithDisadvantageCompensations(mode);
		} else {
			assessedIdentityKeys = assessmentModeManager.getAssessedIdentityKeys(mode);
		
			boolean partial = false;
			if(!withDisadvantaged) {
				List<IdentityRef> disadvantagedIdentities = disadvantageCompensationDao
						.getActiveDisadvantagedUsers(mode.getRepositoryEntry(), mode.getElementAsList());
				for(IdentityRef disadvantagedIdentity:disadvantagedIdentities) {
					if(assessedIdentityKeys.remove(disadvantagedIdentity.getKey())) {
						partial |= true;
					}
				}
			}
			
			endStatus = partial ? EndStatus.withoutDisadvantage : EndStatus.all;
		}
		
		if(pullTestSessions) {
			pullSessions(mode, assessedIdentityKeys, doer);
		}
		
		return stopAssessment(mode, assessedIdentityKeys, endStatus);
	}
	
	private void pullSessions(AssessmentMode mode, Set<Long> assessedIdentityKeys, Identity doer) {
		List<IdentityRef> identityRefs = assessedIdentityKeys.stream()
				.map(IdentityRefImpl::new)
				.collect(Collectors.toList());
		List<AssessmentTestSession> testSessions = assessmentTestSessionDao
				.getRunningTestSessions(mode.getRepositoryEntry(), mode.getElementAsList(), identityRefs);
		if(!testSessions.isEmpty()) {
			List<Long> testSessionKeys = testSessions.stream()
					.map(AssessmentTestSession::getKey)
					.collect(Collectors.toList());
			
			Long doerKey = doer == null ? null : doer.getKey();
			PullTestSessionsTask task = new PullTestSessionsTask(mode.getRepositoryEntry().getKey(), testSessionKeys, doerKey);
			taskExecutor.schedule(task, 30000);
		}
	}
	
	private AssessmentMode stopAssessment(AssessmentMode mode, Set<Long> assessedIdentityKeys, EndStatus endStatus) {
		String cmd;
		if(mode.getFollowupTime() > 0) {
			if(mode.getStatus() != Status.followup || mode.getStatus() != Status.end) {
				Date now = new Date();
				Date followupTime = assessmentModeManager.evaluateFollowupTime(now, mode.getFollowupTime());
				((AssessmentModeImpl)mode).setEnd(now);
				((AssessmentModeImpl)mode).setEndWithFollowupTime(followupTime);
				mode.setStatus(Status.followup);
			}
			mode.setEndStatus(endStatus);
			mode = dbInstance.getCurrentEntityManager().merge(mode);
			cmd = AssessmentModeNotificationEvent.STOP_ASSESSMENT;
		} else {
			mode = ensureBothStatusOfMode(mode, Status.end, endStatus);
			cmd = AssessmentModeNotificationEvent.END;
		}
		dbInstance.commit();
		sendEvent(cmd, mode, assessedIdentityKeys);
		return mode;
	}
	
	/**
	 * This force the end of an assessment for all users.
	 * 
	 * @param mode The assessment to end
	 * @return The merged assessment mode
	 */
	private AssessmentMode endAssessment(AssessmentMode mode) {
		mode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		Set<Long> assessedIdentityKeys = assessmentModeManager.getAssessedIdentityKeys(mode);
		mode = ensureBothStatusOfMode(mode, Status.end, EndStatus.all);
		Date now = new Date();
		((AssessmentModeImpl)mode).setEnd(now);
		((AssessmentModeImpl)mode).setEndWithFollowupTime(now);
		mode = dbInstance.getCurrentEntityManager().merge(mode);
		dbInstance.commit();
		sendEvent(AssessmentModeNotificationEvent.END, mode, assessedIdentityKeys);
		return mode;
	}
	
	private void warmUpAssessment(AssessmentMode mode) {
		RepositoryEntry entry = repositoryEntryDao.loadByKey(mode.getRepositoryEntry().getKey());
		CourseFactory.loadCourse(entry);
	}
	
	private Set<Long> getAssessedIdentitiesWithDisadvantageCompensations(AssessmentMode assessmentMode) {
		List<IdentityRef> identities = disadvantageCompensationDao
				.getActiveDisadvantagedUsers(assessmentMode.getRepositoryEntry(), assessmentMode.getElementAsList());
		return identities.stream()
				.map(IdentityRef::getKey)
				.collect(Collectors.toSet());
	}

	@Override
	public AssessmentModeStatistics getStatistics(AssessmentMode assessmentMode) {
		AssessmentModeStatistics statistics = getStatistics(assessmentMode.getKey(), null);
		if(assessmentMode.getStatus() == AssessmentMode.Status.none) {
			Set<Long> notRegisteredIdentityKeys = statistics.getNotRegisteredAssessedIdentitiesKeys();
			for(Long notRegisteredIdentityKey:notRegisteredIdentityKeys) {
				if(userSessionManager.isOnline(notRegisteredIdentityKey)) {
					statistics.addLoggedInIdentitiesKeys(new IdentityRefImpl(notRegisteredIdentityKey));
				}
			}
		}
		return statistics;
	}
	
	private AssessmentModeStatistics getStatistics(Long assessmentModeKey, Set<Long> assessedIdentitiesKeys) {
		AssessmentModeStatistics coordinatedAssessmentMode = coordinatedModeStatistics
				.computeIfAbsent(assessmentModeKey, key -> new AssessmentModeStatistics());
		
		if(assessedIdentitiesKeys != null) {
			coordinatedAssessmentMode.updatePlannedAssessedIdentitiesKeys(assessedIdentitiesKeys);
		} else if(coordinatedAssessmentMode.getNumPlanned() == 0) {
			AssessmentMode mode = assessmentModeManager.getAssessmentModeById(assessmentModeKey);
			if(mode != null) {
				assessedIdentitiesKeys = assessmentModeManager.getAssessedIdentityKeys(mode);
				coordinatedAssessmentMode.updatePlannedAssessedIdentitiesKeys(assessedIdentitiesKeys);
			}
		}
		return coordinatedAssessmentMode;
	}

	@Override
	public void waitFor(IdentityRef identity, TransientAssessmentMode assessmentMode) {
		AssessmentModeStatistics coordinatedAssessmentMode = getStatistics(assessmentMode.getModeKey(), null);
		coordinatedAssessmentMode.addWaitingAssessedIdentity(identity);
	}
	
	@Override
	public void start(IdentityRef identity, TransientAssessmentMode assessmentMode) {
		AssessmentModeStatistics coordinatedAssessmentMode = getStatistics(assessmentMode.getModeKey(), null);
		coordinatedAssessmentMode.addStartedAssessedIdentity(identity);
	}
}