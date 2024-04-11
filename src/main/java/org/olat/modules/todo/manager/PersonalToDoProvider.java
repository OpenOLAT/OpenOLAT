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

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
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
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Buddy;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoContextFilter;
import org.olat.modules.todo.ToDoModule;
import org.olat.modules.todo.ToDoProvider;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoTaskDetailsController;
import org.olat.modules.todo.ui.ToDoTaskEditController;
import org.olat.modules.todo.ui.ToDoTaskEditForm.MemberSelection;
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
public class PersonalToDoProvider implements ToDoProvider, ToDoContextFilter {
	
	public static final String TYPE = "personal";
	private static final List<ToDoContext> CONTEXTS = List.of(ToDoContext.of(TYPE));
	private static final ToDoRight[] ASSIGNEE_RIGHTS = new ToDoRight[] {ToDoRight.all};
	
	@Autowired
	private ToDoModule toDoModule;
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private InstantMessagingService instantMessagingService;
	@Autowired
	private BaseSecurity securityManager;

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
	public String getContextFilterType() {
		return TYPE;
	}
	
	@Override
	public int getFilterSortOrder() {
		return 100;
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
		return createEditController(ureq, wControl, null, null, true);
	}
	
	@Override
	public boolean isCopyable() {
		return true;
	}
	
	@Override
	public boolean isRestorable() {
		return false;
	}

	@Override
	public Controller createCopyController(UserRequest ureq, WindowControl wControl, Identity doer,
			ToDoTask sourceToDoTask, boolean showContext) {
		return createEditController(ureq, wControl, null, sourceToDoTask, true);
	}

	@Override
	public Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			boolean showContext, boolean showSingleAssignee) {
		return createEditController(ureq, wControl, toDoTask, null, showContext);
	}

	private Controller createEditController(UserRequest ureq, WindowControl wControl, ToDoTask toDoTask,
			ToDoTask sourceToDoTask, boolean showContext) {
		ToDoTaskSearchParams tagInfoSearchParams = new ToDoTaskSearchParams();
		tagInfoSearchParams.setAssigneeOrDelegatee(ureq.getIdentity());
		
		MemberSelection assigneeSelection = getMemberSelection(toDoModule.getPersonalAssigneeCandidate());
		MemberSelection delegateeSelection = getMemberSelection(toDoModule.getPersonalDelegateeCandidate());
		Collection<Identity> assigneeCandidates;
		Collection<Identity> delegateeCandidates;
		if (MemberSelection.candidates == assigneeSelection) {
			assigneeCandidates = getMemberCandidates(ureq.getIdentity());
		} else {
			assigneeCandidates = List.of(ureq.getIdentity());
		}
		if (MemberSelection.candidates == assigneeSelection) {
			if (MemberSelection.candidates == assigneeSelection) {
				// load only once
				delegateeCandidates = assigneeCandidates;
			} else {
				delegateeCandidates = getMemberCandidates(ureq.getIdentity());
			}
		} else {
			delegateeCandidates = List.of(ureq.getIdentity());
		}
		
		return new ToDoTaskEditController(ureq, wControl, toDoTask, sourceToDoTask, showContext, CONTEXTS,
				CONTEXTS.get(0), tagInfoSearchParams, ASSIGNEE_RIGHTS, assigneeSelection, assigneeCandidates,
				List.of(ureq.getIdentity()), delegateeSelection, delegateeCandidates);
	}
	
	private MemberSelection getMemberSelection(String config) {
		return switch (config) {
		case ToDoModule.PERSONAL_CANDIDATE_NONE -> MemberSelection.disabled;
		case ToDoModule.PERSONAL_CANDIDATE_BUDDIES -> MemberSelection.candidates;
		default -> MemberSelection.search;
		};
	}
	
	private Set<Identity> getMemberCandidates(Identity identity) {
		Set<Long> buddyIdentityKeys = instantMessagingService.getBuddyGroups(identity, true).stream()
				.flatMap(buddyGroup -> buddyGroup.getBuddy().stream())
				.filter(buddy -> !buddy.isAnonym())
				.map(Buddy::getIdentityKey)
				.collect(Collectors.toSet());
		Set<Identity> buddyIdentities = new HashSet<>(securityManager.loadIdentityByKeys(buddyIdentityKeys));
		buddyIdentities.add(identity);
		return buddyIdentities;
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
