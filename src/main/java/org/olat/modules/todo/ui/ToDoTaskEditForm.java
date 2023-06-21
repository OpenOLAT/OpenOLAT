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

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskEditForm extends FormBasicController {
	
	private SingleSelection contextEl;
	private MultipleSelectionElement doEl;
	private TextElement titleEl;
	private TagSelection tagsEl;
	private MultipleSelectionElement assignedEl;
	private FormLink assigneeAddLink;
	private MultipleSelectionElement delegatedEl;
	private FormLink delegateeAddLink;
	private SingleSelection statusEl;
	private SingleSelection priorityEl;
	private DateChooser startDateEl;
	private DateChooser dueDateEl;
	private TextElement expenditureOfWorkEl;
	private TextAreaElement descriptionEl;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;

	private final ToDoTask toDoTask;
	private final boolean showContext;
	private final Collection<ToDoContext> availableContexts;
	private final ToDoContext currentContext;
	private final Collection<Identity> availableIdentities;
	private final boolean availableIdentitiesSearch;
	private final Collection<Identity> currentAssignee;
	private final Collection<Identity> currentDelegatee;
	private final List<? extends TagInfo> allTags;
	private Map<String, ToDoContext> keyToContext;
	
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private UserManager userManager;

	public ToDoTaskEditForm(UserRequest ureq, WindowControl wControl, Form mainForm, ToDoTask toDoTask,
			boolean showContext, Collection<ToDoContext> availableContexts, ToDoContext currentContext,
			Collection<Identity> availableIdentities, boolean availableIdentitiesSearch,
			Collection<Identity> currentAssignee, Collection<Identity> currentDelegatee, List<? extends TagInfo> allTags) {
		super(ureq, wControl, LAYOUT_CUSTOM, "todo_task_edit", mainForm);
		this.toDoTask = toDoTask;
		this.showContext = showContext;
		this.availableContexts = availableContexts;
		this.currentContext = currentContext;
		this.availableIdentities = availableIdentities;
		this.availableIdentitiesSearch = availableIdentitiesSearch;
		this.currentAssignee = currentAssignee;
		this.currentDelegatee = currentDelegatee;
		this.allTags = allTags;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (showContext) {
			SelectionValues contextSV = createContextSV();
			contextEl = uifactory.addDropdownSingleselect("task.context", formLayout, contextSV.keys(), contextSV.values());
			if (currentContext != null) {
				contextEl.select(getContextKey(currentContext), true);
				contextEl.setEnabled(availableContexts.size() > 1);
			}
		}
		
		doEl = uifactory.addCheckboxesHorizontal("task.do", formLayout, new String[] {"do"}, new String[] {""});
		doEl.setElementCssClass("o_todo_task_check");
		doEl.addActionListener(FormEvent.ONCHANGE);
		if (toDoTask != null && ToDoStatus.done == toDoTask.getStatus()) {
			doEl.select(doEl.getKey(0), true);
		}
		
		String title = toDoTask != null? toDoTask.getTitle(): null;
		titleEl = uifactory.addTextElement("task.title", 120, title, formLayout);
		titleEl.setMandatory(true);
		
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), allTags);
		
		assignedEl = createMembersElement(formLayout, "task.assigned", availableIdentities, currentAssignee);
		if (availableIdentitiesSearch) {
			FormLayoutContainer assigneeCont = FormLayoutContainer.createButtonLayout("assigneeCont", getTranslator());
			assigneeCont.setLabel("noTransOnlyParam", new String[] {"&nbsp;"});
			assigneeCont.setRootForm(mainForm);
			formLayout.add("assigneeCont", assigneeCont);
			
			assigneeAddLink = uifactory.addFormLink("task.assignee.add", assigneeCont, Link.BUTTON);
		}
		
		delegatedEl = createMembersElement(formLayout, "task.delegated", availableIdentities, currentDelegatee);
		if (availableIdentitiesSearch) {
			FormLayoutContainer delegateeCont = FormLayoutContainer.createButtonLayout("delegateeCont", getTranslator());
			delegateeCont.setLabel("noTransOnlyParam", new String[] {"&nbsp;"});
			delegateeCont.setRootForm(mainForm);
			formLayout.add("delegateeCont", delegateeCont);
			
			delegateeAddLink = uifactory.addFormLink("task.delegatee.add", delegateeCont, Link.BUTTON);
		}
		
		SelectionValues statusSV = new SelectionValues();
		statusSV.add(getSVEntry(ToDoStatus.open));
		statusSV.add(getSVEntry(ToDoStatus.inProgress));
		statusSV.add(getSVEntry(ToDoStatus.done));
		statusEl = uifactory.addDropdownSingleselect("task.status", formLayout, statusSV.keys(), statusSV.values(), statusSV.icons());
		statusEl.addActionListener(FormEvent.ONCHANGE);
		if (toDoTask != null) {
			statusEl.select(toDoTask.getStatus().name(), true);
		}
		
		SelectionValues prioritySV = new SelectionValues();
		prioritySV.add(getSVEntry(ToDoPriority.urgent));
		prioritySV.add(getSVEntry(ToDoPriority.high));
		prioritySV.add(getSVEntry(ToDoPriority.medium));
		prioritySV.add(getSVEntry(ToDoPriority.low));
		priorityEl = uifactory.addDropdownSingleselect("task.priority", formLayout, prioritySV.keys(), prioritySV.values(), prioritySV.icons());
		priorityEl.enableNoneSelection();
		if (toDoTask != null && toDoTask.getPriority() != null) {
			priorityEl.select(toDoTask.getPriority().name(), true);
		}
		
		Date startDate = toDoTask != null? toDoTask.getStartDate(): null;
		startDateEl = uifactory.addDateChooser("task.start.date", startDate, formLayout);
		
		Date dueDate = toDoTask != null? toDoTask.getDueDate(): null;
		dueDateEl = uifactory.addDateChooser("task.due.date", dueDate, formLayout);
		dueDateEl.setDefaultTimeAtEndOfDay(true);
		
		// The input-group does not work very well (Display of errors, round border-radius on the right) 
		Long hours = toDoTask != null? toDoTask.getExpenditureOfWork(): null;
		ToDoExpenditureOfWork expenditureOfWork = toDoService.getExpenditureOfWork(hours);
		String formattedExpenditureOfWork = ToDoUIFactory.format(expenditureOfWork);
		expenditureOfWorkEl = uifactory.addTextElement("task.expenditure.of.work", 30, formattedExpenditureOfWork, formLayout);
		expenditureOfWorkEl.setDomReplacementWrapperRequired(false);
		flc.contextPut("eowId", expenditureOfWorkEl.getForId());
		
		String description = toDoTask != null? toDoTask.getDescription(): null;
		descriptionEl = uifactory.addTextAreaElement("task.description", "task.description", -1, 3, 40, true, false, description, formLayout);
	}

	private SelectionValues createContextSV() {
		SelectionValues contextSV = new SelectionValues();
		keyToContext = new HashMap<>(availableContexts.size());
		for (ToDoContext context : availableContexts) {
			String contextKey = getContextKey(context);
			contextSV.add(SelectionValues.entry(contextKey, getContextValue(context)));
			keyToContext.put(contextKey, context);
		}
		if (currentContext != null) {
			String contextKey = getContextKey(currentContext);
			contextSV.add(SelectionValues.entry(contextKey, getContextValue(currentContext)));
			keyToContext.put(contextKey, currentContext);
		}
		contextSV.sort(SelectionValues.VALUE_ASC);
		return contextSV;
	}

	private String getContextKey(ToDoContext context) {
		String key = context.getType();
		if (context.getOriginId() != null) {
			key += "---" + context.getOriginId().toString();
			if (StringHelper.containsNonWhitespace(context.getOriginSubPath())) {
				key += "---" + context.getOriginSubPath();
			}
		}
		return key;
	}
	
	private String getContextValue(ToDoContext context) {
		String displayName = toDoService.getProvider(context.getType()).getDisplayName(getLocale());
		if (StringHelper.containsNonWhitespace(context.getOriginTitle())) {
			displayName += " - " + context.getOriginTitle();
		}
		return displayName;
	}

	private SelectionValue getSVEntry(ToDoStatus status) {
		return new SelectionValue(status.name(), ToDoUIFactory.getDisplayName(getTranslator(), status), null,
				ToDoUIFactory.getIconCss(status), null, true);
	}
	
	private SelectionValue getSVEntry(ToDoPriority prioriry) {
		return new SelectionValue(prioriry.name(), ToDoUIFactory.getDisplayName(getTranslator(), prioriry), null,
				ToDoUIFactory.getIconCss(prioriry), null, true);
	}
	
	public MultipleSelectionElement createMembersElement(FormItemContainer formLayout, String name,
			Collection<Identity> availableIdentities, Collection<Identity> currentIdentities) {
		Set<Identity> allIdentities = new HashSet<>(availableIdentities);
		allIdentities.addAll(currentIdentities);
		
		SelectionValues membersSV = new SelectionValues();
		allIdentities.forEach(member -> membersSV.add(
				SelectionValues.entry(
						member.getKey().toString(),
						userManager.getUserDisplayName(member.getKey()))));
		membersSV.sort(SelectionValues.VALUE_ASC);
		
		MultipleSelectionElement membersEl = uifactory.addCheckboxesDropdown(name, name, formLayout, membersSV.keys(),
				membersSV.values());
		currentIdentities.forEach(member -> membersEl.select(member.getKey().toString(), true));
		
		return membersEl;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					doAddUser(choosenIdentity, (Boolean)userSearchCtrl.getUserObject());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(cmc);
		userSearchCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == doEl) {
			doToggleStatus(doEl.isAtLeastSelected(1));
		} else if (source == assigneeAddLink) {
			doSelectAssignee(ureq);
		} else if (source == delegateeAddLink) {
			doSelectDelegatee(ureq);
		} else if (source == statusEl) {
			doToogleDo();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if (!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		assignedEl.clearError();
		if (assignedEl.getSelectedKeys().isEmpty()) {
			assignedEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		delegatedEl.clearError();
		if (!delegatedEl.getSelectedKeys().isEmpty() 
				&& delegatedEl.getSelectedKeys().stream().allMatch(key -> assignedEl.getSelectedKeys().contains(key))) {
			delegatedEl.setErrorKey("error.delegatee.is.assignee");
			allOk &= false;
		}
		
		dueDateEl.clearError();
		if (startDateEl.getDate() != null && dueDateEl.getDate() != null && startDateEl.getDate().after(dueDateEl.getDate())) {
			dueDateEl.setErrorKey("error.start.after.due");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doToggleStatus(boolean done) {
		if (done) {
			statusEl.select(ToDoStatus.done.name(), true);
		} else {
			statusEl.select(ToDoStatus.open.name(), true);
		}
	}

	private void doToogleDo() {
		ToDoStatus status = ToDoStatus.valueOf(statusEl.getSelectedKey());
		doEl.select(doEl.getKey(0), ToDoStatus.done == status);
	}

	private void doSelectAssignee(UserRequest ureq) {
		if (guardModalController(userSearchCtrl)) return;
		
		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, false, false);
		userSearchCtrl.setUserObject(Boolean.TRUE);
		listenTo(userSearchCtrl);
		
		String title = translate("task.assignee.add.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doSelectDelegatee(UserRequest ureq) {
		if (guardModalController(userSearchCtrl)) return;
		
		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, false, false);
		userSearchCtrl.setUserObject(Boolean.FALSE);
		listenTo(userSearchCtrl);
		
		String title = translate("task.delegatee.add.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doAddUser(Identity identity, Boolean assignee) {
		MultipleSelectionElement mse = assignee.booleanValue()? assignedEl: delegatedEl;
		
		Set<String> selectedKeys = new HashSet<>(mse.getSelectedKeys());
		selectedKeys.add(identity.getKey().toString());
		
		SelectionValues identitySV = new SelectionValues();
		for (String key : mse.getKeys()) {
			identitySV.add(new SelectionValue(key, mse.getValue(key)));
		}
		identitySV.add(new SelectionValue(identity.getKey().toString(), userManager.getUserDisplayName(identity.getKey())));
		identitySV.sort(SelectionValues.VALUE_ASC);
		
		mse.setKeysAndValues(identitySV.keys(), identitySV.values());
		for (String key : selectedKeys) {
			mse.select(key, true);
		}
	}
	
	public ToDoContext getContext() {
		if (contextEl != null && contextEl.isOneSelected()) {
			return keyToContext.get(contextEl.getSelectedKey());
		}
		return null;
	}
	
	public String getTitle() {
		return titleEl.getValue();
	}
	
	public List<String> getTagDisplayNames() {
		return tagsEl.getDisplayNames();
	}
	
	public Collection<? extends IdentityRef> getAssignees() {
		return assignedEl.getSelectedKeys().stream().map(Long::valueOf).map(IdentityRefImpl::new).toList();
	}
	
	public Collection<? extends IdentityRef> getDelegatees() {
		return delegatedEl.getSelectedKeys().stream().map(Long::valueOf).map(IdentityRefImpl::new).toList();
	}
	
	public ToDoStatus getStatus() {
		return ToDoStatus.valueOf(statusEl.getSelectedKey());
	}
	
	public ToDoPriority getPriority() {
		return priorityEl.isOneSelected()? ToDoPriority.valueOf(priorityEl.getSelectedKey()): null;
	}
	
	public Date getStartDate() {
		return startDateEl.getDate();
	}
	
	public Date getDueDate() {
		return dueDateEl.getDate();
	}
	
	public Long getExpenditureOfWork() {
		ToDoExpenditureOfWork expenditureOfWork = ToDoUIFactory.parseHours(expenditureOfWorkEl.getValue());
		return toDoService.getHours(expenditureOfWork);
	}
	
	public String getDescription() {
		return StringHelper.containsNonWhitespace(descriptionEl.getValue())
				? descriptionEl.getValue()
				: null;
	}

}
