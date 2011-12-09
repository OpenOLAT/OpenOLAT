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
package org.olat.core.util.i18n;

import java.io.File;
import java.util.Set;

import org.olat.core.commons.scheduler.JobWithDB;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * Description:<br>
 * This job checks all available languages on the system and produces a little
 * statistic file that can be embedded somewhere, e.g. in a homepage
 * 
 * <P>
 * Initial Date: 02.12.2008 <br>
 * 
 * @author gnaegi
 */
public class I18nTranslationStatusGeneratorJob extends JobWithDB {
	private static final File statsFile = new File(WebappHelper.getContextRoot() + "/static/translation_status.html");
	
	/**
	 * @see org.olat.core.commons.scheduler.JobWithDB#executeWithDB(org.quartz.JobExecutionContext)
	 */
	@SuppressWarnings("unused")
	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		if( ! I18nModule.isTransToolEnabled()){
			// return immediate if translation tool is not enabled.
			return;
		}
		log.info("Start generation of translation status");
		// Get all languages and count percentages
		I18nManager i18nMgr = I18nManager.getInstance();
		Set<String> availableKeys = I18nModule.getAvailableLanguageKeys();
		String[] availablelangKeys = ArrayHelper.toArray(availableKeys);
		String[] availableValues = new String[availablelangKeys.length];
		int referenceKeyCount = i18nMgr.countI18nItems(I18nModule.getFallbackLocale(), null, true);
		for (int i = 0; i < availablelangKeys.length; i++) {
			String key = availablelangKeys[i];
			String explLang = i18nMgr.getLanguageInEnglish(key, false);
			String all = explLang;
			if (explLang != null && !explLang.equals(key)) all += " (" + key + ")";
			// count translation status
			int keyCount = i18nMgr.countI18nItems(i18nMgr.getLocaleOrNull(key), null, true);
			all += "   <span class='b_translation_status'>" + (keyCount * 100 / referenceKeyCount) + "%</span>";
			availableValues[i] = all;
		}
		ArrayHelper.sort(availablelangKeys, availableValues, false, true, false);
		//
		// Now write to string
		StringBuffer sb = new StringBuffer();
		sb.append("<!-- include the olat default css to style this html fragment properly -->\n");
		sb.append("<ul class='b_translation_status'>\n");
		for (int i = 0; i < availablelangKeys.length; i++) {
			String langKey = availablelangKeys[i];
			sb.append("<li class='b_with_small_icon_left b_flag_").append(langKey).append("'>");
			sb.append(availableValues[i]);
			sb.append("</li>\n");
		}
		sb.append("</ul>");
		// 
		// Write aliases file to disk
		statsFile.getParentFile().mkdirs();
		FileUtils.save(statsFile, sb.toString(), "UTF-8");

	}

}
