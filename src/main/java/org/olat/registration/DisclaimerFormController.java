/**
 * OLAT - Online Learning and Training<br>
 * https://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
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
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * This file has been modified by the OpenOLAT community. Changes are licensed
 * under the Apache 2.0 license as the original file.
 */

package org.olat.registration;

import java.io.File;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormCancel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.modules.co.ContactFormController;
import org.olat.user.UserLifecycleManager;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date:  10.08.2004
 *
 * @author Mike Stock
 *
 * Comment:  
 * Presents a disclaimer form with two text paragraphs and a checkbox. The text can be changed using the i18n tool.
 * <p />
 * The controller tries to read the following keys from the i18n files to add
 * some optional features:
 * <ul>
 * <li>disclaimer.additionalcheckbox : if defined, a second checkbox is added
 * with the text translated by this key</li>
 * <li>disclaimer.filedownloadurl : a relative filename to a file that must be
 * located in the olatdata/customizing/disclaimer/ directory. If defined and the
 * file exists, a file download to this file is offered</li>
 * </ul>
 *
 */

public class DisclaimerFormController extends FormBasicController {

	private static final String SR_ERROR_DISCLAIMER_CHECKBOX = "sr.error.disclaimer.checkbox";
	private static final String SR_ERROR_DISCLAIMER_CHECKBOXES = "sr.error.disclaimer.checkboxes";

	public  static final String DCL_ACCEPT = "dcl.accept";
	public  static final String DCL_CHECKBOX_KEY = "dclchkbox";
	public  static final String DCL_CHECKBOX_KEY2 = "dclchkbox2";
	public  static final String DCL_CHECKBOX_KEY3 = "dclchkbox3";
	private static final String NLS_DISCLAIMER_OK = "disclaimer.ok";
	private static final String NLS_DISCLAIMER_NOK = "disclaimer.nok";
	private static final String NLS_DISCLAIMER_ACKNOWLEDGED = "disclaimer.acknowledged";
	private static final String ACKNOWLEDGE_CHECKBOX_NAME = "acknowledge_checkbox";
	private static final String ADDITIONAL_CHECKBOX_NAME = "additional_checkbox";
	private static final String ADDITIONAL_CHECKBOX_2_NAME = "additional_checkbox_2";

	private final boolean readOnly;
	private final boolean withButtons;

	protected MultipleSelectionElement acceptCheckbox;
	protected MultipleSelectionElement additionalCheckbox;
	protected MultipleSelectionElement additionalCheckbox2;
	private FormLink downloadLink;

	private VFSLeaf downloadFile;
	private final Identity identity;

	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private RequestAccountDeletionController requestAccountDeletionCtrl;
	private RequestAccountDataDeletetionController requestAccountDataDeletionCtrl;

	@Autowired
	private UserModule userModule;
	@Autowired
	private RegistrationModule registrationModule;
	@Autowired
	private UserLifecycleManager userLifecycleManager;

