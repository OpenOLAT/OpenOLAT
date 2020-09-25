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
 * 12.10.2011 by frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.openmeetings.manager;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.xml.parsers.SAXParser;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.xml.XMLFactories;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Map the id (int) to the locale from the languages file of OpenMeetings
 * 
 * 
 * Initial date: 07.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenMeetingsLanguages extends DefaultHandler {
	
	private static final Logger log = Tracing.createLoggerFor(OpenMeetingsLanguages.class);

	private final Map<String, Integer> languageToId = new HashMap<>();
	
	public OpenMeetingsLanguages() {
		//
	}
	
	public void read() {
		try(InputStream in = OpenMeetingsLanguages.class.getResourceAsStream("languages.xml")) {
			SAXParser saxParser = XMLFactories.newSAXParser();
			saxParser.parse(in, this);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	public int getLanguageId(Locale locale) {
		int id = -1;
		if(locale != null) {
			String language = locale.getLanguage();
			if(languageToId.containsKey(language)) {
				id = languageToId.get(language).intValue();
			}
		}
		
		if(id < 1) {
			Locale defLocale = I18nModule.getDefaultLocale();
			String defLanguage = defLocale.getLanguage();
			if(languageToId.containsKey(defLanguage)) {
				id = languageToId.get(defLanguage).intValue();
			}
		}
		
		if(id < 1) {
			return 1;//en
		}
		return id; 
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes)
	throws SAXException {
		if("lang".equals(qName)) {
			String id = attributes.getValue("id");
			String code = attributes.getValue("code");
			int languageId = Integer.parseInt(id);
			languageToId.put(code, Integer.valueOf(languageId));
		}
	}
}
