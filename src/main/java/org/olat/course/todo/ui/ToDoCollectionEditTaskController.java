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
package org.olat.course.todo.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.todo.model.ToDoTaskCollectionEditContext;
import org.olat.course.todo.model.ToDoTaskCollectionEditContext.Field;
import org.olat.modules.todo.ToDoExpenditureOfWork;
import org.olat.modules.todo.ToDoPriority;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 Jan 2024<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ToDoCollectionEditTaskController extends StepFormBasicController {
	
	private static final String[] CHANGE_KEYS = new String[] {"change"};

	private final String[] changeValues;
	private final String[] overrideValues;
	private final List<MultipleSelectionElement> checkboxSwitch = new ArrayList<>(6);
	private final Map<MultipleSelectionElement, FormLayoutContainer> checkboxContainer = new HashMap<>(checkboxSwitch.size());
	private final Map<MultipleSelectionElement, MultipleSelectionElement> checkboxOverride = new HashMap<>(checkboxSwitch.size());
	private TextElement titleEl;
	private TagSelection tagsEl;
	private SingleSelection statusEl;
	private SingleSelection priorityEl;
	private DateChooser startDateEl;
	private DateChooser dueDateEl;
	private TextElement expenditureOfWorkEl;
	private TextAreaElement descriptionEl;
	
	private List<TagInfo> tagInfos;
	private final ToDoTaskCollectionEditContext context;

	@Autowired
	private ToDoService toDoService;

	public ToDoCollectionEditTaskController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, List<TagInfo> tagInfos) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(ToDoUIFactory.class, getLocale(), getTranslator()));
		
		this.tagInfos = tagInfos;
		context = (ToDoTaskCollectionEditContext)getFromRunContext(ToDoTaskCollectionEditContext.KEY);
		
		changeValues = new String[] {translate("change")};
		overrideValues = new String[] {translate("override.customizations")};
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("course.todo.collection.todo.step");
		
		FormLayoutContainer metadataCont = FormLayoutContainer.createDefaultFormLayout("metadataCont", getTranslator());
		metadataCont.setRootForm(mainForm);
		formLayout.add(metadataCont);
		
		titleEl = uifactory.addTextElement("task.title", 120, context.getTitle(), metadataCont);
		titleEl.setMandatory(true);
		decorate(titleEl, metadataCont, ToDoTaskCollectionEditContext.Field.title);
		
		tagsEl = uifactory.addTagSelection("tags", "tags", metadataCont, getWindowControl(), tagInfos);
		decorate(tagsEl, metadataCont, ToDoTaskCollectionEditContext.Field.tagDisplayNames);
		
		SelectionValues statusSV = new SelectionValues();
		statusSV.add(getSVEntry(ToDoStatus.open));
		statusSV.add(getSVEntry(ToDoStatus.inProgress));
		statusSV.add(getSVEntry(ToDoStatus.done));
		statusEl = uifactory.addDropdownSingleselect("task.status", metadataCont, statusSV.keys(), statusSV.values(), statusSV.icons());
		ToDoStatus status = context.getStatus() != null? context.getStatus(): ToDoStatus.open;
		statusEl.select(status.name(), true);
		decorate(statusEl, metadataCont, ToDoTaskCollectionEditContext.Field.status);
		
		SelectionValues prioritySV = new SelectionValues();
		prioritySV.add(getSVEntry(ToDoPriority.urgent));
		prioritySV.add(getSVEntry(ToDoPriority.high));
		prioritySV.add(getSVEntry(ToDoPriority.medium));
		prioritySV.add(getSVEntry(ToDoPriority.low));
		priorityEl = uifactory.addDropdownSingleselect("task.priority", metadataCont, prioritySV.keys(), prioritySV.values(), prioritySV.icons());
		priorityEl.enableNoneSelection();
		if (context.getPriority() != null) {
			priorityEl.select(context.getPriority().name(), true);
		}
		decorate(priorityEl, metadataCont, ToDoTaskCollectionEditContext.Field.priority);
		
		startDateEl = uifactory.addDateChooser("task.start.date", context.getStartDate(), metadataCont);
		decorate(startDateEl, metadataCont, ToDoTaskCollectionEditContext.Field.startDate);
		
		dueDateEl = uifactory.addDateChooser("task.due.date", context.getDueDate(), metadataCont);
		dueDateEl.setDefaultTimeAtEndOfDay(true);
		decorate(dueDateEl, metadataCont, ToDoTaskCollectionEditContext.Field.dueDate);
		
		ToDoExpenditureOfWork expenditureOfWork = toDoService.getExpenditureOfWork(context.getExpenditureOfWork());
		String formattedExpenditureOfWork = ToDoUIFactory.format(expenditureOfWork);
		expenditureOfWorkEl = uifactory.addTextElement("task.expenditure.of.work", 30, formattedExpenditureOfWork, metadataCont);
		expenditureOfWorkEl.setDomReplacementWrapperRequired(false);
		flc.contextPut("eowId", expenditureOfWorkEl.getForId());
		decorate(expenditureOfWorkEl, metadataCont, ToDoTaskCollectionEditContext.Field.expenditureOfWork);
		
		descriptionEl = uifactory.addTextAreaElement("task.description", "task.description", -1, 3, 40, true, false, context.getDescription(), metadataCont);
		decorate(descriptionEl, metadataCont, ToDoTaskCollectionEditContext.Field.description);
	}

	private SelectionValue getSVEntry(ToDoStatus status) {
		return new SelectionValue(status.name(), ToDoUIFactory.getDisplayName(getTranslator(), status), null,
				ToDoUIFactory.getIconCss(status), null, true);
	}
	
	private SelectionValue getSVEntry(ToDoPriority prioriry) {
		return new SelectionValue(prioriry.name(), ToDoUIFactory.getDisplayName(getTranslator(), prioriry), null,
				ToDoUIFactory.getIconCss(prioriry), null, true);
	}
	
	private MultipleSelectionElement decorate(FormItem item, FormLayoutContainer formLayout, ToDoTaskCollectionEditContext.Field field) {
		boolean selected = context.isSelected(field);
		String itemName = item.getName();
		MultipleSelectionElement checkbox = uifactory.addCheckboxesHorizontal("cbx_" + itemName, itemName, formLayout, CHANGE_KEYS, changeValues);
		checkbox.select(checkbox.getKey(0), selected);
		checkbox.addActionListener(FormEvent.ONCLICK);
		checkbox.setUserObject(item);
		
		boolean override = context.isOverride(field);
		MultipleSelectionElement overrideEl = uifactory.addCheckboxesHorizontal("ovr_" + itemName, itemName, formLayout, CHANGE_KEYS, overrideValues);
		overrideEl.select(overrideEl.getKey(0), override);
		overrideEl.setLabel(null, null);
		overrideEl.setVisible(selected);
		overrideEl.setUserObject(item);
		
		item.setLabel(null, null);
		item.setVisible(selected);
		item.setUserObject(checkbox);
		
		checkboxSwitch.add(checkbox);
		checkboxContainer.put(checkbox, formLayout);
		formLayout.moveBefore(checkbox, item);
		checkboxOverride.put(checkbox, overrideEl);
		
		return overrideEl;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (checkboxSwitch.contains(source)) {
			MultipleSelectionElement checkbox = (MultipleSelectionElement)source;
			FormItem item = (FormItem)checkbox.getUserObject();
			item.setVisible(checkbox.isAtLeastSelected(1));
			MultipleSelectionElement overrideEl = checkboxOverride.get(checkbox);
			overrideEl.setVisible(checkbox.isAtLeastSelected(1));
			checkboxContainer.get(checkbox).setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if (titleEl.isVisible() && !StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		dueDateEl.clearError();
		if (startDateEl.isVisible() && dueDateEl.isVisible() 
				&& startDateEl.getDate() != null && dueDateEl.getDate() != null 
				&& startDateEl.getDate().after(dueDateEl.getDate())) {
			dueDateEl.setErrorKey("error.start.after.due");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.select(Field.title, titleEl.isVisible());
		if (titleEl.isVisible()) {
			context.setTitle(titleEl.getValue().trim());
			boolean override = checkboxOverride.get(titleEl.getUserObject()).isAtLeastSelected(1);
			context.override(Field.title, override);
		}
		
		context.select(Field.description, descriptionEl.isVisible());
		if (descriptionEl.isVisible()) {
			context.setDescription(descriptionEl.getValue().trim());
			boolean override = checkboxOverride.get(descriptionEl.getUserObject()).isAtLeastSelected(1);
			context.override(Field.description, override);
		}
		
		context.select(Field.status, statusEl.isVisible());
		if (statusEl.isVisible()) {
			ToDoStatus status = statusEl.isOneSelected() ? ToDoStatus.valueOf(statusEl.getSelectedKey()): null;
			context.setStatus(status);
			boolean override = checkboxOverride.get(statusEl.getUserObject()).isAtLeastSelected(1);
			context.override(Field.status, override);
		}
		
		context.select(Field.priority, priorityEl.isVisible());
		if (priorityEl.isVisible()) {
			context.setPriority(ToDoPriority.valueOf(priorityEl.getSelectedKey()));
			boolean override = checkboxOverride.get(priorityEl.getUserObject()).isAtLeastSelected(1);
			context.override(Field.priority, override);
		}
		
		context.select(Field.startDate, startDateEl.isVisible());
		if (startDateEl.isVisible()) {
			context.setStartDate(startDateEl.getDate());
			boolean override = checkboxOverride.get(startDateEl.getUserObject()).isAtLeastSelected(1);
			context.override(Field.startDate, override);
		}
		
		context.select(Field.dueDate, dueDateEl.isVisible());
		if (dueDateEl.isVisible()) {
			context.setDueDate(dueDateEl.getDate());
			boolean override = checkboxOverride.get(dueDateEl.getUserObject()).isAtLeastSelected(1);
			context.override(Field.dueDate, override);
		}
		
		context.select(Field.expenditureOfWork, expenditureOfWorkEl.isVisible());
		if (expenditureOfWorkEl.isVisible()) {
			ToDoExpenditureOfWork expenditureOfWork = ToDoUIFactory.parseHours(expenditureOfWorkEl.getValue());
			Long hours = toDoService.getHours(expenditureOfWork);
			context.setExpenditureOfWork(hours);
			boolean override = checkboxOverride.get(expenditureOfWorkEl.getUserObject()).isAtLeastSelected(1);
			context.override(Field.expenditureOfWork, override);
		}
		
		context.select(Field.tagDisplayNames, tagsEl.isVisible());
		if (tagsEl.isVisible()) {
			context.setTagDisplayNames(tagsEl.getDisplayNames());
			boolean override = checkboxOverride.get(tagsEl.getUserObject()).isAtLeastSelected(1);
			context.override(Field.tagDisplayNames, override);
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
