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
import org.olat.core.id.Identity;
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
import org.olat.modules.todo.ui.ToDoDeleteConfirmationController;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.olat.modules.todo.ui.ToDoTaskEditController;
import org.olat.modules.todo.ui.ToDoTaskEditForm.MemberSelection;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRelationType;
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
public class CourseCollectionElementToDoTaskProvider implements ToDoProvider {

	public static final String TYPE = "course.todo.collection.element";
	static final ToDoRight[] ASSIGNEE_RIGHTS = new ToDoRight[] {ToDoRight.status};

	@Autowired
	private CourseToDoService courseToDoService;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private CourseToDoContextFilter contextFilter;
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
		return Util.createPackageTranslator(CourseToDoUIFactory.class, locale).translate("course.todo.provider.collection.element.name");
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
		return null;
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask, boolean showContext, boolean showSingleAssignee) {
		return createEditController(ureq, wControl, toDoTask, showContext, () -> toDoTask.getOriginId(), toDoTask, showSingleAssignee);
	}
	
	private Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			boolean showContext, RepositoryEntryRef repositoryEntry, ToDoContext context, boolean showSingleAssignee) {
		Collection<Identity> assigneeCandidates = repositoryService.getMembers(repositoryEntry,
				RepositoryEntryRelationType.all, GroupRoles.participant.name());
		return new ToDoTaskEditController(ureq, wControl, toDoTask, showContext, List.of(context), context,
				courseToDoService.createCourseTagSearchParams(repositoryEntry), ASSIGNEE_RIGHTS,
				showSingleAssignee ? MemberSelection.readOnly : MemberSelection.disabled, 
				assigneeCandidates, List.of(), MemberSelection.disabled, List.of());
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
		return new ToDoDeleteConfirmationController(ureq, wControl, toDoTask);
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

}
