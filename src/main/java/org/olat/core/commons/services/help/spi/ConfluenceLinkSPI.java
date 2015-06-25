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
package org.olat.core.commons.services.help.spi;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.springframework.stereotype.Service;

/**
 * 
 * Build a link to openolat confluence. It has the following form:<br/>
 * https://confluence.openolat.org/display/OO100DE/OpenOLAT+10+Benutzerhandbuch
 * 
 * Initial date: 07.01.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("ooConfluenceLinkHelp")
public class ConfluenceLinkSPI implements HelpLinkSPI {
	
	@Override
	public UserTool getHelpUserTool(WindowControl wControl) {
		return new ConfluenceUserTool();
	}

	@Override
	public String getURL(Locale locale, String page) {
		StringBuilder sb = new StringBuilder(64);
		sb.append("https://confluence.openolat.org/display");
		String version = Settings.getVersion();
		sb.append(generateSpace(version, locale));
		if (page != null) {
			sb.append(page.replace(" ", "%20"));			
		}
		return sb.toString();
	}

	/**
	 * Convert 10.0 -> 100<br/>
	 * Convert 10.1.1 -> 101
	 * 
	 * 
	 * @param version
	 * @param locale
	 * @return
	 */
	protected String generateSpace(String version, Locale locale) {
		StringBuilder sb = new StringBuilder();
		sb.append("/OO");
		
		int firstPointIndex = version.indexOf('.');
		if(firstPointIndex > 0) {
			sb.append(version.substring(0, firstPointIndex));
			int secondPointIndex = version.indexOf('.', firstPointIndex + 1);
			if(secondPointIndex > firstPointIndex) {
				sb.append(version.substring(firstPointIndex+1, secondPointIndex));
			} else if(firstPointIndex + 1 < version.length()) {
				String subVersion = version.substring(firstPointIndex + 1);
				char[] subVersionArr = subVersion.toCharArray();
				for(int i=0; i<subVersionArr.length && Character.isDigit(subVersionArr[i]); i++) {
					sb.append(subVersionArr[i]);
				}
			} else {
				sb.append("0");
			}
		} else {
			char[] versionArr = version.toCharArray();
			for(int i=0; i<versionArr.length && Character.isDigit(versionArr[i]); i++) {
				sb.append(versionArr[i]);
			}
			//add minor version
			sb.append("0");
		}
		
		if(locale.getLanguage().equals(new Locale("de").getLanguage())) {
			sb.append("DE/");
		} else {
			sb.append("EN/");
		}
		
		return sb.toString();
	}
	
	public class ConfluenceUserTool implements UserTool {

		@Override
		public Component getMenuComponent(UserRequest ureq, VelocityContainer container) {
			ExternalLink helpLink = new ExternalLink("topnav.help");
			container.put("topnav.help", helpLink);
			helpLink.setIconLeftCSS("o_icon o_icon_help o_icon-lg");
			helpLink.setName(container.getTranslator().translate("topnav.help"));
			helpLink.setTooltip(container.getTranslator().translate("topnav.help.alt"));
			helpLink.setTarget("oohelp");
			helpLink.setUrl(getURL(ureq.getLocale(), null));
			return helpLink;
		}

		@Override
		public void dispose() {
			//
		}
	}

	@Override
	public Component getHelpPageLink(UserRequest ureq, String title, String tooltip, String iconCSS, String elementCSS, String page) {
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
