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
 * frentix GmbH, Switzerland, http://www.frentix.com
 * <p>
 */
package org.olat.user.propertyhandlers;

import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.util.Util;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;

/**
 * 
 * A User Property Handler for a Yes/No Property.<br />
 * (originated from http://jira.openolat.org/browse/OO-98)
 * 
 * This renders two radio-buttons with "yes" "no" options. (which brings a clear
 * distinction from the homeprofile (vcard) checkbox in the homeprofileform)
 * 
 * @author strentini, sergio.trentini@frentix.com, http://www.frentix.com
 * @date 30.01.2012
 * 
 */
public class GenericYesNoPropertyHandler extends AbstractUserPropertyHandler {

	private static final String KEY_YES = "gchpr.y";
	private static final String KEY_NO = "gchpr.n";

	private static final String VAL_YES = "true";
	private static final String VAL_NO = "false";

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser, FormItemContainer formItemContainer) {
		SelectionElement sElem = null;
		Translator trans = Util.createPackageTranslator(this.getClass(), locale);
		sElem = FormUIFactory.getInstance().addRadiosHorizontal(getName(), i18nFormElementLabelKey(), formItemContainer,
				new String[] { KEY_YES, KEY_NO }, new String[] { trans.translate("yes"), trans.translate("no") });

		// pre-select yes/no
		String internalValue = getInternalValue(user);
		if (isValidValue(user, internalValue, null, null)) {
			if (VAL_YES.equals(internalValue))
				sElem.select(KEY_YES, true);
			if (VAL_NO.equals(internalValue))
				sElem.select(KEY_NO, true);
		}

		UserManager um = UserManager.getInstance();
		if (um.isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
			sElem.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			sElem.setMandatory(true);
		}
		return sElem;
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		setInternalValue(user, internalValue);
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		// the formItem is always valid. if no radio-button is selected, the
		// value is just "NO"
		// this is also ok if item is mandatory...
		return true;
	}

	@Override
	public String getUserProperty(User user, Locale locale) {
		Translator trans = Util.createPackageTranslator(this.getClass(), locale);
		String internalValue = getInternalValue(user);
		if (VAL_YES.equals(internalValue))
			return trans.translate("yes");
		if (VAL_NO.equals(internalValue))
			return trans.translate("no");
		return "-";
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		// a value is valid if it is either val_no or val_yes
		return (VAL_NO.equals(value) || VAL_YES.equals(value));
	}

	@Override
	public String getStringValue(FormItem formItem) {
		if (formItem instanceof SingleSelection) {
			SingleSelection sItem = (SingleSelection) formItem;
			if (sItem.isSelected(0))
				return VAL_YES;
		}
		return VAL_NO;
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		return displayValue;
	}

}
