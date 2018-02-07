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
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.model.xml.TextInput;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;
import org.olat.modules.portfolio.ui.editor.event.ChangePartEvent;
import org.olat.modules.portfolio.ui.editor.event.ClosePartEvent;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputEditorController extends FormBasicController implements PageElementEditorController {
	
	private TextElement rowsEl;
	private FormLink saveButton;
	private TextInputController textInputCtrl;
	
	private final TextInput textInput;
	private boolean editMode = false;
	
	public TextInputEditorController(UserRequest ureq, WindowControl wControl, TextInput textInput) {
		super(ureq, wControl, "textinput_editor");
		this.textInput = textInput;
		initForm(ureq);
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
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		textInputCtrl = new TextInputController(ureq, getWindowControl(), textInput);
		listenTo(textInputCtrl);
		formLayout.add("textInput", textInputCtrl.getInitialFormItem());

		long postfix = CodeHelper.getRAMUniqueID();
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("textinput_cont_" + postfix, getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add("settings", settingsCont);
		
		String rows = "";
		if(textInput.getRows() > 0) {
			rows = Integer.toString(textInput.getRows());
		}
		
		rowsEl = uifactory.addTextElement("textinput_rows_" + postfix, "textinput.rows", 8, rows, settingsCont);
		saveButton = uifactory.addFormLink("save_" + postfix, "save", null, settingsCont, Link.BUTTON);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		rowsEl.clearError();
		if(StringHelper.containsNonWhitespace(rowsEl.getValue())) {
			try {
				Integer.parseInt(rowsEl.getValue());
			} catch (NumberFormatException e) {
				rowsEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(saveButton == source) {
			if(validateFormLogic(ureq)) {
				formOK(ureq);
			}	
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(StringHelper.containsNonWhitespace(rowsEl.getValue())) {
			try {
				int rows = Integer.parseInt(rowsEl.getValue());
				textInput.setRows(rows);
			} catch (NumberFormatException e) {
				logError("Cannot parse integer: " + rowsEl.getValue(), null);
			}
		}
		textInputCtrl.update();
		fireEvent(ureq, new ChangePartEvent(textInput));
		fireEvent(ureq, new ClosePartEvent(textInput));
	}
}
