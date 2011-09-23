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
* <p>
*/
package org.olat.core.commons.fullWebApp.popup;

import java.net.MalformedURLException;
import java.net.URL;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * A dummy controller to create dummy content with a redirect to an URL, used for within a pop up window.
 * 
 * <P>
 * Initial Date:  15.05.2009 <br>
 * @author patrickb
 */
class RedirectionDummyController extends BasicController{
	
	protected RedirectionDummyController(UserRequest ureq, WindowControl control, String url) {
		super(ureq, control);
		try {
			new URL(url);
		} catch (MalformedURLException e) {
			throw new AssertException("invalid URL "+url);
		}
		VelocityContainer vc = createVelocityContainer("redirect");
		vc.contextPut("url", url);
		putInitialPanel(vc);
		
	}

	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// TODO Auto-generated method stub

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}


}
