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
package org.olat.modules.selectus.dispatcher;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;

import org.olat.modules.selectus.RecruitingModule;


/**
 * Adaptation of the LocaleNegociator to the list of languages for the positions
 * 
 * Initial date: 15.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionLocaleNegotiator {
	
	public static final String NEGOTIATED_LOCALE = "negotiated-locale";
	
	private PositionLocaleNegotiator() {
		//
	}
	
	public static Locale getPreferedLocale(UserRequest ureq) {
		//first try with the preferred language
		Locale locale = (Locale)ureq.getUserSession().getEntry(NEGOTIATED_LOCALE);
		if(locale != null) {
			//it's my preferred, I set it alone
			return locale;
		}

		RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
		Locale queryingLocale = getNegotiatedLocale(ureq.getHttpReq().getLocale(), recruitingModule);
		if (queryingLocale != null) {
			return queryingLocale;
		}
		
		//next try with the other accepted languages
		for(Enumeration<Locale> en=ureq.getHttpReq().getLocales(); en.hasMoreElements(); ) {
			Locale nextQueryingLocale = getNegotiatedLocale(en.nextElement(), recruitingModule);
			if (nextQueryingLocale != null) {
				return nextQueryingLocale;
			}
		}
		return recruitingModule.getPositionDefaultLocale();
	}
	
	public static Locale getNegotiatedLocale(String lang, RecruitingModule recruitingModule) {
		Locale locale = getLocaleForLanguage(lang);
		return getNegotiatedLocale(locale, recruitingModule);
	}
	
/**
 * Gets a Locale object for a given language string
 */
	private static Locale getLocaleForLanguage(String lang) {
		Locale loc;
		int semi;
		int dash;
		
		// Cut off any qvalue that might come after a semi-colon
		if ((semi = lang.indexOf(';')) != -1) {
			lang = lang.substring(0, semi);
		}

		// Trim any whitespace
		lang = lang.trim();
		
		// Create a Locale from the language. A dash may separate the
		// language from the country.
		if ((dash = lang.indexOf('-')) == -1) {
			loc = new Locale(lang, ""); // No dash, no country
		} else {
			loc = new Locale(lang.substring(0, dash), lang.substring(dash+1));
		}
		return loc;
	}
	
/**
 * Try first to find a locale which match language, country and variant, then
 * match language and country and the final ty it with language only
 */
	public static Locale getNegotiatedLocale(Locale loc, RecruitingModule recruitingModule) {
		Map<String,Locale> allLocales = new HashMap<>();
		Collection<String> enabledLanguageKeys = new HashSet<>();
		for(Locale positionLocale:recruitingModule.getPositionLocales()) {
			allLocales.put(positionLocale.toString(), positionLocale);
			enabledLanguageKeys.add(positionLocale.toString());
		}
		
		String lang = loc.getLanguage();
		//search a direct match first de_CH_bs...
		for(String enabledLanguageKey:enabledLanguageKeys) {
			if(lang.startsWith(enabledLanguageKey)) {
				Locale locale = allLocales.get(enabledLanguageKey);
				if(locale.equals(loc)) {
					return locale;
				}
			}
		}
		
		//search a match language + country
		for(String enabledLanguageKey:enabledLanguageKeys) {
			Locale locale = allLocales.get(enabledLanguageKey);
			if(locale.getLanguage().equals(loc.getLanguage())
					&& locale.getCountry().equals(loc.getCountry())) {
				return locale;
			}
		}
		
		//search a match language
		for(String enabledLanguageKey:enabledLanguageKeys) {
			Locale locale = allLocales.get(enabledLanguageKey);
			if(locale.getLanguage().equals(loc.getLanguage())) {
				return locale;
			}
		}
		return null;
	}
}
