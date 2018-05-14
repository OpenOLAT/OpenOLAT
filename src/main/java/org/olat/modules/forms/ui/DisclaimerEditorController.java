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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.modules.forms.model.xml.Disclaimer;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;
import org.olat.modules.portfolio.ui.editor.event.ChangePartEvent;
import org.olat.modules.portfolio.ui.editor.event.ClosePartEvent;

/**
 * 
 * Initial date: 09.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DisclaimerEditorController extends FormBasicController implements PageElementEditorController {

	private DisclaimerController disclaimerCtrl;
	private TextAreaElement textEl;
	private FormLink saveButton;

	private Disclaimer disclaimer;
	private final boolean restrictedEdit;
	private boolean editMode = false;
	
	public DisclaimerEditorController(UserRequest ureq, WindowControl wControl, Disclaimer disclaimer,
			boolean restrictedEdit) {
		super(ureq, wControl, "disclaimer_editor");
		this.disclaimer = disclaimer;
		this.restrictedEdit = restrictedEdit;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		disclaimerCtrl = new DisclaimerController(ureq, getWindowControl(), disclaimer);
		formLayout.add("preview", disclaimerCtrl.getInitialFormItem());

		// settings
		long postfix = CodeHelper.getRAMUniqueID();
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("sc_settings_cont_" + postfix,
				getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add("settings", settingsCont);
		
		textEl = uifactory.addTextAreaElement("disclaimer_" + CodeHelper.getRAMUniqueID(), "disclaimer.text", 50000, 12, 72,
				false, disclaimer.getText(), settingsCont);
		textEl.setEnabled(!restrictedEdit);
		
		saveButton = uifactory.addFormLink("save_" + CodeHelper.getRAMUniqueID(), "save", null, settingsCont, Link.BUTTON);
		saveButton.setVisible(!restrictedEdit);
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
		if (saveButton == source) {
			if(validateFormLogic(ureq)) {
				formOK(ureq);
			}	
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String text = textEl.getValue();
		disclaimer.setText(text);

		fireEvent(ureq, new ChangePartEvent(disclaimer));
		fireEvent(ureq, new ClosePartEvent(disclaimer));
	}

	@Override
	protected void doDispose() {
		//
	}

}
