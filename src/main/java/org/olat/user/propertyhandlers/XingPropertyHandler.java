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
 * Implements a user property handler for XING screen names.
 * 
 * <P>
 * Initial Date: Jul 28, 2009 <br>
 * 
 * @author twuersch
 */
public class XingPropertyHandler extends Generic127CharTextPropertyHandler {

	public static final int XING_NAME_MAX_LENGTH = 320;
	public static final String XING_NAME_PROFILE_URL = "http://www.xing.com/profile/";

	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValidValue(java.lang.String,
	 *      org.olat.core.gui.components.form.ValidationError, java.util.Locale)
	 */
	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (!super.isValidValue(user, value, validationError, locale)) {
			return false;
		}
		if (StringHelper.containsNonWhitespace(value)) {
			return value.length() <= XING_NAME_MAX_LENGTH;
		}
		return true;
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
		textElement.setMaxLength(XING_NAME_MAX_LENGTH);

		if (!UserManager.getInstance().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
			textElement.setExampleKey("form.example.xingname", null);
		}
		return textElement;
	}

	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getUserPropertyAsHTML(org.olat.core.id.User,
	 *      java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String xingname = getUserProperty(user, locale);
		if (StringHelper.containsNonWhitespace(xingname)) {
			xingname = StringHelper.escapeHtml(xingname);
			StringBuilder sb = new StringBuilder();
			sb.append("<a href=\"").append(XING_NAME_PROFILE_URL).append(xingname).append("\" target=\"_blank\"><i class='o_icon o_icon-fw o_icon_xing'>&nbsp;</i>")
			  .append(xingname)
			  .append("</a>");
			return sb.toString();
		} else {
			return null;
		}
	}

	/**
	 * @see org.olat.user.propertyhandlers.Generic127CharTextPropertyHandler#isValid(org.olat.core.gui.components.form.flexible.FormItem,
	 *      java.util.Map)
	 */
	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		TextElement textElement = (TextElement) formItem;
		if (StringHelper.containsNonWhitespace(textElement.getValue())) {
			return textElement.getValue().length() <= XING_NAME_MAX_LENGTH;
		}
		return true;
	}
}
