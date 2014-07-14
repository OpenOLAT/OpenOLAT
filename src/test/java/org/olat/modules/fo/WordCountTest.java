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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.modules.fo;

import static org.junit.Assert.assertTrue;

import java.util.Locale;

import org.junit.Test;
import org.olat.core.commons.services.text.TextService;
import org.olat.core.commons.services.text.impl.TextServiceImpl;
import org.olat.core.util.filter.Filter;

public class WordCountTest {
	

	private static TextService languageService = new TextServiceImpl();
	
	
	@Test
	public void testCleanMessage() {
		Filter filter = new QuoteAndTagFilter();
		String text = "<p>&nbsp;</p><div class=\"o_quote_wrapper\"><div class=\"o_quote_author mceNonEditable\">Am 23.11.09 12:29 hat OLAT Administrator geschrieben:</div><blockquote class=\"o_quote\"><p>Quelques mots que je voulais &eacute;crire. Et encore un ou deux.</p></blockquote></div><p>Et une r&eacute;ponse avec citation incorpor&eacute;e</p>";
		String output = filter.filter(text);
		assertTrue("  Et une réponse avec citation incorporée".equals(output));
	}
	
	/**
	 * Test pass if the detection is better as 80%
	 */
	@Test
	public void testDetectLanguage() {
		double count = 0;
		for(TestTextCase.Text text:TestTextCase.getCases()) {
			Locale locale = languageService.detectLocale(text.getText());
			if(locale != null && locale.getLanguage().equals(text.getLanguage())) {
				count++;
			}
		}
		double ratio = count / TestTextCase.getCases().length;
		assertTrue(ratio > 0.8d);
	}
	
	@Test
	public void testWordCount() {
		for(TestTextCase.Text text:TestTextCase.getCases()) {
			Locale locale = new Locale(text.getLanguage());
			int words = languageService.wordCount(text.getText(), locale);
			assertTrue(words == text.getWords());
		}
	}
	
	@Test
	public void testCharacterCount() {
		for(TestTextCase.Text text:TestTextCase.getCases()) {
			Locale locale = new Locale(text.getLanguage());
			int characters = languageService.characterCount(text.getText(), locale);
			assertTrue(characters == text.getCharacters());
		}
	}
}