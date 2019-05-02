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

/**
 * <h3>Description:</h3>
 * The phne property provides a user property that contains a valid phone number. 
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class PhonePropertyHandler extends Generic127CharTextPropertyHandler {
	
	// Regexp to define valid phone numbers
	private static final Pattern VALID_PHONE_PATTERN_IP = Pattern.compile( "[0-9/\\-+'\\(\\)\\. e(ext\\.*)(extension)]+" );
	
	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		TextElement textElement = (TextElement)super.addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
		textElement.setExampleKey("form.example.phone", null);
		return textElement;
	}

	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getUserPropertyAsHTML(org.olat.core.id.User, java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		String phonenr = getUserProperty(user, locale);
		if (StringHelper.containsNonWhitespace(phonenr)) {
			phonenr = StringHelper.escapeHtml(phonenr);
			StringBuilder sb = new StringBuilder(64);
			sb.append("<a href=\"tel:")
			  .append(normalizePhonenumber(phonenr)).append("\"><i class='o_icon o_icon_phone'> </i> ")
			  .append(phonenr).append("</a>");
			return sb.toString();
		}
		return null;
	}
	
	public static String normalizePhonenumber(String phonenr){
		phonenr=phonenr.split("[A-Za-z]")[0]; //just take the first sequence before a alphabetic character appears
		phonenr=phonenr.replaceAll("\\(.*\\)", ""); // remove brackets and their contents
		phonenr=phonenr.replaceAll("[\\s/'\\-\\.,]", ""); //remove bad chars
		return phonenr;
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		// check parent rules first: check if mandatory and empty
		if (!super.isValid(user, formItem, formContext)) {
			return false;
		} 
		
		TextElement textElement = (TextElement)formItem;
		String value = textElement.getValue();
		if (StringHelper.containsNonWhitespace(value)) {
			// check phone address syntax
			if (!VALID_PHONE_PATTERN_IP.matcher(value.toLowerCase()).matches()) {
				formItem.setErrorKey(i18nFormElementLabelKey() + ".error.valid", null);
				return false;
			}
		}
		// everthing ok
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if ( ! super.isValidValue(user, value, validationError, locale)) return false;
		
		if (StringHelper.containsNonWhitespace(value)) {			
			// check phone address syntax
			if ( ! VALID_PHONE_PATTERN_IP.matcher(value.toLowerCase()).matches()) {
				validationError.setErrorKey(i18nFormElementLabelKey()+ ".error.valid");
				return false;
			}
		}
		return true;
	}
	
}


