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
* <p>
*/ 

package org.olat.course.assessment.bulk;

import java.util.Locale;

import org.apache.logging.log4j.Level;
import org.olat.core.gui.translator.Translator;
/**
 * 
 * Description:<br>
 * Proxy for the column numbering
 * 
 * <P>
 * Initial Date:  19.12.2005 <br>
 *
 * @author Alexander Schneider
 */
public class HeaderColumnTranslator implements Translator {
	private static final String[] EMPTY_ARR = new String[0];
	private Translator origTranslator;
	
	public HeaderColumnTranslator(Translator origTranslator){
		this.origTranslator = origTranslator;
	}

	@Override
	public String translate(String key) {
		return translate(key, EMPTY_ARR);
	}

	@Override
	public String translate(String key, String... args) {
		return translate(key, args, Level.WARN);
	}
	
	@Override
	public String translate(String key, String[] args, Level missingTranslationLogLevel) {
		String val;
		if(key.startsWith("ccc")){
			String t = key.substring(3);
			val = origTranslator.translate("column", t);
		}else if (key.startsWith("hhh")){
			val = key.substring(3);
		}else{
			val = origTranslator.translate(key, args, missingTranslationLogLevel);
		}
		return val;
	}

	@Override
	public String translate(String key, String[] args, int recursionLevel, boolean fallBackToDefaultLocale) {
		// no fall back to default locale
		return translate(key, args);
	}

	@Override
	public Locale getLocale() {
		return origTranslator.getLocale();
	}

	@Override
	public void setLocale(Locale locale) {
		origTranslator.setLocale(locale);
	}

	@Override
	public String getPackageName() {
		return origTranslator.getPackageName();
	}
}
