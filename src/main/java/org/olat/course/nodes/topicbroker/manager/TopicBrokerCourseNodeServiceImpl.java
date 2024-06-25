/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.topicbroker.manager;

import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.nodes.CollectingVisitor;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.TopicBrokerCourseNode;
import org.olat.course.nodes.topicbroker.TopicBrokerCourseNodeService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.topicbroker.TBBroker;
import org.olat.modules.topicbroker.TopicBrokerService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 14 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class TopicBrokerCourseNodeServiceImpl implements TopicBrokerCourseNodeService, GenericEventListener {
	
	@Autowired
	private TopicBrokerService topicBrokerService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private DueDateService dueDateService;

	@Autowired
	private Coordinator coordinator;
	
	@PostConstruct
	public void init() {
		coordinator.getEventBus().registerFor(this, null, RepositoryService.REPOSITORY_EVENT_ORES);
	}

	@Override
	public void synchBroker(Identity doer, RepositoryEntry courseEntry, CourseNode courseNode) {
		// Why synchronizing the configs?
		// The relative dates have to be available as absolute dates because they can
		// change without publishing and have important impact to the user rights e.g.
		// possibility to select topics. It's important to hard break.
		// The auto process flag is needed make the very frequent job efficient.
		// It will be easier to integrate the topic broker in other modules.
		
		ModuleConfiguration moduleConfig = courseNode.getModuleConfiguration();
		
		String selectionsPerParticipantConfig = moduleConfig.getStringValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTIONS_PER_PARTICIPANT);
		Integer selectionsPerParticipant = StringHelper.isLong(selectionsPerParticipantConfig)
				? Integer.valueOf(selectionsPerParticipantConfig)
				: Integer.valueOf(3);
		
		String enrollmentsPerParticipantConfig = moduleConfig.getStringValue(TopicBrokerCourseNode.CONFIG_KEY_ENROLLMENTS_PER_PARTICIPANT);
		Integer enrollmentsPerParticipant = StringHelper.isLong(enrollmentsPerParticipantConfig)
				? Integer.valueOf(enrollmentsPerParticipantConfig)
				: Integer.valueOf(1);
		boolean participantCanEditRequiredEnrollments = moduleConfig.getBooleanSafe(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_REDUCE_ENROLLMENTS);
		boolean autoEnrollment = moduleConfig.getBooleanSafe(TopicBrokerCourseNode.CONFIG_KEY_ENROLLMENT_AUTO);
		
		boolean participantCanWithdraw = moduleConfig.getBooleanSafe(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW);
		
		DueDateConfig selectionDateConfig = courseNode.getDueDateConfig(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_START);
		// doer as mandatory identity. It does not mater cause no personal dates can be configured.
		Date selectionStartDate = dueDateService.getDueDate(selectionDateConfig, courseEntry, doer);
		
		Date selectionEndDate = null;
		Date withdrawDate = null;
		if (moduleConfig.getBooleanSafe(TopicBrokerCourseNode.CONFIG_KEY_RELATIVE_DATES)) {
			if (selectionStartDate != null) {
				String durationStr = moduleConfig.getStringValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_DURATION);
				if (StringHelper.isLong(durationStr)) {
					selectionEndDate = DateUtils.addDays(selectionStartDate, Integer.valueOf(durationStr));
					
					String withdrawEndRelative = moduleConfig.getStringValue(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END_RELATIVE);
					if (StringHelper.isLong(withdrawEndRelative)) {
						withdrawDate = DateUtils.addDays(selectionEndDate, Integer.valueOf(withdrawEndRelative));
					}
				}
			}
		} else {
			selectionEndDate = moduleConfig.getDateValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_END);
			withdrawDate = moduleConfig.getDateValue(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END);
		}
		
		TBBroker broker = topicBrokerService.getOrCreateBroker(doer, courseEntry, courseNode.getIdent());
		topicBrokerService.updateBroker(doer, broker, selectionsPerParticipant, selectionStartDate, selectionEndDate,
				enrollmentsPerParticipant, participantCanEditRequiredEnrollments, autoEnrollment,
				participantCanWithdraw, withdrawDate);
	}
	
	private void synchTopicBrokers(Long repositoryEntryKey, Long identityKey) {
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(repositoryEntryKey);
		if (repositoryEntry == null || !repositoryEntry.getOlatResource().getResourceableTypeName().equals(CourseModule.getCourseTypeName())) {
			return;
		}
		ICourse course = CourseFactory.loadCourse(repositoryEntry);
		if (course == null) {
			return;
		}
		
		CollectingVisitor collectingVisitor = CollectingVisitor.testing(courseNode -> TopicBrokerCourseNode.TYPE.equals(courseNode.getType()));
		CourseNode rootNode = course.getRunStructure().getRootNode();
		TreeVisitor tv = new TreeVisitor(collectingVisitor, rootNode, true);
		tv.visitAll();
		List<CourseNode> courseNodes = collectingVisitor.getCourseNodes();
		if (courseNodes.isEmpty()) {
			return;
		}
		
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		courseNodes.forEach(courseNode -> synchBroker(identity, repositoryEntry, courseNode));
	}

	@Override
	public void event(Event event) {
		if (event instanceof EntryChangedEvent entryChangedEvent && entryChangedEvent.isEventOnThisNode()) {
			synchTopicBrokers(entryChangedEvent.getRepositoryEntryKey(), entryChangedEvent.getIdentityKey());
		}
	}

	@Override
	public void deleteBroker(RepositoryEntry courseEntry, CourseNode courseNode) {
		topicBrokerService.deleteBroker(courseEntry, courseNode.getIdent());
	}

}
