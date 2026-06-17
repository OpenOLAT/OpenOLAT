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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.date.OffsetDirection;
import org.olat.core.gui.components.date.RelativeDateElement;
import org.olat.core.gui.components.date.RelativeDateSelection;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.FormToggle.Presentation;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.todo.ToDoContext;
import org.olat.modules.todo.ToDoDateUnit;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoRelativeDates;
import org.olat.modules.todo.ToDoRight;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ui.ToDoTaskContextConfig.ContextSelection;
import org.olat.modules.todo.ui.ToDoTaskMemberConfig.MemberSelection;
import org.olat.user.IdentitySelectionSource;
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
	private ObjectSelectionElement assignedEl;
	private ObjectSelectionElement delegatedEl;
	private SingleSelection statusEl;
	private SingleSelection priorityEl;
	private FormLayoutContainer startDateRowCont;
	private SingleSelection startDateModeEl;
	private DateChooser startDateEl;
	private RelativeDateElement startDateRelEl;
	private FormLayoutContainer dueDateRowCont;
	private SingleSelection dueDateModeEl;
	private DateChooser dueDateEl;
	private RelativeDateElement dueDateRelEl;
	private TextElement expenditureOfWorkEl;
	private TextAreaElement descriptionEl;

	private CloseableModalController cmc;
	private Controller contextPickerCtrl;

	private final ToDoTaskContextConfig contextConfig;
	private ToDoContext selectedContext;
	private final ToDoTaskMemberConfig assigneeConfig;
	private final ToDoTaskMemberConfig delegateeConfig;
	private final ToDoTaskDateConfig datesConfig;
	private final List<? extends TagInfo> allTags;
	private final boolean datesEditable;
	private Map<String, ToDoContext> keyToContext;

	@Autowired
	private ToDoService toDoService;

	public ToDoTaskEditForm(UserRequest ureq, WindowControl wControl, Form mainForm,
			ToDoTaskContextConfig contextConfig,
			ToDoTaskMemberConfig assigneeConfig,
			ToDoTaskMemberConfig delegateeConfig,
			ToDoTaskDateConfig datesConfig,
			List<? extends TagInfo> allTags, boolean datesEditable) {
		super(ureq, wControl, LAYOUT_CUSTOM, "todo_task_edit", mainForm);
		this.contextConfig = contextConfig;
		this.selectedContext = contextConfig.getCurrentContext();
		this.assigneeConfig = assigneeConfig;
		this.delegateeConfig = delegateeConfig;
		this.datesConfig = datesConfig != null ? datesConfig : ToDoTaskDateConfig.absoluteOnly();
		this.allTags = allTags;
		this.datesEditable = datesEditable;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		doEl = uifactory.addToggleButton("task.do", null, null, null, formLayout);
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

		assignedEl = createMembersElement(formLayout, "task.assigned", assigneeConfig);
		delegatedEl = createMembersElement(formLayout, "task.delegated", delegateeConfig);

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
			startDateModeEl.setElementCssClass("o_button_group_always_horizontal");
			startDateModeEl.select(MODE_ABSOLUTE, true);
			startDateModeEl.addActionListener(FormEvent.ONCHANGE);
			dueDateModeEl = uifactory.addButtonGroupSingleSelectHorizontal("task.due.date.mode", dueDateRowCont, modeSV);
			dueDateModeEl.setLabel(null, null);
			dueDateModeEl.setElementCssClass("o_button_group_always_horizontal");
			dueDateModeEl.select(MODE_ABSOLUTE, true);
			dueDateModeEl.addActionListener(FormEvent.ONCHANGE);
		}

		startDateEl = uifactory.addDateChooser("task.start.date", null, startDateRowCont);
		startDateEl.setEnabled(datesEditable);

		dueDateEl = uifactory.addDateChooser("task.due.date", null, dueDateRowCont);
		dueDateEl.setDefaultTimeAtEndOfDay(true);
		dueDateEl.setEnabled(datesEditable);

		if (datesConfig.getPicker() != null) {
			startDateRelEl = uifactory.addRelativeDateElement("startDateRel", null,
					startDateRowCont, getWindowControl(), datesConfig.getPicker().getContext());
			startDateRelEl.setAriaLabel(translate("task.start.date.rel"));
			startDateRelEl.setVisible(false);
			startDateRelEl.addActionListener(FormEvent.ONCHANGE);

			dueDateRelEl = uifactory.addRelativeDateElement("dueDateRel", null,
					dueDateRowCont, getWindowControl(), datesConfig.getPicker().getContext());
			dueDateRelEl.setAriaLabel(translate("task.due.date.rel"));
			dueDateRelEl.setVisible(false);
			dueDateRelEl.addActionListener(FormEvent.ONCHANGE);
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

	private ObjectSelectionElement createMembersElement(FormItemContainer formLayout, String name,
			ToDoTaskMemberConfig config) {
		ObjectSelectionElement el = uifactory.addObjectSelectionElement(name, name, formLayout,
				getWindowControl(), config.isMultiSelection(), config.getSource());
		el.setEnabled(config.getSelection() == MemberSelection.editable);
		el.setMandatory(config.isMandatory());
		if (config.getSelection() == MemberSelection.disabled && isMyOrNoneSelected(config.getSource())) {
			el.setVisible(false);
		}
		return el;
	}

	private boolean isMyOrNoneSelected(IdentitySelectionSource source) {
		if (source == null) return true;
		Collection<String> keys = source.getDefaultSelectedKeys();
		if (keys.isEmpty()) return true;
		return keys.size() == 1 && keys.iterator().next().equals(getIdentity().getKey().toString());
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
				startDateModeEl.select(MODE_RELATIVE, true);
				startDateRelEl.setValue(toRelativeDateSelection(rd.getStartRef(), rd.getStartUnit(), rd.getStartValue()));
				doSwitchDateMode(true);
			} else {
				startDateEl.setDate(values.getStartDate());
			}
			if (rd.getDueRef() != null) {
				dueDateModeEl.select(MODE_RELATIVE, true);
				dueDateRelEl.setValue(toRelativeDateSelection(rd.getDueRef(), rd.getDueUnit(), rd.getDueValue()));
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
		String myKey = getIdentity().getKey().toString();
		boolean isAssignee = assigneeConfig.getSource().getDefaultSelectedKeys().contains(myKey);
		boolean isDelegatee = delegateeConfig.getSource().getDefaultSelectedKeys().contains(myKey);
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
		if (startDateRelEl != null) {
			startDateRelEl.setEnabled(startEnabled);
		}
		boolean dueEnabled = ToDoRight.contains(assigneeRights, ToDoRight.dueDate);
		if (dueDateEl != null) {
			dueDateEl.setEnabled(dueEnabled);
		}
		if (dueDateModeEl != null) {
			dueDateModeEl.setEnabled(dueEnabled);
		}
		if (dueDateRelEl != null) {
			dueDateRelEl.setEnabled(dueEnabled);
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
		if (contextPickerCtrl == source) {
			if (event instanceof ToDoContextSelectedEvent tcse) {
				selectedContext = tcse.getContext();
				if (contextStaticEl != null) {
					contextStaticEl.setValue(getContextValue(selectedContext));
				}
				doRefreshRelativeDates();
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(contextPickerCtrl);
		removeAsListenerAndDispose(cmc);
		contextPickerCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == doEl) {
			doToggleStatus(doEl.isOn());
		} else if (source == contextChangeLink) {
			doOpenContextPicker(ureq);
		} else if (source == statusEl) {
			doToogleDo();
		} else if (source == startDateModeEl) {
			doSwitchDateMode(true);
		} else if (source == dueDateModeEl) {
			doSwitchDateMode(false);
		} else if (source == startDateRelEl && event.wasTriggerdBy(FormEvent.ONCHANGE)) {
			if (startDateRelEl.getValue() == null) {
				startDateModeEl.select(MODE_ABSOLUTE, true);
				doSwitchDateMode(true);
			}
		} else if (source == dueDateRelEl && event.wasTriggerdBy(FormEvent.ONCHANGE)) {
			if (dueDateRelEl.getValue() == null) {
				dueDateModeEl.select(MODE_ABSOLUTE, true);
				doSwitchDateMode(false);
			}
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
		if (assigneeConfig.isMandatory() && assignedEl.isVisible() && assignedEl.getSelectedKeys().isEmpty()) {
			assignedEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}

		delegatedEl.clearError();
		Set<String> assignedKeys = assignedEl.getSelectedKeys();
		Set<String> delegatedKeys = delegatedEl.getSelectedKeys();
		if (delegatedEl.isVisible() && !delegatedKeys.isEmpty()
				&& delegatedKeys.stream().allMatch(assignedKeys::contains)) {
			delegatedEl.setErrorKey("error.delegatee.is.assignee");
			allOk &= false;
		}

		boolean startRelative = isRelativeMode(startDateModeEl);
		boolean dueRelative = isRelativeMode(dueDateModeEl);

		startDateRowCont.clearError();
		dueDateRowCont.clearError();

		if (startRelative && startDateRelEl != null) {
			if (startDateRelEl.getValue() == null) {
				startDateRowCont.setErrorKey("form.mandatory.hover");
				allOk &= false;
			}
		}
		if (dueRelative && dueDateRelEl != null) {
			if (dueDateRelEl.getValue() == null) {
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
				RelativeDateSelection startSel = startDateRelEl != null ? startDateRelEl.getValue() : null;
				resolvedStart = startSel != null ? picker.resolve(toRelativeDates(startSel, true), true) : null;
			} else {
				resolvedStart = startDateEl.getDate();
			}
			Date resolvedDue;
			if (dueRelative) {
				RelativeDateSelection dueSel = dueDateRelEl != null ? dueDateRelEl.getValue() : null;
				resolvedDue = dueSel != null ? picker.resolve(toRelativeDates(dueSel, false), false) : null;
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
			if (startDateRelEl != null) {
				startDateRelEl.setVisible(relativeMode);
			}
		} else {
			dueDateEl.setVisible(!relativeMode);
			if (dueDateRelEl != null) {
				dueDateRelEl.setVisible(relativeMode);
			}
		}
	}

	private void doRefreshRelativeDates() {
		ToDoTaskDatePicker picker = datesConfig.getPicker();
		if (picker == null) {
			return;
		}
		picker.contextChanged(selectedContext);
		if (startDateRelEl != null) {
			startDateRelEl.setContext(picker.getContext());
			startDateRelEl.refreshDisplay();
		}
		if (dueDateRelEl != null) {
			dueDateRelEl.setContext(picker.getContext());
			dueDateRelEl.refreshDisplay();
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

private ToDoRelativeDates toRelativeDates(RelativeDateSelection sel, boolean forStart) {
		String composedRef;
		ToDoDateUnit unit;
		Integer value = null;
		if (!sel.isOffsetEnabled()) {
			composedRef = "SAME_DAY_" + sel.getRefKey();
			unit = ToDoDateUnit.SAME_DAY;
		} else {
			composedRef = sel.getDirection().name() + "_" + sel.getRefKey();
			unit = ToDoDateUnit.valueOf(sel.getUnitKey());
			value = sel.getValue();
		}
		ToDoRelativeDates rd = new ToDoRelativeDates();
		if (forStart) {
			rd.setStartRef(composedRef);
			rd.setStartUnit(unit);
			rd.setStartValue(value);
		} else {
			rd.setDueRef(composedRef);
			rd.setDueUnit(unit);
			rd.setDueValue(value);
		}
		return rd;
	}

	private RelativeDateSelection toRelativeDateSelection(String composedRef, ToDoDateUnit unit, Integer value) {
		if (composedRef == null) {
			return null;
		}
		if (composedRef.startsWith("SAME_DAY_")) {
			String refKey = composedRef.substring("SAME_DAY_".length());
			return new RelativeDateSelection(refKey, OffsetDirection.BEFORE, null, null, false);
		}
		int underscore = composedRef.indexOf('_');
		OffsetDirection direction = OffsetDirection.valueOf(composedRef.substring(0, underscore));
		String refKey = composedRef.substring(underscore + 1);
		String unitKey = unit != null ? unit.name() : null;
		return new RelativeDateSelection(refKey, direction, unitKey, value, true);
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
		return IdentitySelectionSource.toRefs(assignedEl.getSelectedKeys());
	}

	public Collection<? extends IdentityRef> getDelegatees() {
		return IdentitySelectionSource.toRefs(delegatedEl.getSelectedKeys());
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
		if (startRelative && startDateRelEl != null && startDateRelEl.getValue() != null) {
			ToDoRelativeDates startRd = toRelativeDates(startDateRelEl.getValue(), true);
			rd.setStartRef(startRd.getStartRef());
			rd.setStartUnit(startRd.getStartUnit());
			rd.setStartValue(startRd.getStartValue());
		}
		if (dueRelative && dueDateRelEl != null && dueDateRelEl.getValue() != null) {
			ToDoRelativeDates dueRd = toRelativeDates(dueDateRelEl.getValue(), false);
			rd.setDueRef(dueRd.getDueRef());
			rd.setDueUnit(dueRd.getDueUnit());
			rd.setDueValue(dueRd.getDueValue());
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
