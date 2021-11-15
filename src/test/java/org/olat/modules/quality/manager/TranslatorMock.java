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
package org.olat.modules.quality.manager;

import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 16.06.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TranslatorMock implements Translator {

	@Override
	public String getPackageName() {
		return "packageName";
	}

	@Override
	public String translate(String key) {
		return "translated";
	}

	@Override
	public String translate(String key, String... args) {
		return null;
	}

	@Override
	public String translate(String key, String[] args, Level missingTranslationLogLevel) {
		return null;
	}

	@Override
	public String translate(String key, String[] args, int recursionLevel, boolean fallBackToDefaultLocale) {
		return null;
	}

	@Override
	public Locale getLocale() {
		return null;
	}

	@Override
	public void setLocale(Locale locale) {
		//
	}
	
}
