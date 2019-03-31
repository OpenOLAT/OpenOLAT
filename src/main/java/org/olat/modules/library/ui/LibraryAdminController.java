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
package org.olat.modules.library.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.library.LibraryModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LibraryAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{"xx"};
	
	private MultipleSelectionElement enableEl;
	private TextElement mailAfterUploadEl;
	private TextElement mailAfterFreeingEl;
	
	@Autowired
	private LibraryModule libraryModule;
	
	public LibraryAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("library.configuration.title");
		
		boolean enabled = libraryModule.isEnabled();
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("library.enable", "library.enable", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.select("xx", enabled);
		
		String mailAfterUpload = libraryModule.getEmailContactsToNotifyAfterUpload();
		mailAfterUploadEl = uifactory.addTextElement("library.configuration.mail.after.upload", 256, mailAfterUpload, formLayout);
		
		String mailAfterFreeing = libraryModule.getEmailContactsToNotifyAfterFreeing();
		mailAfterFreeingEl = uifactory.addTextElement("library.configuration.mail.after.freeing", 256, mailAfterFreeing, formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		boolean enabled = enableEl.isAtLeastSelected(1);
		mailAfterUploadEl.setVisible(enabled);
		mailAfterFreeingEl.setVisible(enabled);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateEmail(mailAfterUploadEl);
		allOk &= validateEmail(mailAfterFreeingEl);
		return allOk;
	}
	
	private boolean validateEmail(TextElement el) {
		boolean allOk = true;
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue()) && !MailHelper.isValidEmailAddress(el.getValue())) {
			el.setErrorKey("error.mail.not.valid", null);
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		libraryModule.setEnabled(enabled);
		if(enabled) {
			libraryModule.setEmailContactsToNotifyAfterUpload(mailAfterUploadEl.getValue());
			libraryModule.setEmailContactsToNotifyAfterFreeing(mailAfterFreeingEl.getValue());
		}
	}
}
