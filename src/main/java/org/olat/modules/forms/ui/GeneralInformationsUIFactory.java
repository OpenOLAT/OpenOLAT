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
package org.olat.modules.forms.ui;

import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.modules.forms.model.xml.GeneralInformation;
import org.olat.modules.forms.model.xml.GeneralInformations;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneralInformationsUIFactory {
	
	private GeneralInformationsUIFactory() {
		// noninstantiable
	}
	
	static final String[] getTypeKeys() {
		GeneralInformation.Type[] enumVals = GeneralInformation.Type.values();
		Arrays.sort(enumVals, (t1, t2) -> Integer.compare(t1.getOrder(), t2.getOrder()));
		String[] vals = new String[enumVals.length];
		for(int i=enumVals.length; i-->0; ) {
			vals[i] = enumVals[i].name();
		}
		return vals;
	}
	
	static final String[] getTranslatedTypes(Locale locale) {
		GeneralInformation.Type[] enumVals = GeneralInformation.Type.values();
		Arrays.sort(enumVals, (t1, t2) -> Integer.compare(t1.getOrder(), t2.getOrder()));
		String[] names = new String[enumVals.length];
		for(int i=enumVals.length; i-->0; ) {
			names[i] = getTranslatedType(enumVals[i], locale);
		}
		return names;
	}
	
	public static final String getTranslatedType(GeneralInformation.Type type, Locale locale) {
		if (type.name().startsWith("USER_")) {
			return getTranslatedUserType(type, locale);
		}
		return "";
	}
	
	private static final String getTranslatedUserType(GeneralInformation.Type type, Locale locale) {
		Translator translator = Util.createPackageTranslator(UserPropertyHandler.class, locale);
		String i18nKey = "form.name." + getUserProperty(type);
		String translation = translator.translate(i18nKey, null, Level.OFF);
		if(i18nKey.equals(translation) || translation.length() > 256) {
			translation = "";
		}
		return translation;
	}
	
	static final String getUserProperty(GeneralInformation.Type type) {
		switch (type) {
			case USER_EMAIL: return UserConstants.EMAIL;
			case USER_FIRSTNAME: return UserConstants.FIRSTNAME;
			case USER_LASTNAME: return UserConstants.LASTNAME;
			case USER_GENDER: return UserConstants.GENDER;
			case USER_BIRTHDAY: return UserConstants.BIRTHDAY;
			case USER_ORGUNIT: return UserConstants.ORGUNIT;
			case USER_STUDYSUBJECT: return UserConstants.STUDYSUBJECT;
			default: return null;
		}
	}

	public static final Collection<String> getSelectedTypeKeys(GeneralInformations generalInformations) {
		return generalInformations.asCollection().stream()
				.map(GeneralInformation::getType)
				.map(GeneralInformation.Type::name)
				.collect(Collectors.toSet());
	}
}
