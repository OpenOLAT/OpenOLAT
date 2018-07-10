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

import java.util.Locale;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.context.Context;
import org.olat.admin.user.bulkChange.UserBulkChangeManager;
import org.olat.admin.user.bulkChange.UserBulkChangeStep00;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ItemValidatorProvider;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.mail.MailHelper;
import org.olat.user.UserManager;

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
		// save mail addresses always lower case and remove trailing whitespace
		if (mail != null) {
			super.setInternalValue(user, mail.toLowerCase().trim());
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
			mail = StringHelper.escapeHtml(mail);
			StringBuilder sb = new StringBuilder();
			sb.append("<a href=\"mailto:")
			  .append(mail)
			  .append("\"><i class='o_icon o_icon_mail'> </i> ")
			  .append(mail)
			  .append("</a>");
			return StringHelper.xssScan(sb.toString());
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
			tElem.setItemValidatorProvider(new ItemValidatorProvider() {
				@Override
				public boolean isValidValue(String value, ValidationError validationError, Locale locale2) {
					UserBulkChangeManager ubcMan = CoreSpringFactory.getImpl(UserBulkChangeManager.class);
					Context vcContext = new VelocityContext();
					if (user==null){
						vcContext = ubcMan.getDemoContext(locale2);
					}
					//should be used if user-argument !=null --> move to right place
					else {
						Long userKey = user.getKey();
						Identity identity = BaseSecurityManager.getInstance().loadIdentityByKey(userKey);
						ubcMan.setUserContext(identity, vcContext);
					}
					value = value.replace("$", "$!");
					String evaluatedValue = ubcMan.evaluateValueWithUserContext(value, vcContext);
					return EmailProperty.this.isValidValue(user, evaluatedValue, validationError, locale2);
				}
			});
		} 
		return tElem;
	}
	
	
	/* (non-Javadoc)
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValid(org.olat.core.gui.components.form.flexible.FormItem, java.util.Map)
	 */
	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		if (!super.isValid(user, formItem, formContext)) {
			return false;
		}
		org.olat.core.gui.components.form.flexible.elements.TextElement textElement = (org.olat.core.gui.components.form.flexible.elements.TextElement)formItem;
		String value = textElement.getValue();

		if (StringHelper.containsNonWhitespace(value)) {
			value = value.toLowerCase().trim();
			// check mail address syntax
			if (!MailHelper.isValidEmailAddress(value)) {
				textElement.setErrorKey(i18nFormElementLabelKey() + ".error.valid", null);
				return false;
			}
			// email is syntactically correct. 
		  // Check whether it's available.
			if (!isAddressAvailable(value, (formContext != null) ? formContext.get("username") : null)) {
				textElement.setErrorKey(i18nFormElementLabelKey() + ".error.exists", new String[] { value });
			  return false;
			}
		}
		// all checks successful
		return true; 
	}

	/**
	 * check for valid email
	 */
	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		// check for length
		if (!super.isValidValue(user, value, validationError, locale)) {
			return false; 
		}

		if (StringHelper.containsNonWhitespace(value)) {
			value = value.toLowerCase().trim();
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
		User currentUser = null; 
		Identity currentIdentity;
		if (currentUsername != null) {
			currentIdentity = BaseSecurityManager.getInstance().findIdentityByName(currentUsername);
			if (currentIdentity != null) {
				currentUser = currentIdentity.getUser();
			}
		}
		return UserManager.getInstance().isEmailAllowed(emailAddress, currentUser);
	}

}
