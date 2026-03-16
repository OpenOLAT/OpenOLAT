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

import org.olat.core.CoreSpringFactory;
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

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.PersonTitle;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 29 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TitlePropertyHandler extends AbstractUserPropertyHandler {

	@Override
	public String getUserProperty(User user, Locale locale) {
		if (locale == null) {
			locale = I18nModule.getDefaultLocale();			
		}
		Translator trans = Util.createPackageTranslator(PositionController.class, locale);
		String internalValue = getInternalValue(user);
		return trans.translate(internalValue);
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
		return displayValue;
	}

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser, FormItemContainer formItemContainer) {
		Translator trans = Util.createPackageTranslator(PositionController.class, locale);	
		PersonTitle[] personTitles = CoreSpringFactory.getImpl(RecruitingModule.class).getUserPersonTitles();
		String[] titleKeys = new String[personTitles.length + 1];
		String[] titleValues = new String[personTitles.length + 1];
		titleKeys[0] = "";
		titleValues[0] = "-";
		for(int i=personTitles.length; i-->0; ) {
			titleKeys[i+1] = personTitles[i].title();
			titleValues[i+1] = trans.translate(personTitles[i].i18nKey());
		}

		SingleSelection	titleEl = FormUIFactory.getInstance()
				.addDropdownSingleselect(getName(), i18nFormElementLabelKey(), formItemContainer, titleKeys, titleValues, null);
		String key = user == null ? "-" : getInternalValue(user);
		for(int i=titleKeys.length; i-->0; ) {
			if(titleKeys[i].equals(key)) {
				titleEl.select(titleKeys[i], true);
			}
		}
		
		UserManager um = UserManager.getInstance();
		if ( um.isUserViewReadOnly(usageIdentifyer, this) && ! isAdministrativeUser) {
			titleEl.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			titleEl.setMandatory(true);
		}
		return titleEl;
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
				sse.setErrorKey("gender.error");
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (value != null) {
			if("".equals(value)) {
				return true;
			}
			PersonTitle[] personTitles = CoreSpringFactory.getImpl(RecruitingModule.class).getUserPersonTitles();
			for (int i = 0; i < personTitles.length; i++) {
				String key = personTitles[i].title();
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
