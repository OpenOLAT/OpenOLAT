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
 * University of Zurich, Switzerland.<p>
 * 
 * Description:<br>
 * Run controller of the institution portlet.
 * 
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
package org.olat.portal.shiblogin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.shibboleth.SwitchShibbolethAuthenticationConfigurator;

/**
 * 
 * Description:<br>
 * uses the EmbeddedWAYF provided by SWITCH
 * 
 */
public class ShibLoginPortletRunController extends BasicController {	
	
	private VelocityContainer portletVC;
	
	
	protected ShibLoginPortletRunController(UserRequest ureq, WindowControl wControl, SwitchShibbolethAuthenticationConfigurator config) {
		super(ureq, wControl);
		
		this.portletVC = createVelocityContainer("portlet");
		
		portletVC.contextPut("wayfSPEntityID", config.getWayfSPEntityID());
		portletVC.contextPut("wayfSPHandlerURL", config.getWayfSPHandlerURL());
		portletVC.contextPut("wayfSPSamlDSURL", config.getWayfSPSamlDSURL());
		portletVC.contextPut("wayfReturnUrl", config.getWayfReturnUrl());
		portletVC.contextPut("additionalIDPs", config.getAdditionalIdentityProviders());
		
		setInitialComponent(this.portletVC);
	}
	
	/**
	 * @see org.olat.gui.control.DefaultController#event(org.olat.gui.UserRequest, org.olat.gui.components.Component, org.olat.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//nothing to do
	}

	/**
	 * @see org.olat.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// nothing to dispose
	}
	
}
