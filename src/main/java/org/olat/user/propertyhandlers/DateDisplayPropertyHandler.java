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

import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.olat.admin.user.imp.TransientIdentity;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserImpl;

/**
 * this class displays a static textElement. it always shows one of three dates:
 * user creation-date, user last-modified-date or user last-login-date
 * 
 * @author strentini, sergio.trentini@frentix.com
 * 
 */
public class DateDisplayPropertyHandler extends AbstractUserPropertyHandler {

	private OLog log = Tracing.createLoggerFor(this.getClass());

	private static final String DATE_TYPE_CR = "creationDateDisplayProperty";
	private static final String DATE_TYPE_LL = "lastloginDateDisplayProperty";

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser, FormItemContainer formItemContainer) {
		Date date = getDateValue(user);
		String dateString = StringHelper.formatLocaleDate(date.getTime(), locale);
		return FormUIFactory.getInstance().addStaticTextElement(getName(), i18nFormElementLabelKey(), dateString, formItemContainer);
	}

	/**
	 * returns the correct date according to the name of this
	 * DateDisplayPropertyHandler-name<br />
	 * 
	 * which can be one of three: the creation-date, the lastlogin-date or the
	 * last-modified-date
	 */
	private Date getDateValue(User user) {
		String myName = getName();

		if (DATE_TYPE_CR.equals(myName))
			return user.getCreationDate();
		if (DATE_TYPE_LL.equals(myName)) {
			if (user instanceof UserImpl) {
				Identity id = ((UserImpl)user).getIdentity();
				if (id != null) {
					return id.getLastLogin();
				}				
			} else if (user instanceof TransientIdentity) {
				// anticipated, some kind of preview screen
				return new Date(0);
			}
			// huh, we didn't find this identity
			log.warn("Couldn't find Identity for given User: " + user.getKey());
			return new Date(0);
		}

		// huh, something different.. return 1970
		return new Date(0);
	}

	@Override
	public String getUserProperty(User user, Locale locale) {
		Date date = getDateValue(user);
		if (date != null && locale != null)
			return StringHelper.formatLocaleDate(getDateValue(user).getTime(), locale);
		return StringHelper.formatLocaleDate(-1, locale);
	}

	/**
	 * @return The non-i18-ified raw value from the database ( in this case its
	 *         the unix tstamp of this Date-Field, time in milliseconds since
	 *         1970)
	 */
	@Override
	protected String getInternalValue(User user) {
		if (user != null) {
			return String.valueOf(getDateValue(user).getTime());
		}
		return "";
	}

	@Override
	protected void setInternalValue(User user, String value) {
		//read-only
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		// we do nothing here, its read-only
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		// always valid, no check
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		// always valid, no check
		return true;
	}

	@Override
	public String getStringValue(FormItem formItem) {
		if (formItem instanceof TextElement) {
			return ((TextElement) formItem).getValue();
		}
		return ""; // should not happen, passed formItem is expected to be a
					// textElement
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		return displayValue;
	}
	
	@Override
	protected void setInternalGetterSetter(String name) {
		//do nothing, artificial value
	}
}