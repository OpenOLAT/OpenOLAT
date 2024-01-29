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
package org.olat.course.todo.manager;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeHelper;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.todo.CourseNodeToDoHandler;
import org.olat.course.todo.CourseNodesToDoSyncher;
import org.olat.course.todo.CourseToDoEnvironment;
import org.olat.course.todo.CourseToDoService;
import org.olat.course.todo.ui.CourseToDoUIFactory;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.edit.BusinessGroupModifiedEvent;
import org.olat.group.ui.edit.BusinessGroupRepositoryEntryEvent;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembershipEvent;
import org.olat.modules.curriculum.CurriculumElementRepositoryEntryEvent;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.model.RepositoryEntryMembershipModifiedEvent;
import org.olat.repository.model.RepositoryEntryStatusChangedEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 19 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseToDoServiceImpl implements CourseToDoService, GenericEventListener {
	
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private NoToDoHandler noDoToHandler;
	@Autowired
	private CoordinatorManager coordinator;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	
	@Autowired
	private List<CourseNodeToDoHandler> handlers;
	private Map<String, CourseNodeToDoHandler> typeToHandler;
	private List<String> courseNodeToDoTaskProviderTypes;
	
	@PostConstruct
	private void init() {
		typeToHandler = handlers.stream()
				.filter(sync -> sync.acceptCourseNodeType() != null)
				.collect(Collectors.toMap(CourseNodeToDoHandler::acceptCourseNodeType, Function.identity()));
		
		courseNodeToDoTaskProviderTypes = typeToHandler.values().stream()
				.map(CourseNodeToDoHandler::getToDoTaskTypes)
				.flatMap(Set::stream)
				.toList();
		
		coordinator.getCoordinator().getEventBus().registerFor(this, null, RepositoryService.REPOSITORY_EVENT_ORES);
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(RepositoryEntry.class));
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(BusinessGroup.class));
		coordinator.getCoordinator().getEventBus().registerFor(this, null, OresHelper.lookupType(CurriculumElement.class));
	}
	
	@Override
	public ToDoTaskSearchParams createCourseTagSearchParams(RepositoryEntryRef repositoryEntry) {
		ToDoTaskSearchParams tagInfoSearchParams = new ToDoTaskSearchParams();
		tagInfoSearchParams.setTypes(CourseToDoService.COURSE_PROVIDER_TYPES);
		if (repositoryEntry != null) {
			tagInfoSearchParams.setOriginIds(List.of(repositoryEntry.getKey()));
		}
		return tagInfoSearchParams;
	}
	
	@Override
	public List<Identity> getAssigneeCandidates(Identity doer, RepositoryEntry repositoryEntry, boolean coachOnly) {
		return coachOnly
				? repositoryService.getCoachedParticipants(doer, repositoryEntry)
				: repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.all, GroupRoles.participant.name());
	}
	
	@Override
	public void deleteToDoTasks(RepositoryEntry entry) {
		ToDoTaskSearchParams elementSearchParams = new ToDoTaskSearchParams();
		elementSearchParams.setOriginIds(List.of(entry.getKey()));
		elementSearchParams.setTypes(CourseToDoService.COURSE_PROVIDER_TYPES.stream().filter(type -> !type.equals(CourseCollectionToDoTaskProvider.TYPE)).toList());
		toDoService.getToDoTasks(elementSearchParams).forEach(toDoTask -> toDoService.deleteToDoTaskPermanently(toDoTask));
		
		ToDoTaskSearchParams collectionSearchParams = new ToDoTaskSearchParams();
		collectionSearchParams.setOriginIds(List.of(entry.getKey()));
		collectionSearchParams.setTypes(List.of(CourseCollectionToDoTaskProvider.TYPE));
		toDoService.getToDoTasks(collectionSearchParams).forEach(toDoTask -> toDoService.deleteToDoTaskPermanently(toDoTask));
		
		// Course node to-dos are deleted by the course repository handler
	}
	
	@Override
	public List<String> getCourseNodeToDoTaskProviderTypes() {
		return courseNodeToDoTaskProviderTypes;
	}

	@Override
	public CourseNodeToDoHandler getToDoHandler(CourseNode courseNode) {
		return courseNode != null && StringHelper.containsNonWhitespace(courseNode.getType())
				? typeToHandler.getOrDefault(courseNode.getType(), noDoToHandler)
				: noDoToHandler;
	}

	@Override
	public CourseNodesToDoSyncher getCourseNodesToDoSyncher(CourseEnvironment courseEnv, Set<Identity> identities) {
		CourseNodesToDoSyncherImpl courseNodesToDoSyncer = new CourseNodesToDoSyncherImpl();
		
		Set<String> toDoProviderTypes = new HashSet<>();
		CourseToDoEnvironment courseToDoEnv = new MultiUserUserToDoEnvironment(toDoProviderTypes , identities);
		courseNodesToDoSyncer.setCourseToDoEnv(courseToDoEnv);
		
		fillCourseNodeToDoSyncher(courseNodesToDoSyncer, toDoProviderTypes, identities, courseEnv.getRunStructure().getRootNode());
		
		return courseNodesToDoSyncer;
	}
	
	private void fillCourseNodeToDoSyncher(CourseNodesToDoSyncherImpl courseNodesToDoSyncher, Set<String> toDoProviderTypes, Set<Identity> identities, INode node) {
		CourseNode courseNode = CourseNodeHelper.getCourseNode(node);
		if (courseNode != null) {
			CourseNodeToDoHandler handler = getToDoHandler(courseNode);
			courseNodesToDoSyncher.setCourseNodeToDoSyncher(courseNode, handler.getCourseNodeToDoSyncher(courseNode, identities));
			if (!handler.getToDoTaskTypes().isEmpty()) {
				toDoProviderTypes.addAll(handler.getToDoTaskTypes());
			}
			
			for (int i = 0; i < courseNode.getChildCount(); i++) {
				fillCourseNodeToDoSyncher(courseNodesToDoSyncher, toDoProviderTypes, identities, courseNode.getChildAt(i));
			}
		}
	}
	
	@Override
	public void deleteToDoTasks(ICourse course, CourseNode courseNode) {
		Set<String> toDoTaskTypes = getToDoHandler(courseNode).getToDoTaskTypes();
		if (!toDoTaskTypes.isEmpty()) {
			ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
			searchParams.setOriginIds(List.of(course.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey()));
			searchParams.setOriginSubPaths(List.of(courseNode.getIdent()));
			searchParams.setTypes(toDoTaskTypes);
			toDoService.getToDoTasks(searchParams).forEach(toDoTask -> toDoService.deleteToDoTaskPermanently(toDoTask));
		}
	}
	
	@Override
	public void resetToDoTasks(UserCourseEnvironment userCourseEnv, CourseNode courseNode) {
		Set<String> toDoTaskTypes = getToDoHandler(courseNode).getToDoTaskTypes();
		if (!toDoTaskTypes.isEmpty()) {
			ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
			searchParams.setAssigneeOrDelegatee(userCourseEnv.getIdentityEnvironment().getIdentity());
			searchParams.setOriginIds(List.of(userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey()));
			searchParams.setOriginSubPaths(List.of(courseNode.getIdent()));
			searchParams.setTypes(toDoTaskTypes);
			toDoService.getToDoTasks(searchParams).forEach(toDoTask -> toDoService.deleteToDoTaskPermanently(toDoTask));
		}
	}

	@Override
	public void updateOriginTitle(RepositoryEntry courseEntry) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		if (course == null) {
			return;
		}
		
		String originTitle = CourseToDoUIFactory.getOriginTitle(courseEntry);
		
		// Course to-dos
		CourseToDoService.COURSE_PROVIDER_TYPES.forEach(type -> {
			toDoService.updateOriginTitle(type, courseEntry.getKey(), null, originTitle, null);
		});
		
		// Course node to-dos
		Visitor visitor = node -> {
			CourseNode courseNode = CourseNodeHelper.getCourseNode(node);
			if (courseNode != null) {
				getToDoHandler(courseNode).getToDoTaskTypes().forEach(type -> toDoService.updateOriginTitle(type, courseEntry.getKey(), null, originTitle, null));
			}
		};
		new TreeVisitor(visitor, course.getRunStructure().getRootNode(), true).visitAll();
	}
	
	@Override
	public void updateOriginDeletedFalse(RepositoryEntry courseEntry, IdentityRef identity) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		if (course == null) {
			return;
		}
		
		Visitor visitor = node -> {
			CourseNode courseNode = CourseNodeHelper.getCourseNode(node);
			if (courseNode != null) {
				updateOriginDeletedFalse(courseEntry, identity, courseNode);
			}
		};
		new TreeVisitor(visitor, course.getRunStructure().getRootNode(), true).visitAll();
	}

	private void updateOriginDeletedFalse(RepositoryEntry courseEntry, IdentityRef identity, CourseNode courseNode) {
		Set<String> toDoTaskTypes = getToDoHandler(courseNode).getToDoTaskTypes();
		if (!toDoTaskTypes.isEmpty()) {
			ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
			searchParams.setAssigneeOrDelegatee(identity);
			searchParams.setOriginIds(List.of(courseEntry.getKey()));
			searchParams.setOriginSubPaths(List.of(courseNode.getIdent()));
			searchParams.setTypes(toDoTaskTypes);
			toDoService.getToDoTasks(searchParams).forEach(toDoTask -> toDoService.updateOriginDeleted(toDoTask, true, new Date(), null));
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof EntryChangedEvent e && e.isEventOnThisNode()) {
			if (e.getChange() == Change.modifiedDescription) {
				RepositoryEntry repositoryEntry = repositoryService.loadByKey(e.getRepositoryEntryKey());
				if (repositoryEntry != null && CourseModule.ORES_TYPE_COURSE.equals(repositoryEntry.getOlatResource().getResourceableTypeName())) {
					updateOriginTitle(repositoryEntry);
				}
			}
		} else if (event instanceof RepositoryEntryMembershipModifiedEvent e && e.isEventOnThisNode()) {
			if (RepositoryEntryMembershipModifiedEvent.ROLE_PARTICIPANT_ADDED.equals(e.getCommand())) {
				synchCourseToDoTasks(e.getRepositoryEntryKey(), e.getIdentityKey());
			} else if (RepositoryEntryMembershipModifiedEvent.IDENTITY_REMOVED.equals(e.getCommand())) {
				synchCourseToDoTasks(e.getRepositoryEntryKey(), e.getIdentityKey());
			}
		} else if (event instanceof BusinessGroupModifiedEvent e && e.isEventOnThisNode()) {
			if (BusinessGroupModifiedEvent.IDENTITY_ADDED_EVENT.equals(e.getCommand())) {
				synchCourseToDoTasks(e.getAffectedRepositoryEntryKey(), e.getAffectedIdentityKey());
			} else if (BusinessGroupModifiedEvent.IDENTITY_REMOVED_EVENT.equals(e.getCommand())) {
				synchCourseToDoTasks(e.getAffectedRepositoryEntryKey(), e.getAffectedIdentityKey());
			}
		} else if (event instanceof BusinessGroupRepositoryEntryEvent e && e.isEventOnThisNode()) {
			if (BusinessGroupRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED.equals(e.getCommand())) {
				// Just be lazy: Sync all course members but all group members
				synchCourseToDoTasks(e.getEntryKey(), null);
			}
		} else if (event instanceof CurriculumElementMembershipEvent e && e.isEventOnThisNode()) {
			synchCourseToDoTasksOfCurriculumElementMember(e.getCurriculumElementKey(), e.getIdentityKey());
		} else if (event instanceof CurriculumElementRepositoryEntryEvent e && e.isEventOnThisNode()) {
			if (CurriculumElementRepositoryEntryEvent.REPOSITORY_ENTRY_ADDED.equals(e.getCommand())) {
				// Just be lazy: Sync all course members but all curriculum members
				synchCourseToDoTasks(e.getEntryKey(), null);
			}
		} else if (event instanceof RepositoryEntryStatusChangedEvent e && e.isEventOnThisNode()) {
				synchCourseToDoTasks(e.getRepositoryEntryKey(), null);
		}
	}
	
	private void synchCourseToDoTasksOfCurriculumElementMember(Long curriculumElementKey, Long identityKey) {
		curriculumService.getRepositoryEntries(() -> curriculumElementKey)
				.forEach(repositoryEntry -> synchCourseToDoTasks(repositoryEntry.getKey(), identityKey));
	}
	
	private void synchCourseToDoTasks(Long repositoryEntryKey, Long assigneeKey) {
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(repositoryEntryKey);
		boolean courseNotDeleted = isCourseNotDeleted(repositoryEntry);
		
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setTypes(COURSE_PROVIDER_TYPES);
		searchParams.setOriginIds(List.of(repositoryEntryKey));
		if (assigneeKey != null) {
			searchParams.setAssigneeOrDelegateeKeys(List.of(assigneeKey));
		}
		List<ToDoTask> toDoTasks = toDoService.getToDoTasks(searchParams);
		Map<Long, ToDoTaskMembers> toDoTaskGroupKeyToMembers = toDoService.getToDoTaskGroupKeyToMembers(toDoTasks, List.of(ToDoRole.assignee));
		
		Collection<Long> participantKeys = null;
		if (assigneeKey != null) {
			if (repositoryService.isMember(() -> assigneeKey, () -> repositoryEntryKey)) {
				participantKeys = List.of(assigneeKey);
			} else {
				participantKeys = List.of();
			}
		} else {
			participantKeys = repositoryService.getMemberKeys(() -> repositoryEntryKey, RepositoryEntryRelationType.all, GroupRoles.participant.name());
		}
		
		for (ToDoTask toDoTask : toDoTasks) {
			boolean contextValid = isContextValid(toDoTask, courseNotDeleted, toDoTaskGroupKeyToMembers, participantKeys);
			if (contextValid && toDoTask.isOriginDeleted()) {
				toDoService.updateOriginDeleted(toDoTask, false, null, null);
			} else if (!contextValid && !toDoTask.isOriginDeleted()) {
				toDoService.updateOriginDeleted(toDoTask, true, new Date(), null);
			}
		}
	}

	private boolean isContextValid(ToDoTask toDoTask, boolean courseStatusPublished,
			Map<Long, ToDoTaskMembers> toDoTaskGroupKeyToMembers, Collection<Long> participantKeys) {
		/*
		 * As of today to-dos are valid from preparation until closed if user is
		 * participant. The status does not actually need to be checked, as participants
		 * are removed from the course when a course is deleted.
		 */
		
		// CourseCollectionToDoTaskProvider has no assignees, participation not relevant
		if (CourseCollectionToDoTaskProvider.TYPE.equals(toDoTask.getType())) {
			return courseStatusPublished;
		} else if (CourseCollectionElementToDoTaskProvider.TYPE.equals(toDoTask.getType())
				|| CourseIndividualToDoTaskProvider.TYPE.equals(toDoTask.getType())) {
			return courseStatusPublished && isParticipant(toDoTask, toDoTaskGroupKeyToMembers, participantKeys);
		}
		return false;
	}

	private boolean isCourseNotDeleted(RepositoryEntry repositoryEntry) {
		return repositoryEntry != null
				? RepositoryEntryStatusEnum.isInArray(repositoryEntry.getEntryStatus(), RepositoryEntryStatusEnum.preparationToClosed())
				: false;
	}

	private boolean isParticipant(ToDoTask toDoTask, Map<Long, ToDoTaskMembers> toDoTaskGroupKeyToMembers, Collection<Long> participantKeys) {
		Identity assignee = toDoTaskGroupKeyToMembers.get(toDoTask.getBaseGroup().getKey()).getMembers(ToDoRole.assignee).stream()
				.limit(1).findFirst().get();
		return participantKeys.contains(assignee.getKey());
	}

}
