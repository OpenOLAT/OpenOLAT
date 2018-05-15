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

import java.util.Arrays;
import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.model.xml.GeneralInformation;
import org.olat.modules.forms.model.xml.GeneralInformation.Type;
import org.olat.modules.forms.model.xml.GeneralInformations;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;
import org.olat.modules.portfolio.ui.editor.event.ChangePartEvent;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneralInformationsEditorController extends FormBasicController implements PageElementEditorController {

	private GeneralInformationsController generalInforamtionsCtrl;
	private MultipleSelectionElement informationsEl;
	
	private final GeneralInformations generalInformations;
	private final boolean restrictedEdit;
	private boolean editMode = false;
	
	public GeneralInformationsEditorController(UserRequest ureq, WindowControl wControl,
			GeneralInformations generalInformations, boolean restrictedEdit) {
		super(ureq, wControl, "general_informations_editor");
		this.generalInformations = generalInformations;
		this.restrictedEdit = restrictedEdit;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		generalInforamtionsCtrl = new GeneralInformationsController(ureq, getWindowControl(), generalInformations);
		formLayout.add("preview", generalInforamtionsCtrl.getInitialFormItem());

		// settings
		long postfix = CodeHelper.getRAMUniqueID();
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("gi_settings_cont_" + postfix,
				getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add("settings", settingsCont);
		
		String[] keys = GeneralInformationsUIFactory.getTypeKeys();
		String[] values = GeneralInformationsUIFactory.getTranslatedTypes(getLocale());
		informationsEl = uifactory.addCheckboxesVertical("gi_" + postfix,
				"general.informations.informations", settingsCont, keys, values, null, null, 2);
		for (String selectedKey: GeneralInformationsUIFactory.getSelectedTypeKeys(generalInformations)) {
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
		if (source == informationsEl) {
			doEnableInformations();
		}
		generalInforamtionsCtrl.update();
		fireEvent(ureq, new ChangePartEvent(generalInformations));
		super.formInnerEvent(ureq, source, event);
	}

	private void doEnableInformations() {
		Collection<String> selectedKeys = informationsEl.getSelectedKeys();
		Type[] types = GeneralInformation.Type.values();
		Arrays.sort(types, (t1, t2) -> Integer.compare(t1.getOrder(), t2.getOrder()));
		for (GeneralInformation.Type type: types) {
			boolean enabled = selectedKeys.contains(type.name());
			generalInformations.setEnable(type, enabled);
		}
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
