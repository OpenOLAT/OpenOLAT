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

import org.olat.admin.help.ui.HelpAdminController;
import org.olat.admin.user.tools.UserTool;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.commons.services.help.HelpSupportController;
import org.olat.core.commons.services.help.OpenOlatDocsHelper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 27.04.2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
@Service("supportMailHelp")
public class SupportMailSPI implements HelpLinkSPI  {

	private static final String PLUGIN_NAME = "mail";
	
	@Autowired
	private HelpModule helpModule;
	
	@Autowired
	private OpenOlatDocsHelper openOlatDocsHelper;
	
	@Override
	public UserTool getHelpUserTool(WindowControl wControl) {
		return new SupportHelpUserTool(wControl);
	}
	
	public class SupportHelpUserTool implements UserTool, ComponentEventListener, ControllerEventListener {
		
		private final WindowControl wControl;
		private HelpSupportController helpSupportController;
		private CloseableModalController cmc;
		Translator translator;
		
		public SupportHelpUserTool(WindowControl wControl) {
			this.wControl = wControl;
		}

		@Override
		public Component getMenuComponent(UserRequest ureq, VelocityContainer container) {
			Link helpLink = LinkFactory.createLink("help.support", container, this);
			helpLink.setIconLeftCSS("o_icon o_icon-fw " + helpModule.getSupportIcon());
			return helpLink;
		}

		@Override
		public void dispatchEvent(UserRequest ureq, Component source, Event event) {	
			helpSupportController = new HelpSupportController(ureq, wControl);
			helpSupportController.addControllerListener(this);
    		translator = Util.createPackageTranslator(HelpAdminController.class, ureq.getLocale());
			
			cmc = new CloseableModalController(wControl, translator.translate("close"), helpSupportController.getInitialComponent(), true, translator.translate("contact"), true);
			cmc.addControllerListener(this);
			cmc.activate();
		}
		
		@Override
		public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
			if (source == helpSupportController) {
				helpSupportController.dispose();
				cmc.deactivate();
				cmc.dispose();
			} else if (source == cmc) {
				helpSupportController.dispose();
				cmc.deactivate();
				cmc.dispose();
			}
		}

		@Override
		public void dispose() {
			//
		}
	}
	
	@Override
	public String getURL(Locale locale, String page) {
		// Fallback to OpenOlat-docs context help
		return openOlatDocsHelper.getURL(locale, page);
	}

	@Override
	public Component getHelpPageLink(UserRequest ureq, String title, String tooltip, String iconCSS, String elementCSS,
			String page) {
		// Fallback to OpenOlat-docs context help
		return openOlatDocsHelper.createHelpPageLink(ureq, title, tooltip, iconCSS, elementCSS, page);
	}
	
	@Override
	public String getPluginName() {
		return PLUGIN_NAME;
	}
}
