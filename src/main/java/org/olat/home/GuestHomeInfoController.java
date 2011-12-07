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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
* <p>
*/
package org.olat.home;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Description:<br>
 * this controller displays a header and a message (used for guest-info in minimalHome for guests)
 * header and message are set in translation-properties
 * 
 * 
 * <P>
 * Initial Date:  12.09.2011 <br>
 * @author strentini, sergio.trentini@frentix.com,   www.frentix.com
 */
public class GuestHomeInfoController extends BasicController {

	private VelocityContainer vc;

	public GuestHomeInfoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		vc = createVelocityContainer("guestinfo");
		putInitialPanel(vc);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

}
