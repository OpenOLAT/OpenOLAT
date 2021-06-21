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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.user.propertyhandlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import org.olat.core.gui.components.form.ValidationError;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.User;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.user.AbstractUserPropertyHandler;

import com.thoughtworks.xstream.XStream;

/**
 * <h3>Description:</h3>
 *
 * Property handler for user interests. These are stored in the DB in a string, e.g. 
 * <code>:1.2:1.5:3.4:</code> means that from category 1, subcategories 2 and 5 are chosen,
 * and subcategory 3.4.
 * 
 * Initial Date: Aug 13, 2009 <br>
 * 
 * @author twuersch, frentix GmbH, http://www.frentix.com
 */
public class UserInterestsPropertyHandler extends AbstractUserPropertyHandler {
	private static final Logger log = Tracing.createLoggerFor(UserInterestsPropertyHandler.class);

	public static final String PACKAGE_UINTERESTS = "com.frentix.olat.user";
	
	private static final XStream interestsXStream = XStreamHelper.createXStreamInstance();
	static {
		XStreamHelper.allowDefaultPackage(interestsXStream);
		interestsXStream.alias("category", UserInterestsCategory.class);
		interestsXStream.alias("categories", List.class);
		interestsXStream.addImplicitCollection(UserInterestsCategory.class, "subcategories");
		interestsXStream.useAttributeFor(UserInterestsCategory.class, "id");
	}
	
	public static final String USERINTERESTS_CONFIGURATION_FILE = "system/configuration/userinterests.xml";
	public static final String CATEGORY_I18N_PREFIX = "userinterests.category.";
	public static final String SUBCATEGORY_I18N_PREFIX = "userinterests.subcategory.";

	@Override
	public FormItem addFormItem(Locale locale, User user, String usageIdentifyer, boolean isAdministrativeUser,
			FormItemContainer formItemContainer) {
		String name = getName();
		UserInterestsElement userInterestsElement = new UserInterestsElement(name, getInternalValue(user), locale);
		formItemContainer.add(userInterestsElement);
		userInterestsElement.setLabel("form.name." + name, null);
		return userInterestsElement;
	}

	@Override
	public String getStringValue(FormItem formItem) {
		// Nothing to do here.
		return ((UserInterestsElement)formItem).getSelectedInterestsIDs();
	}

	@Override
	public String getStringValue(String displayValue, Locale locale) {
		return null;
	}

	@Override
	public boolean isValid(User user, FormItem formItem, Map<String,String> formContext) {
		return true;
	}

	@Override
	public boolean isValidValue(User user, String value, ValidationError validationError, Locale locale) {
		return true;
	}

	@Override
	public void updateUserFromFormItem(User user, FormItem formItem) {
		String internalValue = getStringValue(formItem);
		// Measure the _byte_ length of the string to be sure that it does not exceed the 255 bytes database field size limit.
		if (internalValue != null) {
			if (internalValue.getBytes().length > 255) {
				throw new IllegalArgumentException("User property values must not be longer than 255 bytes.");
			}
			setInternalValue(user, internalValue);
		}
	}
	
	/**
	 * Returns the available user interests, as configured in the configuration file.
	 * 
	 * @return The available user interests, as configured in the configuration file.
	 */
	public static List<UserInterestsCategory> loadAvailableUserInterests() {
		File userInterestsConfigurationFile = new File(WebappHelper.getUserDataRoot(), USERINTERESTS_CONFIGURATION_FILE);
		List<UserInterestsCategory> availableUserInterests = new ArrayList<>();
		if (userInterestsConfigurationFile.exists()) {
			
			try(InputStream in = new FileInputStream(userInterestsConfigurationFile);
					BufferedInputStream bis = new BufferedInputStream(in)) {
				 @SuppressWarnings("unchecked")
				List<UserInterestsCategory> available = (List<UserInterestsCategory>)interestsXStream.fromXML(bis);
				 if(available != null) {
					 availableUserInterests.addAll(available); 
				 }
			} catch (Exception e) {
				log.warn("Unable to load user interests from configuration file " + userInterestsConfigurationFile.getAbsolutePath(), e);
			}
		}
		return availableUserInterests;
	}
	
	/**
	 * @see org.olat.user.AbstractUserPropertyHandler#getUserPropertyAsHTML(org.olat.core.id.User, java.util.Locale)
	 */
	@Override
	public String getUserPropertyAsHTML(User user, Locale locale) {
		if (locale == null) {
			locale = I18nManager.getInstance().getCurrentThreadLocale();
		}
		Translator packageTranslator = Util.createPackageTranslator(PACKAGE_UINTERESTS, locale, null);
		StringBuilder sb = new StringBuilder(256);
		if (getInternalValue(user) != null) {
			String[] userInterestsIDs = getInternalValue(user).split(":");
			List<String> sortedUserInterestsIDs = new Vector<>();
			for (String id : userInterestsIDs) {
				if (!id.equals("")) {
					sortedUserInterestsIDs.add(id);
				}
			}
			Collections.sort(sortedUserInterestsIDs, new UserInterestsComparator());
			sb.append("<table class=\"b_table\"><tbody>");
			for (String id : sortedUserInterestsIDs) {
				sb.append("<tr><td>");
				sb.append(packageTranslator.translate(UserInterestsPropertyHandler.SUBCATEGORY_I18N_PREFIX + id));
				sb.append("</td></tr>");
			}
			sb.append("</tbody></table>");
		}
		
		return sb.toString();
	}
	
	@Override
	public String getUserProperty(User user, Locale locale) {
		if (locale == null) {
			locale = I18nManager.getInstance().getCurrentThreadLocale();
		}
		Translator packageTranslator = Util.createPackageTranslator(PACKAGE_UINTERESTS, locale, null);
		StringBuilder sb = new StringBuilder(256);
		if (getInternalValue(user) != null) {
			String[] userInterestsIDs = getInternalValue(user).split(":");
			List<String> sortedUserInterestsIDs = new Vector<>();
			for (String id : userInterestsIDs) {
				if (!id.equals("")) {
					sortedUserInterestsIDs.add(id);
				}
			}
			Collections.sort(sortedUserInterestsIDs, new UserInterestsComparator());
			for (String id : sortedUserInterestsIDs) {
				String[] levels = id.split("\\.");
				sb.append(packageTranslator.translate(UserInterestsPropertyHandler.CATEGORY_I18N_PREFIX + levels[0]))
				  .append(" - ")
				  .append(packageTranslator.translate(UserInterestsPropertyHandler.SUBCATEGORY_I18N_PREFIX + id))
				  .append(" ");
			}
		}
		return sb.toString().trim();
	}
}
