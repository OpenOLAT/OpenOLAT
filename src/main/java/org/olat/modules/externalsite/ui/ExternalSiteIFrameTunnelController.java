/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.externalsite.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.navigation.NavElement;

/**
 * Initial date: Nov 21, 2023
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ExternalSiteIFrameTunnelController extends BasicController {

	public ExternalSiteIFrameTunnelController(UserRequest ureq, WindowControl wControl, NavElement origNavElem, String height) {
		super(ureq, wControl);

		VelocityContainer myContent = createVelocityContainer("externalsite_iframe_index");
		myContent.contextPut("url", origNavElem.getExternalUrl());
		myContent.contextPut("height", height);
		myContent.contextPut("title", origNavElem.getTitle());

		String frameId = "ifdc" + hashCode(); // for e.g. js use
		myContent.contextPut("frameId", frameId);
		putInitialPanel(myContent);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
