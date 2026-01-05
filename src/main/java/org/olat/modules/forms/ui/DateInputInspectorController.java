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
package org.olat.modules.forms.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.forms.model.xml.DateInput;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Dec 17, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.co
 *
 */
public class DateInputInspectorController extends FormBasicController implements PageElementInspectorController {

	private static final String OBLIGATION_MANDATORY_KEY = "mandatory";
	private static final String OBLIGATION_OPTIONAL_KEY = "optional";
	private static final String INPUT_TYPE_DATE_KEY = "textinput.numeric.date";
	private static final String INPUT_TYPE_DATE_TIME_KEY = "textinput.numeric.date.time";
	private static final String[] NUMERIC_KEYS = new String[] {
			INPUT_TYPE_DATE_KEY,
			INPUT_TYPE_DATE_TIME_KEY
	};
	private static final String[] NOW_BUTTON_ENABLED_KEYS = new String[] { "on" };

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;

	private SingleSelection inputTypeEl;
	private MultipleSelectionElement nowButtonEnabledEl;
	private TextElement nowButtonLabelEl;
	private SingleSelection obligationEl;
	
	private final DateInput dateInput;
	private final boolean restrictedEdit;

	@Autowired
	private ColorService colorService;

	public DateInputInspectorController(UserRequest ureq, WindowControl wControl, DateInput dateInput, boolean restrictedEdit) {
		super(ureq, wControl, "dateinput_editor");
		this.dateInput = dateInput;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}
	
	@Override
	public String getTitle() {
		return translate("inspector.formdateinput");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);
		
		addGeneralTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);

		updateUI();
	}

	private void addGeneralTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.general"), layoutCont);
		
		inputTypeEl = uifactory.addRadiosVertical("dateinput_type_" + CodeHelper.getRAMUniqueID(),
				"dateinput.type", layoutCont, NUMERIC_KEYS, translateAll(getTranslator(), NUMERIC_KEYS));
		if (dateInput.isTime()) {
			inputTypeEl.select(INPUT_TYPE_DATE_TIME_KEY, true);
		} else {
			inputTypeEl.select(INPUT_TYPE_DATE_KEY, true);
		}
		inputTypeEl.addActionListener(FormEvent.ONCHANGE);
		inputTypeEl.setEnabled(!restrictedEdit);
		
		nowButtonEnabledEl = uifactory.addCheckboxesHorizontal("dateinput_now" + CodeHelper.getRAMUniqueID(),
				"dateinput.now.button.enabled", layoutCont, NOW_BUTTON_ENABLED_KEYS,
				new String[] { translate("dateinput.now.button.enabled.value") });
		nowButtonEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if (StringHelper.containsNonWhitespace(dateInput.getNowButtonLabel())) {
			nowButtonEnabledEl.select(nowButtonEnabledEl.getKey(0), true);
		}
		
		nowButtonLabelEl = uifactory.addTextElement("dateinput_now_label" + CodeHelper.getRAMUniqueID(), null, 64,
				dateInput.getNowButtonLabel(), layoutCont);
		nowButtonLabelEl.setAriaLabel(translate("dateinput.now.button.label"));
		nowButtonLabelEl.addActionListener(FormEvent.ONCHANGE);
		
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosVertical("obli_" + CodeHelper.getRAMUniqueID(), "obligation", layoutCont,
				obligationKV.keys(), obligationKV.values());
		obligationEl.select(OBLIGATION_MANDATORY_KEY, dateInput.isMandatory());
		obligationEl.select(OBLIGATION_OPTIONAL_KEY, !dateInput.isMandatory());
		obligationEl.setEnabled(!restrictedEdit);
		obligationEl.addActionListener(FormEvent.ONCHANGE);
	}

	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		Translator translator = Util.createPackageTranslator(PageElementTarget.class, getLocale());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, translator, uifactory, getLayoutSettings(), velocity_root);
	}

	private BlockLayoutSettings getLayoutSettings() {
		if (dateInput.getLayoutSettings() != null) {
			return dateInput.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings() {
		if (dateInput.getAlertBoxSettings() != null) {
			return dateInput.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	private void updateUI() {
		nowButtonLabelEl.setVisible(nowButtonEnabledEl.isAtLeastSelected(1));
		if (nowButtonLabelEl.isVisible()) {
			if (!StringHelper.containsNonWhitespace(nowButtonLabelEl.getValue())) {
				nowButtonLabelEl.setValue(translate("dateinput.now.button.label.default"));
			}
		}
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nowButtonLabelEl.clearError();
		if(nowButtonLabelEl.isVisible() && !StringHelper.containsNonWhitespace(nowButtonLabelEl.getValue())) {
			nowButtonLabelEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (nowButtonEnabledEl == source) {
			updateUI();
			doValidateAndSave(ureq);
		} else if (inputTypeEl == source || nowButtonLabelEl == source || obligationEl == source) {
			doValidateAndSave(ureq);
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave();
		fireEvent(ureq, new ChangePartEvent(dateInput));
	}
	
	private void doValidateAndSave(UserRequest ureq) {
		if(validateFormLogic(ureq)) {
			doSave();
			fireEvent(ureq, new ChangePartEvent(dateInput));
		}
	}
	
	private void doSave() {
		dateInput.setDate(true);
		
		boolean time = INPUT_TYPE_DATE_TIME_KEY.equals(inputTypeEl.getSelectedKey());
		dateInput.setTime(time);
		
		String nowButtonLabel = null;
		if (nowButtonLabelEl.isVisible()) {
			nowButtonLabel = nowButtonLabelEl.getValue();
		}
		dateInput.setNowButtonLabel(nowButtonLabel);	
		
		boolean mandatory = OBLIGATION_MANDATORY_KEY.equals(obligationEl.getSelectedKey());
		dateInput.setMandatory(mandatory);
	}

	private void doChangeLayout(UserRequest ureq) {
		BlockLayoutSettings layoutSettings = getLayoutSettings();
		layoutTabComponents.sync(layoutSettings);
		dateInput.setLayoutSettings(layoutSettings);
		fireEvent(ureq, new ChangePartEvent(dateInput));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings();
		alertBoxComponents.sync(alertBoxSettings);
		dateInput.setAlertBoxSettings(alertBoxSettings);
		fireEvent(ureq, new ChangePartEvent(dateInput));

		getInitialComponent().setDirty(true);
	}
}
