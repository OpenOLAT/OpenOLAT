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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
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
import org.olat.core.id.User;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;

/**
 * 
 * Description: Checkbox property handler.<br>
 * 
 * <P>
 * Initial Date:  06.08.2008 <br>
 * @author bja
 */
public class GenericCheckboxPropertyHandler extends AbstractUserPropertyHandler {

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		SelectionElement sElem = FormUIFactory.getInstance()
			.addCheckboxesHorizontal(getName(), i18nFormElementLabelKey(), formItemContainer, new String[] { getName() }, new String[]{ "" });
		
		UserManager um = UserManager.getInstance();
		if ( um.isUserViewReadOnly(usageIdentifyer, this) && ! isAdministrativeUser) {
			sElem.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			sElem.setMandatory(true);
		}
		return sElem;
	}

	@Override
	public String getStringValue(FormItem formItem) {
		String value = "";
		if(((org.olat.core.gui.components.form.flexible.elements.SelectionElement) formItem).isSelected(0))
			value = "true";
		else
			value = "false";
		
		return value;
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		return displayValue;
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String, String> formContext) {
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		return true;
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		setInternalValue(user, internalValue);
	}
}
