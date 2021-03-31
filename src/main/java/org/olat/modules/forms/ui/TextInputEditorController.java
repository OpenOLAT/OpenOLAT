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

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.forms.model.xml.TextInput;

/**
 * 
 * Initial date: 7 déc. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TextInputEditorController extends FormBasicController implements PageElementEditorController {
	
	private static final String INPUT_TYPE_TEXT_KEY = "textinput.numeric.text";
	private static final String INPUT_TYPE_NUMERIC_KEY = "textinput.numeric.numeric";
	private static final String INPUT_TYPE_DATE_KEY = "textinput.numeric.date";
	private static final String[] NUMERIC_KEYS = new String[] {
			INPUT_TYPE_TEXT_KEY,
			INPUT_TYPE_NUMERIC_KEY,
			INPUT_TYPE_DATE_KEY
	};
	private static final String SINGLE_ROW_KEY = "textinput.single.row";
	private static final String MULTIPLE_ROWS_KEY = "textinput.multiple.rows";
	private static final String[] ROW_OPTIONS = new String[] {
			SINGLE_ROW_KEY,
			MULTIPLE_ROWS_KEY
	};
	
	private SingleSelection inputTypeEl;
	private SingleSelection singleRowEl;
	private TextElement rowsEl;
	private FormLink saveButton;
	private TextInputController textInputCtrl;
	
	private final TextInput textInput;
	private boolean editMode = false;
	private boolean restrictedEdit;
	
	public TextInputEditorController(UserRequest ureq, WindowControl wControl, TextInput textInput, boolean restrictedEdit) {
		super(ureq, wControl, "textinput_editor");
		this.textInput = textInput;
		this.restrictedEdit = restrictedEdit;
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
		textInputCtrl = new TextInputController(ureq, getWindowControl(), textInput, true);
		listenTo(textInputCtrl);
		formLayout.add("textInput", textInputCtrl.getInitialFormItem());

		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("textinput_cont_" + CodeHelper.getRAMUniqueID(), getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add("settings", settingsCont);
		
		inputTypeEl = uifactory.addRadiosHorizontal("textinput_num_" + CodeHelper.getRAMUniqueID(),
				"textinput.numeric", settingsCont, NUMERIC_KEYS, translateAll(getTranslator(), NUMERIC_KEYS));
		if (textInput.isNumeric()) {
			inputTypeEl.select(INPUT_TYPE_NUMERIC_KEY, true);
		} else if (textInput.isDate()) {
			inputTypeEl.select(INPUT_TYPE_DATE_KEY, true);
		} else {
			inputTypeEl.select(INPUT_TYPE_TEXT_KEY, true);
		}
		inputTypeEl.addActionListener(FormEvent.ONCHANGE);
		inputTypeEl.setEnabled(!restrictedEdit);
		
		singleRowEl = uifactory.addRadiosHorizontal("textinput_row_" + CodeHelper.getRAMUniqueID(),
				"textinput.rows.mode", settingsCont, ROW_OPTIONS, translateAll(getTranslator(), ROW_OPTIONS));
		String selectedRowsKey = textInput.isSingleRow()? SINGLE_ROW_KEY: MULTIPLE_ROWS_KEY;
		singleRowEl.select(selectedRowsKey, true);
		singleRowEl.addActionListener(FormEvent.ONCHANGE);
		singleRowEl.setEnabled(!restrictedEdit);
		
		String rows = "";
		if(textInput.getRows() > 0) {
			rows = Integer.toString(textInput.getRows());
		}
		rowsEl = uifactory.addTextElement("textinput_rows_" + CodeHelper.getRAMUniqueID(), "textinput.rows", 8, rows, settingsCont);
		
		saveButton = uifactory.addFormLink("save_" + CodeHelper.getRAMUniqueID(), "save", null, settingsCont, Link.BUTTON);
		
		updateUI();
	}

	private void updateUI() {
		boolean isText = INPUT_TYPE_TEXT_KEY.equals(inputTypeEl.getSelectedKey());
		boolean isMultipleRows = MULTIPLE_ROWS_KEY.equals(singleRowEl.getSelectedKey());
		singleRowEl.setVisible(isText);
		rowsEl.setVisible(isText && isMultipleRows);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		rowsEl.clearError();
		if(rowsEl.isVisible() && StringHelper.containsNonWhitespace(rowsEl.getValue())) {
			try {
				Integer.parseInt(rowsEl.getValue());
			} catch (NumberFormatException e) {
				rowsEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (inputTypeEl == source) {
			updateUI();
		} else if (singleRowEl == source) {
			updateUI();
		} else if (saveButton == source) {
			if(validateFormLogic(ureq)) {
				formOK(ureq);
			}	
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean numeric = INPUT_TYPE_NUMERIC_KEY.equals(inputTypeEl.getSelectedKey());
		textInput.setNumeric(numeric);
		
		boolean date = INPUT_TYPE_DATE_KEY.equals(inputTypeEl.getSelectedKey());
		textInput.setDate(date);
		
		boolean singleRow = SINGLE_ROW_KEY.equals(singleRowEl.getSelectedKey());
		textInput.setSingleRow(singleRow);
		
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
