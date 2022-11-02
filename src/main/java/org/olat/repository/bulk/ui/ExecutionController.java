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
package org.olat.repository.bulk.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.SettingsBulkEditables;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.bulk.model.SettingsContext.LifecycleType;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExecutionController extends StepFormBasicController {
	
	private static final String[] CHANGE_KEYS = new String[] {"change"};
	
	private final List<MultipleSelectionElement> checkboxSwitch = new ArrayList<>(2);
	private final Map<MultipleSelectionElement, FormLayoutContainer> checkboxContainer = new HashMap<>(checkboxSwitch.size());
	private MultipleSelectionElement dateTypeCheckboxEl;
	private SingleSelection dateTypesEl;
	private MultipleSelectionElement publicDatesCheckboxEl;
	private SingleSelection publicDatesEl;
	private MultipleSelectionElement startDateCheckboxEl;
	private DateChooser startDateEl;
	private MultipleSelectionElement endDateCheckboxEl;
	private DateChooser endDateEl;
	private TextElement locationEl;
	
	private final SettingsContext context;
	private final SettingsBulkEditables editables;
	private final String[] changeValues;
	
	@Autowired
	private RepositoryEntryLifecycleDAO lifecycleDao;

	
	public ExecutionController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.context = (SettingsContext)runContext.get(SettingsContext.DEFAULT_KEY);
		this.editables = (SettingsBulkEditables)runContext.get(SettingsBulkEditables.DEFAULT_KEY);
		this.changeValues = new String[] {translate("settings.bulk.change")};
		
		initForm(ureq);
		updateLifecycleUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer executionCont = FormLayoutContainer.createDefaultFormLayout("executionCont", getTranslator());
		executionCont.setFormTitle(translate("settings.bulk.execution.title"));
		executionCont.setFormInfo(RepositoryBulkUIFactory.getSettingsDescription(getTranslator(), context.getRepositoryEntries(), "settings.bulk.change.fields"));
		executionCont.setRootForm(mainForm);
		formLayout.add(executionCont);
		
		String courseOnlyInfo = "<i class='o_icon o_icon_warn'> </i> " + translate("settings.bulk.course.only.multi");
		StaticTextElement courseOnlyEl = uifactory.addStaticTextElement("course.only.info", null, courseOnlyInfo, executionCont);
		courseOnlyEl.setElementCssClass("o_form_explanation");
		
		SelectionValues dateTypeSV = new SelectionValues();
		dateTypeSV.add(entry(LifecycleType.none.name(), translate("settings.bulk.execution.period.none")));
		dateTypeSV.add(entry(LifecycleType.privateCycle.name(), translate("settings.bulk.execution.period.private")));
		dateTypeSV.add(entry(LifecycleType.publicCycle.name(), translate("settings.bulk.execution.period.public")));
		dateTypesEl = uifactory.addRadiosVertical("settings.bulk.execution.period", executionCont, dateTypeSV.keys(), dateTypeSV.values());
		dateTypesEl.addActionListener(FormEvent.ONCHANGE);
		dateTypesEl.setHelpText(translate("cif.dates.help"));
		if (context.getLifecycleType() != null) {
			dateTypesEl.select(context.getLifecycleType().name(), true);
		} else {
			dateTypesEl.select(LifecycleType.none.name(), true);
		}
		dateTypeCheckboxEl = decorate(dateTypesEl, executionCont, SettingsBulkEditable.lifecycleType);

		List<RepositoryEntryLifecycle> cycles = lifecycleDao.loadPublicLifecycle();
		List<RepositoryEntryLifecycle> filteredCycles = new ArrayList<>();
		//just make the upcomming and acutual running cycles or the pre-selected visible in the UI
		LocalDateTime now = LocalDateTime.now();
		for(RepositoryEntryLifecycle cycle:cycles) {
			if(cycle.getValidTo() == null
					|| now.isBefore(LocalDateTime.ofInstant(cycle.getValidTo().toInstant(), ZoneId.systemDefault()))) {
				filteredCycles.add(cycle);
			}
		}
		
		String[] publicKeys = new String[filteredCycles.size()];
		String[] publicValues = new String[filteredCycles.size()];
		int count = 0;
		for(RepositoryEntryLifecycle cycle:filteredCycles) {
				publicKeys[count] = cycle.getKey().toString();
				
				StringBuilder sb = new StringBuilder(32);
				boolean labelAvailable = StringHelper.containsNonWhitespace(cycle.getLabel());
				if(labelAvailable) {
					sb.append(cycle.getLabel());
				}
				if(StringHelper.containsNonWhitespace(cycle.getSoftKey())) {
					if(labelAvailable) sb.append(" - ");
					sb.append(cycle.getSoftKey());
				}
				publicValues[count++] = sb.toString();
		}
		publicDatesEl = uifactory.addDropdownSingleselect("settings.bulk.execution.public", executionCont, publicKeys, publicValues, null);
		if (context.getLifecyclePublicKey() != null && publicDatesEl.containsKey(context.getLifecyclePublicKey().toString())) {
			publicDatesEl.select(context.getLifecyclePublicKey().toString(), true);
		}
		publicDatesCheckboxEl = decorate(publicDatesEl, executionCont, SettingsBulkEditable.lifecyclePublicKey);
		
		startDateEl = uifactory.addDateChooser("settings.bulk.execution.from", context.getLifecycleValidFrom(), executionCont);
		startDateCheckboxEl = decorate(startDateEl, executionCont, SettingsBulkEditable.lifecycleValidFrom);
		
		endDateEl = uifactory.addDateChooser("settings.bulk.execution.to", context.getLifecycleValidTo(), executionCont);
		endDateCheckboxEl = decorate(endDateEl, executionCont, SettingsBulkEditable.lifecycleValidTo);
		
		locationEl = uifactory.addTextElement("settings.bulk.location", 255, context.getLocation(), executionCont);
		decorate(locationEl, executionCont, SettingsBulkEditable.location);
	}
	
	private MultipleSelectionElement decorate(FormItem item, FormLayoutContainer formLayout, SettingsBulkEditable editable) {
		boolean selected = context.isSelected(editable);
		String itemName = item.getName();
		MultipleSelectionElement checkbox = uifactory.addCheckboxesHorizontal("cbx_" + itemName, itemName, formLayout, CHANGE_KEYS, changeValues);
		checkbox.select(checkbox.getKey(0), selected);
		checkbox.setEnabled(editables.isEditable(editable));
		checkbox.addActionListener(FormEvent.ONCLICK);
		checkbox.setUserObject(item);
		checkboxSwitch.add(checkbox);
		
		item.setLabel(null, null);
		item.setVisible(selected);
		item.setUserObject(checkbox);
		
		checkboxContainer.put(checkbox, formLayout);
		formLayout.moveBefore(checkbox, item);
		return checkbox;
	}

	private void updateLifecycleUI() {
		updateVisible(publicDatesCheckboxEl, true);
		updateVisible(startDateCheckboxEl, true);
		updateVisible(endDateCheckboxEl, true);
		
		if (dateTypeCheckboxEl.isAtLeastSelected(1) && dateTypesEl.isOneSelected()) {
			if (LifecycleType.none.name().equals(dateTypesEl.getSelectedKey())) {
				updateVisible(publicDatesCheckboxEl, false);
				updateVisible(startDateCheckboxEl, false);
				updateVisible(endDateCheckboxEl, false);
			} else if (LifecycleType.publicCycle.name().equals(dateTypesEl.getSelectedKey())) {
				updateVisible(publicDatesCheckboxEl, true);
				updateVisible(startDateCheckboxEl, false);
				updateVisible(endDateCheckboxEl, false);
			} else if (LifecycleType.privateCycle.name().equals(dateTypesEl.getSelectedKey())) {
				updateVisible(publicDatesCheckboxEl, false);
				updateVisible(startDateCheckboxEl, true);
				updateVisible(endDateCheckboxEl, true);
			}
		}
	}
	
	private void updateVisible(MultipleSelectionElement el, boolean visible) {
		el.setVisible(visible);
		((FormItem)el.getUserObject()).setVisible(visible && el.isAtLeastSelected(1));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == dateTypesEl) {
			updateLifecycleUI();
		} else if (checkboxSwitch.contains(source)) {
			MultipleSelectionElement checkbox = (MultipleSelectionElement)source;
			FormItem item = (FormItem)checkbox.getUserObject();
			item.setVisible(checkbox.isAtLeastSelected(1));
			checkboxContainer.get(checkbox).setDirty(true);
			
			if (item == dateTypesEl) {
				updateLifecycleUI();
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.select(SettingsBulkEditable.location, locationEl.isVisible());
		if (locationEl.isVisible()) {
			context.setLocation(locationEl.getValue().trim());
		}
		
		context.select(SettingsBulkEditable.lifecycleType, dateTypesEl.isVisible() && dateTypesEl.isOneSelected());
		if (dateTypesEl.isVisible() && dateTypesEl.isOneSelected()) {
			context.setLifecycleType(LifecycleType.valueOf(dateTypesEl.getSelectedKey()));
		}
		
		context.select(SettingsBulkEditable.lifecyclePublicKey, publicDatesEl.isVisible() && publicDatesEl.isOneSelected());
		if (publicDatesEl.isVisible() && publicDatesEl.isOneSelected()) {
			context.setLifecyclePublicKey(Long.valueOf(publicDatesEl.getSelectedKey()));
		}
		
		context.select(SettingsBulkEditable.lifecycleValidFrom, startDateEl.isVisible());
		if (startDateEl.isVisible()) {
			context.setLifecycleValidFrom(startDateEl.getDate());
		}
		
		context.select(SettingsBulkEditable.lifecycleValidTo, endDateEl.isVisible());
		if (endDateEl.isVisible()) {
			context.setLifecycleValidTo(endDateEl.getDate());
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

}
