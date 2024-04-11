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
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.todo.CourseToDoContextFilter;
import org.olat.course.todo.CourseToDoService;
import org.olat.course.todo.ui.CourseToDoUIFactory;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.olat.modules.todo.ui.ToDoTaskEditController;
import org.olat.modules.todo.ui.ToDoTaskEditForm.MemberSelection;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.olat.repository.RepositoryEntry;
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
public class CourseIndividualToDoTaskProvider implements ToDoProvider {

	public static final String TYPE = "course.todo.individual";
	private static final ToDoRight[] ASSIGNEE_RIGHTS = new ToDoRight[] {ToDoRight.status};

	@Autowired
	private CourseToDoService courseToDoService;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private CourseToDoContextFilter contextFilter;
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
		return Util.createPackageTranslator(CourseToDoUIFactory.class, locale).translate("course.todo.provider.individual.name");
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
	public void upateStatus(Identity doer, ToDoTaskRef toDoTask, Long originId, String originSubPath,
			ToDoStatus status) {
		updateStatus(doer, toDoTask, status);
	}

	@Override
	public Controller createCreateController(UserRequest ureq, WindowControl wControl, Identity doer, Long originId,
			String originSubPath) {
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(originId);
		ToDoContext context = ToDoContext.of(TYPE, repositoryEntry.getKey(), repositoryEntry.getDisplayname());
		return createEditController(ureq, wControl, null, null, true, repositoryEntry, context, MemberSelection.candidatesSingle);
	}
	
	@Override
	public boolean isCopyable() {
		return true;
	}
	
	@Override
	public boolean isRestorable() {
		return true;
	}

	@Override
	public Controller createCopyController(UserRequest ureq, WindowControl wControl, Identity doer,
			ToDoTask sourceToDoTask, boolean showContext) {
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(sourceToDoTask.getOriginId());
		return createEditController(ureq, wControl, null, sourceToDoTask, true, repositoryEntry, sourceToDoTask,
				MemberSelection.candidatesSingle);
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			boolean showContext, boolean showSingleAssignee) {
		RepositoryEntry repositoryEntry = repositoryService.loadByKey(toDoTask.getOriginId());
		return createEditController(ureq, wControl, toDoTask, null, showContext, repositoryEntry, toDoTask,
				showSingleAssignee ? MemberSelection.readOnly : MemberSelection.disabled);
	}
	
	/*
	 * The to-do task can be assigned to one participant. Why? If a to-do is
	 * assigned to multiple participants, we cannot handle the removal of the
	 * membership cleanly. now the context is marked as deleted when a participant
	 * is removed. This does not work if one of several participants of a to-do is
	 * removed from the course. This assignment can't be changed anymore. Only the
	 * assignment of a to-do to participants but not to supervisors and owners is
	 * implemented, as the to-dos are displayed in the "my course" menu.
	 */
	private Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			ToDoTask toDoTaskCopySource, boolean showContext, RepositoryEntry repositoryEntry, ToDoContext context,
			MemberSelection assigneeSelection) {
		Collection<Identity> assigneeCandidates = List.of();
		if (MemberSelection.readOnly != assigneeSelection) {
			RepositoryEntrySecurity reSecurity = repositoryManager.isAllowed(ureq, repositoryEntry);
			assigneeCandidates = reSecurity.isEntryAdmin()
					? repositoryService.getMembers(repositoryEntry, RepositoryEntryRelationType.all, GroupRoles.participant.name())
					: repositoryService.getCoachedParticipants(ureq.getIdentity(), repositoryEntry);
		}
		return new ToDoTaskEditController(ureq, wControl, toDoTask, toDoTaskCopySource, showContext, List.of(context),
				context, courseToDoService.createCourseTagSearchParams(repositoryEntry), ASSIGNEE_RIGHTS,
				assigneeSelection, assigneeCandidates, List.of(), MemberSelection.disabled, List.of());
	}

	@Override
	public FormBasicController createDetailController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskSecurityCallback secCallback, ToDoTask toDoTask, List<Tag> tags, Identity creator,
			Identity modifier, Set<Identity> assignees, Set<Identity> delegatees) {
		return new ToDoTaskDetailsController(ureq, wControl, mainForm, secCallback, toDoTask, tags, creator, modifier, assignees, delegatees);
	}

	@Override
	public void deleteToDoTaskSoftly(Identity doer, ToDoTask toDoTask) {
		updateStatus(doer, toDoTask, ToDoStatus.deleted);
	}

	@Override
	public Controller createDeleteConfirmationController(UserRequest ureq, WindowControl wControl, Locale locale,
			ToDoTask toDoTask) {
		Translator translator = Util.createPackageTranslator(ToDoUIFactory.class, locale);
		return new ConfirmationController(ureq, wControl,
				translator.translate("task.delete.conformation.message", StringHelper.escapeHtml(ToDoUIFactory.getDisplayName(translator, toDoTask))),
				translator.translate("task.delete.confirmation.confirm"),
				translator.translate("delete"), true);
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
		ToDoTask reloadedToDoTask = getToDoTask(toDoTask, false);
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

}
