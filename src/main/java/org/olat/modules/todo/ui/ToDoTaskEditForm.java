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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
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
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoRelativeDates;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ui.ToDoTaskContextConfig.ContextSelection;
import org.olat.modules.todo.ui.ToDoTaskMemberConfig.MemberSelection;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoTaskEditForm extends FormBasicController {

	private static final String MODE_ABSOLUTE = "absolute";
	private static final String MODE_RELATIVE = "relative";

	private FormToggle doEl;
	private TextElement titleEl;
	private SingleSelection contextEl;
	private TextElement contextStaticEl;
	private FormLink contextChangeLink;
	private TagSelection tagsEl;
	private MultipleSelectionElement assignedEl;
	private SingleSelection assignedSingleEl;
	private FormLink assigneeAddLink;
	private MultipleSelectionElement delegatedEl;
	private FormLink delegateeAddLink;
	private SingleSelection statusEl;
	private SingleSelection priorityEl;
	private FormLayoutContainer startDateRowCont;
	private SingleSelection startDateModeEl;
	private DateChooser startDateEl;
	private FormLayoutContainer startDateRelCont;
	private TextElement startRelDisplayEl;
	private FormLink startRelLink;
	private ToDoRelativeDates currentRelStart;
	private FormLayoutContainer dueDateRowCont;
	private SingleSelection dueDateModeEl;
	private DateChooser dueDateEl;
	private FormLayoutContainer dueDateRelCont;
	private TextElement dueRelDisplayEl;
	private FormLink dueRelLink;
	private ToDoRelativeDates currentRelDue;
	private CloseableCalloutWindowController datePickerCalloutCtrl;
	private Controller datePickerCtrl;
	private TextElement expenditureOfWorkEl;
	private TextAreaElement descriptionEl;

	private CloseableModalController cmc;
	private Controller memberSearchCtrl;
	private Boolean memberSearchAssignee;
	private Controller contextPickerCtrl;

	private final ToDoTaskContextConfig contextConfig;
	private ToDoContext selectedContext;
	private final ToDoTaskMemberConfig assigneeConfig;
	private final ToDoTaskMemberConfig delegateeConfig;
	private final Collection<Identity> assignees;
	private final Collection<Identity> delegatees;
	private final ToDoTaskDateConfig datesConfig;
	private final List<? extends TagInfo> allTags;
	private final boolean datesEditable;
	private Map<String, ToDoContext> keyToContext;

	@Autowired
	private ToDoService toDoService;
	@Autowired
	private UserManager userManager;

	public ToDoTaskEditForm(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskContextConfig contextConfig,
			ToDoTaskMemberConfig assigneeConfig,
			ToDoTaskMemberConfig delegateeConfig,
			ToDoTaskMemberSelection memberSelection,
			ToDoTaskDateConfig datesConfig,
			List<? extends TagInfo> allTags, boolean datesEditable) {
		super(ureq, wControl, LAYOUT_CUSTOM, "todo_task_edit", mainForm);
		this.contextConfig = contextConfig;
		this.selectedContext = contextConfig.getCurrentContext();
		this.assigneeConfig = assigneeConfig;
		this.delegateeConfig = delegateeConfig;
		this.assignees = memberSelection.assignees();
		this.delegatees = memberSelection.delegatees();
		this.datesConfig = datesConfig != null ? datesConfig : ToDoTaskDateConfig.absoluteOnly();
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

		if (contextConfig.getSelection() == ContextSelection.dropdown) {
			SelectionValues contextSV = createContextSV();
			contextEl = uifactory.addDropdownSingleselect("task.context", formLayout, contextSV.keys(), contextSV.values());
			if (selectedContext != null) {
				contextEl.select(getContextKey(selectedContext), true);
				contextEl.setEnabled(contextConfig.getAvailableContexts().size() > 1);
			}
		} else if (contextConfig.getSelection() == ContextSelection.picker) {
			initContextPickerUI(formLayout);
		}

		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), allTags);

		if (MemberSelection.candidatesSingle == assigneeConfig.getSelection()) {
			assignedSingleEl = createMembersSingleElement(formLayout, "task.assigned", assigneeConfig.getCandidates(), assignees);
		} else {
			assignedEl = createMembersElement(formLayout, "task.assigned", assigneeConfig.getSelection(), assigneeConfig.getCandidates(), assignees);
		}
		if (MemberSelection.search == assigneeConfig.getSelection()) {
			assignedEl.setDomReplacementWrapperRequired(false);
			assigneeAddLink = uifactory.addFormLink("assigneeAddLink", "task.assignee.add", null, formLayout, Link.BUTTON);
			assigneeAddLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
			assigneeAddLink.setElementCssClass("input-group-addon");
		}

		delegatedEl = createMembersElement(formLayout, "task.delegated", delegateeConfig.getSelection(), delegateeConfig.getCandidates(), delegatees);
		if (MemberSelection.search == delegateeConfig.getSelection()) {
			delegatedEl.setDomReplacementWrapperRequired(false);
			delegateeAddLink = uifactory.addFormLink("delegateeAddLink", "task.delegatee.add", null, formLayout, Link.BUTTON);
			delegateeAddLink.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
			delegateeAddLink.setElementCssClass("input-group-addon");
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

		startDateRowCont = FormLayoutContainer.createInlineFormLayout("startDateRow", getTranslator());
		startDateRowCont.setLabel("task.start.date", null);
		startDateRowCont.setElementCssClass("o_todo_date_row");
		formLayout.add("startDateRow", startDateRowCont);

		dueDateRowCont = FormLayoutContainer.createInlineFormLayout("dueDateRow", getTranslator());
		dueDateRowCont.setLabel("task.due.date", null);
		dueDateRowCont.setElementCssClass("o_todo_date_row");
		formLayout.add("dueDateRow", dueDateRowCont);

		if (datesConfig.getPicker() != null) {
			SelectionValues modeSV = new SelectionValues();
			modeSV.add(SelectionValues.entry(MODE_ABSOLUTE, translate("task.date.mode.absolute")));
			modeSV.add(SelectionValues.entry(MODE_RELATIVE, translate("task.date.mode.relative")));
			startDateModeEl = uifactory.addButtonGroupSingleSelectHorizontal("task.start.date.mode", startDateRowCont, modeSV);
			startDateModeEl.setLabel(null, null);
			startDateModeEl.select(MODE_ABSOLUTE, true);
			startDateModeEl.addActionListener(FormEvent.ONCHANGE);
			dueDateModeEl = uifactory.addButtonGroupSingleSelectHorizontal("task.due.date.mode", dueDateRowCont, modeSV);
			dueDateModeEl.setLabel(null, null);
			dueDateModeEl.select(MODE_ABSOLUTE, true);
			dueDateModeEl.addActionListener(FormEvent.ONCHANGE);
		}

		startDateEl = uifactory.addDateChooser("task.start.date", null, startDateRowCont);
		startDateEl.setEnabled(datesEditable);

		dueDateEl = uifactory.addDateChooser("task.due.date", null, dueDateRowCont);
		dueDateEl.setDefaultTimeAtEndOfDay(true);
		dueDateEl.setEnabled(datesEditable);

		if (datesConfig.getPicker() != null) {
			String relContPage = velocity_root + "/todo_date_rel_cont.html";
			startDateRelCont = FormLayoutContainer.createCustomFormLayout("startDateRelCont", getTranslator(), relContPage);
			startDateRelCont.setLabel(null, null);
			startDateRelCont.setVisible(false);
			startDateRowCont.add("startDateRelCont", startDateRelCont);
			startRelDisplayEl = uifactory.addTextElement("task.start.date.rel", null, 256, null, startDateRelCont);
			startRelDisplayEl.setDomReplacementWrapperRequired(false);
			startRelDisplayEl.setEnabled(false);
			startRelLink = uifactory.addFormLink("startRelLink", "", null, startDateRelCont, Link.NONTRANSLATED | Link.BUTTON);
			startRelLink.setIconLeftCSS("o_icon o_icon_calendar");

			dueDateRelCont = FormLayoutContainer.createCustomFormLayout("dueDateRelCont", getTranslator(), relContPage);
			dueDateRelCont.setLabel(null, null);
			dueDateRelCont.setVisible(false);
			dueDateRowCont.add("dueDateRelCont", dueDateRelCont);
			dueRelDisplayEl = uifactory.addTextElement("task.due.date.rel", null, 256, null, dueDateRelCont);
			dueRelDisplayEl.setDomReplacementWrapperRequired(false);
			dueRelDisplayEl.setEnabled(false);
			dueRelLink = uifactory.addFormLink("dueRelLink", "", null, dueDateRelCont, Link.NONTRANSLATED | Link.BUTTON);
			dueRelLink.setIconLeftCSS("o_icon o_icon_calendar");
		}

		// The input-group does not work very well (Display of errors, round border-radius on the right)
		expenditureOfWorkEl = uifactory.addTextElement("task.expenditure.of.work", 30, null, formLayout);
		expenditureOfWorkEl.setDomReplacementWrapperRequired(false);
		flc.contextPut("eowId", expenditureOfWorkEl.getForId());

		descriptionEl = uifactory.addTextAreaElement("task.description", "task.description", -1, 3, 40, true, false, null, formLayout);
	}

	private SelectionValues createContextSV() {
		SelectionValues contextSV = new SelectionValues();
		keyToContext = new HashMap<>(contextConfig.getAvailableContexts().size());
		for (ToDoContext context : contextConfig.getAvailableContexts()) {
			String contextKey = getContextKey(context);
			contextSV.add(SelectionValues.entry(contextKey, getContextValue(context)));
			keyToContext.put(contextKey, context);
		}
		if (selectedContext != null) {
			String contextKey = getContextKey(selectedContext);
			contextSV.add(SelectionValues.entry(contextKey, getContextValue(selectedContext)));
			keyToContext.put(contextKey, selectedContext);
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
			if (StringHelper.containsNonWhitespace(context.getOriginSubTitle())) {
				displayName += " - " + context.getOriginSubTitle();
			}
		}
		return displayName;
	}

	private void initContextPickerUI(FormItemContainer formLayout) {
		ToDoTaskContextPicker contextPicker = contextConfig.getPicker();
		String displayValue = selectedContext != null ? getContextValue(selectedContext) : "";
		contextStaticEl = uifactory.addTextElement("task.context", "task.context", 256, displayValue, formLayout);
		contextStaticEl.setDomReplacementWrapperRequired(false);
		contextStaticEl.setEnabled(false);
		if (contextPicker != null) {
			contextChangeLink = uifactory.addFormLink("contextChangeLink", "task.context.change", null, formLayout, Link.BUTTON);
			contextChangeLink.setElementCssClass("input-group-addon");
			contextChangeLink.setIconLeftCSS("o_icon o_icon_edit");
		}
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

	public void setValues(ToDoTaskFormValues values) {
		if (values == null) {
			return;
		}

		titleEl.setValue(values.getTitle());
		descriptionEl.setValue(values.getDescription());
		if (values.getStatus() != null) {
			statusEl.select(values.getStatus().name(), true);
			doEl.toggle(ToDoStatus.done == values.getStatus());
		}
		if (values.getPriority() != null) {
			priorityEl.select(values.getPriority().name(), true);
		}
		if (values.getExpenditureOfWork() != null) {
			ToDoExpenditureOfWork eow = toDoService.getExpenditureOfWork(values.getExpenditureOfWork());
			String formattedExpenditureOfWork = ToDoUIFactory.format(eow);
			expenditureOfWorkEl.setValue(formattedExpenditureOfWork);
		}

		ToDoRelativeDates rd = values.getRelativeDates();
		if (rd != null && datesConfig.getPicker() != null) {
			if (rd.getStartRef() != null) {
				currentRelStart = rd;
				startDateModeEl.select(MODE_RELATIVE, true);
				startRelDisplayEl.setValue(datesConfig.getPicker().getDisplayValue(rd, true));
				doSwitchDateMode(true);
			} else {
				startDateEl.setDate(values.getStartDate());
			}
			if (rd.getDueRef() != null) {
				currentRelDue = rd;
				dueDateModeEl.select(MODE_RELATIVE, true);
				dueRelDisplayEl.setValue(datesConfig.getPicker().getDisplayValue(rd, false));
				doSwitchDateMode(false);
			} else {
				dueDateEl.setDate(values.getDueDate());
			}
		} else {
			startDateEl.setDate(values.getStartDate());
			dueDateEl.setDate(values.getDueDate());
		}
	}

	public void updateUIByAssigneeRight(ToDoTask toDoTask) {
		if (toDoTask != null) {
			updateUIByAssigneeRight(toDoTask.getAssigneeRights());
		}
	}

	public void updateUIByAssigneeRight(ToDoRight[] assigneeRights) {
		boolean isAssignee = assignees != null && assignees.contains(getIdentity());
		boolean isDelegatee = delegatees != null && delegatees.contains(getIdentity());
		if (!isAssignee && !isDelegatee) {
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
		boolean startEnabled = ToDoRight.contains(assigneeRights, ToDoRight.startDate);
		if (startDateEl != null) {
			startDateEl.setEnabled(startEnabled);
		}
		if (startDateModeEl != null) {
			startDateModeEl.setEnabled(startEnabled);
		}
		if (startRelLink != null) {
			startRelLink.setEnabled(startEnabled);
		}
		boolean dueEnabled = ToDoRight.contains(assigneeRights, ToDoRight.dueDate);
		if (dueDateEl != null) {
			dueDateEl.setEnabled(dueEnabled);
		}
		if (dueDateModeEl != null) {
			dueDateModeEl.setEnabled(dueEnabled);
		}
		if (dueRelLink != null) {
			dueRelLink.setEnabled(dueEnabled);
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
		if (memberSearchCtrl == source) {
			ToDoTaskMemberSearchProvider provider = memberSearchAssignee
					? assigneeConfig.getSearchProvider()
					: delegateeConfig.getSearchProvider();
			provider.getSelectedIdentities(memberSearchCtrl, event)
					.forEach(id -> doAddUser(id, memberSearchAssignee));
			cmc.deactivate();
			cleanUp();
		} else if (contextPickerCtrl == source) {
			if (event instanceof ToDoContextSelectedEvent tcse) {
				selectedContext = tcse.getContext();
				if (contextStaticEl != null) {
					contextStaticEl.setValue(getContextValue(selectedContext));
				}
				doRefreshRelativeDates();
			}
			cmc.deactivate();
			cleanUp();
		} else if (datePickerCtrl == source) {
			if (event instanceof ToDoRelativeDateSelectedEvent trdse) {
				if (trdse.getRelativeDates() == null) {
					if (trdse.isStart()) {
						currentRelStart = null;
						startDateModeEl.select(MODE_ABSOLUTE, true);
						doSwitchDateMode(true);
					} else {
						currentRelDue = null;
						dueDateModeEl.select(MODE_ABSOLUTE, true);
						doSwitchDateMode(false);
					}
				} else {
					if (trdse.isStart()) {
						currentRelStart = trdse.getRelativeDates();
						startRelDisplayEl.setValue(datesConfig.getPicker().getDisplayValue(currentRelStart, true));
					} else {
						currentRelDue = trdse.getRelativeDates();
						dueRelDisplayEl.setValue(datesConfig.getPicker().getDisplayValue(currentRelDue, false));
					}
				}
			}
			CloseableCalloutWindowController callout = datePickerCalloutCtrl;
			datePickerCalloutCtrl = null;
			if (callout != null) {
				callout.deactivate();
			}
			cleanUp();
		} else if (datePickerCalloutCtrl == source) {
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(memberSearchCtrl);
		removeAsListenerAndDispose(contextPickerCtrl);
		removeAsListenerAndDispose(datePickerCtrl);
		removeAsListenerAndDispose(datePickerCalloutCtrl);
		removeAsListenerAndDispose(cmc);
		memberSearchCtrl = null;
		contextPickerCtrl = null;
		memberSearchAssignee = null;
		datePickerCtrl = null;
		datePickerCalloutCtrl = null;
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
		} else if (source == contextChangeLink) {
			doOpenContextPicker(ureq);
		} else if (source == statusEl) {
			doToogleDo();
		} else if (source == startDateModeEl) {
			doSwitchDateMode(true);
		} else if (source == dueDateModeEl) {
			doSwitchDateMode(false);
		} else if (source == startRelLink) {
			doOpenDatePicker(ureq, startRelLink, true);
		} else if (source == dueRelLink) {
			doOpenDatePicker(ureq, dueRelLink, false);
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
			if (assigneeConfig.isMandatory() && assignedEl.isVisible() && assignedEl.getSelectedKeys().isEmpty()) {
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

		boolean startRelative = isRelativeMode(startDateModeEl);
		boolean dueRelative = isRelativeMode(dueDateModeEl);

		startDateRowCont.clearError();
		dueDateRowCont.clearError();

		if (startRelative && startRelDisplayEl != null) {
			if (currentRelStart == null || currentRelStart.getStartRef() == null) {
				startDateRowCont.setErrorKey("form.mandatory.hover");
				allOk &= false;
			}
		}
		if (dueRelative && dueRelDisplayEl != null) {
			if (currentRelDue == null || currentRelDue.getDueRef() == null) {
				dueDateRowCont.setErrorKey("form.mandatory.hover");
				allOk &= false;
			}
		}

		if (!startRelative && !dueRelative) {
			if (startDateEl.getDate() != null && dueDateEl.getDate() != null && startDateEl.getDate().after(dueDateEl.getDate())) {
				dueDateRowCont.setErrorKey("error.start.after.due");
				allOk &= false;
			}
		} else if (allOk && datesConfig.getPicker() != null) {
			ToDoTaskDatePicker picker = datesConfig.getPicker();
			Date resolvedStart;
			if (startRelative) {
				resolvedStart = currentRelStart != null ? picker.resolve(currentRelStart, true) : null;
			} else {
				resolvedStart = startDateEl.getDate();
			}
			Date resolvedDue;
			if (dueRelative) {
				resolvedDue = currentRelDue != null ? picker.resolve(currentRelDue, false) : null;
			} else {
				resolvedDue = dueDateEl.getDate();
			}
			if (resolvedStart != null && resolvedDue != null && resolvedStart.after(resolvedDue)) {
				dueDateRowCont.setErrorKey("error.start.after.due");
				allOk &= false;
			}
		}

		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private boolean isRelativeMode(SingleSelection modeEl) {
		return modeEl != null && modeEl.isOneSelected()
				&& MODE_RELATIVE.equals(modeEl.getSelectedKey());
	}

	private void doSwitchDateMode(boolean forStart) {
		boolean relativeMode = forStart ? isRelativeMode(startDateModeEl) : isRelativeMode(dueDateModeEl);
		if (forStart) {
			startDateEl.setVisible(!relativeMode);
			if (startDateRelCont != null) {
				startDateRelCont.setVisible(relativeMode);
			}
		} else {
			dueDateEl.setVisible(!relativeMode);
			if (dueDateRelCont != null) {
				dueDateRelCont.setVisible(relativeMode);
			}
		}
	}

	private void doRefreshRelativeDates() {
		ToDoTaskDatePicker picker = datesConfig.getPicker();
		if (picker == null) {
			return;
		}
		picker.contextChanged(selectedContext);
		if (isRelativeMode(startDateModeEl) && currentRelStart != null && startRelDisplayEl != null) {
			startRelDisplayEl.setValue(picker.getDisplayValue(currentRelStart, true));
		}
		if (isRelativeMode(dueDateModeEl) && currentRelDue != null && dueRelDisplayEl != null) {
			dueRelDisplayEl.setValue(picker.getDisplayValue(currentRelDue, false));
		}
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
		if (guardModalController(memberSearchCtrl)) return;

		removeAsListenerAndDispose(memberSearchCtrl);
		removeAsListenerAndDispose(cmc);
		memberSearchAssignee = Boolean.TRUE;
		memberSearchCtrl = assigneeConfig.getSearchProvider().createSearchController(ureq, getWindowControl());
		if (memberSearchCtrl == null) return;
		listenTo(memberSearchCtrl);
		String title = translate("task.assignee.add.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), memberSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doSelectDelegatee(UserRequest ureq) {
		if (guardModalController(memberSearchCtrl)) return;

		removeAsListenerAndDispose(memberSearchCtrl);
		removeAsListenerAndDispose(cmc);
		memberSearchAssignee = Boolean.FALSE;
		memberSearchCtrl = delegateeConfig.getSearchProvider().createSearchController(ureq, getWindowControl());
		if (memberSearchCtrl == null) return;
		listenTo(memberSearchCtrl);
		String title = translate("task.delegatee.add.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), memberSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenContextPicker(UserRequest ureq) {
		removeAsListenerAndDispose(contextPickerCtrl);
		removeAsListenerAndDispose(cmc);
		contextPickerCtrl = contextConfig.getPicker().createPickerController(ureq, getWindowControl(), selectedContext);
		if (contextPickerCtrl == null) return;
		listenTo(contextPickerCtrl);
		String title = translate("task.context");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contextPickerCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenDatePicker(UserRequest ureq, FormLink link, boolean forStart) {
		removeAsListenerAndDispose(datePickerCtrl);
		removeAsListenerAndDispose(datePickerCalloutCtrl);
		ToDoRelativeDates current = forStart ? currentRelStart : currentRelDue;
		datePickerCtrl = datesConfig.getPicker().createPickerController(ureq, getWindowControl(), current, forStart);
		listenTo(datePickerCtrl);
		datePickerCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				datePickerCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "",
				new CalloutSettings(true, CalloutOrientation.bottomOrTop, false, null));
		listenTo(datePickerCalloutCtrl);
		datePickerCalloutCtrl.activate();
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
		if (contextConfig.getSelection() == ContextSelection.picker) {
			return selectedContext;
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
		if (isRelativeMode(startDateModeEl)) {
			return null;
		}
		return startDateEl.isVisible() ? startDateEl.getDate() : null;
	}

	public Date getDueDate() {
		if (isRelativeMode(dueDateModeEl)) {
			return null;
		}
		return dueDateEl.isVisible() ? dueDateEl.getDate() : null;
	}

	public ToDoRelativeDates getRelativeDates() {
		boolean startRelative = isRelativeMode(startDateModeEl);
		boolean dueRelative = isRelativeMode(dueDateModeEl);
		if (!startRelative && !dueRelative) {
			return null;
		}
		ToDoRelativeDates rd = new ToDoRelativeDates();
		if (startRelative && currentRelStart != null) {
			rd.setStartRef(currentRelStart.getStartRef());
			rd.setStartUnit(currentRelStart.getStartUnit());
			rd.setStartValue(currentRelStart.getStartValue());
		}
		if (dueRelative && currentRelDue != null) {
			rd.setDueRef(currentRelDue.getDueRef());
			rd.setDueUnit(currentRelDue.getDueUnit());
			rd.setDueValue(currentRelDue.getDueValue());
		}
		return rd;
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

	public static interface ToDoTaskFormValues {

		public String getTitle();

		public String getDescription();

		public ToDoStatus getStatus();

		public ToDoPriority getPriority();

		public Long getExpenditureOfWork();

		public Date getStartDate();

		public Date getDueDate();

		public ToDoRelativeDates getRelativeDates();
	}

	public static final class ToDoTaskValues implements ToDoTaskFormValues {

		private final ToDoTask toDoTask;

		public ToDoTaskValues(ToDoTask toDoTask) {
			this.toDoTask = toDoTask;
		}

		@Override
		public String getTitle() {
			return toDoTask.getTitle();
		}

		@Override
		public String getDescription() {
			return toDoTask.getDescription();
		}

		@Override
		public ToDoStatus getStatus() {
			return toDoTask.getStatus();
		}

		@Override
		public ToDoPriority getPriority() {
			return toDoTask.getPriority();
		}

		@Override
		public Long getExpenditureOfWork() {
			return toDoTask.getExpenditureOfWork();
		}

		@Override
		public Date getStartDate() {
			return toDoTask.getStartDate();
		}

		@Override
		public Date getDueDate() {
			return toDoTask.getDueDate();
		}

		@Override
		public ToDoRelativeDates getRelativeDates() {
			return toDoTask.getRelativeDates();
		}

	}

	public static final class CopyValues implements ToDoTaskFormValues {

		private final ToDoTask toDoTask;
		private final String title;
		private Date startDate;
		private Date dueDate;

		public CopyValues(Locale locale, ToDoTask toDoTask) {
			this.toDoTask = toDoTask;
			this.title = Util.createPackageTranslator(ToDoUIFactory.class, locale).translate("copy.title", toDoTask.getTitle());

			if (toDoTask.getRelativeDates() == null) {
				startDate = toDoTask.getStartDate();
				dueDate = toDoTask.getDueDate();

				// Move dates at least to now
				LocalDate now = LocalDate.now();
				Long diffDays = null;
				if (toDoTask.getStartDate() != null) {
					Long startDiffDays = DateUtils.toLocalDate(toDoTask.getStartDate()).until(now, ChronoUnit.DAYS);
					if (startDiffDays > 0) {
						diffDays = startDiffDays;
					}
				}
				if (diffDays == null && toDoTask.getDueDate() != null) {
					Long dueDiffDays = DateUtils.toLocalDate(toDoTask.getDueDate()).until(now, ChronoUnit.DAYS);
					if (dueDiffDays > 0) {
						diffDays = dueDiffDays;
					}
				}
				if (diffDays != null) {
					if (startDate != null) {
						startDate = DateUtils.addDays(startDate, diffDays.intValue());
					}
					if (dueDate != null) {
						dueDate = DateUtils.addDays(dueDate, diffDays.intValue());
					}
				}
			}
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getDescription() {
			return toDoTask.getDescription();
		}

		@Override
		public ToDoStatus getStatus() {
			return ToDoStatus.open;
		}

		@Override
		public ToDoPriority getPriority() {
			return toDoTask.getPriority();
		}

		@Override
		public Long getExpenditureOfWork() {
			return toDoTask.getExpenditureOfWork();
		}

		@Override
		public Date getStartDate() {
			return startDate;
		}

		@Override
		public Date getDueDate() {
			return dueDate;
		}

		@Override
		public ToDoRelativeDates getRelativeDates() {
			return toDoTask.getRelativeDates();
		}
	}

}
