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
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.CodeElement;
import org.olat.modules.ceditor.model.CodeLanguage;
import org.olat.modules.ceditor.model.CodeSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-12-12<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CodeInspectorController extends FormBasicController implements PageElementInspectorController {
	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;
	private CodeElement codeElement;
	private final PageElementStore<CodeElement> store;
	private SingleSelection codeLanguageEl;
	private FormToggle enableLineNumbersEl;
	private MultipleSelectionElement numberOfLinesEl;
	private IntegerElement numberOfLinesIntEl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ColorService colorService;

	public CodeInspectorController(UserRequest ureq, WindowControl wControl, CodeElement codeElement,
								   PageElementStore<CodeElement> store) {
		super(ureq, wControl, "tabs_inspector");
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
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addStyleTab(formLayout);
		addLayoutTab(formLayout);

		updateUI();
	}

	private void addStyleTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("style", getTranslator());
		layoutCont.setElementCssClass("o_code_inspector_style");
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.style"), layoutCont);

		SelectionValues codeLanguageKV = new SelectionValues();
		for (CodeLanguage codeLanguage : CodeLanguage.values()) {
			codeLanguageKV.add(SelectionValues.entry(codeLanguage.name(), codeLanguage.getDisplayText(getLocale())));
		}
		codeLanguageEl = uifactory.addDropdownSingleselect("code.language", "code.language", layoutCont,
				codeLanguageKV.keys(), codeLanguageKV.values(), null);
		codeLanguageEl.addActionListener(FormEvent.ONCHANGE);
		enableLineNumbersEl = uifactory.addToggleButton("code.line.numbers", "code.line.numbers",
				translate("on"), translate("off"), layoutCont);
		enableLineNumbersEl.addActionListener(FormEvent.ONCHANGE);
		SelectionValues numberOfLinesKV = new SelectionValues();
		numberOfLinesKV.add(SelectionValues.entry("all", translate("all")));
		numberOfLinesEl = uifactory.addCheckboxesVertical("code.number.of.lines", "code.number.of.lines",
				layoutCont, numberOfLinesKV.keys(), numberOfLinesKV.values(), 1);
		numberOfLinesEl.addActionListener(FormEvent.ONCHANGE);
		numberOfLinesIntEl = uifactory.addIntegerElement("code.number.of.lines.int", null, 0,
				layoutCont);
		numberOfLinesIntEl.addActionListener(FormEvent.ONBLUR);

		alertBoxComponents = MediaUIHelper.addAlertBoxSettings(layoutCont, getTranslator(), uifactory,
				getAlertBoxSettings(getCodeSettings()), colorService, getLocale());
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

	private void addLayoutTab(FormItemContainer formLayout) {
		BlockLayoutSettings layoutSettings = getLayoutSettings(getCodeSettings());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory, layoutSettings, velocity_root);
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
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
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
		if (numberOfLinesIntEl.validateIntValue()) {
			settings.setNumberOfLinesToDisplay(numberOfLinesIntEl.getIntValue());
		}
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

	private void doChangeLayout(UserRequest ureq) {
		CodeSettings codeSettings = getCodeSettings();

		BlockLayoutSettings layoutSettings = getLayoutSettings(codeSettings);
		layoutTabComponents.sync(layoutSettings);
		codeSettings.setLayoutSettings(layoutSettings);

		codeElement.setSettings(codeSettings);
		doSave(ureq);

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		CodeSettings codeSettings = getCodeSettings();

		AlertBoxSettings alertBoxSettings = getAlertBoxSettings(codeSettings);
		alertBoxComponents.sync(alertBoxSettings);
		codeSettings.setAlertBoxSettings(alertBoxSettings);

		codeElement.setSettings(codeSettings);
		doSave(ureq);

		getInitialComponent().setDirty(true);
	}

	private BlockLayoutSettings getLayoutSettings(CodeSettings codeSettings) {
		if (codeSettings.getLayoutSettings() != null) {
			return codeSettings.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings(CodeSettings codeSettings) {
		if (codeSettings.getAlertBoxSettings() != null) {
			return codeSettings.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private CodeSettings getCodeSettings() {
		if (codeElement.getSettings() != null) {
			return codeElement.getSettings();
		}
		return new CodeSettings();
	}
}
