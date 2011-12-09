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
package org.olat.user.propertyhandlers;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.olat.admin.user.bulkChange.UserBulkChangeManager;
import org.olat.admin.user.bulkChange.UserBulkChangeStep00;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ItemValidatorProvider;
import org.olat.core.gui.formelements.FormElement;
import org.olat.core.gui.formelements.TextElement;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.registration.RegistrationManager;
import org.olat.registration.TemporaryKey;
import org.olat.user.UserManager;

import com.thoughtworks.xstream.XStream;

/**
 * <h3>Description:</h3>
 * The email field provides a user property that contains a valid mail address. The
 * validity of the mail address is based on the rules defined in the MailHelper
 * class.
 * <p>
 * If you want to allow only certain mail addresses, e.g. from your domain you
 * can easily create a subclass of this one and override the isValid method.
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class EmailProperty extends Generic127CharTextPropertyHandler {

	@Override
	protected void setInternalValue(User user, String mail) {
		// save mail addresses always lowercase
		if (mail != null) {
			super.setInternalValue(user, mail.toLowerCase());
		} else {
			super.setInternalValue(user, null);			
		}
	}

	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getUserPropertyAsHTML(org.olat.core.id.User, java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String mail = getUserProperty(user, locale);
		if (StringHelper.containsNonWhitespace(mail)) {
			StringBuffer sb = new StringBuffer();
			sb.append("<a href=\"mailto:");
			sb.append(mail);
			sb.append("\" class=\"b_link_mailto\">");
			sb.append(getUserProperty(user, locale));
			sb.append("</a>");
			return sb.toString();
		}
		return null;
	}
	
	/**
	 *  
	 * @see org.olat.user.propertyhandlers.UserPropertyHandler#addFormItem(java.util.Locale, org.olat.core.id.User, java.lang.String, boolean, org.olat.core.gui.components.form.flexible.FormItemContainer)
	 */
	@Override
	public FormItem addFormItem(Locale locale, final User user, String usageIdentifyer, final boolean isAdministrativeUser,	FormItemContainer formItemContainer) {
		org.olat.core.gui.components.form.flexible.elements.TextElement tElem = null;
		tElem = (org.olat.core.gui.components.form.flexible.elements.TextElement) super.addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
		//special validator in case of bulkChange, wizard in first step allows entry of ${userProperty} (velocity-style)
		//to validate the input a special isValidValue is used.
		if (usageIdentifyer.equals(UserBulkChangeStep00.class.getCanonicalName())){
			tElem.setItemValidatorProvider(new ItemValidatorProvider(){
				public boolean isValidValue(String value, ValidationError validationError, Locale locale2) {
					UserBulkChangeManager ubcMan = UserBulkChangeManager.getInstance();
					Context vcContext = new VelocityContext();
					if (user==null){
						vcContext = ubcMan.getDemoContext(locale2, isAdministrativeUser);
					}
					//should be used if user-argument !=null --> move to right place
					else {
						Long userKey = user.getKey();
						Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(userKey);
						ubcMan.setUserContext(identity, vcContext, isAdministrativeUser);
					}
					value = value.replace("$", "$!");
					String evaluatedValue = ubcMan.evaluateValueWithUserContext(value, vcContext);
					return EmailProperty.this.isValidValue(evaluatedValue, validationError, locale2);
				}
			});
		} 
		return tElem;
	}
	
	
	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValid(org.olat.core.gui.formelements.FormElement)
	 */
	@Override
	public boolean isValid(FormElement ui, Map formContext) {
		// check parent rules first: check if mandatory and empty
		if ( ! super.isValid(ui,formContext)) return false;
		TextElement uiEmail = (TextElement) ui;
		String value = uiEmail.getValue();
		if (StringHelper.containsNonWhitespace(value)) {	
			// check mail address syntax
			if ( ! MailHelper.isValidEmailAddress(value)) {
				uiEmail.setErrorKey(i18nFormElementLabelKey() + ".error.valid");
				return false;
			}
			// Email is syntactically correct. 
			// Check whether it's available.
			if (!uiEmail.isEmpty()) {
				if (!isAddressAvailable(value, (String)formContext.get("username"))) {
					uiEmail.setErrorKey(i18nFormElementLabelKey() + ".error.exists");
				  return false;
				}
			}
		}
		// everthing ok
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValid(org.olat.core.gui.components.form.flexible.FormItem, java.util.Map)
	 */
	@Override
	public boolean isValid(FormItem formItem, Map formContext) {
		if (!super.isValid(formItem, formContext)) {
			return false;
		}
		org.olat.core.gui.components.form.flexible.elements.TextElement textElement = (org.olat.core.gui.components.form.flexible.elements.TextElement)formItem;
		String value = textElement.getValue();

		if (StringHelper.containsNonWhitespace(value)) {
			// check mail address syntax
			if (!MailHelper.isValidEmailAddress(value)) {
				textElement.setErrorKey(i18nFormElementLabelKey() + ".error.valid", null);
				return false;
			}
			// email is syntactically correct. 
		  // Check whether it's available.
			if (!isAddressAvailable(value, (formContext != null) ? (String)formContext.get("username") : null)) {
				textElement.setErrorKey(i18nFormElementLabelKey() + ".error.exists", null);
			  return false;
			}
		}
		// all checks successful
		return true; 
	}

	@Override
	/**
	 * check for valid email
	 */
	public boolean isValidValue(String value, ValidationError validationError, Locale locale) {
		// check for length
		if (!super.isValidValue(value, validationError, locale)) {
			return false; 
		}

		if (StringHelper.containsNonWhitespace(value)) {
			// check mail address syntax
			if ( ! MailHelper.isValidEmailAddress(value)) {
				validationError.setErrorKey(i18nFormElementLabelKey() + ".error.valid");
				return false;
			}
			// all checks successful
		}
		// at this point we don't know if the email is mandatory - this must be
		// checked outside this method. empty email is valid here
		return true;
	}
	
	
	private boolean isAddressAvailable(String emailAddress, String currentUsername) {
		
		// Check if mail address already used
		// within the system by a user other than ourselves
		
		Identity identityOfEmail = UserManager.getInstance().findIdentityByEmail(emailAddress);
		if (currentUsername != null) {
			if ((identityOfEmail != null) && (!identityOfEmail.getName().equals(currentUsername))) {
				return false;
			}
		} else {
			if (identityOfEmail != null) {
				return false;
			}
		}
		
		return checkForScheduledAdressChange(emailAddress);
	}
	
	
	private boolean checkForScheduledAdressChange(String emailAddress) {
		// check if mail address scheduled to change
		RegistrationManager rm = RegistrationManager.getInstance();
		List<TemporaryKey> tk = rm.loadTemporaryKeyByAction(RegistrationManager.EMAIL_CHANGE);
		if (tk != null) {
			for (TemporaryKey temporaryKey : tk) {
				XStream xml = new XStream();
				HashMap<String, String> mails = (HashMap<String, String>) xml.fromXML(temporaryKey.getEmailAddress());
				if (emailAddress.equals(mails.get("changedEMail"))) {
					return false;
				}
			}
		}
		return true;
	}
}
