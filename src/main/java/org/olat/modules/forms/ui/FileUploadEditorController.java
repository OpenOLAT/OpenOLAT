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

import static org.olat.core.gui.components.util.KeyValues.entry;

import java.util.Arrays;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.forms.EvaluationFormsModule;
import org.olat.modules.forms.model.xml.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileUploadEditorController extends FormBasicController implements PageElementEditorController {
	
	private static final String OBLIGATION_MANDATORY_KEY = "mandatory";
	private static final String OBLIGATION_OPTIONAL_KEY = "optional";
	
	private SingleSelection fileLimitEl;
	private SingleSelection mimeTypesEl;
	private SingleSelection obligationEl;
	private FileUploadController fileUploadCtrl;
	
	private final FileUpload fileUpload;
	private boolean editMode = false;
	private final boolean restrictedEdit;
	
	@Autowired
	private EvaluationFormsModule evaluationFormsModule;


	public FileUploadEditorController(UserRequest ureq, WindowControl wControl, FileUpload fileUpload, boolean restrictedEdit) {
		super(ureq, wControl, "file_upload_editor");
		this.fileUpload = fileUpload;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
		setEditMode(editMode);
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
		fileUploadCtrl = new FileUploadController(ureq, getWindowControl(), fileUpload);
		formLayout.add("preview", fileUploadCtrl.getInitialFormItem());

		// settings
		long postfix = CodeHelper.getRAMUniqueID();
		FormLayoutContainer settingsCont = FormLayoutContainer.createDefaultFormLayout("file_upload_cont_" + postfix,
				getTranslator());
		settingsCont.setRootForm(mainForm);
		formLayout.add("settings", settingsCont);

		String[] keys = evaluationFormsModule.getOrderedFileUploadLimitsKB().stream().map(String::valueOf)
				.toArray(String[]::new);
		String[] values = evaluationFormsModule.getOrderedFileUploadLimitsKB().stream().map(Formatter::formatKBytes)
				.toArray(String[]::new);
		fileLimitEl = uifactory.addRadiosHorizontal("upload_limit_" + postfix, "file.upload.limit", settingsCont, keys,
				values);
		fileLimitEl.select(getInitialMaxFileUploadLimitKey(keys), true);
		fileLimitEl.addActionListener(FormEvent.ONCHANGE);
		
		mimeTypesEl = uifactory.addDropdownSingleselect("mime_types_" + postfix, "file.upload.mime.types", settingsCont,
				MimeTypeSetFactory.getKeys(), MimeTypeSetFactory.getValues(getTranslator()), null);
		mimeTypesEl.select(getInitialMimeTypeSetKey(), true);
		mimeTypesEl.addActionListener(FormEvent.ONCHANGE);
		mimeTypesEl.setEnabled(!restrictedEdit);
		
		KeyValues obligationKV = new KeyValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosHorizontal("obli_" + CodeHelper.getRAMUniqueID(), "obligation", settingsCont,
				obligationKV.keys(), obligationKV.values());
		obligationEl.select(OBLIGATION_MANDATORY_KEY, fileUpload.isMandatory());
		obligationEl.select(OBLIGATION_OPTIONAL_KEY, !fileUpload.isMandatory());
		obligationEl.setEnabled(!restrictedEdit);
		obligationEl.addActionListener(FormEvent.ONCLICK);
	}

	private String getInitialMaxFileUploadLimitKey(String[] orderedKeys) {
		String keyOfMaxFileUploadLimit = orderedKeys[orderedKeys.length - 1]; //fallback is max value
		if (fileUpload.getMaxUploadSizeKB() > 0) {
			String savedMaxSizeKey = String.valueOf(fileUpload.getMaxUploadSizeKB());
			if (Arrays.asList(orderedKeys).contains(savedMaxSizeKey)) {
				keyOfMaxFileUploadLimit = savedMaxSizeKey;
			}
		}
		return keyOfMaxFileUploadLimit;
	}
	
	private String getInitialMimeTypeSetKey() {
		String initialMimeTypeSetKey = MimeTypeSetFactory.getAllMimeTypesKey();
		String savedMimeTypeSetKey = fileUpload.getMimeTypeSetKey();
		String[] availableKeys = MimeTypeSetFactory.getKeys();
		if (Arrays.asList(availableKeys).contains(savedMimeTypeSetKey)) {
			initialMimeTypeSetKey = fileUpload.getMimeTypeSetKey();
		}
		return initialMimeTypeSetKey;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == fileLimitEl) {
			doSetMaxUploadSize();
		} else if (source == mimeTypesEl) {
			doSetMimeTypes();
		} else if (source == obligationEl) {
			doSetObligation();
		}
		fileUploadCtrl.updateUI(ureq);
		fireEvent(ureq, new ChangePartEvent(fileUpload));
		super.formInnerEvent(ureq, source, event);
	}

	private void doSetMaxUploadSize() {
		long sizeKB = evaluationFormsModule.getMaxFileUploadLimitKB();
		if (fileLimitEl.isOneSelected()) {
			String selectedSizeKB = fileLimitEl.getSelectedKey();
			try {
				sizeKB = Long.parseLong(selectedSizeKB);
			} catch (NumberFormatException e) {
				// 
			}
		}
		fileUpload.setMaxUploadSizeKB(sizeKB);
	}

	private void doSetMimeTypes() {
		if (mimeTypesEl.isOneSelected()) {
			String selectedKey = mimeTypesEl.getSelectedKey();
			fileUpload.setMimeTypeSetKey(selectedKey);
		}
	}
	
	private void doSetObligation() {
		boolean mandatory = OBLIGATION_MANDATORY_KEY.equals(obligationEl.getSelectedKey());
		fileUpload.setMandatory(mandatory);
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