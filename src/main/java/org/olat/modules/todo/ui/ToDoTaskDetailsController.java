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
package org.olat.modules.todo.ui;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.tag.Tag;
import org.olat.core.commons.services.tag.ui.TagUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoUIFactory.Due;
import org.olat.user.PortraitUser;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoProfileController;
import org.olat.user.UserManager;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskDetailsController extends FormBasicController {
	
	private FormLink startLink;
	private FormLink doneLink;
	private FormLink editLink;
	
	private final ToDoTaskSecurityCallback secCallback;
	private final ToDoTask toDoTask;
	private final List<Tag> tags;
	private final Identity creator;
	private final Identity modifier;
	private final Set<Identity> assignees;
	private final Set<Identity> delegatees;

	@Autowired
	private ToDoService toDoService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserPortraitService userPortraitService;

	public ToDoTaskDetailsController(UserRequest ureq, WindowControl wControl, Form mainForm, ToDoTaskSecurityCallback secCallback,
			ToDoTask toDoTask, List<Tag> tags, Identity creator, Identity modifier, Set<Identity> assignees, Set<Identity> delegatees) {
		super(ureq, wControl, LAYOUT_CUSTOM, "todo_task_details", mainForm);
		this.secCallback = secCallback;
		this.toDoTask = toDoTask;
		this.tags = tags;
		this.creator = creator;
		this.modifier = modifier;
		this.assignees = assignees;
		this.delegatees = delegatees;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("title", ToDoUIFactory.getDisplayName(getTranslator(), toDoTask));
		flc.contextPut("description", ToDoUIFactory.getDetailsDescription(toDoTask));
		flc.contextPut("statusLabel", new ToDoTaskStatusRenderer(getTranslator(), false).render(getTranslator(), toDoTask.getStatus()));
		flc.contextPut("priorityLabel", new ToDoTaskPriorityRenderer(getTranslator(), false).render(getTranslator(), toDoTask.getPriority()));
		
		String modifiedDate = Formatter.getInstance(getLocale()).formatDateRelative(toDoTask.getContentModifiedDate());
		String modifiedBy;
		if (modifier != null) {
			modifiedBy = StringHelper.escapeHtml(userManager.getUserDisplayName(modifier.getKey()));
		} else {
			modifiedBy = toDoService.getProvider(toDoTask.getType()).getModifiedBy(getLocale(), toDoTask);
		}
		String modified = translate("date.by", modifiedDate, modifiedBy);
		flc.contextPut("modified", translate("last.updated", modified));
		
		UserInfoProfileConfig profileConfig = userPortraitService.createProfileConfig();
		List<String> assigneeProfiles = forgeProfiles(ureq, profileConfig, assignees, "assignee");
		flc.contextPut("assigneeProfiles", assigneeProfiles);
		List<String> delegateeProfiles = forgeProfiles(ureq, profileConfig, delegatees, "delegatee");
		flc.contextPut("delegateeProfiles", delegateeProfiles);
		int memberColCount = (assigneeProfiles.isEmpty() ? 0 : 1) + (delegateeProfiles.isEmpty() ? 0 : 1);
		flc.contextPut("memberColCount", Integer.valueOf(memberColCount));
		
		boolean hasStartDate = toDoTask.getStartDate() != null;
		boolean hasDueDate = toDoTask.getDueDate() != null;
		if (hasStartDate) {
			flc.contextPut("startDate", ToDoUIFactory.getDateOrAnytime(getTranslator(), toDoTask.getStartDate()));
		}
		if (hasDueDate) {
			flc.contextPut("dueDate", ToDoUIFactory.getDateOrAnytime(getTranslator(), toDoTask.getDueDate()));
			Due due = ToDoUIFactory.getDue(getTranslator(), DateUtils.toLocalDate(toDoTask.getDueDate()), LocalDate.now(), toDoTask.getStatus());
			String dueName = ToDoStatus.STATUS_OVERDUE.contains(toDoTask.getStatus()) ? due.name() : translate("task.success");
			flc.contextPut("due", dueName);
		}
		ToDoExpenditureOfWork expenditureOfWork = toDoService.getExpenditureOfWork(toDoTask.getExpenditureOfWork());
		if (expenditureOfWork != null) {
			flc.contextPut("expenditureOfWork", ToDoUIFactory.formatLong(getTranslator(), expenditureOfWork));
		}
		int dateColCount = (hasStartDate ? 1 : 0) + (hasDueDate ? 2 : 0) + (expenditureOfWork != null ? 1 : 0);
		flc.contextPut("dateColCount", Integer.valueOf(dateColCount));
		boolean showDateRange = hasStartDate && hasDueDate;
		flc.contextPut("showDateRange", Boolean.valueOf(showDateRange));

		if (showDateRange) {
			ProgressBar progressBar = new ProgressBar("date.progress");
			flc.put("date.progress", progressBar);
			progressBar.setLabelAlignment(LabelAlignment.none);
			progressBar.setRenderSize(RenderSize.small);
			progressBar.setWidthInPercent(true);
			if (ChronoUnit.DAYS.between(LocalDate.now(), DateUtils.toLocalDate(toDoTask.getDueDate())) < 0) {
				progressBar.setBarColor(BarColor.danger);
				progressBar.setMax(100);
				progressBar.setActual(100);
			} else {
				progressBar.setMax(ChronoUnit.DAYS.between(DateUtils.toLocalDate(toDoTask.getStartDate()), DateUtils.toLocalDate(toDoTask.getDueDate())));
				progressBar.setActual(ChronoUnit.DAYS.between(DateUtils.toLocalDate(toDoTask.getStartDate()), LocalDate.now()));
			}
		}
		
		String formattedTags = TagUIFactory.getFormattedTags(getLocale(), tags);
		flc.contextPut("tags", formattedTags);
		
		boolean isCreator = creator != null && creator.getKey().equals(getIdentity().getKey());
		if (secCallback.canEdit(toDoTask, isCreator, assignees.contains(getIdentity()), delegatees.contains(getIdentity()))) {
			if (toDoTask.getStatus() == ToDoStatus.open) {
				String startName = "task.start" + toDoTask.getKey();
				flc.contextPut("startName", startName);
				startLink = uifactory.addFormLink(startName, "start", "task.start", null, flc, Link.BUTTON_SMALL);
				startLink.setIconLeftCSS("o_icon o_icon-lg o_icon_todo_start");
				startLink.setTitle("task.start");
				startLink.setGhost(true);
			}
			if (toDoTask.getStatus() == ToDoStatus.open || toDoTask.getStatus() == ToDoStatus.inProgress) {
				String doneName = "task.mark.done" + toDoTask.getKey();
				flc.contextPut("doneName", doneName);
				doneLink = uifactory.addFormLink(doneName, "done", "task.mark.done", null, flc, Link.BUTTON_SMALL);
				doneLink.setIconLeftCSS("o_icon o_icon-lg o_icon_check");
				doneLink.setTitle("task.mark.done");
			}
			String name = "task.edit" + toDoTask.getKey();
			flc.contextPut("editName", name);
			editLink = uifactory.addFormLink(name, "edit", null, flc, Link.BUTTON_SMALL);
			editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
		}
	}

	private List<String> forgeProfiles(UserRequest ureq, UserInfoProfileConfig profileConfig, Set<Identity> identities, String prefix) {
		List<String> names = new ArrayList<>(identities.size());
		int count = 0;
		for (Identity identity : identities) {
			PortraitUser portraitUser = userPortraitService.createPortraitUser(getLocale(), identity);
			UserInfoProfileController profileCtrl = new UserInfoProfileController(ureq, getWindowControl(), profileConfig, portraitUser);
			listenTo(profileCtrl);
			String name = prefix + "_" + toDoTask.getKey() + "_" + count++;
			flc.put(name, profileCtrl.getInitialComponent());
			names.add(name);
		}
		return names;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editLink) {
			fireEvent(ureq, new ToDoTaskEditEvent(toDoTask));
		} else if (source == startLink) {
			fireEvent(ureq, new ToDoTaskStatusChangeEvent(toDoTask, ToDoStatus.inProgress));
		} else if (source == doneLink) {
			fireEvent(ureq, new ToDoTaskStatusChangeEvent(toDoTask, ToDoStatus.done));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
