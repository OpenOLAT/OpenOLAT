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
package org.olat.core.commons.services.help.spi;

import java.util.Locale;

import org.olat.admin.user.tools.UserTool;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.commons.services.help.OpenOlatDocsHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
@Service("ooDocsLinkHelp")
public class OODocsLinkSPI implements HelpLinkSPI {

	private static final String PLUGIN_NAME = "manual";
	
	@Autowired
	HelpModule helpModule;
	
	@Override
	public UserTool getHelpUserTool(WindowControl wControl) {
		return new ConfluenceUserTool();
	}
	
	public class ConfluenceUserTool implements UserTool {

		@Override
		public Component getMenuComponent(UserRequest ureq, VelocityContainer container) {
			ExternalLink helpLink = new ExternalLink("help.ooDocs");
			helpLink.setIconLeftCSS("o_icon o_icon-fw " + helpModule.getOODocsIcon());
			helpLink.setName(container.getTranslator().translate("help.ooDocs"));
			helpLink.setTooltip(container.getTranslator().translate("help.ooDocs"));
			helpLink.setTarget("oohelp");
			helpLink.setUrl(getURL(ureq.getLocale(), null));
			container.put("help.ooDocs", helpLink);
			return helpLink;
		}

		@Override
		public void dispose() {
			//
		}
	}

	@Override
	public String getURL(Locale locale, String page) {
		// delegate to helper
		return OpenOlatDocsHelper.getURL(locale, page);
	}

	@Override
	public Component getHelpPageLink(UserRequest ureq, String title, String tooltip, String iconCSS, String elementCSS,
			String page) {
		// delegate to helper
		return OpenOlatDocsHelper.createHelpPageLink(ureq, title, tooltip, iconCSS, elementCSS, page);
	}
	
	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}
}
