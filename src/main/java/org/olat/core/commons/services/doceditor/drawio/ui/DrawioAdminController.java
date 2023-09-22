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
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
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
	
	private MultipleSelectionElement enabledEl;
	private TextElement editorUrlEl;
	private TextElement exportUrlEl;
	private MultipleSelectionElement dataTransferConfirmationEnabledEl;
	private MultipleSelectionElement thumbnailEnabledEl;
	private MultipleSelectionElement collaborationEnabledEl;

	@Autowired
	private DrawioModule drawioModule;
	
	public DrawioAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(DocEditorController.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		
		String[] enableValues = new String[]{ translate("on") };
		enabledEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, ENABLED_KEYS, enableValues);
		enabledEl.select(ENABLED_KEYS[0], drawioModule.isEnabled());
		
		String editorUrl = drawioModule.getEditorUrl();
		editorUrlEl = uifactory.addTextElement("admin.editor.url", "admin.editor.url", 128, editorUrl, formLayout);
		editorUrlEl.setExampleKey("admin.editor.url.example", null);
		editorUrlEl.setMandatory(true);
		
		String exportUrl = drawioModule.getExportUrl();
		exportUrlEl = uifactory.addTextElement("admin.export.url", "admin.export.url", 128, exportUrl, formLayout);
		exportUrlEl.setExampleKey("admin.export.url.example", null);
		exportUrlEl.setMandatory(true);
		
		dataTransferConfirmationEnabledEl = uifactory.addCheckboxesHorizontal(
				"admin.data.transfer.confirmation.enabled", formLayout, ENABLED_KEYS, enableValues);
		dataTransferConfirmationEnabledEl.select(ENABLED_KEYS[0], drawioModule.isDataTransferConfirmationEnabled());
		
		thumbnailEnabledEl = uifactory.addCheckboxesHorizontal("admin.thumbnail.enabled", formLayout, ENABLED_KEYS, enableValues);
		thumbnailEnabledEl.select(ENABLED_KEYS[0], drawioModule.isThumbnailEnabled());
		
		collaborationEnabledEl = uifactory.addCheckboxesHorizontal("admin.collaboration.enabled", formLayout, ENABLED_KEYS, enableValues);
		collaborationEnabledEl.select(ENABLED_KEYS[0], drawioModule.isCollaborationEnabled());
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
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
		
		if (!StringHelper.containsNonWhitespace(textElement.getValue())) {
			textElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		drawioModule.setEnabled(enabled);
		
		String editorUrl = editorUrlEl.getValue();
		drawioModule.setEditorUrl(editorUrl);
		
		String exportUrl = exportUrlEl.getValue();
		drawioModule.setExportUrl(exportUrl);
		
		boolean dataTransferConfirmationEnabled = dataTransferConfirmationEnabledEl.isAtLeastSelected(1);
		drawioModule.setDataTransferConfirmationEnabled(dataTransferConfirmationEnabled);
		
		boolean thumbnailEnabled = thumbnailEnabledEl.isAtLeastSelected(1);
		drawioModule.setThumbnailEnabled(thumbnailEnabled);
		
		boolean collaborationEnabled = collaborationEnabledEl.isAtLeastSelected(1);
		drawioModule.setCollaborationEnabled(collaborationEnabled);
	}

}
