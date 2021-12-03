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

import java.util.Collection;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.BusinessGroupRefImpl;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.group.ui.edit.BusinessGroupRepositoryEntryEvent;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembershipEvent;
import org.olat.modules.curriculum.CurriculumElementRepositoryEntryEvent;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryMembershipModifiedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseMembershipProcessor implements GenericEventListener {

	private static final Logger log = Tracing.createLoggerFor(CourseMembershipProcessor.class);
	
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
	private CoordinatorManager coordinator;
	
	@PostConstruct
	void initProviders() {
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(RepositoryEntry.class));
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(CurriculumElement.class));
	}

	@Override
	public void event(Event event) {
		if (event instanceof RepositoryEntryMembershipModifiedEvent) {
			// Identity was added to course as participant.
			// Course member got role participant.
			RepositoryEntryMembershipModifiedEvent e = (RepositoryEntryMembershipModifiedEvent)event;
			if (RepositoryEntryMembershipModifiedEvent.ROLE_PARTICIPANT_ADDED.equals(e.getCommand())) {
				tryProcessAddedToRepository(e.getIdentityKey(), e.getRepositoryEntryKey());
			}
		} else if (event instanceof BusinessGroupModifiedEvent) {
			// Identity was added to a group.
			// Group member got other roles in group.
			BusinessGroupModifiedEvent e = (BusinessGroupModifiedEvent)event;
			if (BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT.equals(e.getCommand())
					&& e.getAffectedRepositoryEntryKey() != null) {
				tryProcessAddedToGroup(e.getAffectedIdentityKey(), e.getModifiedGroupKey(), e.getAffectedRepositoryEntryKey());
			}
		} else if (event instanceof BusinessGroupRepositoryEntryEvent) {
			// Group was added to a repository entry
			BusinessGroupRepositoryEntryEvent e = (BusinessGroupRepositoryEntryEvent)event;
			if (BusinessGroupRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED.equals(e.getCommand())) {
				tryProcessRepositoryAdded(e.getEntryKey(), e.getGroupKey());
			}
		} else if (event instanceof CurriculumElementMembershipEvent) {
			// Identity was added to curriculum entries
			CurriculumElementMembershipEvent e = (CurriculumElementMembershipEvent)event;
			if (CurriculumElementMembershipEvent.MEMEBER_ADDED.equals(e.getCommand())) {
				if (CurriculumRoles.participant == e.getRole()) {
					tryProcessCurriculumElementMemberAdded(e.getIdentityKey(), e.getCurriculumElementKeys());
				}
			}
		} else if (event instanceof CurriculumElementRepositoryEntryEvent) {
			CurriculumElementRepositoryEntryEvent e = (CurriculumElementRepositoryEntryEvent) event;
			if (CurriculumElementRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED.equals(e.getCommand())) {
				tryProcessCurriculumElementRepositoryAdded(e.getCurriculumElementKey(), e.getEntryKey());
			}
		}
		
	}

	private void tryProcessAddedToRepository(Long identityKey, Long courseEntryKey) {
		try {
			Identity identity = securityManager.loadIdentityByKey(identityKey);
			RepositoryEntry courseEntry = repositoryService.loadByKey(courseEntryKey);
			evaluateAll(identity, courseEntry);
		} catch (Exception e) {
			log.error("Error when tried to evaluate all assessment entries of Identity {} in RepositoryEntry {}",
					courseEntryKey, identityKey);
		}
	}
	
	private void tryProcessAddedToGroup(Long identityKey, Long groupKey, Long repositoryEntryKey) {
		try {
			processAddedToGroup(identityKey, groupKey, repositoryEntryKey);
		} catch (Exception e) {
			log.error("Error when tried to process added to group of Identity {} to Group {}",
					groupKey, identityKey);
		}
	}
	
	private void processAddedToGroup(Long identityKey, Long groupKey, Long repositoryEntryKey) {
		if (!isParticipant(identityKey, groupKey)) return;
	
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(repositoryEntryKey);
		if(repositoryEntry != null) {
			evaluateAll(identity, repositoryEntry);
		}
	}

	private boolean isParticipant(Long identityKey, Long groupKey) {
		BusinessGroupRef groupRef = new BusinessGroupRefImpl(groupKey);
		IdentityRef identityRef = new IdentityRefImpl(identityKey);
		return groupService.hasRoles(identityRef, groupRef, GroupRoles.participant.name());
	}
	
	private void tryProcessRepositoryAdded(Long entryKey, Long groupKey) {
		try {
			processRepositoryAdded(entryKey, groupKey);
		} catch (Exception e) {
			log.error("Error when tried to process added RepositoryEntry {} to Group {}",
					entryKey, groupKey);
		}
	}
	
	private void processRepositoryAdded(Long entryKey, Long groupKey) {
		BusinessGroupRefImpl group = new BusinessGroupRefImpl(groupKey);
		List<Identity> participants = groupService.getMembers(group, GroupRoles.participant.name());
		RepositoryEntry courseEntry = repositoryService.loadByKey(entryKey);
		
		for (Identity identity : participants) {
			evaluateAll(identity, courseEntry);
		}
	}
	
	private void tryProcessCurriculumElementMemberAdded(Long identityKey, Collection<Long> curriculumElementKeys) {
		try {
			processCurriculumElementMemberAdded(identityKey, curriculumElementKeys);
		} catch (Exception e) {
			log.error("Error when tried to process added Identity {} to CurriculumElements {}",
					identityKey, curriculumElementKeys);
		}
	}

	private void processCurriculumElementMemberAdded(Long identityKey, Collection<Long> curriculumElementKeys) {
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		
		for (Long curriculumElementKey : curriculumElementKeys) {
			List<RepositoryEntry> repositoryEntries = curriculumService
					.getRepositoryEntries(new CurriculumElementRefImpl(curriculumElementKey));
			
			for (RepositoryEntry repositoryEntry : repositoryEntries) {
				evaluateAll(identity, repositoryEntry);
			}
		}
	}
	
	private void tryProcessCurriculumElementRepositoryAdded(Long curriculumElementKey, Long entryKey) {
		try {
			processCurriculumElementRepositoryAdded(curriculumElementKey, entryKey);
		} catch (Exception e) {
			log.error("Error when tried to process added RepositoryEntry {} to CurriculumElement {}",
					entryKey, curriculumElementKey);
		}	
	}

	private void processCurriculumElementRepositoryAdded(Long curriculumElementKey, Long entryKey) {
		RepositoryEntry courseEntry = repositoryService.loadByKey(entryKey);
		List<Identity> participants = curriculumService.getMembersIdentity(new CurriculumElementRefImpl(curriculumElementKey), CurriculumRoles.participant);
		for (Identity identity : participants) {
			evaluateAll(identity, courseEntry);
		}
	}

	private void evaluateAll(Identity identity, RepositoryEntry courseEntry) {
		if (identity == null || courseEntry == null) return;
		
		IdentityEnvironment identityEnv = new IdentityEnvironment();
		identityEnv.setIdentity(identity);
		
		ICourse course = CourseFactory.loadCourse(courseEntry);
		if (course == null) return;
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		
		UserCourseEnvironment userCourseEnv = new UserCourseEnvironmentImpl(identityEnv, courseEnv);
		userCourseEnv.getScoreAccounting().evaluateAll(true);
		log.debug("Evaluated all assessment entries of {} in {}", identity, courseEntry);
		dbInstance.commitAndCloseSession();
	}
	
}
