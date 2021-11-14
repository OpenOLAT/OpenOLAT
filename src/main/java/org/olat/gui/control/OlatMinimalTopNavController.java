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

import org.olat.core.commons.fullWebApp.LockableController;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.RedirectMediaResource;

/**
 * Description:<br>
 * Minimal top novaigation
 * 
 * <P>
 * Initial Date:  15.02.2008 <br>
 * @author patrickb
 */
public class OlatMinimalTopNavController extends BasicController implements LockableController {

	private final Link closeLink;

	public OlatMinimalTopNavController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		VelocityContainer vc = createVelocityContainer("topnavminimal");
		vc.setDomReplacementWrapperRequired(false);
		closeLink = LinkFactory.createLink("topnav.close", vc, this);
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
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == closeLink){
			// close window (a html page which calls Window.close onLoad
			ureq.getDispatchResult().setResultingMediaResource(new RedirectMediaResource(StaticMediaDispatcher.createStaticURIFor("closewindow.html")));
			// release all resources and close window
			WindowBackOffice wbo = getWindowControl().getWindowBackOffice(); 
			Window w = wbo.getWindow();
			Windows.getWindows(ureq).deregisterWindow(w);
			wbo.dispose();			
		}
	}
}