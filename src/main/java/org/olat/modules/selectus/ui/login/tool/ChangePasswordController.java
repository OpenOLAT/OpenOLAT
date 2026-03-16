/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.login.tool;

import static org.olat.login.ui.LoginUIFactory.formatDescriptionAsList;

import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.login.auth.OLATAuthManager;
import org.olat.login.validation.SyntaxValidator;
import org.olat.login.validation.ValidationResult;
import org.olat.user.ChangePasswordForm;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ui.RecruitingMainController;


/**
 * 
 * Description:<br>
 * Form to change the password saved in clear text in the user property
 * <P>
 * Initial Date:  20 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ChangePasswordController extends FormBasicController {
	
	private TextElement oldPasswordElement;
	private TextElement newPasswordElement;
	private TextElement confirmationElement;
	
	private final SyntaxValidator syntaxValidator;
	
	@Autowired
	private BaseSecurity baseSecurity;
	@Autowired
	private OLATAuthManager olatAuthManager;
	@Autowired
	private OLATAuthManager olatAuthenticationSpi;
	
	public ChangePasswordController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, null, Util.createPackageTranslator(ChangePasswordForm.class, ureq.getLocale(),
				Util.createPackageTranslator(RecruitingMainController.class, ureq.getLocale())));
		this.syntaxValidator = olatAuthManager.createPasswordSytaxValidator();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String descriptions = formatDescriptionAsList(syntaxValidator.getAllDescriptions(), getLocale());
		setFormDescription("form.password.validation.rules", new String[] { descriptions });

		//current password
		oldPasswordElement = uifactory.addPasswordElement("old_password", "change.password.currentPassword", 32, "", formLayout);
		oldPasswordElement.setMandatory(true);
		oldPasswordElement.setNotEmptyCheck("form.please.enter.old");
		//new password
		newPasswordElement = uifactory.addPasswordElement("new_password", "change.password.newPassword", 32, "", formLayout);
		newPasswordElement.setMandatory(true);
		newPasswordElement.setNotEmptyCheck("form.please.enter.new");
		//password confirmation
		confirmationElement = uifactory.addPasswordElement("confirmation", "change.password.passwordConfirmation", 32, "", formLayout);
		confirmationElement.setMandatory(true);
		confirmationElement.setNotEmptyCheck("form.please.enter.new");
		
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		newPasswordElement.clearError();
		String newPassword = newPasswordElement.getValue();
		ValidationResult validationResult = syntaxValidator.validate(newPassword, getIdentity());
		if (!validationResult.isValid()) {
			String descriptions = formatDescriptionAsList(validationResult.getInvalidDescriptions(), getLocale());
			newPasswordElement.setErrorKey("error.password.invalid", descriptions);
			allOk &= false;
		}

		oldPasswordElement.clearError();
		String oldPassword = getOldPassword();
		if(!StringHelper.containsNonWhitespace(oldPassword)) {
			oldPasswordElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		confirmationElement.clearError();
		String confirmation = confirmationElement.getValue();
		// Check if email is not already taken
		if (!StringHelper.containsNonWhitespace(confirmation) || !confirmation.equals(newPassword)) {
			// Oups, email already taken, display error
			confirmationElement.setErrorKey("error.password.nomatch");
			allOk &= false;
		}
		
		return allOk;
	}
	
	public String getNewPassword() {
		return newPasswordElement.getValue();
	}
	
	public String getOldPassword() {
		return oldPasswordElement.getValue();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String oldPassword = getOldPassword();
		Identity identity = null;
		Authentication olatAuthentication = baseSecurity.findAuthentication(ureq.getIdentity(), OLATAuthManager.PROVIDER_OLAT, BaseSecurity.DEFAULT_ISSUER);
		if (olatAuthentication == null) {
			showError("change.password.ldap_user");
		} else {
			identity = olatAuthenticationSpi.authenticate(ureq.getIdentity(), ureq.getIdentity().getName(), oldPassword, null);
		}

		if (identity == null) {
			showError("error.password.noauth");	
			return;
		}
		
		final String newPassword = getNewPassword();
		if (!StringHelper.containsNonWhitespace(newPassword)) {
			showError("error.password.noauth");	
			return;
		}
		
		boolean ok = olatAuthenticationSpi.changeOlatPassword(getIdentity(), identity, newPassword);
		if(ok) {
			showInfo("password.successful");
			logAudit("user password changed successfully of " + identity.getName(), null);
		} else {
			showError("password.failed");
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}