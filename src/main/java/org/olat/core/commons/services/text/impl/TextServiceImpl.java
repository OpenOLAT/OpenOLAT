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
package org.olat.core.commons.services.text.impl;

import java.text.BreakIterator;
import java.util.Locale;

import org.olat.core.commons.services.text.TextService;
import org.olat.core.commons.services.text.impl.nutch.LanguageIdentifier;
import org.olat.core.util.i18n.I18nModule;
import org.springframework.stereotype.Service;

/**
 * Description:<br>
 * The language guess is based on a NGRam algorithm. The implementation
 * is based on a plugin of nutch (an apache project). To create a profile
 * for a new language, do collect a sample text of the language. It need
 * a lot of text (the standard are done with more than a millions of words).
 * Then create the profile:<br>
 * java org.olat.core.commons.services.text.impl.nutch.NGramPofile -create profile-name filename encoding<br>
 * OLAT work with UTF-8 so create the profile with UTF-8. The right encoding is very important.<br>
 * Then add the profile-name.ngp file with the other in _resources. The profile-name is the name of the language.
 * 
 * <P>
 * Initial Date:  25 nov. 2009 <br>
 * @author srosse
 */
@Service
public class TextServiceImpl implements TextService {
	
	private static final float CHINESE_RATIO_WORD_CHARACTER = 2.2f;
	private LanguageIdentifier identifier = new LanguageIdentifier();
	
	/**
	 * [spring only]
	 */
	public TextServiceImpl() {
		//
	}

	/**
	 * Return the locale found by a NGram analyse. The different profile are saved in _resources.
	 * The best result are with longer text, other more then 100 characters.
	 * @see org.olat.core.util.lang.LanguageService#detectLocale(java.lang.String)
	 */
	@Override
	public Locale detectLocale(String text) {
		String language = identifier.identify(text);
		for(Locale locale:Locale.getAvailableLocales()) {
			if(language.equals(locale.getLanguage())) {
				return locale;
			}
		}
		return null;
	}
	
	@Override
	public int characterCount(String text, Locale locale) {
		return countCharacters(text, locale);
	}
	
	/**
	 * Use the java.text.BreakIterator to count  the number of words. There is an excpetion for chinese
	 * language because only a human count reliably count the number of words in a chinese text (Word are
	 * not separated by space in Chinese, Japanese and Thai). The different entreprise  involved in traduction 
	 * count the words with the number of characters and a factor 2.2.
	 * @param text
	 * @param locale
	 * @return
	 */
	@Override
	public int wordCount(String text, Locale locale) {
		if(locale == null) {
			locale = I18nModule.getDefaultLocale();
		}
		if(Locale.CHINESE.getLanguage().equals(locale.getLanguage())) {
			return countChineseWords(text, locale);
		}
		return countWords(text, locale);
	}
	
	private int countChineseWords(String text, Locale locale) {
		int characters = countCharacters(text, locale);
		return Math.round(characters / CHINESE_RATIO_WORD_CHARACTER);
	}
	
	private int countWords(String text, Locale locale) {
		int count = 0;
		BreakIterator wordIterator = BreakIterator.getWordInstance(locale);
		
		wordIterator.setText(text);
		int start = wordIterator.first();
		int end = wordIterator.next();
		while (end != BreakIterator.DONE) {
			char ch = text.charAt(start);
			if (Character.isLetterOrDigit(ch)) {
				count++;
			}
			start = end;
			end = wordIterator.next();
		}
		
		return count;
	}
	
	private int countCharacters(String text, Locale locale) {
		if(locale == null) {
			locale = I18nModule.getDefaultLocale();
		}
		
		int count = 0;
		BreakIterator characterIterator = BreakIterator.getCharacterInstance(locale);
		
		characterIterator.setText(text);
		int start = characterIterator.first();
		int end = characterIterator.next();
		while (end != BreakIterator.DONE) {
			char ch = text.charAt(start);
			if (Character.isLetterOrDigit(ch)) {
				count++;
			}
			start = end;
			end = characterIterator.next();
		}
		
		return count;
	}
}
