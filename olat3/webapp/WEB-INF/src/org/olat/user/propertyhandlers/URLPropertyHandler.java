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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2007 frentix GmbH, Switzerland<br>
 * <p>
 */
package org.olat.user.propertyhandlers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.formelements.FormElement;
import org.olat.core.gui.formelements.TextElement;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;

/**
 * <h3>Description:</h3>
 * The url field provides a user property that contains a valid URL. 
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class URLPropertyHandler extends Generic127CharTextPropertyHandler {
	
	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getUserPropertyAsHTML(org.olat.core.id.User, java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String href = getUserProperty(user, locale);
		if (StringHelper.containsNonWhitespace(href)) {
			StringBuffer sb = new StringBuffer();
			sb.append("<a href=\"");
			sb.append(href);
			sb.append("\" class=\"b_link_extern\" target=\"_blank\">");
			sb.append(getUserProperty(user, locale));
			sb.append("</a>");
			return sb.toString();
		}
		return null;
	}

	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#getFormElement(java.util.Locale, org.olat.core.id.User, java.lang.String, boolean)
	 */
	@Override
	public FormElement getFormElement(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser) {
		TextElement ui = (TextElement) super.getFormElement(locale, user, usageIdentifyer, isAdministrativeUser);
		if ( ! UserManager.getInstance().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
			ui.setExample("http://www.olat.org");
		}
		return ui;
	}
	
	/* (non-Javadoc)
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#addFormItem(java.util.Locale, org.olat.core.id.User, java.lang.String, boolean, org.olat.core.gui.components.form.flexible.FormItemContainer)
	 */
	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		org.olat.core.gui.components.form.flexible.elements.TextElement textElement = (org.olat.core.gui.components.form.flexible.elements.TextElement)super.addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
		if ( ! UserManager.getInstance().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
			textElement.setExampleKey("form.example.url", null);
		}
		return textElement;
	}
	
	

	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValid(org.olat.core.gui.formelements.FormElement)
	 */
	@Override
	public boolean isValid(FormElement ui, Map formContext) {
		// check parent rules first: check if mandatory and empty
		if ( ! super.isValid(ui, formContext)) return false;
		TextElement uiURL = (TextElement) ui;
		String value = uiURL.getValue();
		if (StringHelper.containsNonWhitespace(value)) {			
			// check url address syntax
			try {
				new URL(value);
			} catch (MalformedURLException e) {
				uiURL.setErrorKey(i18nFormElementLabelKey() + ".error.valid");
				return false;
			}
		}
		// everthing ok
		return true;			
	}

	@Override
	public boolean isValid(FormItem formItem, Map formContext) {
		// check parent rules first: check if mandatory and empty
		if ( ! super.isValid(formItem, formContext)) return false;
		org.olat.core.gui.components.form.flexible.elements.TextElement uiEl = (org.olat.core.gui.components.form.flexible.elements.TextElement) formItem;
		String value = uiEl.getValue();
		ValidationError validationError = new ValidationError();
		boolean valid = isValidValue(value, validationError, formItem.getTranslator().getLocale());
		if(!valid) {
			uiEl.setErrorKey(validationError.getErrorKey(), new String[]{});
		}
		return valid;	
	}

	/* (non-Javadoc)
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValidValue(java.lang.String, org.olat.core.gui.components.form.ValidationError, java.util.Locale)
	 */
	@Override
	public boolean isValidValue(String value, ValidationError validationError, Locale locale) {
		if ( ! super.isValidValue(value, validationError, locale)) return false;
		
		if (StringHelper.containsNonWhitespace(value)) {			
			// check url address syntax
			try {
				new URL(value);
			} catch (MalformedURLException e) {
				validationError.setErrorKey(i18nFormElementLabelKey() + ".error.valid");
				return false;
			}
		}
		// everthing ok
		return true;
	}

}
