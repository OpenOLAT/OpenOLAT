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
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.User;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;
/**
 * <h3>Description:</h3>
 * This generic text property provides a userfield that has a maximum of 127
 * characters length. It can contain any string, the only validity check that is
 * performed is the not empty check if the property is declared as mandatory.
 * <p>
 * Create subclass of this class if you allow only a certain range of entered
 * data. See EmailProperty as an example.
 * <p>
 * Initial Date: 27.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class Generic127CharTextPropertyHandler extends AbstractUserPropertyHandler {
	
	private Pattern regExp;
	private String regExpMsgKey;

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		setInternalValue(user, internalValue);
	}

	@Override
	public String getStringValue(FormItem formItem) {
		return ((TextElement)formItem).getValue();
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		// default implementation is to use same as displayValue
		return displayValue;
	}

	@Override
	public FormItem addFormItem(Locale locale, final User user, String usageIdentifyer, boolean isAdministrativeUser,	FormItemContainer formItemContainer) {
		TextElement tElem = FormUIFactory.getInstance().addTextElement(getName(), i18nFormElementLabelKey(), 127, getInternalValue(user), formItemContainer);
		tElem.setItemValidatorProvider((value, validationError, llocale) -> 
			Generic127CharTextPropertyHandler.this.isValidValue(user, value, validationError, llocale)
		);
		tElem.setLabel(i18nFormElementLabelKey(), null);
		UserManager um = UserManager.getInstance();
		if (um.isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
			tElem.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			tElem.setMandatory(true);
		}
		return tElem;
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		TextElement textElemItem = (TextElement) formItem;
		if (textElemItem.isMandatory()) {
			if (textElemItem.isEmpty("new.form.mandatory")) {
				return false;
			}
		} else {
			if (textElemItem.getValue().equals("")) {
				return true;
			}
		}

		if (regExp != null) {
			return regExp.matcher(textElemItem.getValue()).matches();
		}
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (value != null && value.length() > 127) {
			validationError.setErrorKey("general.error.max.127");
			return false;
		}
		
		if (regExp != null && !regExp.matcher(value).matches()) {
			validationError.setErrorKey(regExpMsgKey);
			return false;
		}
		return true;
	}
	
	public void setRegExp(String rx) {
		this.regExp = Pattern.compile(rx);
	}
	
	public void setRegExpMsgKey(String key) {
		this.regExpMsgKey = key;
	}
}
