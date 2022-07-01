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
package org.olat.test;

import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * A dummy translator to use in unit tests without the whole OpenOLAT setup.
 * 
 * Initial date: 24.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class KeyTranslator implements Translator {
	
	private Locale locale;
	private String prefix;
	
	public KeyTranslator(Locale locale) {
		this.locale = locale;
	}
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	@Override
	public Locale getLocale() {
		return locale;
	}

	@Override
	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
	@Override
	public String getPackageName() {
		return "";
	}

	@Override
	public String translate(String key) {
		return prefix != null? prefix + key: key;
	}

	@Override
	public String translate(String key, String... args) {
		return translate(key);
	}

	@Override
	public String translate(String key, String[] args, Level missingTranslationLogLevel) {
		return translate(key);
	}

	@Override
	public String translate(String key, String[] args, int recursionLevel, boolean fallBackToDefaultLocale) {
		return translate(key);
	}
}