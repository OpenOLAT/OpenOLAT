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

import java.util.Arrays;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.forms.EvaluationFormsModule;
import org.olat.modules.forms.model.xml.FileUpload;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileUploadInspectorController extends FormBasicController implements PageElementInspectorController {
	
	private static final String OBLIGATION_MANDATORY_KEY = "mandatory";
	private static final String OBLIGATION_OPTIONAL_KEY = "optional";

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;
	private SingleSelection fileLimitEl;
	private SingleSelection mimeTypesEl;
	private SingleSelection obligationEl;
	
	private final FileUpload fileUpload;
	private final boolean restrictedEdit;
	
	@Autowired
	private EvaluationFormsModule evaluationFormsModule;
	@Autowired
	private ColorService colorService;

	public FileUploadInspectorController(UserRequest ureq, WindowControl wControl, FileUpload fileUpload, boolean restrictedEdit) {
		super(ureq, wControl, "file_upload_editor");
		this.fileUpload = fileUpload;
		this.restrictedEdit = restrictedEdit;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("inspector.formfileupload");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addGeneralTab(formLayout);
		addStyleTab(formLayout);
		addLayoutTab(formLayout);
	}

	private void addGeneralTab(FormItemContainer formLayout) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createVerticalFormLayout("general", getTranslator());
		formLayout.add(layoutCont);
		tabbedPane.addTab(getTranslator().translate("tab.general"), layoutCont);

		// settings
		long postfix = CodeHelper.getRAMUniqueID();

		String[] keys = evaluationFormsModule.getOrderedFileUploadLimitsKB().stream().map(String::valueOf)
				.toArray(String[]::new);
		String[] values = evaluationFormsModule.getOrderedFileUploadLimitsKB().stream().map(Formatter::formatKBytes)
				.toArray(String[]::new);
		fileLimitEl = uifactory.addRadiosVertical("upload_limit_" + postfix, "file.upload.limit", layoutCont, keys,
				values);
		fileLimitEl.select(getInitialMaxFileUploadLimitKey(keys), true);
		fileLimitEl.addActionListener(FormEvent.ONCHANGE);

		mimeTypesEl = uifactory.addDropdownSingleselect("mime_types_" + postfix, "file.upload.mime.types", layoutCont,
				MimeTypeSetFactory.getKeys(), MimeTypeSetFactory.getValues(getTranslator()), null);
		mimeTypesEl.select(getInitialMimeTypeSetKey(), true);
		mimeTypesEl.addActionListener(FormEvent.ONCHANGE);
		mimeTypesEl.setEnabled(!restrictedEdit);

		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(OBLIGATION_MANDATORY_KEY, translate("obligation.mandatory")));
		obligationKV.add(entry(OBLIGATION_OPTIONAL_KEY, translate("obligation.optional")));
		obligationEl = uifactory.addRadiosVertical("obli_" + CodeHelper.getRAMUniqueID(), "obligation", layoutCont,
				obligationKV.keys(), obligationKV.values());
		obligationEl.select(OBLIGATION_MANDATORY_KEY, fileUpload.isMandatory());
		obligationEl.select(OBLIGATION_OPTIONAL_KEY, !fileUpload.isMandatory());
		obligationEl.setEnabled(!restrictedEdit);
		obligationEl.addActionListener(FormEvent.ONCLICK);
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
		if (fileUpload.getLayoutSettings() != null) {
			return fileUpload.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings() {
		if (fileUpload.getAlertBoxSettings() != null) {
			return fileUpload.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
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
		} else if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
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

	private void doChangeLayout(UserRequest ureq) {
		BlockLayoutSettings layoutSettings = getLayoutSettings();
		layoutTabComponents.sync(layoutSettings);
		fileUpload.setLayoutSettings(layoutSettings);
		fireEvent(ureq, new ChangePartEvent(fileUpload));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings();
		alertBoxComponents.sync(alertBoxSettings);
		fileUpload.setAlertBoxSettings(alertBoxSettings);
		fireEvent(ureq, new ChangePartEvent(fileUpload));

		getInitialComponent().setDirty(true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}