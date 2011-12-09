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
package org.olat.core.commons.chiefcontrollers;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.StringMediaResource;
import org.olat.core.util.i18n.I18nManager;

/**
 * Description:<br>
 * The JSTranslatorMapper offers data files needed to generate java script
 * translators. The file support time stamp checks to allow browser caching.
 * 
 * <P>
 * Initial Date: 29.09.2008 <br>
 * 
 * @author gnaegi
 */
class JSTranslatorMapper implements Mapper {

	/**
	 * @see org.olat.core.dispatcher.mapper.Mapper#handle(java.lang.String, javax.servlet.http.HttpServletRequest)
	 */
	public MediaResource handle(String relPath, HttpServletRequest request) {
		I18nManager i18nManager = I18nManager.getInstance();
		// Get bundle and locale
		String[] parts = relPath.split("/");
		String localeKey = parts[1];
		String bundleName = parts[2];
		Locale locale = i18nManager.getLocaleOrDefault(localeKey);
		// Create a media resource		
		StringMediaResource resource = new StringMediaResource() {
			@Override
			public void prepare(HttpServletResponse hres) {
				// don't use normal string media headers which prevent caching,
				// use standard browser caching based on last modified timestamp
			}
		};
		resource.setLastModified(i18nManager.getLastModifiedDate(locale, bundleName));
		resource.setContentType("text/javascript");
		// Get the translation data 
		String translationData = i18nManager.getJSTranslatorData(locale, bundleName);
		resource.setData(translationData);
		// UTF-8 encoding used in this js file since explicitly set in the ajax
		// call (usually js files are 8859-1)
		resource.setEncoding("utf-8");
		return resource;
	}

}
