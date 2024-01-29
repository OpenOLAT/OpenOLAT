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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.todo.CourseToDoContextFilter;
import org.olat.course.todo.CourseToDoService;
import org.olat.course.todo.model.ToDoTaskCollectionCreateContext;
import org.olat.course.todo.model.ToDoTaskCollectionEditContext;
import org.olat.course.todo.model.ToDoTaskCollectionEditContext.Field;
import org.olat.course.todo.ui.CourseToDoUIFactory;
import org.olat.course.todo.ui.ToDoCollectionCreateTaskStep;
import org.olat.course.todo.ui.ToDoCollectionEditTaskStep;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoRole;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskMembers;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoDeleteCollectionConfirmationController;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class CourseCollectionToDoTaskProvider implements ToDoProvider {

	public static final String TYPE = "course.todo.collection";

	@Autowired
	private CourseToDoService courseToDoService;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private CourseToDoContextFilter contextFilter;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public String getBusinessPath(ToDoTask toDoTask) {
		return "[RepositoryEntry:" + toDoTask.getOriginId() + "][ToDoTasks:0]";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return Util.createPackageTranslator(CourseToDoUIFactory.class, locale).translate("course.todo.provider.collection.name");
	}

	@Override
	public String getContextFilterType() {
		return contextFilter.getType();
	}

	@Override
	public String getModifiedBy(Locale locale, ToDoTask toDoTask) {
		return null;
	}

	@Override
	public void upateStatus(Identity doer, ToDoTaskRef toDoTask, Long originId, String originSubPath, ToDoStatus status) {
		//
	}

	@Override
	public Controller createCreateController(UserRequest ureq, WindowControl wControl, Identity doer, Long originId,
			String originSubPath) {
		return null;
	}
	
	@Override
	public boolean isCopyable() {
		return true;
	}

	@Override
	public boolean isEditWizard() {
		return true;
	}

	@Override
	public StepsMainRunController createEditWizardController(UserRequest ureq, WindowControl wControl,
			Translator translator, ToDoTask toDoTask) {
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(toDoTask.getOriginId());
		ToDoTaskSearchParams tagInfoSearchParams = courseToDoService.createCourseTagSearchParams(repositoryEntry);
		List<TagInfo> tagInfos = toDoService.getTagInfos(tagInfoSearchParams, toDoTask);
		
		return new StepsMainRunController(ureq, wControl, new ToDoCollectionEditTaskStep(ureq, toDoTask, tagInfos),
				createEditCallback(), null, translator.translate("task.edit"), "");
	}
	
	@Override
	public boolean isCopyWizard() {
		return true;
	}
	
	@Override
	public StepsMainRunController createCopyWizardController(UserRequest ureq, WindowControl wControl,
			Translator translator, Identity doer, ToDoTask sourceToDoTask) {
		RepositoryEntry repositoryEntry = repositoryManager.lookupRepositoryEntry(sourceToDoTask.getOriginId());
		RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, repositoryEntry);
		boolean coachOnly = reSecurity.isCoach() && !reSecurity.isEntryAdmin();
		return new StepsMainRunController(ureq, wControl,
				new ToDoCollectionCreateTaskStep(ureq, coachOnly, repositoryEntry, sourceToDoTask, false),
				createCreateCallback(), null,
				Util.createPackageTranslator(ToDoUIFactory.class, ureq.getLocale()) .translate("task.copy"), "");
	}

	@Override
	public Controller createCopyController(UserRequest ureq, WindowControl wControl, Identity doer, ToDoTask sourceToDoTask, boolean showContext) {
		return null;
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask, boolean showContext, boolean showSingleAssignee) {
		return null;
	}

	@Override
	public FormBasicController createDetailController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskSecurityCallback secCallback, ToDoTask toDoTask, List<Tag> tags, Identity creator,
			Identity modifier, Set<Identity> assignees, Set<Identity> delegatees) {
		return new ToDoTaskDetailsController(ureq, wControl, mainForm, secCallback, toDoTask, tags, creator, modifier, assignees, delegatees);
	}

	@Override
	public Controller createDeleteConfirmationController(UserRequest ureq, WindowControl wControl, Locale locale, ToDoTask toDoTask) {
		return new ToDoDeleteCollectionConfirmationController(ureq, wControl, toDoTask);
	}

	@Override
	public void deleteToDoTaskSoftly(Identity doer, ToDoTask toDoTask) {
		getCollectionElementToDoTasks(toDoTask).forEach(toDoTaskElement -> {
			ToDoStatus previousStatus = toDoTaskElement.getStatus();
			toDoTaskElement.setStatus(ToDoStatus.deleted);
			toDoTaskElement.setContentModifiedDate(new Date());
			toDoService.update(doer, toDoTaskElement, previousStatus);
		});
		
		updateStatus(doer, toDoTask, ToDoStatus.deleted);
	}
	
	private ToDoTask getToDoTask(ToDoTaskRef toDoTaskRef, boolean active) {
		ToDoTask toDoTask = toDoService.getToDoTask(toDoTaskRef);
		if (toDoTask == null) {
			return null;
		}
		if (active && toDoTask.getStatus() == ToDoStatus.deleted) {
			return null;
		}
		return toDoTask;
	}
	
	private void updateStatus(Identity doer, ToDoTaskRef toDoTask, ToDoStatus status) {
		ToDoTask reloadedToDoTask = getToDoTask(toDoTask, true);
		if (reloadedToDoTask == null) {
			return;
		}
		ToDoStatus previousStatus = reloadedToDoTask.getStatus();
		if (previousStatus == status) {
			return;
		}
		
		reloadedToDoTask.setStatus(status);
		reloadedToDoTask.setContentModifiedDate(new Date());
		toDoService.update(doer, reloadedToDoTask, previousStatus);
	}

	public StepRunnerCallback createCreateCallback() {
		return (uureq, wControl, runContext) -> {
			ToDoTaskCollectionCreateContext context = (ToDoTaskCollectionCreateContext)runContext.get(ToDoTaskCollectionCreateContext.KEY);
			createToDoTaskCollection(uureq.getIdentity(), context);
			return StepsMainRunController.DONE_MODIFIED;
		};
	}
	
	private void createToDoTaskCollection(Identity doer, ToDoTaskCollectionCreateContext context) {
		RepositoryEntry repositoryEntry = context.getRepositoryEntry();
		ToDoStatus status = context.getStatus();
		List<String> tagDisplayNames = context.getTagDisplayNames();
		
		ToDoTask collection = createCollectionToDoTask(doer, repositoryEntry, status, tagDisplayNames, context);
		
		getToDoTaskCollectionAssignees(context).forEach(assignee -> {
			createCollectionElementToDoTask(doer, repositoryEntry, collection, status, tagDisplayNames, assignee);
		});
		
		if (context.getConvertFromKey() != null) {
			ToDoTask toDoTaskTemplate = getToDoTask(() -> context.getConvertFromKey(), false);
			toDoService.deleteToDoTaskPermanently(toDoTaskTemplate);
		}
	}

	private ToDoTask createCollectionToDoTask(Identity doer, RepositoryEntry repositoryEntry, ToDoStatus status,
			List<String> tagDisplayNames, ToDoTaskCollectionCreateContext context) {
		ToDoTask collection = toDoService.createToDoTask(doer, TYPE, repositoryEntry.getKey(), null,
				repositoryEntry.getDisplayname(), null, null);
		collection.setTitle(context.getTitle());
		collection.setDescription(context.getDescription());
		collection.setStatus(status);
		collection.setPriority(context.getPriority());
		collection.setExpenditureOfWork(context.getExpenditureOfWork());
		collection.setStartDate(context.getStartDate());
		collection.setDueDate(context.getDueDate());
		collection.setAssigneeRights(new ToDoRight[] {ToDoRight.all});
		collection = toDoService.update(doer, collection, status);
		if (tagDisplayNames != null && !tagDisplayNames.isEmpty()) {
			toDoService.updateTags(collection, tagDisplayNames);
		}
		return collection;
	}

	public void addRemainingAssignees(Identity doer, ToDoTask toDoTaskRef, boolean coachOnly) {
		ToDoTask toDoTaskCollection = toDoService.getToDoTask(toDoTaskRef);
		if (toDoTaskCollection == null || !TYPE.endsWith(toDoTaskCollection.getType()) || toDoTaskCollection.getStatus() == ToDoStatus.deleted) {
			return;
		}
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(toDoTaskCollection.getOriginId());
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setToDoTasks(List.of(toDoTaskCollection));
		List<String> tagDisplayNames = toDoService.getTagInfos(tagSearchParams, toDoTaskCollection).stream().map(TagInfo::getDisplayName).toList();
		
		List<Identity> participants = getAssigneeCandidates(doer, () -> toDoTaskCollection.getOriginId(), coachOnly);
		addRemainingAssignees(doer, toDoTaskCollection, repositoryEntry, tagDisplayNames, participants);
	}

	public void addRemainingAssignees(Identity doer, ToDoTask toDoTaskRef, Collection<Long> identityKeys) {
		ToDoTask toDoTaskCollection = toDoService.getToDoTask(toDoTaskRef);
		if (toDoTaskCollection == null || !TYPE.endsWith(toDoTaskCollection.getType()) || toDoTaskCollection.getStatus() == ToDoStatus.deleted) {
			return;
		}
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(toDoTaskCollection.getOriginId());
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setToDoTasks(List.of(toDoTaskCollection));
		List<String> tagDisplayNames = toDoService.getTagInfos(tagSearchParams, toDoTaskCollection).stream().map(TagInfo::getDisplayName).toList();
		
		List<Identity> participants = securityManager.loadIdentityByKeys(identityKeys);
		addRemainingAssignees(doer, toDoTaskCollection, repositoryEntry, tagDisplayNames, participants);
	}

	private void addRemainingAssignees(Identity doer, ToDoTask toDoTaskCollection, RepositoryEntry repositoryEntry,
			List<String> tagDisplayNames, List<Identity> participants) {
		Set<Long> toDoAssigneeKeys = getCollectionElementAssigneeKeys(toDoTaskCollection);
		participants.stream()
				.filter(participant -> !toDoAssigneeKeys.contains(participant.getKey()))
				.forEach(identity -> createCollectionElementToDoTask(doer, repositoryEntry, toDoTaskCollection,
						toDoTaskCollection.getStatus(), tagDisplayNames, identity));
	}

	public Set<Long> getCollectionElementAssigneeKeys(ToDoTask toDoTaskCollection) {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setCollectionKeys(List.of(toDoTaskCollection.getKey()));
		List<ToDoTask> elementToDoTasks = toDoService.getToDoTasks(searchParams);
		
		return toDoService.getToDoTaskGroupKeyToMembers(elementToDoTasks, List.of(ToDoRole.assignee)).values().stream()
				.map(ToDoTaskMembers::getMembers)
				.flatMap(Set::stream)
				.map(Identity::getKey)
				.collect(Collectors.toSet());
	}

	private void createCollectionElementToDoTask(Identity doer, RepositoryEntry repositoryEntry, ToDoTask collection,
			ToDoStatus status, List<String> tagDisplayNames, Identity assignee) {
		ToDoTask toDoTask = toDoService.createToDoTask(doer, CourseCollectionElementToDoTaskProvider.TYPE,
				repositoryEntry.getKey(), null, repositoryEntry.getDisplayname(), null, collection);
		mapToDoTask(toDoTask, collection);
		toDoTask = toDoService.update(doer, toDoTask, status);
		toDoService.updateMember(doer, toDoTask, List.of(assignee), List.of());
		if (tagDisplayNames != null && !tagDisplayNames.isEmpty()) {
			toDoService.updateTags(toDoTask, tagDisplayNames);
		}
	}
	
	private void mapToDoTask(ToDoTask toDoTask, ToDoTask collection) {
		toDoTask.setTitle(collection.getTitle());
		toDoTask.setDescription(collection.getDescription());
		toDoTask.setStatus(collection.getStatus());
		toDoTask.setPriority(collection.getPriority());
		toDoTask.setExpenditureOfWork(collection.getExpenditureOfWork());
		toDoTask.setStartDate(collection.getStartDate());
		toDoTask.setDueDate(collection.getDueDate());
		toDoTask.setAssigneeRights(CourseCollectionElementToDoTaskProvider.ASSIGNEE_RIGHTS);
	}

	public List<Identity> getToDoTaskCollectionAssignees(ToDoTaskCollectionCreateContext context) {
		return context.isAssigneesSelected()
				? securityManager.loadIdentityByKeys(context.getAssigneeKeys())
				: getAssigneeCandidates(context.getDoer(), context.getRepositoryEntry(), context.isCoach());
	}

	public List<Identity> getAssigneeCandidates(Identity doer, RepositoryEntryRef repositoryEntry, boolean coachOnly) {
		return coachOnly
				? repositoryService.getCoachedParticipants(doer, repositoryEntry)
				: repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.all, GroupRoles.participant.name());
	}
	
	public StepRunnerCallback createEditCallback() {
		return (uureq, wControl, runContext) -> {
			ToDoTaskCollectionEditContext context = (ToDoTaskCollectionEditContext)runContext.get(ToDoTaskCollectionEditContext.KEY);
			updateToDoTaskCollection(uureq.getIdentity(), context.getToDoTaskCollection(), context);
			return StepsMainRunController.DONE_MODIFIED;
		};
	}

	private void updateToDoTaskCollection(Identity doer, ToDoTask toDoTaskRef, ToDoTaskCollectionEditContext context) {
		ToDoTask toDoTask = getToDoTask(toDoTaskRef, true);
		if (toDoTask == null) {
			return;
		}
		
		PersistUpdateToDoTaskStrategie strategie = new PersistUpdateToDoTaskStrategie(doer);
		updateToDoTaskCollection(context, strategie);
		
		// Update the collection as well
		List<String> collectionTagDisplayNames = getTagDisplayNames(toDoTaskRef);
		updateToDoTask(toDoTask, toDoTask, collectionTagDisplayNames, collectionTagDisplayNames, null, context, new PersistUpdateToDoTaskStrategie(doer));
	}

	public void updateToDoTaskCollection(ToDoTaskCollectionEditContext context, UpdateToDoTaskStrategie strategie) {
		List<ToDoTask> toDoTaskElements = getCollectionElementToDoTasks(context.getToDoTaskCollection());
		Map<Long, ToDoTaskMembers> toDoTaskElementGroupKeyToMembers = toDoService.getToDoTaskGroupKeyToMembers(toDoTaskElements, List.of(ToDoRole.assignee));
		
		List<String> toDoTaskCollectionTagDisplayNames = getTagDisplayNames(context.getToDoTaskCollection());
		
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setToDoTasks(toDoTaskElements);
		Map<Long, List<String>> toDoTaskKeyToTagDisplayNames = toDoService.getToDoTaskTags(tagSearchParams).stream()
				.collect(Collectors.groupingBy(
						tt -> tt.getToDoTask().getKey(),
						Collectors.collectingAndThen(
								Collectors.toList(),
								toDoTaskTag -> toDoTaskTag.stream()
										.map(tt -> tt.getTag().getDisplayName())
										.distinct()
										.collect(Collectors.toList()))));
		
		toDoTaskElements.forEach(toDoTaskElement -> updateToDoTask(
				toDoTaskElement, 
				context.getToDoTaskCollection(),
				toDoTaskKeyToTagDisplayNames.getOrDefault(toDoTaskElement.getKey(), List.of()),
				toDoTaskCollectionTagDisplayNames,
				toDoTaskElementGroupKeyToMembers.get(toDoTaskElement.getBaseGroup().getKey()),
				context,
				strategie));
	}

	private void updateToDoTask(ToDoTask toDoTaskElement, ToDoTask toDoTaskCollection,
			List<String> toDoTaskElementTagDisplayNames, List<String> toDoTaskCollectionTagDisplayNames,
			ToDoTaskMembers toDoTaskElementkMembers, ToDoTaskCollectionEditContext context,
			UpdateToDoTaskStrategie strategie) {
		strategie.init(toDoTaskElement, toDoTaskElementkMembers);
		
		if (context.isSelected(Field.title)) {
			if (context.isOverride(Field.title) || Objects.equals(toDoTaskElement.getTitle(), toDoTaskCollection.getTitle())) {
				if (!Objects.equals(toDoTaskElement.getTitle(), context.getTitle())) {
					strategie.updateTitle(context.getTitle());
				}
			}
		}
		
		if (context.isSelected(Field.description)) {
			if (context.isOverride(Field.description) || Objects.equals(toDoTaskElement.getDescription(), toDoTaskCollection.getDescription())) {
				if (!Objects.equals(toDoTaskElement.getDescription(), context.getDescription())) {
					strategie.updateDescription(context.getDescription());
				}
			}
		}
		
		if (context.isSelected(Field.status)) {
			if (context.isOverride(Field.status) || Objects.equals(toDoTaskElement.getStatus(), toDoTaskCollection.getStatus())) {
				if (!Objects.equals(toDoTaskElement.getStatus(), context.getStatus())) {
					strategie.updateStatus(context.getStatus());
				}
			}
		}
		
		if (context.isSelected(Field.priority)) {
			if (context.isOverride(Field.priority) || Objects.equals(toDoTaskElement.getPriority(), toDoTaskCollection.getPriority())) {
				if (!Objects.equals(toDoTaskElement.getPriority(), context.getPriority())) {
					strategie.updatePriority(context.getPriority());
				}
			}
		}
		
		if (context.isSelected(Field.startDate)) {
			if (context.isOverride(Field.startDate) || Objects.equals(toDoTaskElement.getStartDate(), toDoTaskCollection.getStartDate())) {
				if (!Objects.equals(toDoTaskElement.getStartDate(), context.getStartDate())) {
					strategie.updateStartDate(context.getStartDate());
				}
			}
		}
		
		if (context.isSelected(Field.dueDate)) {
			if (context.isOverride(Field.dueDate) || Objects.equals(toDoTaskElement.getDueDate(), toDoTaskCollection.getDueDate())) {
				if (!Objects.equals(toDoTaskElement.getDueDate(), context.getDueDate())) {
					strategie.updateDueDate(context.getDueDate());
				}
			}
		}
		
		if (context.isSelected(Field.expenditureOfWork)) {
			if (context.isOverride(Field.expenditureOfWork) || Objects.equals(toDoTaskElement.getExpenditureOfWork(), toDoTaskCollection.getExpenditureOfWork())) {
				if (!Objects.equals(toDoTaskElement.getExpenditureOfWork(), context.getExpenditureOfWork())) {
					strategie.updateExpenditureOfWork(context.getExpenditureOfWork());
				}
			}
		}
		
		if (context.isSelected(Field.tagDisplayNames)) {
			// Compare if element and collection have the same tags
			List<String> tagDisplayNameAdded = new ArrayList<>(toDoTaskCollectionTagDisplayNames);
			tagDisplayNameAdded.removeAll(toDoTaskElementTagDisplayNames);
			List<String> tagDisplayNameRemoved = new ArrayList<>(toDoTaskElementTagDisplayNames);
			tagDisplayNameRemoved.removeAll(toDoTaskCollectionTagDisplayNames);
			
			if (context.isOverride(Field.tagDisplayNames) || (tagDisplayNameAdded.isEmpty() && tagDisplayNameRemoved.isEmpty())) {
				// Compare if element and context have the same tags
				tagDisplayNameAdded = new ArrayList<>(context.getTagDisplayNames());
				tagDisplayNameAdded.removeAll(toDoTaskElementTagDisplayNames);
				tagDisplayNameRemoved = new ArrayList<>(toDoTaskElementTagDisplayNames);
				tagDisplayNameRemoved.removeAll(context.getTagDisplayNames());
				if (!tagDisplayNameAdded.isEmpty() || !tagDisplayNameRemoved.isEmpty()) {
					strategie.updateTagDisplayNames(context.getTagDisplayNames(), tagDisplayNameAdded, tagDisplayNameRemoved);
				}
			}
		}
		
		strategie.update();
	}

	private List<ToDoTask> getCollectionElementToDoTasks(ToDoTask toDoTask) {
		ToDoTaskSearchParams searchParams = new ToDoTaskSearchParams();
		searchParams.setStatus(ToDoStatus.OPEN_TO_DONE);
		searchParams.setCollections(List.of(toDoTask));
		return toDoService.getToDoTasks(searchParams);
	}
	
	private List<String> getTagDisplayNames(ToDoTask toDoTaskRef) {
		ToDoTaskSearchParams tagSearchParams = new ToDoTaskSearchParams();
		tagSearchParams.setToDoTasks(List.of(toDoTaskRef));
		return toDoService.getToDoTaskTags(tagSearchParams).stream()
				.map(tt -> tt.getTag().getDisplayName())
				.toList();
	}
	
	public static interface UpdateToDoTaskStrategie {
		
		void init(ToDoTask toDoTask, ToDoTaskMembers members);

		void updateTitle(String title);
		
		void updateDescription(String description);

		void updateStatus(ToDoStatus status);

		void updatePriority(ToDoPriority priority);

		void updateExpenditureOfWork(Long expenditureOfWork);

		void updateStartDate(Date startDate);

		void updateDueDate(Date dueDate);

		void updateTagDisplayNames(List<String> tagDisplayNames, List<String> tagDisplayNameAdded, List<String> tagDisplayNameRemoved);

		void update();
		
	}
	
	private class PersistUpdateToDoTaskStrategie implements UpdateToDoTaskStrategie {
		
		private final Identity doer;
		private ToDoTask toDoTask;
		private ToDoStatus previousStatus;
		private boolean contentChanged;

		public PersistUpdateToDoTaskStrategie(Identity doer) {
			this.doer = doer;
		}

		@Override
		public void init(ToDoTask toDoTask, ToDoTaskMembers members) {
			this.toDoTask = toDoTask;
			this.previousStatus = toDoTask.getStatus();
			this.contentChanged = false;
		}

		@Override
		public void updateTitle(String title) {
			toDoTask.setTitle(title);
			contentChanged = true;
		}

		@Override
		public void updateDescription(String description) {
			toDoTask.setDescription(description);
			contentChanged = true;
		}

		@Override
		public void updateStatus(ToDoStatus status) {
			toDoTask.setStatus(status);
			contentChanged = true;
		}

		@Override
		public void updatePriority(ToDoPriority priority) {
			toDoTask.setPriority(priority);
			contentChanged = true;
		}

		@Override
		public void updateExpenditureOfWork(Long expenditureOfWork) {
			toDoTask.setExpenditureOfWork(expenditureOfWork);
			contentChanged = true;
		}

		@Override
		public void updateStartDate(Date startDate) {
			toDoTask.setStartDate(startDate);
			contentChanged = true;
		}

		@Override
		public void updateDueDate(Date dueDate) {
			toDoTask.setDueDate(dueDate);
			contentChanged = true;
		}

		@Override
		public void updateTagDisplayNames(List<String> tagDisplayNames, List<String> tagDisplayNameAdded, List<String> tagDisplayNameRemoved) {
			toDoService.updateTags(toDoTask, tagDisplayNames);
		}

		@Override
		public void update() {
			if (contentChanged) {
				toDoTask.setContentModifiedDate(new Date());
				toDoService.update(doer, toDoTask, previousStatus);
			}
		}
		
	}

}
