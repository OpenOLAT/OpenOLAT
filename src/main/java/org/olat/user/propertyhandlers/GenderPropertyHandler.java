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

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;

/**
 * <h3>Description:</h3>
 * The gender user property implemente a user attribute to specify the users gender
 * in a drop down. 
 * <p>
 * Initial Date: 23.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class GenderPropertyHandler extends AbstractUserPropertyHandler {
	
	private static final String[] keys = new String[] { "male", "female", "-" };
	
	/**
	 * Helper method to create translated values that correspond with the static keys
	 * @param locale
	 * @return
	 */
	
	private String[] getTranslatedValues(Locale locale) {
		Translator trans = Util.createPackageTranslator(this.getClass(), locale);
		return new String[] { 
				trans.translate(i18nFormElementLabelKey() + "." +keys[0]), 
				trans.translate(i18nFormElementLabelKey() + "." +keys[1]),
				trans.translate(i18nFormElementLabelKey() + "." +keys[2])
		};
	}

	@Override
	public void setUserProperty(User user, String value) {
		if(value != null) {
			if("m".equals(value)) {
				value = "male";
			} else if("f".equals(value)) {
				value = "female";
			}	
		}
		super.setUserProperty(user, value);
	}

	@Override
	public String getUserProperty(User user, Locale locale) {
		String internalValue = getInternalValue(user);
		if(internalValue != null && (internalValue.equals(keys[0]) || internalValue.equals(keys[1]) || internalValue.equals(keys[2]))) {
			Translator myTrans;
			if (locale == null) {
				myTrans = Util.createPackageTranslator(this.getClass(), I18nModule.getDefaultLocale());			
			} else {
				myTrans = Util.createPackageTranslator(this.getClass(), locale);
			}
			return myTrans.translate("form.name.gender." + internalValue);
		}
		return internalValue;
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		setInternalValue(user, internalValue);
	}

	@Override
	public String getStringValue(FormItem formItem) {
		return ((org.olat.core.gui.components.form.flexible.elements.SingleSelection) formItem).getSelectedKey();
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		// This should be refactored, but currently the bulk change does not work
		// otherwhise. When changing this here, the isValidValue must also to
		// changed to work with the real display value

		// use default: use key as value
		return displayValue;
	}

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,	FormItemContainer formItemContainer) {
		SingleSelection	genderElem = FormUIFactory.getInstance().addRadiosVertical(getName(), i18nFormElementLabelKey(), formItemContainer, keys, getTranslatedValues(locale));
		String key = user == null ? "-" : getInternalValue(user);
		for(int i=keys.length; i-->0; ) {
			if(keys[i].equals(key)) {
				genderElem.select(keys[i], true);
			}
		}
		
		UserManager um = UserManager.getInstance();
		if ( um.isUserViewReadOnly(usageIdentifyer, this) && ! isAdministrativeUser) {
			genderElem.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			genderElem.setMandatory(true);
		}
		return genderElem;
	}

	@Override
	public String getInternalValue(User user) {
		String value = super.getInternalValue(user);
		return (StringHelper.containsNonWhitespace(value) ? value : "-"); // default		
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		if (formItem.isMandatory()) {
			org.olat.core.gui.components.form.flexible.elements.SingleSelection sse = (org.olat.core.gui.components.form.flexible.elements.SingleSelection) formItem;
			// when mandatory, the - must not be selected
			if (sse.getSelectedKey().equals("-")) {
				sse.setErrorKey("gender.error", null);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (value != null) {
			for (int i = 0; i < keys.length; i++) {
				String key = keys[i];
				if (key.equals(value)) {
					return true;
				}
			}
			validationError.setErrorKey("gender.error");
			return false;
		}
		// null values are ok
		return true;
	}
}
