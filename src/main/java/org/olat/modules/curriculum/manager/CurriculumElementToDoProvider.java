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
package org.olat.modules.curriculum.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.curriculum.ui.CurriculumElementContextPicker;
import org.olat.modules.curriculum.ui.CurriculumElementToDoMemberProvider;
import org.olat.modules.curriculum.ui.CurriculumUIFactory;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoContextFilter;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskContextConfig;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.olat.modules.todo.ui.ToDoTaskEditController;
import org.olat.modules.todo.ui.ToDoTaskMemberConfig;
import org.olat.modules.todo.ui.ToDoTaskMemberSelection;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * Initial date: 29 Apr 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Service
public class CurriculumElementToDoProvider implements ToDoProvider, ToDoContextFilter {

	public static final String TYPE = "curriculum.element";
	private static final ToDoRight[] ASSIGNEE_RIGHTS = new ToDoRight[] {ToDoRight.status};

	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private ToDoService toDoService;

	@Override
	public String getType() {
		return TYPE;
	}

	@Override
	public boolean isEnabled() {
		return curriculumModule.isEnabled();
	}

	@Override
	public String getBusinessPath(ToDoTask toDoTask) {
		if (toDoTask.getOriginId() == null || toDoTask.getOriginSubPath() == null) {
			return null;
		}
		Long elementKey = Long.valueOf(toDoTask.getOriginSubPath());
		return "[CurriculumAdmin:0][Curriculum:" + toDoTask.getOriginId() + "][CurriculumElement:" + elementKey + "][ToDos:0]";
	}

	@Override
	public String getDisplayName(Locale locale) {
		return Util.createPackageTranslator(CurriculumUIFactory.class, locale).translate("curriculum.element.todo.provider.name");
	}

	@Override
	public String getContextFilterType() {
		return TYPE;
	}

	@Override
	public int getFilterSortOrder() {
		return 150;
	}

	@Override
	public String getModifiedBy(Locale locale, ToDoTask toDoTask) {
		return null;
	}

	@Override
	public void upateStatus(Identity doer, ToDoTaskRef toDoTask, Long originId, String originSubPath, ToDoStatus status) {
		updateStatus(doer, toDoTask, status);
	}

	@Override
	public Controller createCreateController(UserRequest ureq, WindowControl wControl, Identity doer, Long originId,
			String originSubPath) {
		CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(originId));
		ToDoContext context = ToDoContext.of(CurriculumElementToDoProvider.TYPE, element.getCurriculum().getKey(),
				element.getKey().toString(), element.getCurriculum().getDisplayName(), element.getDisplayName());
		return createEditController(ureq, wControl, null, element, context);
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
		return null;
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			boolean showContext, boolean showSingleAssignee) {
		CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(Long.valueOf(toDoTask.getOriginSubPath())));
		return createEditController(ureq, wControl, toDoTask, element, toDoTask);
	}

	private Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			CurriculumElement element, ToDoContext context) {
		Set<Identity> candidates = getCandidates(element).stream()
				.map(CurriculumMember::getIdentity)
				.collect(Collectors.toSet());
		CurriculumElement implementation = curriculumService.getImplementationOf(element);
		CurriculumElement effectiveRoot = implementation != null ? implementation : element;
		CurriculumElementType implementationType = effectiveRoot.getType();
		boolean isStructuredProduct = implementationType == null || !implementationType.isSingleElement();
		ToDoTaskContextConfig contextConfig = isStructuredProduct
				? ToDoTaskContextConfig.picker(new CurriculumElementContextPicker(effectiveRoot.getKey(), element.getKey()), context)
				: ToDoTaskContextConfig.off(context);
		CurriculumElementToDoMemberProvider memberSearchProvider = new CurriculumElementToDoMemberProvider(element);
		return new ToDoTaskEditController(ureq, wControl, toDoTask, null,
				contextConfig,
				ToDoTaskMemberConfig.search(candidates, memberSearchProvider).notMandatory(),
				ToDoTaskMemberConfig.search(candidates, memberSearchProvider),
				ToDoTaskMemberSelection.empty(),
				createTagSearchParams(element), ASSIGNEE_RIGHTS);
	}

	public List<CurriculumMember> getCandidates(CurriculumElement element) {
		SearchMemberParameters elementParams = new SearchMemberParameters(element);
		elementParams.setRoles(List.of(CurriculumRoles.curriculumelementowner, CurriculumRoles.owner));
		List<CurriculumMember> elementMembers = curriculumService.getCurriculumElementsMembers(elementParams);

		SearchMemberParameters curriculumParams = new SearchMemberParameters(element.getCurriculum());
		curriculumParams.setRoles(List.of(CurriculumRoles.curriculummanager, CurriculumRoles.curriculumowner));
		List<CurriculumMember> curriculumMembers = curriculumService.getCurriculumMembers(curriculumParams);

		List<CurriculumMember> members = new ArrayList<>(elementMembers.size() + curriculumMembers.size());
		members.addAll(elementMembers);
		members.addAll(curriculumMembers);
		return members;
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
				translator.translate("delete"), ButtonType.danger);
	}

	private ToDoTaskSearchParams createTagSearchParams(CurriculumElement element) {
		ToDoTaskSearchParams params = new ToDoTaskSearchParams();
		params.setTypes(List.of(TYPE));
		params.setOriginIds(List.of(element.getCurriculum().getKey()));
		params.setOriginSubPaths(List.of(element.getKey().toString()));
		return params;
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
