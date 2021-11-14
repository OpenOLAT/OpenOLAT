/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.util.i18n.ui;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * This form allows the user to delete language packs that are found in the
 * olatdata/customizing/lang/packs directory
 * 
 * <h3>Events fired by this controller</h3>
 * <ul>
 * <li>Event.CANCELLED_EVENT</li>
 * <li>Event.DONE_EVENT</li>
 * </ul>
 * <P>
 * Initial Date: 09.12.2008 <br>
 * 
 * @author gnaegi
 */
class I18nConfigSubDeletePackageController extends FormBasicController {
	private MultipleSelectionElement deleteLangPackSelection;
	private DialogBoxController dialogCtr;
	private FormLink cancelButton;
	private FormSubmit submitButton;
	
	@Autowired
	private I18nModule i18nModule;

	/**
	 * Constructor for the delete-language pack workflow
	 * 
	 * @param ureq
	 * @param control
	 */
	public I18nConfigSubDeletePackageController(UserRequest ureq, WindowControl control) {
		super(ureq, control, LAYOUT_VERTICAL);
		initForm(ureq);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#initForm(org.olat.core.gui.components.form.flexible.FormItemContainer,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// A title, displayed in fieldset
		setFormTitle("configuration.management.package.delete.title");
		setFormDescription("configuration.management.package.delete.description");
		// Add cancel and submit in button group layout
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		cancelButton = uifactory.addFormLink("cancel", buttonGroupLayout, Link.BUTTON);
		submitButton = uifactory.addFormSubmitButton("configuration.management.package.delete", buttonGroupLayout);
		submitButton.setEnabled(false); // enable as soon as something is checked
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> toDelete = deleteLangPackSelection.getSelectedKeys();
		if (toDelete.isEmpty()) {
			// should not happen since button disabled
			return;
		}
		dialogCtr = activateYesNoDialog(ureq, translate("configuration.management.package.delete.confirm.title"), translate(
				"configuration.management.package.delete.confirm", toDelete.toString()), dialogCtr);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doDelete();
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if (source == cancelButton) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
			showInfo("configuration.management.package.delete.cancel");

		} else if (source == deleteLangPackSelection) {
			if (deleteLangPackSelection.getSelectedKeys().isEmpty()) {
				submitButton.setEnabled(false);
			} else {
				submitButton.setEnabled(true);
			}
		}
	}
	
	private void doDelete() {
		try {
			for (String deleteLangPack : deleteLangPackSelection.getSelectedKeys()) {
				File file = new File(i18nModule.getLangPacksDirectory(), deleteLangPack);
				Files.deleteIfExists(file.toPath());
				logAudit("Deleted language pack::" + deleteLangPack);
			}
			// Reset i18n system
			i18nModule.reInitializeAndFlushCache();
			// wow, everything worked fine
			showInfo("configuration.management.package.delete.success", deleteLangPackSelection.getSelectedKeys().toString());
		} catch (IOException e) {
			logError("", e);
		}
	}
}
