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

public class OlatDmzTopNavController extends BasicController{
	
	private static final Boolean contextHelpEnabled = Boolean.valueOf(ContextHelpModule.isContextHelpEnabled());
	private Link impressumLink;
	private VelocityContainer topNavVC;
	private LanguageChooserController languageChooserC;
	
	public OlatDmzTopNavController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, false);
	}

	public OlatDmzTopNavController(UserRequest ureq, WindowControl wControl, boolean impressum) {
		super(ureq, wControl);

		topNavVC = createVelocityContainer("dmztopnav");
		
		// impressum
		if(impressum) {
			impressumLink = LinkFactory.createLink("_top_nav_dmz_impressum", "topnav.impressum", topNavVC, this);
			impressumLink.setTooltip("topnav.impressum.alt");
			impressumLink.setIconCSS("o_icon o_icon_impress o_icon-lg");
			impressumLink.setAjaxEnabled(false);
			impressumLink.setTarget("_blank");
		}
		
		// help on login page
		topNavVC.contextPut("isContextHelpEnabled", contextHelpEnabled);

		//choosing language 
		languageChooserC = new LanguageChooserController(getWindowControl(), ureq, "_top_nav_dmz_lang_chooser");
		//DOKU:pb:2008-01 listenTo(languageChooserC); not necessary as LanguageChooser sends a MultiUserEvent
		//which is catched by the BaseFullWebappController. This one is then 
		//responsible to recreate the GUI with the new Locale 
		//
		topNavVC.put("languageChooser", languageChooserC.getInitialComponent());
		

		putInitialPanel(topNavVC);		
	}
	
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == impressumLink) {
			ControllerCreator impressumControllerCreator = new ControllerCreator() {
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					return new ImpressumDmzMainController(lureq, lwControl);
				}
			};
			PopupBrowserWindow popupBrowserWindow = Windows.getWindows(ureq).getWindowManager().createNewUnauthenticatedPopupWindowFor(ureq, impressumControllerCreator);
			popupBrowserWindow.open(ureq);
		}
	}

	protected void doDispose() {
		if (languageChooserC != null) {
			languageChooserC.dispose();
			languageChooserC = null;
		}
	}
	
	
}
