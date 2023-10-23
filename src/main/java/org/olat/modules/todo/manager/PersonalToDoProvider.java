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
package org.olat.modules.todo.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoDeleteConfirmationController;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.olat.modules.todo.ui.ToDoTaskEditController;
import org.olat.modules.todo.ui.ToDoTaskListController;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 4 Oct 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class PersonalToDoProvider implements ToDoProvider {
	
	public static final String TYPE = "personal";
	private static final List<ToDoContext> CONTEXTS = List.of(ToDoContext.of(TYPE, null, null, null));
	
	@Autowired
	private ToDoService toDoService;

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
		return "[HomeSite:0][ToDos:0][" + ToDoTaskListController.TYPE_TODO +":"  + toDoTask.getKey() + "]";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return Util.createPackageTranslator(ToDoUIFactory.class, locale).translate("personal.type");
	}
	
	@Override
	public int getFilterSortOrder() {
		return 100;
	}

	@Override
	public void upateStatus(Identity doer, ToDoTaskRef toDoTask, Long originId, String originSubPath,
			ToDoStatus status) {
		updateStatus(doer, toDoTask, status);
	}

	@Override
	public Controller createCreateController(UserRequest ureq, WindowControl wControl, Identity doer, Long originId,
			String originSubPath) {
		return new ToDoTaskEditController(ureq, wControl, null, true, CONTEXTS, CONTEXTS.get(0));
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask, boolean showContext) {
		return new ToDoTaskEditController(ureq, wControl, toDoTask, showContext, CONTEXTS, CONTEXTS.get(0));
	}

	@Override
	public FormBasicController createDetailController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskSecurityCallback secCallback, ToDoTask toDoTask, List<Tag> tags, Identity modifier,
			Set<Identity> assignees, Set<Identity> delegatees) {
		return new ToDoTaskDetailsController(ureq, wControl, mainForm, secCallback, toDoTask, tags, modifier, assignees, delegatees);
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
