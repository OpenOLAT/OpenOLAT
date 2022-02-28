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
package org.olat.core.commons.services.help;

import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;

/**
 * Helper to create openolat docs help links.  
 * 
 * Help links have the following form:<br/>
 * https://docs.openolat.org/de/manual_user/general/
 * 
 * Initial date: 24.02.2022<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class OpenOlatDocsHelper {
	private static final String docsBaseUrl = "https://docs.openolat.org/";
	
	public static final Locale EN_Locale = new Locale("en");
	public static final Locale DE_Locale = new Locale("de");

	public static final String getURL(Locale locale, String page) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(docsBaseUrl);
		// for DE use DE prefix, for all other languages use EN as manual language
		if (locale.equals(DE_Locale)) {
			sb.append(DE_Locale.getLanguage());
			sb.append("/");
		}
		if (page != null) {			
			sb.append(page);
		}
		return sb.toString();
	}

	public static final Component createHelpPageLink(UserRequest ureq, String title, String tooltip, String iconCSS, String elementCSS,
			String page) {
		ExternalLink helpLink = new ExternalLink("topnav.help." + page);
		helpLink.setName(title);
		helpLink.setTooltip(tooltip);
		helpLink.setIconLeftCSS(iconCSS);
		helpLink.setElementCssClass(elementCSS);
		helpLink.setTarget("oohelp");
		helpLink.setUrl(getURL(ureq.getLocale(), page));
		return helpLink;
	}
	
}
