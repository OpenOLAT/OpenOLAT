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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.elements.ItemValidatorProvider;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.i18n.I18nModule;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;

/**
 * <h3>Description:</h3>
 * The DatePropertyHandler offers the functionality of a date. It can be used to store
 * something like a birth date.
 * <p>
 * Initial Date: 26.07.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class DatePropertyHandler extends AbstractUserPropertyHandler {
	private static final Logger log = Tracing.createLoggerFor(DatePropertyHandler.class);
	
	/**
	 * Format internal values as yyyyMMdd string e.g. "19751210".
	 * So it is possible to use the less, greater and equal operators.  
	 */
	private DateFormat INTERNAL_DATE_FORMATTER = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);

	@Override
	public String getUserProperty(User user, Locale locale) {
		Date date = decode(getInternalValue(user));
		if (date == null) return null;
		if (locale == null) locale = I18nModule.getDefaultLocale();
		return Formatter.getInstance(locale).formatDate(date);
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		setInternalValue(user, internalValue);
	}

	@Override
	public String getStringValue(FormItem formItem) {
		Date date = ((org.olat.core.gui.components.form.flexible.elements.DateChooser) formItem).getDate();
		return encode(date);
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		if (StringHelper.containsNonWhitespace(displayValue)) {
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			df.setLenient(false);
			try {
				Date date = df.parse(displayValue.trim());
				return encode(date);
			} catch (ParseException e) {
				// catch but do nothing, return null in the end
				if(displayValue.length() == 8 && decode(displayValue) != null) {
					return displayValue;
				}
			} 
		}
		return null;
	}

	@Override
	public FormItem addFormItem(Locale locale, final User user, String usageIdentifyer, boolean isAdministrativeUser,	FormItemContainer formItemContainer) {
		org.olat.core.gui.components.form.flexible.elements.DateChooser dateElem = null;
		Date val = decode(getInternalValue(user));
		dateElem = FormUIFactory.getInstance().addDateChooser(getName(), i18nFormElementLabelKey(), val, formItemContainer);
		dateElem.setItemValidatorProvider(new ItemValidatorProvider() {
			@Override
			public boolean isValidValue(String value,ValidationError validationError, Locale llocale) {
				return DatePropertyHandler.this.isValidValue(user, value, validationError, llocale);
			}
		});
		UserManager um = UserManager.getInstance();
		if ( um.isUserViewReadOnly(usageIdentifyer, this) && ! isAdministrativeUser) {
			dateElem.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			dateElem.setMandatory(true);
		}
		
		dateElem.setExampleKey("form.example.free", new String[] {Formatter.getInstance(locale).formatDate(new Date())});
		return dateElem;
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		DateChooser dateElem = (DateChooser) formItem;
		if (dateElem.isEmpty()) {
			return !dateElem.isMandatory() || !dateElem.isEmpty("new.form.mandatory");
		}
		return dateElem.validate();
	}

	/**
	 * Helper to encode the date as a String
	 * @param date
	 * @return
	 */
	public String encode(Date date) {
		if (date == null) return null;
		return INTERNAL_DATE_FORMATTER.format(date);
	}

	/**
	 * Helper to decode a String value to a date
	 * @param value
	 * @return
	 */
	private Date decode(String value) {
		if (!StringHelper.containsNonWhitespace(value)) return null;
		try {
			return INTERNAL_DATE_FORMATTER.parse(value.trim());
		} catch (ParseException | NumberFormatException e) {
			log.warn("Could not parse BirthDayField from database", e);
			return null;
		}
	}
	
	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (StringHelper.containsNonWhitespace(value)) {
			DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			df.setLenient(false);
			try {
				df.parse(value.trim());
			} catch (ParseException | NumberFormatException e) {
				validationError.setErrorKey(i18nFormElementLabelKey()+ ".error");
				return false;
			}
			return true;
		}
		//  null values are ok
		return true;
	}
}