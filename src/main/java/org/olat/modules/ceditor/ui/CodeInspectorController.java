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
package org.olat.modules.ceditor.ui;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.CodeElement;
import org.olat.modules.ceditor.model.CodeLanguage;
import org.olat.modules.ceditor.model.CodeSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-12-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CodeInspectorController extends FormBasicController implements PageElementInspectorController {
	private CodeElement codeElement;
	private final PageElementStore<CodeElement> store;
	private SingleSelection codeLanguageEl;
	private FormToggle enableLineNumbersEl;
	private MultipleSelectionElement numberOfLinesEl;
	private IntegerElement numberOfLinesIntEl;

	@Autowired
	private DB dbInstance;

	public CodeInspectorController(UserRequest ureq, WindowControl wControl, CodeElement codeElement,
								   PageElementStore<CodeElement> store) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.codeElement = codeElement;
		this.store = store;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("add.code");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues codeLanguageKV = new SelectionValues();
		for (CodeLanguage codeLanguage : CodeLanguage.values()) {
			codeLanguageKV.add(SelectionValues.entry(codeLanguage.name(), codeLanguage.getDisplayText(getLocale())));
		}
		codeLanguageEl = uifactory.addDropdownSingleselect("code.language", "code.language", formLayout,
				codeLanguageKV.keys(), codeLanguageKV.values(), null);
		codeLanguageEl.addActionListener(FormEvent.ONCHANGE);
		enableLineNumbersEl = uifactory.addToggleButton("code.line.numbers", "code.line.numbers",
				translate("on"), translate("off"), formLayout);
		enableLineNumbersEl.addActionListener(FormEvent.ONCHANGE);
		SelectionValues numberOfLinesKV = new SelectionValues();
		numberOfLinesKV.add(SelectionValues.entry("all", translate("all")));
		numberOfLinesEl = uifactory.addCheckboxesVertical("code.number.of.lines", "code.number.of.lines",
				formLayout, numberOfLinesKV.keys(), numberOfLinesKV.values(), 1);
		numberOfLinesEl.addActionListener(FormEvent.ONCHANGE);
		numberOfLinesIntEl = uifactory.addIntegerElement("code.number.of.lines.int", null, 0,
				formLayout);
		numberOfLinesIntEl.addActionListener(FormEvent.ONBLUR);
		updateUI();
	}

	private void updateUI() {
		CodeSettings codeSettings = codeElement.getSettings();
		codeLanguageEl.select(codeSettings.getCodeLanguage().name(), true);
		enableLineNumbersEl.toggle(codeSettings.isLineNumbersEnabled());
		enableLineNumbersEl.setEnabled(!codeSettings.getCodeLanguage().equals(CodeLanguage.plaintext));
		numberOfLinesEl.select("all", codeSettings.isDisplayAllLines());
		numberOfLinesIntEl.setIntValue(codeSettings.getNumberOfLinesToDisplay());
		numberOfLinesIntEl.setVisible(!codeSettings.isDisplayAllLines());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (codeLanguageEl == source) {
			doSaveSettings(ureq);
		} else if (enableLineNumbersEl == source) {
			doSaveSettings(ureq);
		} else if (numberOfLinesEl == source) {
			doSaveSettings(ureq);
		} else if (numberOfLinesIntEl == source) {
			doSaveSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}

	private void doSaveSettings(UserRequest ureq) {
		CodeSettings settings = codeElement.getSettings();
		if (codeLanguageEl.isOneSelected()) {
			settings.setCodeLanguage(CodeLanguage.valueOf(codeLanguageEl.getSelectedKey()));
		}
		settings.setLineNumbersEnabled(enableLineNumbersEl.isOn() && !settings.getCodeLanguage().equals(CodeLanguage.plaintext));
		settings.setDisplayAllLines(numberOfLinesEl.isAtLeastSelected(1));
		settings.setNumberOfLinesToDisplay(numberOfLinesIntEl.getIntValue());
		codeElement.setSettings(settings);
		store.savePageElement(codeElement);
		dbInstance.commit();
		updateUI();
		fireEvent(ureq, new ChangePartEvent(codeElement));
	}

	private void doSave(UserRequest ureq) {
		codeElement = store.savePageElement(codeElement);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(codeElement));
	}
}
