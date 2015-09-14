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
package org.olat.gui.control;

import org.olat.core.commons.chiefcontrollers.LanguageChooserController;
import org.olat.core.commons.contextHelp.ContextHelpModule;
import org.olat.core.commons.controllers.impressum.ImpressumDmzMainController;
import org.olat.core.commons.controllers.impressum.ImpressumInformations;
import org.olat.core.commons.controllers.impressum.ImpressumModule;
import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.commons.services.help.HelpLinkSPI;
import org.olat.core.commons.services.help.HelpModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.id.OLATResourceable;
import org.olat.login.AboutController;
import org.springframework.beans.factory.annotation.Autowired;

public class OlatDmzTopNavController extends BasicController implements LockableController {
	
	private static final Boolean contextHelpEnabled = Boolean.valueOf(ContextHelpModule.isContextHelpEnabled());
	private Link impressumLink, aboutLink;
	private LanguageChooserController languageChooserC;

	@Autowired
	private ImpressumModule impressumModule;
	@Autowired
	private HelpModule helpModule;
	
	public OlatDmzTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		VelocityContainer vc = createVelocityContainer("dmztopnav");
		
		// impressum
		vc.contextPut("impressumInfos", new ImpressumInformations(impressumModule));
		impressumLink = LinkFactory.createLink("_top_nav_dmz_impressum", "topnav.impressum", vc, this);
		impressumLink.setTooltip("topnav.impressum.alt");
		impressumLink.setIconLeftCSS("o_icon o_icon_impress o_icon-lg");
		impressumLink.setAjaxEnabled(false);
		impressumLink.setTarget("_blank");

		// help on login page
		vc.contextPut("isContextHelpEnabled", contextHelpEnabled);
		if (helpModule.isHelpEnabled()) {
			HelpLinkSPI provider = helpModule.getHelpProvider();
			Component helpLink = provider.getHelpPageLink(ureq, translate("help.manual"), translate("help.manual"), "o_icon o_icon-wf o_icon_manual", null, "Login page");
			vc.put("topnav.help", helpLink);
		}
		// about link
		aboutLink = AboutController.aboutLinkFactory(getLocale(), this, true, false);
		vc.put("topnav.about", aboutLink);

		//choosing language 
		languageChooserC = new LanguageChooserController(getWindowControl(), ureq, "_top_nav_dmz_lang_chooser");
		//DOKU:pb:2008-01 listenTo(languageChooserC); not necessary as LanguageChooser sends a MultiUserEvent
		//which is catched by the BaseFullWebappController. This one is then 
		//responsible to recreate the GUI with the new Locale
		vc.put("languageChooser", languageChooserC.getInitialComponent());
		putInitialPanel(vc);		
	}

	@Override
	public void lockResource(OLATResourceable resource) {
		//
	}

	@Override
	public void unlockResource() {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == impressumLink) {
			ControllerCreator impressumControllerCreator = new ControllerCreator() {
				@Override
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					return new ImpressumDmzMainController(lureq, lwControl);
				}
			};
			PopupBrowserWindow popupBrowserWindow = Windows.getWindows(ureq).getWindowManager().createNewUnauthenticatedPopupWindowFor(ureq, impressumControllerCreator);
			popupBrowserWindow.open(ureq);
		} else if (source == aboutLink) {
			AboutController aboutCtr = new AboutController(ureq, getWindowControl());
			listenTo(aboutCtr);
			aboutCtr.activateAsModalDialog();
		}
	}

	@Override
	protected void doDispose() {
		if (languageChooserC != null) {
			languageChooserC.dispose();
			languageChooserC = null;
		}
	}
}