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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.nodes.INode;
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
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
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
	private List<CourseNodeToDoHandler> handlers;
	private Map<String, CourseNodeToDoHandler> typeToHandler;
	
	@PostConstruct
	private void init() {
		typeToHandler = handlers.stream()
				.filter(sync -> sync.acceptCourseNodeType() != null)
				.collect(Collectors.toMap(CourseNodeToDoHandler::acceptCourseNodeType, Function.identity()));
		
		coordinator.getCoordinator().getEventBus().registerFor(this, null, RepositoryService.REPOSITORY_EVENT_ORES);
	}

	@Override
	public CourseNodeToDoHandler getToDoHandler(CourseNode courseNode) {
		return typeToHandler.getOrDefault(courseNode.getType(), noDoToHandler);
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
		if (event instanceof EntryChangedEvent ecEvent && ecEvent.isEventOnThisNode()) {
			if (ecEvent.getChange() == Change.modifiedDescription) {
				RepositoryEntry repositoryEntry = repositoryService.loadByKey(ecEvent.getRepositoryEntryKey());
				if (repositoryEntry != null && CourseModule.ORES_TYPE_COURSE.equals(repositoryEntry.getOlatResource().getResourceableTypeName())) {
					updateOriginTitle(repositoryEntry);
				}
			}
		}
		
	}

}
