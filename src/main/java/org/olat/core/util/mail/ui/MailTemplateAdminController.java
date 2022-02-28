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
package org.olat.core.util.mail.ui;

import java.util.Arrays;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.GUISettings;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailContent;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  14 avr. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MailTemplateAdminController extends FormBasicController  {

	private static final int MAX_LENGTH = 10 * 1000 * 1000;
	
	private TextElement templateEl;
	private FormLink resetButton;
	private FormLink previewButton;
	
	private DialogBoxController confirmResetCtrl;
	private DialogBoxController previewCtrl;
	
	@Autowired
	private MailManager mailManager;
	@Autowired
	private GUISettings guiSettings;

	public MailTemplateAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, Util.createPackageTranslator(MailModule.class, ureq.getLocale()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("mail.template.title");
		setFormDescription("mail.template.description");
		setFormContextHelp("manual_admin/administration/E-Mail_Settings/#template");
		
		String emailCss = guiSettings.getGuiTheme().getEmailCss();
		emailCss = emailCss.replaceAll("}", "}\n");
		TextAreaElement emailCssEl = uifactory.addTextAreaElement("mail.admin.css", "mail.admin.css", -1, 10, 50, true, true, emailCss, formLayout);
		emailCssEl.setEnabled(false);

		String def = mailManager.getMailTemplate();
		templateEl = uifactory.addTextAreaElement("mail.template", "mail.template", -1, 12, 50, true, false, def, formLayout);

		final FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		buttonGroupLayout.setRootForm(mainForm);
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("save", buttonGroupLayout);
		resetButton = uifactory.addFormLink("mail.admin.reset.button", buttonGroupLayout, Link.BUTTON);
		previewButton = uifactory.addFormLink("mail.admin.preview.button", buttonGroupLayout, Link.BUTTON);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == resetButton) {
			doConfirmReset(ureq);
		} else if (source == previewButton) {
			doPreview(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (confirmResetCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doReset();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		String value = templateEl.getValue();
		templateEl.clearError();
		if(value.length() <= 0) {
			templateEl.setErrorKey("form.legende.mandatory", null);
			allOk = false;
		} else if(value.length() > MAX_LENGTH) {
			templateEl.setErrorKey("error.too.long", new String[] { Formatter.formatBytes(MAX_LENGTH) } );
			allOk = false;
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String value = templateEl.getValue();
		mailManager.setMailTemplate(value);
		getWindowControl().setInfo("saved");
	}
	
	private void doConfirmReset(UserRequest ureq) {
		confirmResetCtrl = activateYesNoDialog(ureq, null, translate("mail.admin.reset.confirm"), confirmResetCtrl);
	}

	private void doReset() {
		mailManager.deleteCustomMailTemplate();
		String template = mailManager.getMailTemplate();
		templateEl.setValue(template);
	}
	
	private void doPreview(UserRequest ureq) {
		String title = translate("mail.admin.preview.title");
		String body = translate("mail.admin.preview.text");
		MailBundle bundle = new MailBundle();
		bundle.setContent("Subject", body);
		MailContent mail = mailManager.decorateMail(bundle);
		List<String> buttonLabels = Arrays.asList(translate("ok"));
		activateGenericDialog(ureq, title, mail.getBody(), buttonLabels, previewCtrl);
	}

}