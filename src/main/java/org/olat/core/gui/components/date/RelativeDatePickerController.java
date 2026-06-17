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
package org.olat.core.gui.components.date;

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
import org.olat.core.util.Util;


/**
 * Initial date: 2026-06-15<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class RelativeDatePickerController extends FormBasicController {

	protected SingleSelection refEl;
	protected FormToggle offsetToggleEl;
	protected FormLayoutContainer offsetRowCont;
	protected TextElement offsetValueEl;
	protected SingleSelection offsetDirEl;
	protected SingleSelection unitEl;
	protected StaticTextElement calculatedEl;
	protected FormLink removeLink;

	private final RelativeDateContext context;
	private final RelativeDateSelection initialSelection;

	public RelativeDatePickerController(UserRequest ureq, WindowControl wControl,
			RelativeDateContext context, RelativeDateSelection initialSelection) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.context = context;
		this.initialSelection = initialSelection;
		setTranslator(Util.createPackageTranslator(RelativeDatePickerController.class, getLocale(), getTranslator()));
		initRelativeForm(ureq);
	}

	protected SelectionValues getReferenceSelectionValues() {
		return context.getReferenceSelectionValues();
	}

	protected SelectionValues getUnitSelectionValues() {
		return context.getUnitSelectionValues();
	}

	protected RelativeDateSelection getInitialSelection() {
		return initialSelection;
	}

	protected Date resolveDate(RelativeDateSelection sel) {
		return context.resolveDate(sel);
	}

	protected final void initRelativeForm(UserRequest ureq) {
		initForm(ureq);
		applyInitialSelection(getInitialSelection());
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues refSV = getReferenceSelectionValues();
		refEl = uifactory.addRadiosVertical("rel.ref", "relative.date.callout.ref.label",
				formLayout, refSV.keys(), refSV.values());
		if (refSV.size() > 0) {
			refEl.select(refSV.keys()[0], true);
		}
		refEl.addActionListener(FormEvent.ONCHANGE);

		offsetToggleEl = uifactory.addToggleButton("rel.offset.with", "relative.date.callout.offset.with",
				"on", "off", formLayout);
		offsetToggleEl.addActionListener(FormEvent.ONCHANGE);

		offsetRowCont = FormLayoutContainer.createInlineFormLayout("offset.row", getTranslator());
		offsetRowCont.setLabel("relative.date.callout.offset", null);
		offsetRowCont.setElementCssClass("o_relative_date_offset_row");
		formLayout.add("offset.row", offsetRowCont);

		offsetValueEl = uifactory.addTextElement("rel.offset.value", null, 6, null, offsetRowCont);
		offsetValueEl.setAriaLabel(translate("relative.date.callout.offset"));
		offsetValueEl.setDisplaySize(6);
		offsetValueEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues dirSV = new SelectionValues();
		dirSV.add(SelectionValues.entry(OffsetDirection.BEFORE.name(), translate("relative.date.callout.offset.before")));
		dirSV.add(SelectionValues.entry(OffsetDirection.AFTER.name(), translate("relative.date.callout.offset.after")));
		offsetDirEl = uifactory.addButtonGroupSingleSelectHorizontal("rel.offset.dir", offsetRowCont, dirSV);
		offsetDirEl.select(OffsetDirection.BEFORE.name(), true);
		offsetDirEl.setElementCssClass("o_button_group_always_horizontal");
		offsetDirEl.setAriaLabel(translate("relative.date.callout.offset.dir.aria"));
		offsetDirEl.addActionListener(FormEvent.ONCHANGE);

		SelectionValues unitSV = getUnitSelectionValues();
		unitEl = uifactory.addButtonGroupSingleSelectHorizontal("rel.unit", formLayout, unitSV);
		unitEl.setLabel(null, null);
		unitEl.setElementCssClass("o_relative_date_unit_row");
		if (unitSV.size() > 0) {
			unitEl.select(unitSV.keys()[0], true);
		}
		unitEl.addActionListener(FormEvent.ONCHANGE);

		calculatedEl = uifactory.addStaticTextElement("rel.calculated", "relative.date.callout.calculated", "", formLayout);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("apply", "relative.date.callout.apply", buttonLayout);
		removeLink = uifactory.addFormLink("remove", "relative.date.callout.remove", null, buttonLayout, Link.BUTTON);
	}

	protected void applyInitialSelection(RelativeDateSelection sel) {
		if (sel == null) return;
		if (refEl.containsKey(sel.getRefKey())) {
			refEl.select(sel.getRefKey(), true);
		}
		if (!sel.isOffsetEnabled()) {
			offsetToggleEl.toggleOff();
		} else {
			offsetToggleEl.toggleOn();
			String dirKey = sel.getDirection().name();
			if (offsetDirEl.containsKey(dirKey)) {
				offsetDirEl.select(dirKey, true);
			}
			if (sel.getValue() != null) {
				offsetValueEl.setValue(String.valueOf(sel.getValue()));
			}
			if (sel.getUnitKey() != null && unitEl.containsKey(sel.getUnitKey())) {
				unitEl.select(sel.getUnitKey(), true);
			}
		}
	}

	protected void updateUI() {
		boolean hasOffset = offsetToggleEl.isOn();
		offsetRowCont.setVisible(hasOffset);
		unitEl.setVisible(hasOffset);
		calculatedEl.setVisible(hasOffset);
		if (hasOffset) {
			updateCalculatedDate();
		}
	}

	private void updateCalculatedDate() {
		String refKey = refEl.isOneSelected() ? refEl.getSelectedKey() : (refEl.getKeys().length > 0 ? refEl.getKeys()[0] : null);
		OffsetDirection direction = offsetDirEl.isOneSelected()
				? OffsetDirection.valueOf(offsetDirEl.getSelectedKey())
				: OffsetDirection.BEFORE;
		String unitKey = unitEl.isOneSelected() ? unitEl.getSelectedKey() : (unitEl.getKeys().length > 0 ? unitEl.getKeys()[0] : null);
		Integer value = parseOffsetValue();
		RelativeDateSelection sel = new RelativeDateSelection(refKey, direction, unitKey, value, true);

		Date resolved = resolveDate(sel);
		calculatedEl.setValue(resolved != null
				? Formatter.getInstance(getLocale()).formatDate(resolved)
				: translate("relative.date.callout.calculated.empty"));
	}

	private Integer parseOffsetValue() {
		String valueStr = offsetValueEl.getValue();
		if (!StringHelper.containsNonWhitespace(valueStr)) return null;
		try {
			return Integer.parseInt(valueStr.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == offsetToggleEl || source == refEl) {
			updateUI();
		} else if (source == offsetValueEl || source == offsetDirEl || source == unitEl) {
			updateCalculatedDate();
		} else if (source == removeLink) {
			doRemove(ureq);
		}
		super.formInnerEvent(ureq, source, event);
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

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, new RelativeDateAppliedEvent(buildSelection()));
	}

	private void doRemove(UserRequest ureq) {
		fireEvent(ureq, new RelativeDateRemovedEvent());
	}

	private RelativeDateSelection buildSelection() {
		String refKey = refEl.isOneSelected() ? refEl.getSelectedKey() : null;
		boolean offsetEnabled = offsetToggleEl.isOn();
		OffsetDirection direction = OffsetDirection.BEFORE;
		String unitKey = null;
		Integer value = null;
		if (offsetEnabled) {
			direction = offsetDirEl.isOneSelected()
					? OffsetDirection.valueOf(offsetDirEl.getSelectedKey())
					: OffsetDirection.BEFORE;
			unitKey = unitEl.isOneSelected() ? unitEl.getSelectedKey() : unitEl.getKeys()[0];
			value = parseOffsetValue();
		}
		return new RelativeDateSelection(refKey, direction, unitKey, value, offsetEnabled);
	}

}
