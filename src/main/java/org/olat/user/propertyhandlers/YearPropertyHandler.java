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

import java.util.Calendar;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.id.User;
import org.olat.core.logging.Tracing;
import org.olat.user.AbstractUserPropertyHandler;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.ui.UsrPropHandlerCfgFactory;


/**
 * <h3>Description:</h3> The YearPropertyHandler offers the functionality of a
 * Year date. It can be used to store something like a graduation date. The
 * property is rendered as a dropdown menu. (from- , to- year values can be
 * configured through <code>YearPropertyHandlerController</code>)
 * <p>
 * 
 * Initial Date: 15.12.2011 <br>
 * 
 * @author strentini, sergio.trentini@frentix.com, www.frentix.com
 */
public class YearPropertyHandler extends AbstractUserPropertyHandler {

	private static final Logger logger = Tracing.createLoggerFor(YearPropertyHandler.class);

	public static final String PROP_FROM = "yph.from";
	public static final String PROP_TO = "yph.to";

	private static final String NO_SEL_KEY = "select";
	private String[] selectionKeys = getDefaultYears();
	private UsrPropHandlerCfgFactory cfgFactory;

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser, FormItemContainer formItemContainer) {

		/* let's load the years */
		loadSelectionKeysFromConfig();

		// add the no-selection entry to the dropdown
		String[] allKeys = new String[selectionKeys.length + 1];
		System.arraycopy(selectionKeys, 0, allKeys, 1, selectionKeys.length);
		allKeys[0] = NO_SEL_KEY;

		SingleSelection sse = FormUIFactory.getInstance().addDropdownSingleselect(getName(), i18nFormElementLabelKey(), formItemContainer, allKeys,
				allKeys, null);
		String internalValue = getInternalValue(user);
		if (isValidValue(user, internalValue, null, null) && internalValue != null)
			sse.select(internalValue, true);

		// enable/disable according to settings
		UserManager um = UserManager.getInstance();
		if (um.isUserViewReadOnly(usageIdentifyer, this) && !isAdministrativeUser) {
			sse.setEnabled(false);
		}
		if (um.isMandatoryUserProperty(usageIdentifyer, this)) {
			sse.setMandatory(true);
		}

		return sse;
	}

	private void loadSelectionKeysFromConfig() {
		Map<String, String> handlerConfig = cfgFactory.loadConfigForHandler(this);

		// now "calculate" available year-values for dropdown, according to
		// handler config
		if (handlerConfig.containsKey(PROP_FROM) && handlerConfig.containsKey(PROP_TO)) {
			// we have a valid config
			int nowYear = Calendar.getInstance().get(Calendar.YEAR);

			String from = handlerConfig.get(PROP_FROM);
			String to = handlerConfig.get(PROP_TO);
			int i_from = 1900;
			int i_to = 1900;

			if (from.startsWith("+"))
				i_from = nowYear + Integer.parseInt(from.substring(1));
			else if (from.startsWith("-"))
				i_from = nowYear - Integer.parseInt(from.substring(1));
			else
				i_from = Integer.parseInt(from);

			if (to.startsWith("+"))
				i_to = nowYear + Integer.parseInt(to.substring(1));
			else if (to.startsWith("-"))
				i_to = nowYear - Integer.parseInt(to.substring(1));
			else
				i_to = Integer.parseInt(to);

			if (i_to < i_from) {
				logger.warn("wrong config in YearPropertyHandler : to is smaller than from...");
				//leave selectionKeys to default
				selectionKeys = getDefaultYears();
			}else{
				// now fill the array
				int span = i_to - i_from;
				if(span > 1000)span = 1000;//just prevent toooooo long dropdown-list ^
				selectionKeys = new String[span+1];
				for (int j = 0; j <= span; j++)
					selectionKeys[j] = String.valueOf(i_from + j);
			}
		}
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		setInternalValue(user, internalValue);
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		if (formItem.isMandatory()) {
			SingleSelection ssel = (SingleSelection) formItem;
			if (ssel.getSelectedKey().equals(NO_SEL_KEY)) {
				ssel.setErrorKey("form.legende.mandatory", null);
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		if (value != null) {
			for (int i = 0; i < selectionKeys.length; i++) {
				String key = selectionKeys[i];
				if (key.equals(value)) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	@Override
	public String getStringValue(FormItem formItem) {
		return ((SingleSelection) formItem).getSelectedKey();
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		return displayValue;
	}

	/**
	 * 
	 * @return
	 */
	public UsrPropHandlerCfgFactory getHandlerConfigFactory() {
		return cfgFactory;
	}

	/**
	 * [spring] setter
	 * 
	 * @param factory
	 */
	public void setHandlerConfigFactory(UsrPropHandlerCfgFactory factory) {
		cfgFactory = factory;
	}

	/**
	 * returns an array of 10 year-strings (5 years back from current year, to 5
	 * years into the future)<br />
	 * e.g., if called in 2000, this method returns an array :
	 * [1995,1996,1997,.....,2004,2005]
	 * 
	 * @return
	 */
	private static String[] getDefaultYears() {
		int nowYear = Calendar.getInstance().get(Calendar.YEAR);
		int from = nowYear - 5;
		int to = nowYear + 5;
		int span = to - from;
		String[] years = new String[span];

		for (int j = 0; j < span; j++)
			years[j] = String.valueOf(from + j);

		return years;
	}


}
