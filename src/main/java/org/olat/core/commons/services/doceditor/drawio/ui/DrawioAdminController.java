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
package org.olat.core.commons.services.doceditor.drawio.ui;

import org.olat.core.commons.services.doceditor.drawio.DrawioModule;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11 Aug 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DrawioAdminController extends FormBasicController {
	
	private static final String[] ENABLED_KEYS = new String[]{ "on" };
	
	private FormToggle enabledEl;
	private TextElement editorUrlEl;
	private TextElement exportUrlEl;
	private MultipleSelectionElement dataTransferConfirmationEnabledEl;
	private MultipleSelectionElement thumbnailEnabledEl;
	private MultipleSelectionElement collaborationEnabledEl;
	
	private boolean dataTransferConfirmationEnabled;
	private boolean thumbnailEnabled;
	private boolean collaborationEnabled;

	@Autowired
	private DrawioModule drawioModule;
	
	public DrawioAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(DocEditorController.class, getLocale(), getTranslator()));
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		
		String[] enableValues = new String[]{ translate("on") };
		enabledEl = uifactory.addToggleButton("admin.enabled", "admin.enabled", null, null, formLayout);
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		enabledEl.toggle(drawioModule.isEnabled());
		
		String editorUrl = drawioModule.getEditorUrl();
		editorUrlEl = uifactory.addTextElement("admin.editor.url", "admin.editor.url", 128, editorUrl, formLayout);
		editorUrlEl.setExampleKey("admin.editor.url.example", null);
		editorUrlEl.setMandatory(true);
		
		String exportUrl = drawioModule.getExportUrl();
		exportUrlEl = uifactory.addTextElement("admin.export.url", "admin.export.url", 128, exportUrl, formLayout);
		exportUrlEl.setExampleKey("admin.export.url.example", null);
		exportUrlEl.setMandatory(true);
		
		dataTransferConfirmationEnabled = drawioModule.isDataTransferConfirmationEnabled();
		dataTransferConfirmationEnabledEl = uifactory.addCheckboxesHorizontal(
				"admin.data.transfer.confirmation.enabled", formLayout, ENABLED_KEYS, enableValues);
		dataTransferConfirmationEnabledEl.select(ENABLED_KEYS[0], drawioModule.isDataTransferConfirmationEnabled());
		dataTransferConfirmationEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		thumbnailEnabled = drawioModule.isThumbnailEnabled();
		thumbnailEnabledEl = uifactory.addCheckboxesHorizontal("admin.thumbnail.enabled", formLayout, ENABLED_KEYS, enableValues);
		thumbnailEnabledEl.select(ENABLED_KEYS[0], drawioModule.isThumbnailEnabled());
		thumbnailEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		collaborationEnabled = drawioModule.isCollaborationEnabled();
		collaborationEnabledEl = uifactory.addCheckboxesHorizontal("admin.collaboration.enabled", formLayout, ENABLED_KEYS, enableValues);
		collaborationEnabledEl.select(ENABLED_KEYS[0], drawioModule.isCollaborationEnabled());
		collaborationEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}

	private void updateUI() {
		boolean enabled = enabledEl.isOn();
		editorUrlEl.setVisible(enabled);
		exportUrlEl.setVisible(enabled);
		dataTransferConfirmationEnabledEl.setVisible(enabled);
		dataTransferConfirmationEnabledEl.select(ENABLED_KEYS[0], dataTransferConfirmationEnabled);
		thumbnailEnabledEl.setVisible(enabled);
		thumbnailEnabledEl.select(ENABLED_KEYS[0], thumbnailEnabled);
		collaborationEnabledEl.setVisible(enabled);
		collaborationEnabledEl.select(ENABLED_KEYS[0], collaborationEnabled);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			updateUI();
		} else if (source == dataTransferConfirmationEnabledEl) {
			dataTransferConfirmationEnabled = dataTransferConfirmationEnabledEl.isAtLeastSelected(1);
		} else if (source == thumbnailEnabledEl) {
			thumbnailEnabled = thumbnailEnabledEl.isAtLeastSelected(1);
		} else if (source == collaborationEnabledEl) {
			collaborationEnabled = collaborationEnabledEl.isAtLeastSelected(1);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= validateIsMandatory(editorUrlEl);
		allOk &= validateIsMandatory(exportUrlEl);
		
		return allOk;
	}

	private boolean validateIsMandatory(TextElement textElement) {
		boolean allOk = true;
		
		textElement.clearError();
		if (textElement.isVisible() && !StringHelper.containsNonWhitespace(textElement.getValue())) {
			textElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isOn();
		drawioModule.setEnabled(enabled);
		
		String editorUrl = editorUrlEl.getValue();
		drawioModule.setEditorUrl(editorUrl);
		
		String exportUrl = exportUrlEl.getValue();
		drawioModule.setExportUrl(exportUrl);
		
		drawioModule.setDataTransferConfirmationEnabled(dataTransferConfirmationEnabled);
		
		drawioModule.setThumbnailEnabled(thumbnailEnabled);
		
		drawioModule.setCollaborationEnabled(collaborationEnabled);
	}

}
