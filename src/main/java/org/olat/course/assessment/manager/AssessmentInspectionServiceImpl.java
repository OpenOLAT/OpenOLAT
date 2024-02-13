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
package org.olat.course.assessment.manager;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.IdentityDAO;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.crypto.PasswordGenerator;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionChangeEvent;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionLog;
import org.olat.course.assessment.AssessmentInspectionLog.Action;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.model.AssessmentEntryInspection;
import org.olat.course.assessment.model.AssessmentInspectionConfigurationWithUsage;
import org.olat.course.assessment.model.AssessmentInspectionImpl;
import org.olat.course.assessment.model.TransientAssessmentInspection;
import org.olat.course.assessment.ui.inspection.AssessmentInspectionOverviewController;
import org.olat.course.assessment.ui.inspection.CreateInspectionContext.InspectionCompensation;
import org.olat.course.assessment.ui.inspection.SearchAssessmentInspectionParameters;
import org.olat.modules.assessment.Role;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 15 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class AssessmentInspectionServiceImpl implements AssessmentInspectionService {

	@Autowired
	private DB dbInstance;
	@Autowired
	private IdentityDAO identityDao;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private AssessmentInspectionDAO inspectionDao;
	@Autowired
	private AssessmentInspectionLogDAO inspectionLogDao;
	@Autowired
	private AssessmentInspectionConfigurationDAO inspectionConfigurationDao;
	
	protected void checkInspectionsToStart() {
		List<AssessmentInspection> inspectionsToStart = inspectionDao.searchInspectionsToStart(new Date());
		for(AssessmentInspection inspectionToStart:inspectionsToStart) {
			notifyStartInspection(inspectionToStart);
		}
	}
	
	private void notifyStartInspection(AssessmentInspection inspection) {
		Identity assessedIdentity = inspection.getIdentity();
		TransientAssessmentInspection transientMode = TransientAssessmentInspection.valueOf(inspection);
		AssessmentModeNotificationEvent event = new AssessmentModeNotificationEvent(AssessmentModeNotificationEvent.START_ASSESSMENT, transientMode,
				Set.of(assessedIdentity.getKey()), Map.of());
		coordinatorManager.getCoordinator().getEventBus()
			.fireEventToListenersOf(event, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
	
	protected void checkNoShowInspections() {
		List<AssessmentInspection> noShowInspections = inspectionDao.searchNoShowInspections(new Date());
		for(AssessmentInspection noShowInspection:noShowInspections) {
			noShowInspection.setInspectionStatus(AssessmentInspectionStatusEnum.noShow);
			
			// Add a comment if the participant han't done any test
			String comment = null;
			Identity assessedIdentity = noShowInspection.getIdentity();
			RepositoryEntry entry = noShowInspection.getConfiguration().getRepositoryEntry();
			boolean hasTestSession = inspectionDao.hasAssessmentTestSession(assessedIdentity, entry, noShowInspection.getSubIdent());
			if(!hasTestSession) {
				Locale defaultLocale = i18nManager.getLocaleOrDefault(assessedIdentity.getUser().getPreferences().getLanguage());
				Translator translator = Util.createPackageTranslator(AssessmentInspectionOverviewController.class, defaultLocale);
				comment = translator.translate("comment.no.test.sessions");
				noShowInspection.setComment(comment);
			}
			inspectionLogDao.createLog(Action.noShow, null, comment, noShowInspection, null);
			inspectionDao.updateInspection(noShowInspection);
			dbInstance.commit();
		}
		dbInstance.commitAndCloseSession();
	}
	
	protected void checkInProgressInspections() {
		List<AssessmentInspection> inspectionsToClose = inspectionDao.searchInProgressInspectionsToClose(new Date());
		for(AssessmentInspection inspectionToClose:inspectionsToClose) {
			updateStatusAndEndInspection(inspectionToClose, AssessmentInspectionStatusEnum.carriedOut, null, Action.finishAuto, null);
		}
		dbInstance.commitAndCloseSession();
	}
	
	@Override
	public AssessmentInspectionConfiguration createInspectionConfiguration(RepositoryEntry entry) {
		return inspectionConfigurationDao.createInspectionConfiguration(entry);
	}

	@Override
	public AssessmentInspectionConfiguration saveConfiguration(AssessmentInspectionConfiguration configuration) {
		return inspectionConfigurationDao.saveConfiguration(configuration);
	}
	
	@Override
	public AssessmentInspectionConfiguration duplicateConfiguration(AssessmentInspectionConfiguration configuration, String newName) {
		configuration = getConfigurationById(configuration.getKey());
		if(configuration == null) {
			return null;
		}
		return inspectionConfigurationDao.duplicateInspectionConfiguration(configuration, newName);
	}

	@Override
	public AssessmentInspectionConfiguration getConfigurationById(Long key) {
		return inspectionConfigurationDao.getConfigurationById(key);
	}

	@Override
	public List<AssessmentInspectionConfiguration> getInspectionConfigurations(RepositoryEntryRef entry) {
		return inspectionConfigurationDao.loadConfigurationsByEntry(entry);
	}
	
	@Override
	public boolean hasInspectionConfigurations(RepositoryEntryRef entry) {
		return inspectionConfigurationDao.hasConfigurationByRepositoryEntry(entry);
	}

	@Override
	public boolean isInspectionConfigurationNameInUse(RepositoryEntryRef entry, String newName,
			AssessmentInspectionConfiguration configuration) {
		return inspectionConfigurationDao.hasInspectionConfigurationNameInUse(entry, newName, configuration);
	}

	@Override
	public List<AssessmentInspectionConfigurationWithUsage> getInspectionConfigurationsWithUsage(RepositoryEntryRef entry) {
		return inspectionConfigurationDao.loadConfigurationsWithUsageByEntry(entry);
	}

	@Override
	public void deleteConfiguration(AssessmentInspectionConfiguration configuration) {
		if(configuration == null || configuration.getKey() == null) return;
		
		configuration = inspectionConfigurationDao.getConfigurationById(configuration.getKey());
		inspectionConfigurationDao.deleteConfiguation(configuration);
	}

	@Override
	public int hasInspection(AssessmentInspectionConfiguration configuration) {
		return inspectionDao.hasInspection(configuration);
	}

	@Override
	public void addInspection(AssessmentInspectionConfiguration configuration, Date start, Date end, List<InspectionCompensation> compensations,
			boolean withAccessCode, String subIdent, List<IdentityRef> identitiesRefs, Identity doer) {
		List<Identity> identities = identityDao.loadByRefs(identitiesRefs);
		for(Identity identity:identities) {
			String accessCode = withAccessCode ? PasswordGenerator.generateNumericalCode(6) : null;
			Integer extraTime = getExtraTime(compensations, identity);
			AssessmentInspection inspection = inspectionDao
					.createInspection(identity, start, end, extraTime, accessCode, subIdent, configuration);
			String after = AssessmentInspectionXStream.toXml(inspection);
			inspectionLogDao.createLog(Action.create, null, after, inspection, doer);
		}
	}
	
	private Integer getExtraTime(List<InspectionCompensation> compensations, Identity identity) {
		if(compensations != null && !compensations.isEmpty()) {
			for(InspectionCompensation compensation:compensations) {
				if(identity.getKey().equals(compensation.identity().getKey())) {
					return compensation.extraTimeInSeconds();
				}
			}
		}
		return null;
	}

	@Override
	public AssessmentInspection updateInspection(AssessmentInspection inspection, AssessmentInspectionConfiguration configuration,
			Date from, Date to, Integer extraTime, boolean accessCode, Identity doer) {
		String before = AssessmentInspectionXStream.toXml(inspection);
		
		inspection.setFromDate(from);
		inspection.setToDate(to);
		inspection.setExtraTime(extraTime);
		if(accessCode && !StringHelper.containsNonWhitespace(inspection.getAccessCode())) {
			inspection.setAccessCode(PasswordGenerator.generateNumericalCode(6));
		} else if(!accessCode) {
			inspection.setAccessCode(null);
		}
		if(configuration != null) {
			((AssessmentInspectionImpl)inspection).setConfiguration(configuration);
		}
		
		inspection = inspectionDao.updateInspection(inspection);
		inspection.getIdentity().getUser();// load
		
		String after = AssessmentInspectionXStream.toXml(inspection);
		inspectionLogDao.createLog(Action.update, before, after, inspection, doer);
		dbInstance.commit();
		
		AssessmentInspectionChangeEvent event = new AssessmentInspectionChangeEvent(AssessmentInspectionChangeEvent.UPDATE,
				inspection, inspection.getIdentity().getKey());
		OLATResourceable inspectionOres = OresHelper
        		.createOLATResourceableInstance(AssessmentInspection.class, inspection.getKey());
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(event, inspectionOres);

		return inspection;
	}

	@Override
	public AssessmentInspection cancelInspection(AssessmentInspection inspection, String comment, Identity doer) {
		return updateStatusAndEndInspection(inspection, AssessmentInspectionStatusEnum.cancelled, comment, Action.cancelled, doer);
	}

	@Override
	public AssessmentInspection withdrawInspection(AssessmentInspection inspection, String comment, Identity doer) {
		return updateStatusAndEndInspection(inspection, AssessmentInspectionStatusEnum.withdrawn, comment, Action.withdrawn, doer);
	}

	@Override
	public AssessmentInspection updateStatus(AssessmentInspection inspection,
			AssessmentInspectionStatusEnum status, Identity doer) {
		if(status == AssessmentInspectionStatusEnum.cancelled) {
			updateStatusAndEndInspection(inspection, status, null, Action.cancelled, doer);
		} else if(status == AssessmentInspectionStatusEnum.withdrawn) {
			updateStatusAndEndInspection(inspection, status, null, Action.withdrawn, doer);
		}
		return updateStatusInternal(inspection, status, null, Action.update, doer);
	}
	
	private AssessmentInspection updateStatusAndEndInspection(AssessmentInspection inspection,
			AssessmentInspectionStatusEnum status, String comment, Action logAction, Identity doer) {
		inspection = updateStatusInternal(inspection, status, comment, logAction, doer);
		
		Identity assessedIdentity = inspection.getIdentity();
		TransientAssessmentInspection transientMode = TransientAssessmentInspection.valueOf(inspection);
		AssessmentModeNotificationEvent event = new AssessmentModeNotificationEvent(AssessmentModeNotificationEvent.END, transientMode,
				Set.of(assessedIdentity.getKey()), Map.of());
		coordinatorManager.getCoordinator().getEventBus()
			.fireEventToListenersOf(event, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
		
		return inspection;
	}
	
	private AssessmentInspection updateStatusInternal(AssessmentInspection inspection,
			AssessmentInspectionStatusEnum status, String comment, Action logAction, Identity doer) {
		String before = AssessmentInspectionXStream.toXml(inspection);
		inspection.setInspectionStatus(status);
		if(status == AssessmentInspectionStatusEnum.cancelled) {
			inspection.setComment(comment);
			calculateEffectiveDuration(inspection, doer);
			calculateEndBy(inspection, doer);
		} else if(status == AssessmentInspectionStatusEnum.withdrawn) {
			inspection.setComment(comment);
		} else if(status == AssessmentInspectionStatusEnum.carriedOut) {
			calculateEffectiveDuration(inspection, doer);
			calculateEndBy(inspection, doer);
		}
		inspection = inspectionDao.updateInspection(inspection);
		inspection.getIdentity().getUser();
		String after = AssessmentInspectionXStream.toXml(inspection);
		inspectionLogDao.createLog(logAction, before, after, inspection, doer);
		dbInstance.commit();
		
		return inspection;
	}
	
	private void calculateEffectiveDuration(AssessmentInspection inspection, Identity doer) {
		if(inspection.getStartTime() == null) return;
		
		Long currentEffectiveDuration = inspection.getEffectiveDuration();
		String currentEffectiveDurationVal = currentEffectiveDuration == null ? null : currentEffectiveDuration.toString();
		
		Date startTime = inspection.getStartTime();
		int alreadyDone = inspection.getEffectiveDuration() == null ? 0 : inspection.getEffectiveDuration().intValue();
		// Calculate in seconds
		long effectiveDuration = ((new Date().getTime() - startTime.getTime()) / 1000) + alreadyDone;
		inspection.setEffectiveDuration(effectiveDuration);
	
		inspectionLogDao.createLog(Action.effectiveDuration, currentEffectiveDurationVal, Long.toString(effectiveDuration), inspection, doer);
	}
	

	private void calculateEndBy(AssessmentInspection inspection, Identity doer) {
		if(doer == null) {
			((AssessmentInspectionImpl)inspection).setEndBy(Role.auto);
		} else if(doer.getKey().equals(inspection.getIdentity().getKey())) {
			((AssessmentInspectionImpl)inspection).setEndBy(Role.user);
		} else {
			((AssessmentInspectionImpl)inspection).setEndBy(Role.coach);
		}
	}

	@Override
	public List<AssessmentEntryInspection> searchInspection(SearchAssessmentInspectionParameters params) {
		return inspectionDao.searchInspection(params);
	}

	@Override
	public List<AssessmentInspection> getInspectionFor(IdentityRef identity, Date date) {
		return inspectionDao.searchInspectionFor(identity, date);
	}

	@Override
	public AssessmentInspection getInspectionFor(IdentityRef identity, Date date, Long inspectKey) {
		return inspectionDao.searchInspectionFor(identity, date, inspectKey);
	}

	@Override
	public AssessmentInspection getInspection(Long inspectKey) {
		return inspectionDao.loadByKey(inspectKey);
	}

	@Override
	public AssessmentInspection startInspection(Identity assessedIdentity, TransientAssessmentInspection transientInspection) {
		AssessmentInspection inspection = inspectionDao.loadByKey(transientInspection.getInspectionKey());
		String before = AssessmentInspectionXStream.toXml(inspection);
		
		Date start = new Date();
		((AssessmentInspectionImpl)inspection).setStartTime(start);
		inspection.setInspectionStatus(AssessmentInspectionStatusEnum.inProgress);
		
		// Calculate end time based on start and configuration duration, extra time and already used time
		AssessmentInspectionConfiguration configuration = inspection.getConfiguration();
		Calendar cal = Calendar.getInstance();
		cal.setTime(start);
		cal.add(Calendar.SECOND, configuration.getDuration());
		if(inspection.getExtraTime() != null) {
			cal.add(Calendar.SECOND, inspection.getExtraTime().intValue());
		}
		if(inspection.getEffectiveDuration() != null) {
			cal.add(Calendar.SECOND, -inspection.getEffectiveDuration().intValue());
		}
		((AssessmentInspectionImpl)inspection).setEndTime(cal.getTime());
		inspection = inspectionDao.updateInspection(inspection);
		inspection.getIdentity().getUser();
		String after = AssessmentInspectionXStream.toXml(inspection);
		inspectionLogDao.createLog(Action.start, before, after, inspection, assessedIdentity);
		
		AssessmentInspection updatedInspection =  inspectionDao.updateInspection(inspection);
		dbInstance.commit();
		return updatedInspection;
	}
	
	@Override
	public AssessmentInspection pauseInspection(Identity assessedIdentity, AssessmentInspection inspection, long duration) {
		int alreadyDone = inspection.getEffectiveDuration() == null ? 0 : inspection.getEffectiveDuration().intValue();
		inspection.setEffectiveDuration(duration + alreadyDone);
		inspection = inspectionDao.updateInspection(inspection);
		dbInstance.commit();
		return inspection;
	}

	@Override
	public AssessmentInspection endInspection(Identity assessedIdentity, AssessmentInspection inspection, long duration, Identity doer) {
		inspection = inspectionDao.loadByKey(inspection.getKey());
		String before = AssessmentInspectionXStream.toXml(inspection);
		inspection.setInspectionStatus(AssessmentInspectionStatusEnum.carriedOut);
		if(assessedIdentity.equals(doer)) {
			((AssessmentInspectionImpl)inspection).setEndBy(Role.user);
		} else if(doer != null) {
			((AssessmentInspectionImpl)inspection).setEndBy(Role.coach);
		} else {
			((AssessmentInspectionImpl)inspection).setEndBy(Role.auto);
		}
		long alreadyDone = inspection.getEffectiveDuration() == null ? 0 : inspection.getEffectiveDuration().intValue();
		long effectiveDuration = duration + alreadyDone;
		inspection.setEffectiveDuration(effectiveDuration);
		inspection = inspectionDao.updateInspection(inspection);
		inspection.getIdentity().getUser();
		
		String after = AssessmentInspectionXStream.toXml(inspection);
		Action action = assessedIdentity.equals(doer) ? Action.finishByParticipant : Action.finishByCoach;
		inspectionLogDao.createLog(Action.effectiveDuration, Long.toString(alreadyDone), Long.toString(effectiveDuration), inspection, doer);
		inspectionLogDao.createLog(action, before, after, inspection, doer);
	
		AssessmentInspection updatedInspection = inspectionDao.updateInspection(inspection);
		dbInstance.commit();
		return updatedInspection;
	}
	
	@Override
	public void log(Action action, String before, String after, TransientAssessmentInspection inspection, Identity doer) {
		AssessmentInspection assessmentInspection = inspectionDao.loadByKey(inspection.getInspectionKey());
		inspectionLogDao.createLog(action, before, after, assessmentInspection, doer);
	}

	@Override
	public List<AssessmentInspectionLog> getLogFor(AssessmentInspection inspection, Date from, Date to) {
		return inspectionLogDao.loadLogs(inspection, from, to);
	}
	
	
}
