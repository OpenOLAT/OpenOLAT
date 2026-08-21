/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.propertyhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 26 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class TypeOfUserHandler extends Generic127CharTextPropertyHandler {

	private static final String NO_SEL_KEY = "gsph.doselect";
	
	private String typeOf;
	
	public String getTypeOf() {
		return typeOf;
	}

	public void setTypeOf(String typeOf) {
		this.typeOf = typeOf;
	}
	
	private List<String> getTypeOfList() {
		List<String> list = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(typeOf)) {
			String[] arr = typeOf.split(",");
			for(String str:arr) {
				if(StringHelper.containsNonWhitespace(str)) {
					list.add(str.trim());
				}
			}
		}
		return list;
	}

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		if(StringHelper.containsNonWhitespace(typeOf)) {
			return addSelectItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
		}
		return super.addFormItem(locale, user, usageIdentifyer, isAdministrativeUser, formItemContainer);
	}
	
	private FormItem addSelectItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		Translator trans = Util.createPackageTranslator(this.getClass(), locale);
		
		String i18nFormElementLabelKey = i18nFormElementLabelKey();
		SelectionValues keyValues = new SelectionValues();
		List<String> types = getTypeOfList();
		for(String type:types) {
			keyValues.add(SelectionValues.entry(type, trans.translate(i18nFormElementLabelKey + "." + type, null, Level.OFF)));
		}

		String internalValue = getInternalValue(user);
		if (StringHelper.containsNonWhitespace(internalValue) && !keyValues.containsKey(internalValue)) {
			String key = i18nFormElementLabelKey + "." + internalValue;
			String val = trans.translate(key, null, Level.OFF);
			// If the i18n key is in the translation, it is not translated, stay with the internal value
			if(val.contains(key)) {
				keyValues.add(SelectionValues.entry(internalValue, internalValue));
			} else {
				keyValues.add(SelectionValues.entry(internalValue, val));
			}
		}
		
		SingleSelection sse = FormUIFactory.getInstance().addRadiosVertical(getName(), i18nFormElementLabelKey, formItemContainer,
				keyValues.keys(), keyValues.values());
		if (internalValue != null && keyValues.containsKey(internalValue)) {
			sse.select(internalValue, true);
		}
		
		// enable/disable according to settings
		UserManager um = UserManager.getInstance();
		if (um.isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
			sse.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			sse.setMandatory(true);
			sse.setExampleKey("form.name.typeOfUser.mandatory.hint", null);
		} else {
			sse.setAllowNoSelection(true);
		}
		return sse;
	}
	
	@Override
	public String getUserProperty(User user, Locale locale) {
		if(StringHelper.containsNonWhitespace(typeOf)) {
			Translator myTrans;
			if (locale == null) {
				myTrans = Util.createPackageTranslator(this.getClass(), I18nModule.getDefaultLocale());			
			} else {
				myTrans = Util.createPackageTranslator(this.getClass(), locale);
			}
			String internalValue = getInternalValue(user);
			if(StringHelper.containsNonWhitespace(internalValue)) {
				String key = i18nFormElementLabelKey() + "." + internalValue;
				String val = myTrans.translate(key, null, Level.OFF);
				// If the i18n key is in the translation, it is not translated, stay with the internal value
				if(val.contains(key)) {
					val = internalValue;
				}
				return val;
			}
		} else {
			return super.getUserProperty(user, locale);
		}
		return null;
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String, String> formContext) {
		if (formItem.isMandatory()) {
			if(formItem instanceof SingleSelection ssel) {
				if (!ssel.isOneSelected()) {
					ssel.setErrorKey("form.legende.mandatory");
					return false;
				}
			} else if(formItem instanceof TextElement te) {
				return super.isValid(user, te, formContext);
			}
		}
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if(StringHelper.containsNonWhitespace(typeOf)) {
			return true;
		}
		return super.isValidValue(user, value, validationError, locale);
	}

	@Override
	public String getStringValue(FormItem formItem) {
		if (formItem instanceof SingleSelection sel) {
			if(sel.isOneSelected() && !NO_SEL_KEY.equals(sel.getSelectedKey())) {
				return sel.getSelectedKey();
			}
		} else if(formItem instanceof TextElement el) {
			return super.getStringValue(el);
		}
		return null;
	}
}
