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
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.basesecurity.model.OrganisationMembershipEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Organisation;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.ScoreAccountingTriggerSearchParams;
import org.olat.course.editor.PublishEvent;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.scoring.SingleUserObligationContext;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.group.ui.edit.BusinessGroupRepositoryEntryEvent;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembershipEvent;
import org.olat.modules.curriculum.CurriculumElementRepositoryEntryEvent;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryMembershipModifiedEvent;
import org.olat.user.UserPropertyChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ScoreAccountingProcessor implements GenericEventListener {

	private static final Logger log = Tracing.createLoggerFor(ScoreAccountingProcessor.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BusinessGroupService groupService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	@PostConstruct
	void initProviders() {
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(RepositoryEntry.class));
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(BusinessGroup.class));
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(CurriculumElement.class));
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(Organisation.class));
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(Identity.class));
		CourseModule.registerForCourseType(this, null);
	}

	@Override
	public void event(Event event) {
		if (event instanceof RepositoryEntryMembershipModifiedEvent) {
			// Identity was added to course as participant.
			// Course member got role participant.
			RepositoryEntryMembershipModifiedEvent e = (RepositoryEntryMembershipModifiedEvent)event;
			if (RepositoryEntryMembershipModifiedEvent.ROLE_PARTICIPANT_ADDED.equals(e.getCommand())) {
				tryProcessIdentityAddedToRepositoryEntry(e.getIdentityKey(), e.getRepositoryEntryKey());
			}
		} else if (event instanceof BusinessGroupModifiedEvent) {
			// Identity was added to a group.
			// Group member got other roles in group.
			BusinessGroupModifiedEvent e = (BusinessGroupModifiedEvent)event;
			if (BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT.equals(e.getCommand())
					&& e.getAffectedRepositoryEntryKey() != null) {
				tryProcessIdentityAddedToBusinessGroup(e.getAffectedIdentityKey(), e.getModifiedGroupKey(), e.getAffectedRepositoryEntryKey());
			} else if (BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT.equals(e.getCommand())
					&& e.getAffectedRepositoryEntryKey() == null) {
				// process will take care of all courses
				tryProcessIdentityRemovedFromBusinessGroup(e.getAffectedIdentityKey(), e.getModifiedGroupKey());
			}
		} else if (event instanceof BusinessGroupRepositoryEntryEvent) {
			// Group was added to a repository entry
			BusinessGroupRepositoryEntryEvent e = (BusinessGroupRepositoryEntryEvent)event;
			if (BusinessGroupRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED.equals(e.getCommand())) {
				tryProcessBusinessGroupAddedToRepositoryEntry(e.getGroupKey(), e.getEntryKey());
			}
		} else if (event instanceof CurriculumElementMembershipEvent) {
			CurriculumElementMembershipEvent e = (CurriculumElementMembershipEvent)event;
			if (CurriculumElementMembershipEvent.MEMEBER_ADDED.equals(e.getCommand())) {
				tryProcessIdentityAddedToCurriculumElement(e.getIdentityKey(), e.getCurriculumElementKey(), e.getRole());
			} else if (CurriculumElementMembershipEvent.MEMEBER_REMOVED.equals(e.getCommand())) {
				tryProcessIdentityRemovedFromCurriculumElement(e.getIdentityKey(), e.getCurriculumElementKey(), e.getRole());
			}
		} else if (event instanceof CurriculumElementRepositoryEntryEvent) {
			CurriculumElementRepositoryEntryEvent e = (CurriculumElementRepositoryEntryEvent) event;
			if (CurriculumElementRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED.equals(e.getCommand())) {
				tryProcessCurriculumElementAddedToRepositoryEntry(e.getCurriculumElementKey(), e.getEntryKey());
			}
		} else if (event instanceof OrganisationMembershipEvent) {
			OrganisationMembershipEvent omEvent = (OrganisationMembershipEvent)event;
			if (OrganisationMembershipEvent.IDENTITY_ADDED.equals(omEvent.getCommand())
					|| OrganisationMembershipEvent.IDENTITY_REMOVED.equals(omEvent.getCommand())) {
				tryProcessIdentityAddedToOrganisation(omEvent.getIdentityKey(), omEvent.getOrganisationKey());
			}
		} else if (event instanceof UserPropertyChangedEvent) {
			UserPropertyChangedEvent upEvent = (UserPropertyChangedEvent)event;
			if (StringHelper.containsNonWhitespace(upEvent.getNewValue())) {
				tryProcessUserPropertyChanged(upEvent.getIdentityKey(), upEvent.getPropertyName(), upEvent.getNewValue());
			}
			if (StringHelper.containsNonWhitespace(upEvent.getOldValue())) {
				tryProcessUserPropertyChanged(upEvent.getIdentityKey(), upEvent.getPropertyName(), upEvent.getOldValue());
			}
		} else if (event instanceof PublishEvent) {
			PublishEvent pe = (PublishEvent) event;
			if (pe.getState() == PublishEvent.PUBLISH && pe.isEventOnThisNode()) {
				tryProcessPublish(pe);
			}
		}
	}

	private void tryProcessIdentityAddedToRepositoryEntry(Long identityKey, Long courseEntryKey) {
		try {
			log.debug("Process Identity {} added to RepositoryEntry {}", identityKey, courseEntryKey);
			Identity identity = securityManager.loadIdentityByKey(identityKey);
			RepositoryEntry courseEntry = repositoryService.loadByKey(courseEntryKey);
			evaluateAll(identity, courseEntry, false, null);
		} catch (Exception e) {
			log.warn("Error when processing Identity {} added to RepositoryEntry {}",
					identityKey, courseEntryKey);
		}
	}
	
	private void tryProcessIdentityAddedToBusinessGroup(Long identityKey, Long groupKey, Long repositoryEntryKey) {
		try {
			log.debug("Process Identity {} added to BusinessGroup {}", identityKey, groupKey);
			processIdentityAddedToBusinessGroup(identityKey, groupKey, repositoryEntryKey);
		} catch (Exception e) {
			log.warn("Error when processing Identity {} added to BusinessGroup {}", identityKey, groupKey);
		}
	}
	
	private void processIdentityAddedToBusinessGroup(Long identityKey, Long groupKey, Long entryKey) {
		if (!isParticipant(identityKey, groupKey)) return;
	
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(entryKey);

		List<Long> evalutedRepositoryEntryKeys = new ArrayList<>(1);
		ObligationContext obligationContext = new SingleUserObligationContext();
		evaluateAll(identity, repositoryEntry, false, obligationContext);
		evalutedRepositoryEntryKeys.add(repositoryEntry.getKey());
		processExceptionlObligationsGroup(identity, groupKey, evalutedRepositoryEntryKeys, obligationContext);
	}

	private boolean isParticipant(Long identityKey, Long groupKey) {
		BusinessGroupRef groupRef = new BusinessGroupRefImpl(groupKey);
		IdentityRef identityRef = new IdentityRefImpl(identityKey);
		return groupService.hasRoles(identityRef, groupRef, GroupRoles.participant.name());
	}
	
	private void tryProcessIdentityRemovedFromBusinessGroup(Long identityKey, Long groupKey) {
		try {
			log.debug("Process Identity {} removed from BusinessGroup {}", identityKey, groupKey);
			processIdentityRemovedFromBusinessGroup(identityKey, groupKey);
		} catch (Exception e) {
			log.warn("Error when processing Identity {} removed from BusinessGroup {}", identityKey, groupKey);
		}
	}

	private void processIdentityRemovedFromBusinessGroup(Long identityKey, Long groupKey) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		List<Long> evalutedRepositoryEntryKeys = Collections.emptyList();
		ObligationContext obligationContext = new SingleUserObligationContext();
		processExceptionlObligationsGroup(identity, groupKey, evalutedRepositoryEntryKeys, obligationContext);
	}

	private void processExceptionlObligationsGroup(Identity identity, Long groupKey,
			List<Long> evalutedRepositoryEntryKeys, ObligationContext obligationContext) {
		ScoreAccountingTriggerSearchParams searchParams = new ScoreAccountingTriggerSearchParams();
		searchParams.setBusinessGroupRef(() -> groupKey);
		List<RepositoryEntry> entries = courseAssessmentService.getTriggeredRepositoryEntries(searchParams);
		entries.removeIf(entry -> evalutedRepositoryEntryKeys.contains(entry.getKey()));
		if (!entries.isEmpty()) {
			for (RepositoryEntry entry : entries) {
				evaluateAll(identity, entry, true, obligationContext);
			}
		}
	}
	
	private void tryProcessBusinessGroupAddedToRepositoryEntry(Long groupKey, Long entryKey) {
		try {
			log.debug("Process BusinessGroup {} added to RepositoryEntry {}", groupKey, entryKey);
			processBusinessGroupAddedToRepositoryEntry(groupKey, entryKey);
		} catch (Exception e) {
			log.warn("Error when processing BusinessGroup {} added to RepositoryEntry {}", groupKey, entryKey);
		}
	}
	
	private void processBusinessGroupAddedToRepositoryEntry(Long groupKey, Long entryKey) {
		BusinessGroupRefImpl group = new BusinessGroupRefImpl(groupKey);
		List<Identity> participants = groupService.getMembers(group, GroupRoles.participant.name());
		RepositoryEntry courseEntry = repositoryService.loadByKey(entryKey);
		
		for (Identity identity : participants) {
			evaluateAll(identity, courseEntry, false, null);
		}
	}
	
	private void tryProcessIdentityAddedToCurriculumElement(Long identityKey, Long curriculumElementKey, CurriculumRoles role) {
		try {
			log.debug("Process Identity {} added to CurriculumElement {} with role {}", identityKey, curriculumElementKey, role);
			processIdentityAddedToCurriculumElement(identityKey, curriculumElementKey, role);
		} catch (Exception e) {
			log.warn("Error when processing Identity {} added to CurriculumElement {} with role {}",
					identityKey, curriculumElementKey, role);
		}
	}

	private void processIdentityAddedToCurriculumElement(Long identityKey, Long curriculumElementKey, CurriculumRoles role) {
		if (CurriculumRoles.participant == role) {
			Identity identity = securityManager.loadIdentityByKey(identityKey);
			
			List<Long> evalutedRepositoryEntryKeys = new ArrayList<>();
			ObligationContext obligationContext = new SingleUserObligationContext();
			List<RepositoryEntry> repositoryEntries = curriculumService.getRepositoryEntries(() -> curriculumElementKey);
			
			for (RepositoryEntry repositoryEntry : repositoryEntries) {
				evaluateAll(identity, repositoryEntry, false, obligationContext);
				evalutedRepositoryEntryKeys.add(repositoryEntry.getKey());
			}
			
			processExceptionlObligationsCurriculumElement(identity, curriculumElementKey, evalutedRepositoryEntryKeys, obligationContext);
		}
	}

	private void tryProcessIdentityRemovedFromCurriculumElement(Long identityKey, Long curriculumElementKey, CurriculumRoles role) {
		try {
			log.debug("Process Identity {} removed from CurriculumElement {} with role {}", identityKey, curriculumElementKey, role);
			processIdentityRemovedFromCurriculumElement(identityKey, curriculumElementKey, role);
		} catch (Exception e) {
			log.warn("Error when processing Identity {} removed from CurriculumElement {} with role {}",
					identityKey, curriculumElementKey, role);
		}
	}
	
	private void processIdentityRemovedFromCurriculumElement(Long identityKey, Long curriculumElementKey, CurriculumRoles role) {
		if (role == null || role != CurriculumRoles.participant) {
			Identity identity = securityManager.loadIdentityByKey(identityKey);
			
			List<Long> evalutedRepositoryEntryKeys = new ArrayList<>();
			ObligationContext obligationContext = new SingleUserObligationContext();
			processExceptionlObligationsCurriculumElement(identity, curriculumElementKey, evalutedRepositoryEntryKeys, obligationContext);
		}
	}

	private void processExceptionlObligationsCurriculumElement(Identity identity, Long curriculumElementKey,
			List<Long> evalutedRepositoryEntryKeys, ObligationContext obligationContext) {
		ScoreAccountingTriggerSearchParams searchParams = new ScoreAccountingTriggerSearchParams();
		searchParams.setCurriculumElementRef(() -> curriculumElementKey);
		List<RepositoryEntry> entries = courseAssessmentService.getTriggeredRepositoryEntries(searchParams);
		entries.removeIf(entry -> evalutedRepositoryEntryKeys.contains(entry.getKey()));
		if (!entries.isEmpty()) {
			for (RepositoryEntry entry : entries) {
				evaluateAll(identity, entry, true, obligationContext);
				entries.add(entry);
			}
		}
	}
	
	private void tryProcessCurriculumElementAddedToRepositoryEntry(Long curriculumElementKey, Long entryKey) {
		try {
			log.debug("Process CurriculumElement {} added to RepositoryEntry {}", curriculumElementKey, entryKey);
			processCurriculumElementAddedToRepositoryEntry(curriculumElementKey, entryKey);
		} catch (Exception e) {
			log.warn("Error when processing CurriculumElement {} added to RepositoryEntry {}", curriculumElementKey, entryKey);
		}	
	}

	private void processCurriculumElementAddedToRepositoryEntry(Long curriculumElementKey, Long entryKey) {
		RepositoryEntry courseEntry = repositoryService.loadByKey(entryKey);
		List<Identity> participants = curriculumService.getMembersIdentity(new CurriculumElementRefImpl(curriculumElementKey), CurriculumRoles.participant);
		for (Identity identity : participants) {
			evaluateAll(identity, courseEntry, false, null);
		}
	}
	
	private void tryProcessIdentityAddedToOrganisation(Long identityKey, Long organisationKey) {
		try {
			log.debug("Process Identity {} added to Organisation {}", identityKey, organisationKey);
			processProcessIdentityAddedToOrganisation(identityKey, organisationKey);
		} catch (Exception e) {
			log.warn("Error when processing Identity {} removed from Organisation {}", identityKey, organisationKey);
		}
	}

	private void processProcessIdentityAddedToOrganisation(Long identityKey, Long organisationKey) {
		ScoreAccountingTriggerSearchParams searchParams = new ScoreAccountingTriggerSearchParams();
		searchParams.setOrganisationRef(() -> organisationKey);
		List<RepositoryEntry> entries = courseAssessmentService.getTriggeredRepositoryEntries(searchParams);
		if (!entries.isEmpty()) {
			Identity identity = securityManager.loadIdentityByKey(identityKey);
			ObligationContext obligationContext = new SingleUserObligationContext();
			for (RepositoryEntry entry : entries) {
				evaluateAll(identity, entry, true, obligationContext);
			}
		}
	}

	private void tryProcessUserPropertyChanged(Long identityKey, String propertyName, String propertyValue) {
		try {
			log.debug("Process Identity {} with changed user property {}={}", identityKey, propertyName, propertyValue);
			processUserPropertyChanged(identityKey, propertyName, propertyValue);
		} catch (Exception e) {
			log.warn("Error when processing Identity {} with changed user property {}={}",
					identityKey, propertyName, propertyValue);
		}
	}
	
	private void processUserPropertyChanged(Long identityKey, String propertyName, String propertyValue) {
		ScoreAccountingTriggerSearchParams searchParams = new ScoreAccountingTriggerSearchParams();
		searchParams.setUserPropertyName(propertyName);
		searchParams.setUserPropertyValue(propertyValue);
		List<RepositoryEntry> entries = courseAssessmentService.getTriggeredRepositoryEntries(searchParams);
		if (!entries.isEmpty()) {
			Identity identity = securityManager.loadIdentityByKey(identityKey);
			ObligationContext obligationContext = new SingleUserObligationContext();
			for (RepositoryEntry entry : entries) {
				evaluateAll(identity, entry, true, obligationContext);
			}
		}
	}

	private void evaluateAll(Identity identity, RepositoryEntry courseEntry, boolean existingOnly, ObligationContext obligationContext) {
		if (identity == null || courseEntry == null) return;
		
		if (existingOnly) {
			// If the user is has no assessment entry he is probably not a course participant.
			// In this case he must not have assessment entries just because there is an
			// trigger (e.g. exceptional obligation) that affects the course.
			if (!assessmentService.hasAssessmentEntry(identity, courseEntry)) {
				return;
			}
		}
		
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(identity);
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		if (course == null) return;
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, courseEnv);
		ScoreAccounting scoreAccounting = userCourseEnv.getScoreAccounting();
		if (obligationContext != null) {
			scoreAccounting.setObligationContext(obligationContext);
		}
		scoreAccounting.evaluateAll(true);
		log.debug("Evaluated all assessment entries of {} in {}", identity, courseEntry);
		dbInstance.commitAndCloseSession();
	}
	
	private void tryProcessPublish(PublishEvent pe) {
		try {
			log.debug("Process publish of course {}", pe.getPublishedCourseResId());
			ICourse course = CourseFactory.loadCourse(pe.getPublishedCourseResId());
			boolean update = nodeAccessService.isUpdateEvaluationOnPublish(NodeAccessType.of(course));
			courseAssessmentService.evaluateAllAsync(pe.getPublishedCourseResId(), update);
		} catch (Exception e) {
			log.warn("Error when processing publish of course {}", pe.getPublishedCourseResId());
		}
	}
	
}
