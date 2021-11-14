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

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.help.ui.HelpAdminController;
import org.olat.core.commons.chiefcontrollers.LanguageChooserController;
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
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.login.AboutController;
import org.springframework.beans.factory.annotation.Autowired;

public class OlatDmzTopNavController extends BasicController implements LockableController {
	
	private Link impressumLink, aboutLink;
	
	private AboutController aboutCtr;
	private LanguageChooserController languageChooserC;

	@Autowired
	private ImpressumModule impressumModule;
	@Autowired
	private HelpModule helpModule;
	@Autowired
	private I18nModule i18nModule;
	
	public OlatDmzTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		// Add translator for help plugins
		setTranslator(Util.createPackageTranslator(HelpAdminController.class, getLocale(), getTranslator()));
		
		VelocityContainer vc = createVelocityContainer("dmztopnav");
		
		// impressum
		vc.contextPut("impressumInfos", new ImpressumInformations(impressumModule));
		impressumLink = LinkFactory.createLink("_top_nav_dmz_impressum", "topnav.impressum", vc, this);
		impressumLink.setSuppressDirtyFormWarning(true);
		impressumLink.setTitle("topnav.impressum.alt");
		impressumLink.setIconLeftCSS("o_icon o_icon_impress o_icon-lg");
		impressumLink.setAjaxEnabled(false);
		impressumLink.setTarget("_blank");

		// help on login page
		if (helpModule.isHelpEnabled()) {
			List<String> helpPlugins = new ArrayList<>();
			for (HelpLinkSPI helpLinkSPI : helpModule.getDMZHelpPlugins()) {
				helpPlugins.add(helpLinkSPI.getHelpUserTool(getWindowControl()).getMenuComponent(ureq, vc).getComponentName());
			}
			vc.contextPut("helpPlugins", helpPlugins);
		}
		// about link
		aboutLink = AboutController.aboutLinkFactory("top.menu.about", getLocale(), this, true, false);
		aboutLink.setSuppressDirtyFormWarning(true);
		vc.put("topnav.about", aboutLink);

		//choosing language 
		if (i18nModule.getEnabledLanguageKeys().size() > 1) {
			languageChooserC = new LanguageChooserController(getWindowControl(), ureq, "_top_nav_dmz_lang_chooser");
			//DOKU:pb:2008-01 listenTo(languageChooserC); not necessary as LanguageChooser sends a MultiUserEvent
			//which is catched by the BaseFullWebappController. This one is then 
			//responsible to recreate the GUI with the new Locale
			vc.put("languageChooser", languageChooserC.getInitialComponent());
		}
		putInitialPanel(vc);		
	}

	@Override
	public void lock() {
		//
	}

	@Override
	public void unlock() {
		//
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == impressumLink) {
			doImpressum(ureq);
		} else if (source == aboutLink) {
			doAbout(ureq);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (aboutCtr == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(aboutCtr);
		aboutCtr = null;
	}

	@Override
	protected void doDispose() {
		if (languageChooserC != null) {
			languageChooserC.dispose();
			languageChooserC = null;
		}
        super.doDispose();
	}
	
	private void doAbout(UserRequest ureq) {
		if(aboutCtr != null) return;
		
		aboutCtr = new AboutController(ureq, getWindowControl());
		listenTo(aboutCtr);
		aboutCtr.activateAsModalDialog();
	}
	
	private void doImpressum(UserRequest ureq) {
		ControllerCreator impressumControllerCreator = new ControllerCreator() {
			@Override
			public Controller createController(UserRequest lureq, WindowControl lwControl) {
				return new ImpressumDmzMainController(lureq, lwControl);
			}
		};
		PopupBrowserWindow popupBrowserWindow = Windows.getWindows(ureq).getWindowManager().createNewUnauthenticatedPopupWindowFor(ureq, impressumControllerCreator);
		popupBrowserWindow.open(ureq);
	}
}