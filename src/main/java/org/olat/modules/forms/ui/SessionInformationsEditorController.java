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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;
import org.olat.modules.forms.model.xml.SessionInformations.Obligation;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionInformationsEditorController extends FormBasicController implements PageElementEditorController {
	
	private SessionInformationsController sessionInforamtionsCtrl;
	private SingleSelection obligationEl;
	private MultipleSelectionElement informationsEl;
	
	private final SessionInformations sessionInformations;
	private final boolean restrictedEdit;
	private boolean editMode = false;
	
	public SessionInformationsEditorController(UserRequest ureq, WindowControl wControl,
			SessionInformations sessionInformations, boolean restrictedEdit) {
		super(ureq, wControl, "session_informations_editor");
		this.sessionInformations = sessionInformations;
		this.restrictedEdit = restrictedEdit;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		sessionInforamtionsCtrl = new SessionInformationsController(ureq, getWindowControl(), sessionInformations);
		formLayout.add("preview", sessionInforamtionsCtrl.getInitialFormItem());

		// settings
		long postfix = CodeHelper.getRAMUniqueID();
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("gi_settings_cont_" + postfix,
				getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add("settings", settingsCont);
		
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(SelectionValues.entry(Obligation.optional.name(), translate("session.information.obligation.optional")));
		obligationKV.add(SelectionValues.entry(Obligation.mandatory.name(), translate("session.information.obligation.mandatory")));
		obligationKV.add(SelectionValues.entry(Obligation.autofill.name(), translate("session.information.obligation.autofill")));
		obligationEl = uifactory.addDropdownSingleselect("gi_m_" + postfix, "session.information.obligation",
				settingsCont, obligationKV.keys(), obligationKV.values());
		String selectedObligation = sessionInformations.getObligation() != null
				? sessionInformations.getObligation().name()
				: Obligation.optional.name();
		obligationEl.select(selectedObligation, true);
		obligationEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] keys = SessionInformationsUIFactory.getTypeKeys();
		String[] values = SessionInformationsUIFactory.getTranslatedTypes(getLocale());
		informationsEl = uifactory.addCheckboxesVertical("gi_" + postfix,
				"session.informations.informations", settingsCont, keys, values, null, null, 2);
		for (String selectedKey: SessionInformationsUIFactory.getSelectedTypeKeys(sessionInformations)) {
			informationsEl.select(selectedKey, true);
		}
		if (restrictedEdit) {
			informationsEl.setEnabled(false);
		} else {
			informationsEl.addActionListener(FormEvent.ONCHANGE);
		}
	}
	
	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		this.editMode = editMode;
		flc.getFormItemComponent().contextPut("editMode", Boolean.valueOf(editMode));
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == obligationEl) {
			doSetObligation();
		} else if (source == informationsEl) {
			doEnableInformations();
		}
		sessionInforamtionsCtrl.update();
		fireEvent(ureq, new ChangePartEvent(sessionInformations));
		super.formInnerEvent(ureq, source, event);
	}

	private void doSetObligation() {
		Obligation sselectedObligation = obligationEl.isOneSelected()
				? Obligation.valueOf(obligationEl.getSelectedKey())
				: Obligation.optional;
		sessionInformations.setObligation(sselectedObligation);
	}

	private void doEnableInformations() {
		Collection<String> selectedKeys = informationsEl.getSelectedKeys();
		List<InformationType> informationTypes = new ArrayList<>();
		InformationType[] types = InformationType.values();
		Arrays.sort(types, (t1, t2) -> Integer.compare(t1.getOrder(), t2.getOrder()));
		for (InformationType type: types) {
			boolean enabled = selectedKeys.contains(type.name());
			if (enabled) {
				informationTypes.add(type);
			}
		}
		sessionInformations.setInformationTypes(informationTypes);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
