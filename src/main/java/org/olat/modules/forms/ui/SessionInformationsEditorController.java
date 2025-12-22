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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.ui.BlockLayoutClassFactory;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SessionInformationsEditorController extends FormBasicController implements PageElementEditorController {
	
	private MultipleSelectionElement informationsEl;
	
	private SessionInformations sessionInformations;
	private final boolean restrictedEdit;
	private final List<InformationType> availableTypes;
	
	public SessionInformationsEditorController(UserRequest ureq, WindowControl wControl,
			SessionInformations sessionInformations, boolean restrictedEdit, List<InformationType> availableTypes) {
		super(ureq, wControl, "session_informations_editor");
		this.sessionInformations = sessionInformations;
		this.restrictedEdit = restrictedEdit;
		this.availableTypes = availableTypes;
		
		initForm(ureq);

		setBlockLayoutClass();
	}

	private void setBlockLayoutClass() {
		flc.contextPut("blockLayoutClass", BlockLayoutClassFactory.buildClass(sessionInformations.getLayoutSettings(), true));
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// settings
		long postfix = CodeHelper.getRAMUniqueID();
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("gi_settings_cont_" + postfix,
				getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add("settings", settingsCont);
		
		SelectionValues informationsSV = SessionInformationsUIFactory.getInformationsSV(getLocale(), availableTypes);
		informationsEl = uifactory.addCheckboxesVertical("gi_" + postfix, "session.informations.informations",
				settingsCont, informationsSV.keys(), informationsSV.values(), null, null, 2);
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == informationsEl) {
			doSave(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof SessionInfoInspectorController && event instanceof ChangePartEvent changePartEvent) {
			if (changePartEvent.isElement(sessionInformations) && changePartEvent.getElement() instanceof SessionInformations sessionInfo) {
				this.sessionInformations = sessionInfo;
				setBlockLayoutClass();
			}
		}
		super.event(ureq, source, event);
	}

	private void doSave(UserRequest ureq) {
		List<InformationType> informationTypes = informationsEl.getSelectedKeys().stream()
				.map(InformationType::valueOf)
				.toList();
		sessionInformations.setInformationTypes(new ArrayList<>(informationTypes));
		fireEvent(ureq, new ChangePartEvent(sessionInformations));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
