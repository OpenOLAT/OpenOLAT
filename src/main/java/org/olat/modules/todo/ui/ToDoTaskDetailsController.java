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
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskSecurityCallback;
import org.olat.modules.todo.ui.ToDoUIFactory.Due;
import org.olat.user.UserManager;
import org.olat.user.UsersAvatarController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskDetailsController extends FormBasicController {
	
	private FormLink editLink;
	
	private final ToDoTaskSecurityCallback secCallback;
	private final ToDoTask toDoTask;
	private final List<Tag> tags;
	private final Identity modifier;
	private final Set<Identity> assignees;
	private final Set<Identity> delegatees;

	@Autowired
	private ToDoService toDoService;
	@Autowired
	private UserManager userManager;

	public ToDoTaskDetailsController(UserRequest ureq, WindowControl wControl, Form mainForm, ToDoTaskSecurityCallback secCallback,
			ToDoTask toDoTask, List<Tag> tags, Identity modifier, Set<Identity> assignees, Set<Identity> delegatees) {
		super(ureq, wControl, LAYOUT_CUSTOM, "todo_task_details", mainForm);
		this.secCallback = secCallback;
		this.toDoTask = toDoTask;
		this.tags = tags;
		this.modifier = modifier;
		this.assignees = assignees;
		this.delegatees = delegatees;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("title", ToDoUIFactory.getDisplayName(getTranslator(), toDoTask));
		flc.contextPut("description", toDoTask.getDescription());
		flc.contextPut("priorityIconCss", ToDoUIFactory.getIconCss(toDoTask.getPriority()));
		flc.contextPut("priority", ToDoUIFactory.getDisplayName(getTranslator(), toDoTask.getPriority()));
		flc.contextPut("statusIconCss", ToDoUIFactory.getIconCss(toDoTask.getStatus()));
		flc.contextPut("status", ToDoUIFactory.getDisplayName(getTranslator(), toDoTask.getStatus()));
		
		String modifiedDate = Formatter.getInstance(getLocale()).formatDateRelative(toDoTask.getContentModifiedDate());
		String modifiedBy = userManager.getUserDisplayName(modifier != null? modifier.getKey(): null);
		String modified = translate("date.by", modifiedDate, modifiedBy);
		flc.contextPut("modified", modified);
		
		UsersAvatarController assigneesCtrl = new UsersAvatarController(ureq, getWindowControl(), assignees);
		listenTo(assigneesCtrl);
		flc.put("assignees", assigneesCtrl.getInitialComponent());
		
		if (!delegatees.isEmpty()) {
			UsersAvatarController delegateesCtrl = new UsersAvatarController(ureq, getWindowControl(), delegatees);
			listenTo(delegateesCtrl);
			flc.put("delegatees", delegateesCtrl.getInitialComponent());
		}
		
		flc.contextPut("startDate", ToDoUIFactory.getDateOrAnytime(getTranslator(), toDoTask.getStartDate()));
		flc.contextPut("dueDate", ToDoUIFactory.getDateOrAnytime(getTranslator(), toDoTask.getDueDate()));
		Due due = ToDoUIFactory.getDue(getTranslator(), DateUtils.toLocalDate(toDoTask.getDueDate()), LocalDate.now(),
				toDoTask.getStatus());
		String dueName =  ToDoStatus.STATUS_OVERDUE.contains(toDoTask.getStatus())? due.name(): translate("task.success");
		flc.contextPut("due", dueName);
		ToDoExpenditureOfWork expenditureOfWork = toDoService.getExpenditureOfWork(toDoTask.getExpenditureOfWork());
		String expenditureOfWorkStr = ToDoUIFactory.formatLong(getTranslator(), expenditureOfWork);
		flc.contextPut("expenditureOfWork", expenditureOfWorkStr);
		
		ProgressBar progressBar = new ProgressBar("date.progress");
		flc.put("date.progress", progressBar);
		progressBar.setLabelAlignment(LabelAlignment.none);
		progressBar.setRenderSize(RenderSize.small);
		progressBar.setWidthInPercent(true);
		if (toDoTask.getDueDate() != null) {
			if (ChronoUnit.DAYS.between(LocalDate.now(), DateUtils.toLocalDate(toDoTask.getDueDate())) < 0) {
				progressBar.setBarColor(BarColor.danger);
				progressBar.setMax(100);
				progressBar.setActual(100);
			} else if (toDoTask.getStartDate() != null) {
				progressBar.setMax(ChronoUnit.DAYS.between(DateUtils.toLocalDate(toDoTask.getStartDate()), DateUtils.toLocalDate(toDoTask.getDueDate())));
				progressBar.setActual(ChronoUnit.DAYS.between(DateUtils.toLocalDate(toDoTask.getStartDate()), LocalDate.now()));
			} else {
				progressBar.setMax(ChronoUnit.DAYS.between(DateUtils.toLocalDate(toDoTask.getCreationDate()), DateUtils.toLocalDate(toDoTask.getDueDate())));
				progressBar.setActual(ChronoUnit.DAYS.between(DateUtils.toLocalDate(toDoTask.getCreationDate()), LocalDate.now()));
			}
		}
		
		String formattedTags = TagUIFactory.getFormattedTags(getLocale(), tags);
		flc.contextPut("tags", formattedTags);
		
		if (secCallback.canEdit(toDoTask, assignees.contains(getIdentity()), delegatees.contains(getIdentity()))) {
			String name = "task.edit" + toDoTask.getKey();
			flc.contextPut("editName", name);
			editLink = uifactory.addFormLink(name, "task.edit", null, flc, Link.BUTTON);
			editLink.setElementCssClass("o_todo_task_edit_button");
			editLink.setIconLeftCSS("o_icon o_icon-lg o_icon_edit");
			editLink.setGhost(true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == editLink) {
			fireEvent(ureq, new ToDoTaskEditEvent(toDoTask));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
