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
package org.olat.modules.todo.ui;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.todo.ToDoDateUnit;
import org.olat.modules.todo.ToDoRelativeDates;

/**
 *
 * Initial date: 2026-05-18<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class ToDoRelativeDatePickerController extends FormBasicController {

	private static final String REF_PREFIX_SAME_DAY = "SAME_DAY_";
	private static final String REF_PREFIX_BEFORE = "BEFORE_";
	private static final String REF_PREFIX_AFTER = "AFTER_";

	private SingleSelection refEl;
	private FormToggle offsetToggleEl;
	private FormLayoutContainer offsetRowCont;
	private TextElement offsetValueEl;
	private SingleSelection offsetDirEl;
	private SingleSelection unitEl;
	private StaticTextElement calculatedEl;
	private FormLink removeLink;

	private final SelectionValues refOptions;
	private final ToDoDateResolver resolver;
	private final boolean start;

	public ToDoRelativeDatePickerController(UserRequest ureq, WindowControl wControl,
			SelectionValues refOptions, ToDoDateResolver resolver, ToDoRelativeDates current, boolean start) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.refOptions = refOptions;
		this.resolver = resolver;
		this.start = start;
		initForm(ureq);
		updateUI(current);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		refEl = uifactory.addRadiosVertical("rel.ref", "task.date.relative.callout.ref.label",
				formLayout, refOptions.keys(), refOptions.values());
		if (refOptions.size() > 0) {
			refEl.select(refOptions.keys()[0], true);
		}
		refEl.addActionListener(FormEvent.ONCHANGE);

		offsetToggleEl = uifactory.addToggleButton("rel.offset.with", "task.date.relative.callout.offset.with",
			"on", "off", formLayout);
		offsetToggleEl.addActionListener(FormEvent.ONCHANGE);

		offsetRowCont = FormLayoutContainer.createInlineFormLayout("offset.row", getTranslator());
		offsetRowCont.setLabel("task.date.relative.callout.offset", null);
		offsetRowCont.setElementCssClass("o_todo_relative_offset_row");
		formLayout.add("offset.row", offsetRowCont);

		offsetValueEl = uifactory.addTextElement("rel.offset.value", null, 6, null, offsetRowCont);
		offsetValueEl.setAriaLabel(translate("task.date.relative.callout.offset"));
		offsetValueEl.setDisplaySize(6);
		offsetValueEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues dirSV = new SelectionValues();
		dirSV.add(SelectionValues.entry("BEFORE", translate("task.date.relative.callout.offset.before")));
		dirSV.add(SelectionValues.entry("AFTER",  translate("task.date.relative.callout.offset.after")));
		offsetDirEl = uifactory.addButtonGroupSingleSelectHorizontal("rel.offset.dir", offsetRowCont, dirSV);
		offsetDirEl.select("BEFORE", true);
		offsetDirEl.setAriaLabel(translate("task.date.relative.callout.offset.dir.aria"));
		offsetDirEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues unitSV = new SelectionValues();
		unitSV.add(SelectionValues.entry(ToDoDateUnit.DAYS.name(),   translate("unit.days")));
		unitSV.add(SelectionValues.entry(ToDoDateUnit.WEEKS.name(),  translate("unit.weeks")));
		unitSV.add(SelectionValues.entry(ToDoDateUnit.MONTHS.name(), translate("unit.months")));
		unitSV.add(SelectionValues.entry(ToDoDateUnit.YEARS.name(),  translate("unit.years")));
		unitEl = uifactory.addButtonGroupSingleSelectHorizontal("rel.unit", formLayout, unitSV);
		unitEl.setLabel("task.date.relative.callout.unit", null);
		unitEl.setElementCssClass("o_todo_unit_row");
		unitEl.select(ToDoDateUnit.DAYS.name(), true);
		unitEl.addActionListener(FormEvent.ONCHANGE);

		calculatedEl = uifactory.addStaticTextElement("rel.calculated", "task.date.relative.callout.calculated", "", formLayout);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("apply", "task.date.relative.callout.apply", buttonLayout);
		removeLink = uifactory.addFormLink("remove", "task.date.relative.callout.remove", null, buttonLayout, Link.BUTTON);
	}

	private void updateUI(ToDoRelativeDates rd) {
		if (rd == null) return;
		String storedRef = start ? rd.getStartRef() : rd.getDueRef();
		ToDoDateUnit unit = start ? rd.getStartUnit() : rd.getDueUnit();
		Integer value = start ? rd.getStartValue() : rd.getDueValue();
		if (storedRef == null) return;

		String prefix;
		String refOptionKey;
		if (storedRef.startsWith(REF_PREFIX_SAME_DAY)) {
			prefix = "SAME_DAY";
			refOptionKey = storedRef.substring(REF_PREFIX_SAME_DAY.length());
		} else if (storedRef.startsWith(REF_PREFIX_BEFORE)) {
			prefix = "BEFORE";
			refOptionKey = storedRef.substring(REF_PREFIX_BEFORE.length());
		} else if (storedRef.startsWith(REF_PREFIX_AFTER)) {
			prefix = "AFTER";
			refOptionKey = storedRef.substring(REF_PREFIX_AFTER.length());
		} else {
			return;
		}

		if (refEl.containsKey(refOptionKey)) {
			refEl.select(refOptionKey, true);
		}

		if ("SAME_DAY".equals(prefix)) {
			offsetToggleEl.toggleOff();
		} else {
			offsetToggleEl.toggleOn();
			if (offsetDirEl.containsKey(prefix)) {
				offsetDirEl.select(prefix, true);
			}
			if (value != null) {
				offsetValueEl.setValue(String.valueOf(value));
			}
			if (unit != null && unitEl.containsKey(unit.name())) {
				unitEl.select(unit.name(), true);
			}
		}
	}

	private void updateUI() {
		boolean hasOffset = offsetToggleEl.isOn();
		offsetRowCont.setVisible(hasOffset);
		unitEl.setVisible(hasOffset);
		calculatedEl.setVisible(hasOffset);
		if (hasOffset) {
			updateCalculatedDate();
		}
	}

	private void updateCalculatedDate() {
		String refOptionKey = getSelectedRefKey();
		String dirKey = offsetDirEl.isOneSelected()
				? offsetDirEl.getSelectedKey()
				: "BEFORE";
		String composedRef = dirKey + "_" + refOptionKey;

		Integer value = null;
		String valueStr = offsetValueEl.getValue();
		if (StringHelper.containsNonWhitespace(valueStr)) {
			try {
				value = Integer.parseInt(valueStr.trim());
			} catch (NumberFormatException e) {
				// leave value null
			}
		}

		ToDoDateUnit unit = unitEl.isOneSelected()
				? ToDoDateUnit.valueOf(unitEl.getSelectedKey())
				: ToDoDateUnit.DAYS;

		Date resolved = resolver.resolve(composedRef, unit, value);
		calculatedEl.setValue(resolved != null
				? Formatter.getInstance(getLocale()).formatDate(resolved)
				: translate("task.date.relative.callout.calculated.empty"));
	}

	private String getSelectedRefKey() {
		return refEl.isOneSelected()
				? refEl.getSelectedKey()
				: refOptions.keys()[0];
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == offsetToggleEl) {
			updateUI();
		} else if (source == refEl || source == offsetDirEl || source == unitEl || source == offsetValueEl) {
			updateCalculatedDate();
		} else if (source == removeLink) {
			doRemove(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new ToDoRelativeDateSelectedEvent(buildRelativeDates(), start));
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		offsetRowCont.clearError();
		if (offsetToggleEl.isOn()) {
			String valueStr = offsetValueEl.getValue();
			if (!StringHelper.containsNonWhitespace(valueStr)) {
				offsetRowCont.setErrorKey("form.mandatory.hover");
				allOk = false;
			} else {
				try {
					int parsed = Integer.parseInt(valueStr.trim());
					if (parsed <= 0) {
						offsetRowCont.setErrorKey("form.error.positive.integer");
						allOk = false;
					}
				} catch (NumberFormatException e) {
					offsetRowCont.setErrorKey("form.error.positive.integer");
					allOk = false;
				}
			}
		}

		return allOk;
	}

	private void doRemove(UserRequest ureq) {
		fireEvent(ureq, new ToDoRelativeDateSelectedEvent(null, start));
	}

	private ToDoRelativeDates buildRelativeDates() {
		String refOptionKey = getSelectedRefKey();
		String composedRef;
		ToDoDateUnit unit;
		Integer value = null;

		if (!offsetToggleEl.isOn()) {
			composedRef = REF_PREFIX_SAME_DAY + refOptionKey;
			unit = ToDoDateUnit.SAME_DAY;
		} else {
			String dirKey = offsetDirEl.isOneSelected()
					? offsetDirEl.getSelectedKey()
					: "BEFORE";
			composedRef = dirKey + "_" + refOptionKey;
			unit = unitEl.isOneSelected()
					? ToDoDateUnit.valueOf(unitEl.getSelectedKey())
					: ToDoDateUnit.DAYS;
			String valueStr = offsetValueEl.getValue();
			if (StringHelper.containsNonWhitespace(valueStr)) {
				try {
					value = Integer.parseInt(valueStr.trim());
				} catch (NumberFormatException e) {
					value = null;
				}
			}
		}

		ToDoRelativeDates rd = new ToDoRelativeDates();
		if (start) {
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

}
