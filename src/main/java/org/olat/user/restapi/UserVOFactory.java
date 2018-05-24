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
package org.olat.user.restapi;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageConfigManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.DatePropertyHandler;
import org.olat.user.propertyhandlers.GenderPropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Description:<br>
 * Factory for object needed by the REST Api
 * 
 * <P>
 * Initial Date:  7 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class UserVOFactory {
	
	private static final OLog log = Tracing.createLoggerFor(UserVOFactory.class);
	
	private static final String[] keys = new String[] { "male", "female", "-" };
	
	public static UserVO get(Identity identity) {
		return get(identity, I18nModule.getDefaultLocale(), false, false, false);
	}
	
	public static UserVO get(Identity identity, Locale locale) {
		return get(identity, locale, false, false, false);
	}
	
	public static UserVO get(Identity identity, boolean allProperties, boolean isAdmin) {
		return get(identity, I18nModule.getDefaultLocale(), allProperties, isAdmin, false);
	}
	
	public static UserVO get(Identity identity, Locale locale, boolean allProperties, boolean isAdmin, boolean withPortrait) {
		if(locale == null) {
			locale = I18nModule.getDefaultLocale();
		}
		
		UserVO userVO = new UserVO();
		User user = identity.getUser();
		userVO.setKey(identity.getKey());
		if(identity != null) {
			userVO.setLogin(identity.getName());
		}
		userVO.setExternalId(identity.getExternalId());
		userVO.setFirstName(user.getProperty(UserConstants.FIRSTNAME, null));
		userVO.setLastName(user.getProperty(UserConstants.LASTNAME, null));
		userVO.setEmail(user.getProperty(UserConstants.EMAIL, null));
		
		if(withPortrait) {
			File portrait = CoreSpringFactory.getImpl(DisplayPortraitManager.class).getSmallPortrait(identity.getName());
			if(portrait != null && portrait.exists()) {
				try {
					InputStream input = new FileInputStream(portrait);
					byte[] datas = IOUtils.toByteArray(input);
					FileUtils.closeSafely(input);
					byte[] data64 = Base64.encodeBase64(datas);
					userVO.setPortrait(new String(data64, "UTF8"));
				} catch (IOException e) {
					log.error("", e);
				}
			}
		}
		
		
		if(allProperties) {
			UserManager um = UserManager.getInstance();
			HomePageConfig hpc = isAdmin ? null : CoreSpringFactory.getImpl(HomePageConfigManager.class).loadConfigFor(identity.getName());
			List<UserPropertyHandler> propertyHandlers = um.getUserPropertyHandlersFor(UserWebService.PROPERTY_HANDLER_IDENTIFIER, false);
			for (UserPropertyHandler propertyHandler : propertyHandlers) {
				String propName = propertyHandler.getName();
				if(hpc != null && !hpc.isEnabled(propName)) {
					continue;
				}

				if(!UserConstants.FIRSTNAME.equals(propName)
						&& !UserConstants.LASTNAME.equals(propName)
						&& !UserConstants.EMAIL.equals(propName)) {
					
					String value = propertyHandler.getUserProperty(user, locale);
					userVO.putProperty(propName, value);
				}
			}
		}
		return userVO;
	}
	
	public static ManagedUserVO getManaged(Identity identity) {
		ManagedUserVO managedUserVo = new ManagedUserVO();
		managedUserVo.setKey(identity.getKey());
		managedUserVo.setLogin(identity.getName());
		managedUserVo.setExternalId(identity.getExternalId());
		return managedUserVo;
	}

	public static void post(User dbUser, UserVO user, Locale locale) {
		UserManager um = UserManager.getInstance();
		List<UserPropertyHandler> propertyHandlers = um.getUserPropertyHandlersFor(UserWebService.PROPERTY_HANDLER_IDENTIFIER, false);
		
		dbUser.setProperty(UserConstants.FIRSTNAME, user.getFirstName());
		dbUser.setProperty(UserConstants.LASTNAME, user.getLastName());
		dbUser.setProperty(UserConstants.EMAIL, user.getEmail());
		for(UserPropertyVO entry:user.getProperties()) {
			for(UserPropertyHandler propertyHandler:propertyHandlers) {
				if(entry.getName().equals(propertyHandler.getName())) {
					String value = parseUserProperty(entry.getValue(), propertyHandler, locale);
					String parsedValue;
					if(propertyHandler instanceof DatePropertyHandler) {
						parsedValue = formatDbDate(value, locale);
					} else if(propertyHandler instanceof GenderPropertyHandler) {
						parsedValue = parseGender(value, (GenderPropertyHandler)propertyHandler, locale);
					} else {
						parsedValue = propertyHandler.getStringValue(value, locale);
					}
					dbUser.setProperty(entry.getName(), parsedValue);
					break;
				}
			}
		}
	}
	
	public static String parseUserProperty(String value, UserPropertyHandler propertyHandler, Locale locale) {
		String parsedValue;
		if(propertyHandler instanceof DatePropertyHandler) {
			parsedValue = parseDate(value, locale);
		} else if(propertyHandler instanceof GenderPropertyHandler) {
			parsedValue = parseGender(value, (GenderPropertyHandler)propertyHandler, locale);
		} else {
			parsedValue = propertyHandler.getStringValue(value, locale);
		}
		return parsedValue;
	}
	
	public static String formatDbUserProperty(String value, UserPropertyHandler propertyHandler, Locale locale) {
		String formatedValue;
		if(propertyHandler instanceof DatePropertyHandler) {
			formatedValue = formatDbDate(value, locale);
		} else if(propertyHandler instanceof GenderPropertyHandler) {
			formatedValue = parseGender(value, (GenderPropertyHandler)propertyHandler, locale);
		} else {
			formatedValue = propertyHandler.getStringValue(value, locale);
		}
		return formatedValue;
	}
	
	/**
	 * Allow the date to be in the raw form (yyyyMMdd) or translated
	 * to be translated
	 * @param value
	 * @param handler
	 * @param locale
	 * @return
	 */
	public static final String parseDate(String value, Locale locale) {
		if(!StringHelper.containsNonWhitespace(value)) {
			return value;
		}
		
		boolean raw = true;
		for(int i=0; i<value.length(); i++) {
			if(!Character.isDigit(value.charAt(i))) {
				raw = false;
				break;
			}
		}
		
		if(raw) {
			try {
				DateFormat formater = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
				Date date = formater.parse(value);
				value = Formatter.getInstance(locale).formatDate(date);
			} catch (ParseException e) {
				/* silently failed */
			}
		}
		return value;
	}
	
	/**
	 * Allow the date to be in the localized form or not
	 * @param value
	 * @param handler
	 * @param locale
	 * @return
	 */
	public static final String formatDbDate(String value, Locale locale) {
		if(!StringHelper.containsNonWhitespace(value)) {
			return value;
		}
		
		boolean raw = true;
		for(int i=0; i<value.length(); i++) {
			if(!Character.isDigit(value.charAt(i))) {
				raw = false;
				break;
			}
		}
		
		if(raw) {
			return value;
		}
		try {
			DateFormat outFormater = new SimpleDateFormat("yyyyMMdd", Locale.GERMAN);
			DateFormat inFormater = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			Date date = inFormater.parse(value);
			value = outFormater.format(date);
		} catch (ParseException e) {
			/* silently failed */
		}
		return value;
	}
	
	/**
	 * Allow the value of gender to be in the raw form (male, female key word) or
	 * to be translated
	 * @param value
	 * @param handler
	 * @param locale
	 * @return
	 */
	public static final String parseGender(String value, GenderPropertyHandler handler, Locale locale) {
		if(!StringHelper.containsNonWhitespace(value)) {
			value = "-";
		}
		
		int index = Arrays.binarySearch(UserVOFactory.keys, value);
		if(index < 0) {
			//try to translate them
			boolean found = false;
			Translator trans = Util.createPackageTranslator(GenderPropertyHandler.class, locale);
			for(String key:keys) { 
				String translation = trans.translate(handler.i18nFormElementLabelKey() + "." + key);
				if(translation.equals(value)) {
					value = key;
					found = true;
					break;
				}
			}
			
			if(!found && !locale.equals(I18nModule.getDefaultLocale())) {
				//very last chance, try with the default locale
				trans = Util.createPackageTranslator(GenderPropertyHandler.class, I18nModule.getDefaultLocale());
				for(String key:keys) { 
					String translation = trans.translate(handler.i18nFormElementLabelKey() + "." + key);
					if(translation.equals(value)) {
						value = key;
						found = true;
						break;
					}
				}
			}
		}
		return value;
	}
}
