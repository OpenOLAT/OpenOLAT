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
import org.olat.admin.user.tools.UserTool;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.impressum.ImpressumInformations;
import org.olat.core.commons.controllers.impressum.ImpressumMainController;
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
import org.olat.gui.control.OlatTopNavController.Tool;
import org.olat.search.SearchModule;
import org.olat.search.SearchUserToolExtension;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial Date: 13.06.2006 <br>
 * 
 * @author patrickb
 */
public class OlatGuestTopNavController extends BasicController implements LockableController {

	private Link loginLink, impressumLink;

	@Autowired
	private SearchModule searchModule;
	@Autowired
	private ImpressumModule impressumModule;
	
	public OlatGuestTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(HelpAdminController.class, getLocale(), getTranslator()));
		
		VelocityContainer vc = createVelocityContainer("guesttopnav");
		vc.setDomReplacementWrapperRequired(false); // we provide our own DOM replacement ID
		
		// impressum
		vc.contextPut("impressumInfos", new ImpressumInformations(impressumModule));
		impressumLink = LinkFactory.createLink("_top_nav_guest_impressum", "topnav.impressum", vc, this);
		impressumLink.setTitle("topnav.impressum.alt");
		impressumLink.setIconLeftCSS("o_icon o_icon_impress o_icon-lg");
		impressumLink.setAjaxEnabled(false);
		impressumLink.setTarget("_blank");
		
		if(searchModule.isGuestEnabled()) {
			SearchUserToolExtension search = CoreSpringFactory.getImpl(SearchUserToolExtension.class);
			UserTool userTool = search.createUserTool(ureq, getWindowControl(), getLocale());
			Component cmp = userTool.getMenuComponent(ureq, vc);
			vc.put("search.tool", cmp);
		}
		
		// the help link
		HelpModule helpModule = CoreSpringFactory.getImpl(HelpModule.class);
		if (helpModule.isHelpEnabled()) {
			List<Tool> helpPluginLinksName = new ArrayList<>();
			for (HelpLinkSPI helpLinkSPI : helpModule.getDMZHelpPlugins()) {
				UserTool helpTool = helpLinkSPI.getHelpUserTool(getWindowControl());
				if (helpTool != null) {
					Component cmp = helpTool.getMenuComponent(ureq, vc);
					String CssId = "o_navbar_help_" + helpLinkSPI.getPluginName();
					String cssClass = "";
					helpPluginLinksName.add(new Tool(CssId, cssClass, cmp.getComponentName()));
				}
			}
			
			vc.contextPut("helpPlugins", helpPluginLinksName);
		}
		
		loginLink = LinkFactory.createLink("topnav.login", vc, this);
		loginLink.setIconLeftCSS("o_icon o_icon_login o_icon-lg");
		loginLink.setTitle("topnav.login.alt");

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
		if (source == loginLink) {
			AuthHelper.doLogout(ureq);
		} else if (source == impressumLink) {
			ControllerCreator impressumControllerCreator = new ControllerCreator() {
				@Override
				public Controller createController(UserRequest lureq, WindowControl lwControl) {
					return new ImpressumMainController(lureq, lwControl);
				}
			};
			PopupBrowserWindow popupBrowserWindow = Windows.getWindows(ureq).getWindowManager().createNewPopupBrowserWindowFor(ureq, impressumControllerCreator);
			popupBrowserWindow.open(ureq);
		}
	}

	@Override
	protected void doDispose() {
		// controllers disposed by BasicController:
	}

}