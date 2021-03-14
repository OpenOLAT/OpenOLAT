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

package org.olat.registration;

import java.io.File;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
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

public class DisclaimerController extends BasicController {

	private static final String SR_ERROR_DISCLAIMER_CHECKBOX = "sr.error.disclaimer.checkbox";
	private static final String SR_ERROR_DISCLAIMER_CHECKBOXES = "sr.error.disclaimer.checkboxes";

	private Link downloadLink;
	private VFSLeaf downloadFile;
	private VelocityContainer main;

	private CloseableModalController cmc;
	private ContactFormController contactCtrl;
	private DisclaimerFormController disclaimerFormController;
	private RequestAccountDeletionController requestAccountDeletetionCtrl;
	private RequestAccountDataDeletetionController requestAccountDataDeletetionCtrl;
	
	private Identity identity;

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
	public DisclaimerController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, ureq.getIdentity(), true);
		
		if(userModule.isAllowRequestToDeleteAccount() && ureq.getIdentity() != null) {
			requestAccountDeletetionCtrl = new RequestAccountDeletionController(ureq, getWindowControl());
			listenTo(requestAccountDeletetionCtrl);
			main.put("radform", requestAccountDeletetionCtrl.getInitialComponent());
		}
	}

	/**
	 * Display a disclaimer which can be accepted or denied or in a read only manner
	 * @param ureq The user request
	 * @param wControl The window control
	 * @param identity The identity which need to accept the disclaimer (or null if it doesn't exist now)
	 * @param readOnly true: show only read only; false: allow user to accept
	 */
	public DisclaimerController(UserRequest ureq, WindowControl wControl, Identity identity, boolean readOnly) {
		super(ureq, wControl);
		
		this.identity = identity;
	
		disclaimerFormController = new DisclaimerFormController(ureq, wControl, readOnly);
		listenTo(disclaimerFormController);
		
		main = createVelocityContainer("disclaimer");
		main.put("dclform", disclaimerFormController.getInitialComponent());
		
		// add optinal download link, see class comments in DisclaimerFormController
		// Add the additional link to the form (depending on the configuration)
		if (registrationModule.isDisclaimerAdditionaLinkText()) {
			File disclaimerDir = new File(WebappHelper.getUserDataRoot() + "/customizing/disclaimer/");
			disclaimerDir.mkdirs();
			VFSContainer disclaimerContainer = new LocalFolderImpl(disclaimerDir);
			String i18nIfiedFilename = translate("disclaimer.filedownloadurl");
			downloadFile = (VFSLeaf)disclaimerContainer.resolve(i18nIfiedFilename);
			if (downloadFile != null) {
				downloadLink = LinkFactory.createLink("disclaimer.additionallinktext", main, this);
				downloadLink.setTarget("_blank");
				
				if (i18nIfiedFilename.toLowerCase().endsWith(".pdf")) {
					downloadLink.setIconLeftCSS("o_icon o_icon-fw o_filetype_pdf");
				} else if (i18nIfiedFilename.toLowerCase().endsWith(".html") || i18nIfiedFilename.toLowerCase().endsWith(".htm")) {
					downloadLink.setIconLeftCSS("o_icon o_icon-fw o_filetype_html");
				} else if (i18nIfiedFilename.toLowerCase().endsWith(".doc")) {
					downloadLink.setIconLeftCSS("o_icon o_icon-fw o_filetype_doc");
				}
			}
		}

		putInitialPanel(main);
	}
	
	public String getAndRemoveTitle() {
		if(disclaimerFormController != null) {
			return disclaimerFormController.getAndRemoveFormTitle();
		}
		return null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == downloadLink) {
			ureq.getDispatchResult().setResultingMediaResource(new VFSMediaResource(downloadFile));
			// Prevent "do not press reload" message.
			downloadLink.setDirty(false);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == disclaimerFormController) {
			if (event == Event.CANCELLED_EVENT) {
				doCancel(ureq);
			} else if (event == Event.DONE_EVENT) {
				acceptDisclaimer(ureq);
			}
		} else if(requestAccountDeletetionCtrl == source) {
			if (event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
		} else if(requestAccountDataDeletetionCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, Event.CANCELLED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doDeleteData(ureq);
			}
		} else if(contactCtrl == source) {
			cmc.deactivate();
			cleanUp();
			fireEvent(ureq, Event.CANCELLED_EVENT);
			if(event == Event.DONE_EVENT) {
				showInfo("request.delete.account.sent");
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(requestAccountDataDeletetionCtrl);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(cmc);
		requestAccountDataDeletetionCtrl = null;
		contactCtrl = null;
		cmc = null;
	}
	
	private void acceptDisclaimer(UserRequest ureq) {
		// Verify that, if the additional checkbox is configured to be visible, it is checked as well
		boolean accepted = (disclaimerFormController.acceptCheckbox != null) ? (disclaimerFormController.acceptCheckbox.isSelected(0)) : false;
		// configure additional checkbox, see class comments in DisclaimerFormController
		if (accepted && registrationModule.isDisclaimerAdditionalCheckbox()) {
			accepted = (disclaimerFormController.additionalCheckbox != null) ? (disclaimerFormController.additionalCheckbox.isSelected(0)) : false;
			if (accepted && registrationModule.isDisclaimerAdditionalCheckbox2()) {
				accepted = (disclaimerFormController.additionalCheckbox2 != null) ? (disclaimerFormController.additionalCheckbox2.isSelected(0)) : false;
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
	}
	
	/**
	 * Change the locale of this controller.
	 * @param locale
	 */
	public void changeLocale(Locale locale) {
		getTranslator().setLocale(locale);
		main.put("dclform", this.disclaimerFormController.getInitialComponent());
	}
	
	private void doDeleteData(UserRequest ureq) {
		if(identity.getLastLogin() == null) {
			userLifecycleManager.deleteIdentity(identity, identity);
			fireEvent(ureq, Event.CANCELLED_EVENT);
		} else {
			doOpenContactForm(ureq);
		}
	}
	
	private void doOpenContactForm(UserRequest ureq) {
		if(contactCtrl != null) return;
		
		String[] args = new String[] {
			identity.getKey().toString(),											// 0
			identity.getName(),														// 1
			identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()),	// 2
			identity.getUser().getProperty(UserConstants.LASTNAME, getLocale())		// 3
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
		if(identity == null || !userModule.isAllowRequestToDeleteAccountDisclaimer()) {
			fireEvent(ureq, Event.CANCELLED_EVENT);
			return;
		}
		
		requestAccountDataDeletetionCtrl = new RequestAccountDataDeletetionController(ureq, getWindowControl());
		listenTo(requestAccountDataDeletetionCtrl);
		
		String title = translate("request.data.deletion.title");
		cmc = new CloseableModalController(getWindowControl(), "c", requestAccountDataDeletetionCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	@Override
	protected void doDispose() {
		//
	}
}
