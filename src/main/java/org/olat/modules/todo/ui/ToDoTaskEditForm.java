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
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.FormToggle.Presentation;
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
import org.olat.modules.todo.ToDoRight;
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
	
	// read-only: not editable, always displayed
	// disabled: not editable, displayed if at least one but not me selected
	public enum MemberSelection { search, candidatesSingle, candidates, readOnly, disabled }
	
	private FormToggle doEl;
	private TextElement titleEl;
	private SingleSelection contextEl;
	private TagSelection tagsEl;
	private MultipleSelectionElement assignedEl;
	private SingleSelection assignedSingleEl;
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

	private final boolean showContext;
	private final Collection<ToDoContext> availableContexts;
	private final ToDoContext currentContext;
	private final MemberSelection assigneeSelection;
	private final Collection<Identity> assigneeCandidates;
	private final Collection<Identity> assigneeCurrent;
	private final MemberSelection delegateeSelection;
	private final Collection<Identity> delegateeCandidates;
	private final Collection<Identity> delegateeCurrent;
	private final List<? extends TagInfo> allTags;
	private final boolean datesEditable;
	private Map<String, ToDoContext> keyToContext;
	
	@Autowired
	private ToDoService toDoService;
	@Autowired
	private UserManager userManager;

	public ToDoTaskEditForm(UserRequest ureq, WindowControl wControl, Form mainForm, boolean showContext,
			Collection<ToDoContext> availableContexts, ToDoContext currentContext, MemberSelection assigneeSelection,
			Collection<Identity> assigneeCandidates, Collection<Identity> assigneeCurrent,
			MemberSelection delegateeSelection, Collection<Identity> delegateeCandidates,
			Collection<Identity> delegateeCurrent, List<? extends TagInfo> allTags, boolean datesEditable) {
		super(ureq, wControl, LAYOUT_CUSTOM, "todo_task_edit", mainForm);
		this.showContext = showContext;
		this.availableContexts = availableContexts;
		this.currentContext = currentContext;
		this.assigneeSelection = assigneeSelection;
		this.assigneeCandidates = assigneeCandidates;
		this.assigneeCurrent = assigneeCurrent;
		this.delegateeSelection = delegateeSelection;
		this.delegateeCandidates = delegateeCandidates;
		this.delegateeCurrent = delegateeCurrent;
		this.allTags = allTags;
		this.datesEditable = datesEditable;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		doEl = uifactory.addToggleButton("task.do", "task.do", null, null, formLayout);
		doEl.setPresentation(Presentation.CHECK);
		doEl.setAriaLabel(ToDoUIFactory.getDisplayName(getTranslator(), ToDoStatus.done));
		doEl.addActionListener(FormEvent.ONCHANGE);
		
		titleEl = uifactory.addTextElement("task.title", 120, null, formLayout);
		titleEl.setElementCssClass("o_sel_task_title");
		titleEl.setMandatory(true);
		
		if (showContext) {
			SelectionValues contextSV = createContextSV();
			contextEl = uifactory.addDropdownSingleselect("task.context", formLayout, contextSV.keys(), contextSV.values());
			if (currentContext != null) {
				contextEl.select(getContextKey(currentContext), true);
				contextEl.setEnabled(availableContexts.size() > 1);
			}
		}
		
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), allTags);
		
		if (MemberSelection.candidatesSingle == assigneeSelection) {
			assignedSingleEl = createMembersSingleElement(formLayout, "task.assigned", assigneeCandidates, assigneeCurrent);
		} else {
			assignedEl = createMembersElement(formLayout, "task.assigned", assigneeSelection, assigneeCandidates, assigneeCurrent);
		}
		if (MemberSelection.search == assigneeSelection) {
			FormLayoutContainer assigneeCont = FormLayoutContainer.createButtonLayout("assigneeCont", getTranslator());
			assigneeCont.setLabel("noTransOnlyParam", new String[] {"&nbsp;"});
			assigneeCont.setRootForm(mainForm);
			formLayout.add("assigneeCont", assigneeCont);
			
			assigneeAddLink = uifactory.addFormLink("task.assignee.add", assigneeCont, Link.BUTTON);
		}
		
		delegatedEl = createMembersElement(formLayout, "task.delegated", delegateeSelection, delegateeCandidates, delegateeCurrent);
		if (MemberSelection.search == delegateeSelection) {
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
		statusEl.select(ToDoStatus.open.name(), true);
		
		SelectionValues prioritySV = new SelectionValues();
		prioritySV.add(getSVEntry(ToDoPriority.urgent));
		prioritySV.add(getSVEntry(ToDoPriority.high));
		prioritySV.add(getSVEntry(ToDoPriority.medium));
		prioritySV.add(getSVEntry(ToDoPriority.low));
		priorityEl = uifactory.addDropdownSingleselect("task.priority", formLayout, prioritySV.keys(), prioritySV.values(), prioritySV.icons());
		priorityEl.enableNoneSelection();
		
		startDateEl = uifactory.addDateChooser("task.start.date", null, formLayout);
		startDateEl.setEnabled(datesEditable);
		
		dueDateEl = uifactory.addDateChooser("task.due.date", null, formLayout);
		dueDateEl.setDefaultTimeAtEndOfDay(true);
		dueDateEl.setEnabled(datesEditable);
		
		// The input-group does not work very well (Display of errors, round border-radius on the right) 
		expenditureOfWorkEl = uifactory.addTextElement("task.expenditure.of.work", 30, null, formLayout);
		expenditureOfWorkEl.setDomReplacementWrapperRequired(false);
		flc.contextPut("eowId", expenditureOfWorkEl.getForId());
		
		descriptionEl = uifactory.addTextAreaElement("task.description", "task.description", -1, 3, 40, true, false, null, formLayout);
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
			MemberSelection selection, Collection<Identity> candidateIdentities, Collection<Identity> currentIdentities) {
		boolean membersEditable = MemberSelection.readOnly != selection && MemberSelection.disabled != selection;
		
		SelectionValues membersSV = createMembersSV(candidateIdentities, currentIdentities, membersEditable);
		
		MultipleSelectionElement membersEl = uifactory.addCheckboxesDropdown(name, name, formLayout, membersSV.keys(),
				membersSV.values());
		currentIdentities.forEach(member -> membersEl.select(member.getKey().toString(), true));
		membersEl.setEnabled(membersEditable);
		membersEl.setVisible(MemberSelection.disabled != selection || membersEl.isAtLeastSelected(1));
		if (MemberSelection.disabled == selection && isMyOrNoneSelected(membersEl)) {
			membersEl.setVisible(false);
		}
		
		return membersEl;
	}

	private SelectionValues createMembersSV(Collection<Identity> candidateIdentities,
			Collection<Identity> currentIdentities, boolean membersEditable) {
		Set<Identity> allIdentities = new HashSet<>(currentIdentities);
		if (membersEditable) {
			allIdentities.addAll(candidateIdentities);
		}
		
		SelectionValues membersSV = new SelectionValues();
		allIdentities.forEach(member -> membersSV.add(
				SelectionValues.entry(
						member.getKey().toString(),
						userManager.getUserDisplayName(member.getKey()))));
		membersSV.sort(SelectionValues.VALUE_ASC);
		return membersSV;
	}
	
	private boolean isMyOrNoneSelected(MultipleSelectionElement membersEl) {
		return !membersEl.isAtLeastSelected(1)
				|| (membersEl.getSelectedKeys().size() == 1 && membersEl.getSelectedKeys().contains(getIdentity().getKey().toString()));
	}
	
	public SingleSelection createMembersSingleElement(FormItemContainer formLayout, String name,
			Collection<Identity> candidateIdentities, Collection<Identity> currentIdentities) {
		
		SelectionValues membersSV = createMembersSV(candidateIdentities, currentIdentities, true);
		SingleSelection membersEl = uifactory.addDropdownSingleselect(name, formLayout, membersSV.keys(), membersSV.values());
		if (!currentIdentities.isEmpty()) {
			membersEl.select(currentIdentities.stream().findFirst().get().getKey().toString(), true);
		} else if (!membersSV.isEmpty()) {
			membersEl.select(membersEl.getKey(0), true);
		}
		
		return membersEl;
	}
	
	public void setValues(ToDoTask toDoTask) {
		if (toDoTask != null) {
			setValues(toDoTask.getTitle(), toDoTask.getDescription(), toDoTask.getStatus(), toDoTask.getPriority(),
					toDoTask.getExpenditureOfWork(), toDoTask.getStartDate(), toDoTask.getDueDate());
		}
	}
	
	public void setValues(String title, String description, ToDoStatus status, ToDoPriority priority,
			Long expenditureOfWork, Date startDate, Date dueDate) {
		titleEl.setValue(title);
		descriptionEl.setValue(description);
		if (status != null) {
			statusEl.select(status.name(), true);
			doEl.toggle(ToDoStatus.done == status);
		}
		if (priority != null) {
			priorityEl.select(priority.name(), true);
		}
		if (expenditureOfWork != null) {
			ToDoExpenditureOfWork eow = toDoService.getExpenditureOfWork(expenditureOfWork);
			String formattedExpenditureOfWork = ToDoUIFactory.format(eow);
			expenditureOfWorkEl.setValue(formattedExpenditureOfWork);
		}
		startDateEl.setDate(startDate);
		dueDateEl.setDate(dueDate);
	}
	
	public void updateUIByAssigneeRight(ToDoTask toDoTask) {
		if (toDoTask != null) {
			updateUIByAssigneeRight(toDoTask.getAssigneeRights());
		}
	}
	
	public void updateUIByAssigneeRight(ToDoRight[] assigneeRights) {
		if (assigneeCurrent == null || !assigneeCurrent.contains(getIdentity())) {
			// No assignee, no application of assignee rights
			return;
		}
		if (doEl != null) {
			doEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.status));
		}
		if (titleEl != null) {
			titleEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.title));
		}
		if (tagsEl != null) {
			tagsEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.tags));
		}
		// Mmmh, how should it work together with the configs in the controller?
		/*
		if (assignedEl != null) {
			assignedEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.assignees));
		}
		if (assigneeAddLink != null) {
			assigneeAddLink.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.assignees));
		}
		if (delegatedEl != null) {
			delegatedEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.delegates));
		}
		if (assigneeAddLink != null) {
			assigneeAddLink.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.delegates));
		}
		*/
		if (statusEl != null) {
			statusEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.status));
		}
		if (priorityEl != null) {
			priorityEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.priority));
		}
		if (startDateEl != null) {
			startDateEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.startDate));
		}
		if (dueDateEl != null) {
			dueDateEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.dueDate));
		}
		if (expenditureOfWorkEl != null) {
			expenditureOfWorkEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.expenditureOfWork));
		}
		if (descriptionEl != null) {
			descriptionEl.setEnabled(ToDoRight.contains(assigneeRights, ToDoRight.description));
		}
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
			doToggleStatus(doEl.isOn());
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
		
		if (assignedEl != null) {
			assignedEl.clearError();
			if (assignedEl.isVisible() && assignedEl.getSelectedKeys().isEmpty()) {
				assignedEl.setErrorKey("form.mandatory.hover");
				allOk &= false;
			}
			
			delegatedEl.clearError();
			if (delegatedEl.isVisible() && !delegatedEl.getSelectedKeys().isEmpty() 
					&& delegatedEl.getSelectedKeys().stream().allMatch(key -> assignedEl.getSelectedKeys().contains(key))) {
				delegatedEl.setErrorKey("error.delegatee.is.assignee");
				allOk &= false;
			}
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
		if (ToDoStatus.done == status) {
			doEl.toggleOn();
		} else {
			doEl.toggleOff();
		}
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
		return assignedEl != null
				? assignedEl.getSelectedKeys().stream().map(Long::valueOf).map(IdentityRefImpl::new).toList()
				: assignedSingleEl.isOneSelected()
						? List.of(new IdentityRefImpl(Long.valueOf(assignedSingleEl.getSelectedKey())))
						: List.of();
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
