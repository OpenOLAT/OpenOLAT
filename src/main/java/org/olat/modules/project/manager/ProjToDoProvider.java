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
package org.olat.modules.project.manager;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjToDo;
import org.olat.modules.project.ProjectModule;
import org.olat.modules.project.ProjectService;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.project.ui.ProjConfirmationController;
import org.olat.modules.project.ui.ProjToDoDetailController;
import org.olat.modules.project.ui.ProjToDoEditController;
import org.olat.modules.project.ui.ProjectBCFactory;
import org.olat.modules.project.ui.ProjectUIFactory;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ProjToDoProvider implements ToDoProvider {

	public static final String TYPE = "project";
	
	@Autowired
	private ProjectModule projectModule;
	@Autowired
	private ProjectService projectService;

	@Override
	public String getType() {
		return TYPE;
	}
	@Override
	public boolean isEnabled() {
		return projectModule.isEnabled();
	}

	@Override
	public String getBusinessPath(ToDoTask toDoTask) {
		ProjToDo toDo = projectService.getToDo(toDoTask.getOriginSubPath());
		if (toDo == null) {
			return null;
		}
		return ProjectBCFactory.getBusinessPath(toDo.getArtefact().getProject(), ProjToDo.TYPE, toDo.getKey());
	}

	@Override
	public String getDisplayName(Locale locale) {
		return Util.createPackageTranslator(ProjectUIFactory.class, locale).translate("todo.type");
	}

	@Override
	public void upateStatus(Identity doer, ToDoTaskRef toDoTask, Long originId, String originSubPath, ToDoStatus status) {
		projectService.updateToDoStatus(doer, originSubPath, status);
	}

	@Override
	public Controller createCreateController(UserRequest ureq, WindowControl wControl, Identity doer, Long originId, String originSubPath) {
		ProjProject project = projectService.getProject(() -> originId);
		if (project == null || project.getStatus() == ProjectStatus.deleted) {
			return null;
		}
		
		return new ProjToDoEditController(ureq, wControl, project, false);
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask, boolean showContext) {
		ProjToDo toDo = projectService.getToDo(toDoTask.getOriginSubPath());
		return new ProjToDoEditController(ureq, wControl, toDo, false, showContext);
	}

	@Override
	public FormBasicController createDetailController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskSecurityCallback secCallback, ToDoTask toDoTask, List<Tag> tags, Identity modifier,
			Set<Identity> assignees, Set<Identity> delegatees) {
		return new ProjToDoDetailController(ureq, wControl, mainForm, secCallback, toDoTask, tags, modifier, assignees, delegatees);
	}

	@Override
	public void deleteToDoTaskSoftly(Identity doer, ToDoTask toDoTask) {
		ProjToDo toDo = projectService.getToDo(toDoTask.getOriginSubPath());
		projectService.deleteToDoSoftly(doer, toDo);
	}

	@Override
	public Controller createDeleteConfirmationController(UserRequest ureq, WindowControl wControl, Locale locale, ToDoTask toDoTask) {
		Translator translator = Util.createPackageTranslator(ProjectUIFactory.class, locale);
		String message = translator.translate("todo.delete.confirmation.message", StringHelper.escapeHtml(ToDoUIFactory.getDisplayName(translator, toDoTask)));
		return new ProjConfirmationController(ureq, wControl, message, "todo.delete.confirmation.confirm", "todo.delete.confirmation.button", true);
	}
	
}
