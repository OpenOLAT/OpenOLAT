/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.propertyhandlers;

import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;

/**
 * Initial date: Jun 26, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class Generic2048RichTextPropertyHandler extends AbstractUserPropertyHandler {


	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifier, boolean isAdministrativeUser, FormItemContainer formItemContainer) {
		RichTextElement richTextElement =
				FormUIFactory.getInstance().addRichTextElementForStringDataMinimalistic(getName(), i18nFormElementLabelKey(),
						getInternalValue(user), 4, -1, formItemContainer);
		richTextElement.setMaxLength(2048);
		richTextElement.setItemValidatorProvider((value, validationError, llocale) ->
				Generic2048RichTextPropertyHandler.this.isValidValue(user, value, validationError, llocale)
		);
		richTextElement.setLabel(i18nFormElementLabelKey(), null);
		UserManager um = UserManager.getInstance();
		if (um.isUserViewReadOnly(usageIdentifier, this) && !isAdministrativeUser) {
			richTextElement.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifier, this)) {
			richTextElement.setMandatory(true);
		}
		return richTextElement;
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);

		// if value contains only whitespace then set to null
		if (!StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(internalValue))) {
			internalValue = null;
		}
		setInternalValue(user, internalValue);
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String, String> formContext) {
		RichTextElement richTextElement = (RichTextElement) formItem;
		if (richTextElement.isMandatory()) {
			return !richTextElement.isEmpty("new.form.mandatory");
		} else if (richTextElement.getValue().equals("")) {
			return true;
		}

		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (value != null && value.length() > 2048) {
			validationError.setErrorKey("general.error.max.2048");
			return false;
		}

		return true;
	}

	@Override
	public String getStringValue(FormItem formItem) {
		return ((TextElement) formItem).getValue();
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		// default implementation is to use same as displayValue
		return displayValue;
	}
}
