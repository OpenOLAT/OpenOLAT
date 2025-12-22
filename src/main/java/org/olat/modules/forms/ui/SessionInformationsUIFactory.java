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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.forms.model.xml.SessionInformations.InformationType;
import org.olat.modules.forms.model.xml.UserInfo;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 14.05.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
class SessionInformationsUIFactory {
	
	private SessionInformationsUIFactory() {
		// noninstantiable
	}
	
	static final SelectionValues getInformationsSV(Locale locale, List<InformationType> types) {
		SelectionValues informationSV = new SelectionValues();
		types.forEach(type -> informationSV.add(SelectionValues.entry(
				type.name(),
				getTranslatedType(type, locale))));
		return informationSV;
	}
	
	static final String getTranslatedType(InformationType informationType, Locale locale) {
		if (informationType.name().startsWith("USER_")) {
			return getTranslatedUserType(informationType, locale);
		}
		
		Translator translator = Util.createPackageTranslator(SessionInformationsController.class, locale);
		String i18nKey = "session.informations.type." + informationType.name().toLowerCase();
		String translation = translator.translate(i18nKey);
		if (i18nKey.equals(translation) || translation.length() > 256) {
			translation = i18nKey;
		}
		return translation;
	}
	
	private static final String getTranslatedUserType(InformationType informationType, Locale locale) {
		Translator translator = Util.createPackageTranslator(UserPropertyHandler.class, locale);
		String i18nKey = "form.name." + getUserProperty(informationType);
		String translation = translator.translate(i18nKey, null, Level.OFF);
		if(i18nKey.equals(translation) || translation.length() > 256) {
			translation = "";
		}
		return translation;
	}
	
	static final String getUserProperty(InformationType informationType) {
		switch (informationType) {
			case USER_EMAIL: return UserConstants.EMAIL;
			case USER_FIRSTNAME: return UserConstants.FIRSTNAME;
			case USER_LASTNAME: return UserConstants.LASTNAME;
			case USER_GENDER: return UserConstants.GENDER;
			case USER_ORGUNIT: return UserConstants.ORGUNIT;
			case USER_STUDYSUBJECT: return UserConstants.STUDYSUBJECT;
			default: return null;
		}
	}

	static final Collection<String> getSelectedTypeKeys(SessionInformations sessionInformations) {
		return sessionInformations.getInformationTypes().stream()
				.map(SessionInformations.InformationType::name)
				.collect(Collectors.toSet());
	}

	static boolean hasInformationType(Form form, InformationType informationType) {
		for (AbstractElement element: form.getElements()) {
			if (element instanceof SessionInformations) {
				SessionInformations sessionInforamtions = (SessionInformations) element;
				if (sessionInforamtions.getInformationTypes().contains(informationType)) {
					return true;
				}
			}
		}
		return false;
	}
	
	static String getValue(InformationType informationType, EvaluationFormSession session) {
		switch (informationType) {
		case USER_EMAIL:
			return session.getEmail();
		case USER_FIRSTNAME:
			return session.getFirstname();
		case USER_LASTNAME:
			return session.getLastname();
		case AGE:
			return session.getAge();
		case USER_GENDER:
			return session.getGender();
		case USER_ORGUNIT:
			return session.getOrgUnit();
		case USER_STUDYSUBJECT:
			return session.getStudySubject();
		default:
			return null;
		}
	}
	
	static String getValue(InformationType informationType, UserInfo userInfo) {
		if (userInfo == null) {
			return null;
		}
		
		switch (informationType) {
		case USER_FIRSTNAME:
			return userInfo.getFirstName();
		case USER_LASTNAME:
			return userInfo.getLastName();
		case USER_EMAIL:
			return userInfo.getEmail();
		default:
			return null;
		}
	}
}