	/**
	 * Display the disclaimer in a read only view to the current user.
	 *
	 * @param ureq
	 * @param wControl
	 */
	public DisclaimerFormController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, ureq.getIdentity(), true);
		if (userModule.isAllowRequestToDeleteAccount() && ureq.getIdentity() != null) {
			requestAccountDeletionCtrl = new RequestAccountDeletionController(ureq, getWindowControl());
			listenTo(requestAccountDeletionCtrl);

			flc.put("radform", requestAccountDeletionCtrl.getInitialComponent());
		}
	}

	public DisclaimerFormController(UserRequest ureq, WindowControl wControl, Form mainForm) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		this.identity = ureq.getIdentity();
		this.readOnly = false;
		this.withButtons = false;

		initForm(ureq);
	}

	/**
	 * Display a disclaimer which can be accepted or denied or in a read only manner
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param identity The identity which need to accept the disclaimer (or null if it doesn't exist now)
	 * @param readOnly true: show only read only; false: allow user to accept
	 */
	public DisclaimerFormController(UserRequest ureq, WindowControl wControl, Identity identity, boolean readOnly) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.identity = identity;
		this.readOnly = readOnly;
		this.withButtons = !readOnly;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_disclaimer");
		
		FormLayoutContainer disclaimerCont = FormLayoutContainer.createCustomFormLayout("disclaimer", getTranslator(), velocity_root + "/disclaimer.html");
		disclaimerCont.setRootForm(mainForm);
		formLayout.add(disclaimerCont);

		FormLayoutContainer legendContainer = FormLayoutContainer.createBareBoneFormLayout("legend", getTranslator());
		legendContainer.setElementCssClass("o_disclaimer");
		legendContainer.setFormTitle(translate("disclaimer.terms.of.usage"));
		legendContainer.setFormTitleIconCss("o_icon o_icon_fw o_icon_disclaimer");
		formLayout.add(legendContainer);

		String paragraph1 = translate("disclaimer.paragraph1");
		String paragraph2 = translate("disclaimer.paragraph2");
		FormLayoutContainer contentContainer = FormLayoutContainer.createBareBoneFormLayout("content", getTranslator());
		contentContainer.setElementCssClass("o_disclaimer_content");
		contentContainer.contextPut("paragraph1", paragraph1);
		contentContainer.contextPut("paragraph2", paragraph2);
		disclaimerCont.add(contentContainer);

		if (registrationModule.isDisclaimerAdditionaLinkText()) {
			File disclaimerDir = new File(WebappHelper.getUserDataRoot() + "/customizing/disclaimer/");
			disclaimerDir.mkdirs();
			VFSContainer disclaimerContainer = new LocalFolderImpl(disclaimerDir);
			String i18nIfiedFilename = translate("disclaimer.filedownloadurl");
			downloadFile = (VFSLeaf) disclaimerContainer.resolve(i18nIfiedFilename);

			if (downloadFile != null) {
				downloadLink = uifactory.addFormLink("disclaimer.additionallinktext", formLayout, Link.NONTRANSLATED);
				downloadLink.setTarget("_blank");
				if (downloadFile.getName().toLowerCase().endsWith(".pdf")) {
					downloadLink.setIconLeftCSS("o_icon o_icon-fw o_filetype_pdf");
				} else if (downloadFile.getName().toLowerCase().endsWith(".html") || downloadFile.getName().toLowerCase().endsWith(".htm")) {
					downloadLink.setIconLeftCSS("o_icon o_icon-fw o_filetype_html");
				} else if (downloadFile.getName().toLowerCase().endsWith(".doc")) {
					downloadLink.setIconLeftCSS("o_icon o_icon-fw o_filetype_doc");
				}
			}
		}

		// Add the "accept" checkbox to the form.
		acceptCheckbox = uifactory.addCheckboxesVertical(ACKNOWLEDGE_CHECKBOX_NAME, null, formLayout,
				new String[]{DCL_CHECKBOX_KEY}, new String[]{translate(NLS_DISCLAIMER_ACKNOWLEDGED)}, 1);
		acceptCheckbox.setEscapeHtml(false);
		acceptCheckbox.setMandatory(false);
		acceptCheckbox.select(DCL_CHECKBOX_KEY, readOnly);

		// Add the additional checkbox to the form (depending on the configuration)
		if (registrationModule.isDisclaimerAdditionalCheckbox()) {
			String additionalCheckboxText = translate("disclaimer.additionalcheckbox");
			additionalCheckbox = uifactory.addCheckboxesVertical(ADDITIONAL_CHECKBOX_NAME, null, formLayout,
					new String[]{DCL_CHECKBOX_KEY2}, new String[]{additionalCheckboxText}, 1);
			additionalCheckbox.setEscapeHtml(false);
			additionalCheckbox.select(DCL_CHECKBOX_KEY2, readOnly);
			if (registrationModule.isDisclaimerAdditionalCheckbox2()) {
				String additionalCheckbox2Text = translate("disclaimer.additionalcheckbox2");
				additionalCheckbox2 = uifactory.addCheckboxesVertical(ADDITIONAL_CHECKBOX_2_NAME, null, formLayout,
						new String[]{DCL_CHECKBOX_KEY3}, new String[]{additionalCheckbox2Text}, 1);
				additionalCheckbox2.setEscapeHtml(false);
				additionalCheckbox2.select(DCL_CHECKBOX_KEY3, readOnly);
			}
		}

		if (readOnly) {
			formLayout.setEnabled(false);
		} else if(withButtons) {
			// Create submit and cancel buttons
			final FormLayoutContainer buttonLayout = uifactory.addButtonsFormLayout("buttons", null, formLayout);
			buttonLayout.setElementCssClass("o_sel_disclaimer_buttons");
			uifactory.addFormSubmitButton(DCL_ACCEPT, NLS_DISCLAIMER_OK, buttonLayout);
			FormCancel cancelButton = uifactory.addFormCancelButton(NLS_DISCLAIMER_NOK, buttonLayout, ureq, getWindowControl());	
			cancelButton.setI18nKey(NLS_DISCLAIMER_NOK);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		acceptDisclaimer(ureq);
	}

	public String getAndRemoveTitle() {
		return getAndRemoveFormTitle();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == downloadLink) {
			ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(downloadFile));
			// Prevent "do not press reload" message.
			downloadLink.setForceOwnDirtyFormWarning(false);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (requestAccountDeletionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if (requestAccountDataDeletionCtrl == source) {
			if (event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
			if (event == Event.DONE_EVENT) {
				doDeleteData(ureq);
			}
		} else if (contactCtrl == source) {
			cmc.deactivate();
			cleanUp();
			fireEvent(ureq, Event.CANCELLED_EVENT);
			if (event == Event.DONE_EVENT) {
				showInfo("request.delete.account.sent");
			}
		} else if (cmc == source) {
			cleanUp();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(requestAccountDataDeletionCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(cmc);
		requestAccountDataDeletionCtrl = null;
		contactCtrl = null;
		cmc = null;
	}

	protected boolean acceptDisclaimer(UserRequest ureq) {
		// Verify that, if the additional checkbox is configured to be visible, it is checked as well
		boolean accepted = acceptCheckbox != null && (acceptCheckbox.isSelected(0));
		// configure additional checkbox, see class comments in DisclaimerFormController
		if (accepted && registrationModule.isDisclaimerAdditionalCheckbox()) {
			accepted = additionalCheckbox != null && (additionalCheckbox.isSelected(0));
			if (accepted && registrationModule.isDisclaimerAdditionalCheckbox2()) {
				accepted = additionalCheckbox2 != null && (additionalCheckbox2.isSelected(0));
			}
		}
		if (accepted) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (registrationModule.isDisclaimerAdditionalCheckbox()) {
			// error handling case multiple checkboxes enabled
			showError(SR_ERROR_DISCLAIMER_CHECKBOXES);
		} else {
			// error handling case single checkboxe enabled
			showError(SR_ERROR_DISCLAIMER_CHECKBOX);
		}
		return accepted;
	}

	/**
	 * Change the locale of this controller.
	 * @param locale
	 */
	public void changeLocale(Locale locale) {
		getTranslator().setLocale(locale);
	}

	private void doDeleteData(UserRequest ureq) {
		if (identity.getLastLogin() == null) {
			userLifecycleManager.deleteIdentity(identity, identity);
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else {
			doOpenContactForm(ureq);
		}
	}

	private void doOpenContactForm(UserRequest ureq) {
		if (contactCtrl != null) return;

		String[] args = new String[]{
				identity.getKey().toString(),                                            // 0
				identity.getName(),                                                      // 1
				identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()),    // 2
				identity.getUser().getProperty(UserConstants.LASTNAME, getLocale())      // 3
		};
		ContactMessage contactMessage = new ContactMessage(identity);
		contactMessage.setSubject(translate("request.delete.account.subject", args));
		contactMessage.setBodyText(translate("request.delete.account.body", args));

		String mailAddress = userModule.getMailToRequestAccountDeletion();
		ContactList contact = new ContactList(mailAddress);
		contact.add(mailAddress);
		contactMessage.addEmailTo(contact);

		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, false, contactMessage);
		listenTo(contactCtrl);

		String title = translate("request.delete.account");
		cmc = new CloseableModalController(getWindowControl(), "c", contactCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	private void doCancel(UserRequest ureq) {
		if (identity == null || !userModule.isAllowRequestToDeleteAccountDisclaimer()) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
			return;
		}

		requestAccountDataDeletionCtrl = new RequestAccountDataDeletetionController(ureq, getWindowControl());
		listenTo(requestAccountDataDeletionCtrl);

		String title = translate("request.data.deletion.title");
		cmc = new CloseableModalController(getWindowControl(), "c", requestAccountDataDeletionCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		doCancel(ureq);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}
}
