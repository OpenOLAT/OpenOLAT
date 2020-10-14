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
import java.util.regex.Pattern;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 14 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LinkedinPropertyHandler extends Generic127CharTextPropertyHandler {

	private static final Pattern plinkedIn = Pattern.compile("^https:\\/\\/[a-z]{2,3}\\.linkedin\\.com\\/.*$");

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (!super.isValidValue(user, value, validationError, locale)) {
			return false;
		}
		if(StringHelper.containsNonWhitespace(value) && !validUrl(value)) {
			validationError.setErrorKey("form.name.linkedin.error");
			return false;
		}
		return true;
	}

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		TextElement textElement = (TextElement) super.addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
		textElement.setMaxLength(255);

		if (!UserManager.getInstance().isUserViewReadOnly(usageIdentifyer, this) || isAdministrativeUser) {
			textElement.setExampleKey("form.example.linkedinname", null);
		}
		return textElement;
	}

	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String url = getUserProperty(user, locale);
		if (StringHelper.containsNonWhitespace(url)) {
			url = StringHelper.escapeHtml(url);
			StringBuilder sb = new StringBuilder();
			sb.append("<a href='").append(url).append("' target='_blank'><i class='o_icon o_icon-fw o_icon_linkedin'> </i>")
			  .append(url)
			  .append("</a>");
			return sb.toString();
		}
		return null;
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		TextElement textElement = (TextElement)formItem;
		if (StringHelper.containsNonWhitespace(textElement.getValue()) && !validUrl(textElement.getValue())) {
			textElement.setErrorKey("form.name.linkedin.error", null);
			return false;
		}
		return true;
	}
	
	protected static boolean validUrl(String url) {
		return plinkedIn.matcher(url).find() && url.length() <= 255;
	}
}
