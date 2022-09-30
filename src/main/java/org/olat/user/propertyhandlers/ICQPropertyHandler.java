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
 * Copyright (c) 2009 frentix GmbH, Switzerland<br>
 * <p>
 */

package org.olat.user.propertyhandlers;

import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;

/**
 * Implements a user property handler for ICQ screen names.
 * 
 * <P>
 * Initial Date: Jul 28, 2009 <br>
 * 
 * @author twuersch
 */
public class ICQPropertyHandler extends Generic127CharTextPropertyHandler {

	public static final int ICQ_NAME_MIN_LENGTH = 5;
	public static final int ICQ_NAME_MAX_LENGTH = 16;
	public static final String ICQ_INDICATOR_URL = "http://status.icq.com/online.gif";
	public static final String ICQ_NAME_VALIDATION_URL = "http://www.icq.com/people/";

	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getUserPropertyAsHTML(org.olat.core.id.User,
	 *      java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		// return super.getUserPropertyAsHTML(user, locale);
		String icqname = getUserProperty(user, locale);
		if (StringHelper.containsNonWhitespace(icqname)) {
			icqname = StringHelper.escapeHtml(icqname);
			StringBuilder sb = new StringBuilder();
			sb.append("<a href=\"").append(ICQ_NAME_VALIDATION_URL).append(icqname).append("\" target=\"_blank\">")
			  .append(icqname).append("</a>")
			  .append("<img src=\"").append(ICQ_INDICATOR_URL).append("?icq=").append(icqname)
			  .append("&img=5\" style=\"width:10px; height:10px; margin-left:2px;\" alt=\"\">");
			return sb.toString();
		} else {
			return null;
		}
	}

	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValidValue(java.lang.String,
	 *      org.olat.core.gui.components.form.ValidationError, java.util.Locale)
	 */
	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (!super.isValidValue(user, value, validationError, locale)) {
			return false;
		}

		// allow empty string
		if (!StringHelper.containsNonWhitespace(value))
			return true;
		return isValidICQNumber(value);
	}

	/**
	 * checks wheter given string is numerical and not too long. DOES NOT check
	 * if a icq user exists with this number!
	 * 
	 * @param input
	 * @return
	 */
	private boolean isValidICQNumber(String input) {
		if (StringHelper.containsNonWhitespace(input)) {
			if (input.length() > ICQ_NAME_MAX_LENGTH || input.length() < ICQ_NAME_MIN_LENGTH)
				return false;
			try {
				Long.parseLong(input);
			} catch (NumberFormatException e) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#addFormItem(java.util.Locale,
	 *      org.olat.core.id.User, java.lang.String, boolean,
	 *      org.olat.core.gui.components.form.flexible.FormItemContainer)
	 */
	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		TextElement textElement = (TextElement) super.addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
		textElement.setMaxLength(ICQ_NAME_MAX_LENGTH);

		if (!UserManager.getInstance().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
			textElement.setExampleKey("form.example.icqname", null);
		}
		return textElement;
	}

	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValid(org.olat.core.gui.components.form.flexible.FormItem,
	 *      java.util.Map)
	 */
	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		// FXOLAT-343 ::
		// there's no official icq-api to check if a user exists..
		// the previous check failed (nov 2011), urls changed etc...
		// so check only for numerical value and length!
		TextElement textElement = (TextElement) formItem;
		if (StringHelper.containsNonWhitespace(textElement.getValue())) {
			boolean valid = isValidICQNumber(textElement.getValue());
			if (!valid) {
				textElement.setErrorKey("form.name.icq.error", null);
			}
			return valid;
		}
		return true;

	}
}
