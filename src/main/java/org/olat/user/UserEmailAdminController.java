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
package org.olat.user;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30.10.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UserEmailAdminController extends FormBasicController {
	
	private static final String USER_EMAIL_ADMIN_TITLE = "user.email.admin.title";
	private static final String USER_EMAIL_ADMIN_DESCRIPTION = "user.email.admin.description";
	private static final String USER_EMAIL_MANDATORY = "user.email.mandatory";
	private static final String USER_WITHOUT_EMAIL = "user.without.email";
	private static final String USER_EMAIL_UNIQUE = "user.email.unique";
	private static final String USER_WITH_EMAIL_DUPLICATES = "user.email.duplicates";
	private static final String USERS_DUPLICATE_EMAILS_EXIST = "users.duplicate.emails.exist";
	private static final String USER_EMAIL_MANDATORY_DISABLE_CONFIRMATION_TITLE = "user.email.mandatory.disable.confirmation.title";
	private static final String USER_EMAIL_MANDATORY_DISABLE_CONFIRMATION_TEXT = "user.email.mandatory.disable.confirmation.text";
	private static final String USER_EMAIL_UNIQUE_DISABLE_CONFIRMATION_TITLE = "user.email.unique.disable.confirmation.title";
	private static final String USER_EMAIL_UNIQUE_DISABLE_CONFIRMATION_TEXT = "user.email.unique.disable.confirmation.text";
	
	private MultipleSelectionElement userEmailMandatoryEl;
	private String[] userEmailMandatoryKey;
	private String[] userEmailMandatoryValue;
	private FormLink showUserWithoutEmailEl;
	private MultipleSelectionElement userEmailUniqueEl;
	private String[] userEmailUniqueKey;
	private String[] userEmailUniqueValue;
	private FormLink showUserEmailDuplicatesEl;
	private DialogBoxController confirmDisableMandatoryCtrl;
	private DialogBoxController confirmDisableUniqueCtrl;
	
	private int numberOfUsersWithDuplicateEmail;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserModule userModule;

	public UserEmailAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		userEmailMandatoryKey = new String [] { USER_EMAIL_MANDATORY};
		userEmailMandatoryValue = new String [] { "" };
		userEmailUniqueKey = new String [] { USER_EMAIL_UNIQUE};
		userEmailUniqueValue = new String [] { ""};
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle(USER_EMAIL_ADMIN_TITLE);
		setFormDescription(USER_EMAIL_ADMIN_DESCRIPTION);
		
		userEmailMandatoryEl = uifactory.addCheckboxesVertical(USER_EMAIL_MANDATORY, formLayout, userEmailMandatoryKey, userEmailMandatoryValue, 1);
		userEmailMandatoryEl.select(USER_EMAIL_MANDATORY, userModule.isEmailMandatory());
		userEmailMandatoryEl.addActionListener(FormEvent.ONCHANGE);
		
		int numberOfUsersWithoutEmail = userManager.findVisibleIdentitiesWithoutEmail().size();
		String usersWithoutEmailLinkText = translate(USER_WITHOUT_EMAIL, new String[] { Integer.toString(numberOfUsersWithoutEmail) });
		showUserWithoutEmailEl = uifactory.addFormLink(USER_WITHOUT_EMAIL, usersWithoutEmailLinkText, null, formLayout, Link.NONTRANSLATED);
		
		userEmailUniqueEl = uifactory.addCheckboxesVertical(USER_EMAIL_UNIQUE, formLayout, userEmailUniqueKey, userEmailUniqueValue, 1);
		userEmailUniqueEl.select(USER_EMAIL_UNIQUE, userModule.isEmailUnique());
		userEmailUniqueEl.addActionListener(FormEvent.ONCHANGE);
		
		numberOfUsersWithDuplicateEmail = userManager.findVisibleIdentitiesWithEmailDuplicates().size();
		String usersEmailDuplicatesLinkText = translate(USER_WITH_EMAIL_DUPLICATES, new String[] { Integer.toString(numberOfUsersWithDuplicateEmail) });
		showUserEmailDuplicatesEl = uifactory.addFormLink(USER_WITH_EMAIL_DUPLICATES, usersEmailDuplicatesLinkText, null, formLayout, Link.NONTRANSLATED);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (userEmailMandatoryEl.equals(source)) {
			boolean isEmailMandatory = userEmailMandatoryEl.isAtLeastSelected(1);
			doSetEmailMandatory(ureq, isEmailMandatory);
		} else if (userEmailUniqueEl.equals(source)) {
			boolean isEmailUnique = userEmailUniqueEl.isAtLeastSelected(1);
			doSetEmailUnique(ureq, isEmailUnique);
		} else if (showUserWithoutEmailEl.equals(source)) {
			doOpenUsersWithoutEmail(ureq);	
		} else if (showUserEmailDuplicatesEl.equals(source)) {
			doOpenUsersWithEmailDuplicates(ureq);
		}
	}

	private void doSetEmailMandatory(UserRequest ureq, boolean isEmailMandatory) {
		if (!isEmailMandatory) {
			doOpenEmailMandatoryDisableconfirmation(ureq);
		} else {
			userModule.setEmailMandatory(isEmailMandatory);
		}
	}
	
	private void doOpenEmailMandatoryDisableconfirmation(UserRequest ureq) {
		confirmDisableMandatoryCtrl = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(),
				translate(USER_EMAIL_MANDATORY_DISABLE_CONFIRMATION_TITLE),
				translate(USER_EMAIL_MANDATORY_DISABLE_CONFIRMATION_TEXT));
		listenTo(confirmDisableMandatoryCtrl);
		confirmDisableMandatoryCtrl.activate();
	}

	private void doSetEmailUnique(UserRequest ureq, boolean isEmailUnique) {
		if (isEmailUnique && existUsersWithDuplicateEmail()) {
			userEmailUniqueEl.select(USER_EMAIL_UNIQUE, false);
			showError(USERS_DUPLICATE_EMAILS_EXIST, Integer.toString(numberOfUsersWithDuplicateEmail));
		} else if (!isEmailUnique) {
			doOpenEmailUniqueDisableconfirmation(ureq);
		} else {
			userModule.setEmailUnique(isEmailUnique);
		}
	}

	private boolean existUsersWithDuplicateEmail() {
		return numberOfUsersWithDuplicateEmail > 0;
	}

	private void doOpenEmailUniqueDisableconfirmation(UserRequest ureq) {
		confirmDisableUniqueCtrl = DialogBoxUIFactory.createYesNoDialog(ureq, getWindowControl(),
				translate(USER_EMAIL_UNIQUE_DISABLE_CONFIRMATION_TITLE),
				translate(USER_EMAIL_UNIQUE_DISABLE_CONFIRMATION_TEXT));
		listenTo(confirmDisableUniqueCtrl);
		confirmDisableUniqueCtrl.activate();
	}

	private void doOpenUsersWithoutEmail(UserRequest ureq) {
		String businessPath = "[UserAdminSite:0][userswithoutemail:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenUsersWithEmailDuplicates(UserRequest ureq) {
		String businessPath = "[UserAdminSite:0][usersemailduplicates:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmDisableMandatoryCtrl) {
			boolean isEmailMandatory = !DialogBoxUIFactory.isYesEvent(event);
			userModule.setEmailMandatory(isEmailMandatory);
			userEmailMandatoryEl.select(USER_EMAIL_MANDATORY, isEmailMandatory);
			cleanUp();
		} else if (source == confirmDisableUniqueCtrl) {
			boolean isEmailUnique = !DialogBoxUIFactory.isYesEvent(event);
			userModule.setEmailUnique(isEmailUnique);
			userEmailUniqueEl.select(USER_EMAIL_UNIQUE, isEmailUnique);
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(confirmDisableUniqueCtrl);
		confirmDisableUniqueCtrl = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
