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
package org.olat.modules.catalog.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Nov 19, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class SortPriorityEditController extends FormBasicController {
	
	private static final String KEY_CUSTOM = "-1";

	private SingleSelection priorityEl;
	private TextElement priorityValueEl;
	
	private Integer sortPriority;

	public SortPriorityEditController(UserRequest ureq, WindowControl wControl, Integer sortPriority) {
		super(ureq, wControl);
		this.sortPriority = sortPriority;
		
		initForm(ureq);
	}

	public Integer getSortPriority() {
		return sortPriority;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues prioritySV = new SelectionValues();
		CatalogV2UIFactory.SORT_PRIORITIES.forEach(priority -> prioritySV.add(SelectionValues.entry(
				String.valueOf(priority),
				CatalogV2UIFactory.translateSortPriority(getTranslator(), priority, false))));
		prioritySV.add(SelectionValues.entry(KEY_CUSTOM, CatalogV2UIFactory.translateSortPriority(getTranslator(), -1, false)));
		priorityEl = uifactory.addDropdownSingleselect("sort.priority.priority", formLayout, prioritySV.keys(), prioritySV.values());
		priorityEl.addActionListener(FormEvent.ONCHANGE);
		if (sortPriority == null) {
			priorityEl.select("0", true);
		} else if (CatalogV2UIFactory.SORT_PRIORITIES.contains(sortPriority)) {
			priorityEl.select(String.valueOf(sortPriority), true);
		} else {
			priorityEl.select(KEY_CUSTOM, true);
		}
		
		priorityValueEl = uifactory.addTextElement("sort.priority.boost", 6,
				String.valueOf(CatalogV2UIFactory.normalPriorityIfNull(sortPriority)), formLayout);
		updateValueUI();
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void updateValueUI() {
		if (priorityEl.isOneSelected()) {
			Integer selectedValue = Integer.valueOf(priorityEl.getSelectedKey());
			if (CatalogV2UIFactory.SORT_PRIORITIES.contains(selectedValue)) {
				priorityValueEl.setValue(priorityEl.getSelectedKey());
				priorityValueEl.setEnabled(false);
				priorityValueEl.setMandatory(false);
			} else {
				priorityValueEl.setEnabled(true);
				priorityValueEl.setMandatory(true);
			}
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == priorityEl) {
			updateValueUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		priorityValueEl.clearError();
		if (!StringHelper.containsNonWhitespace(priorityValueEl.getValue())) {
			priorityValueEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			try {
				Integer days = Integer.parseInt(priorityValueEl.getValue());
				if (days.intValue() < 0) {
					priorityValueEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				priorityValueEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		sortPriority = Integer.valueOf(priorityValueEl.getValue());
		fireEvent(ureq, Event.DONE_EVENT);
	}

}
