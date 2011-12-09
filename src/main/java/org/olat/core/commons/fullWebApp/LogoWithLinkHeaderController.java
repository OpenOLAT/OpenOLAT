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
package org.olat.core.commons.fullWebApp;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * <h3>Description:</h3>
 * This is a simple controller that displays a clickable logo for in the layout
 * header section.
 * <p>
 * Initial Date: 09.10.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class LogoWithLinkHeaderController extends BasicController {
	private VelocityContainer headerVC;

	/**
	 * Constructor for a simple header creator that displays a configured logo
	 * 
	 * @param ureq
	 * @param wControl
	 * @param linkURI
	 *            Clickable link URL
	 * @param imgURI
	 *            URL for clickable image
	 * @param imgAltText
	 *            Alt text for image
	 */
	protected LogoWithLinkHeaderController(UserRequest ureq,
			WindowControl wControl, String linkURI, String imgURI,
			String imgAltText) {
		super(ureq, wControl);
		headerVC = createVelocityContainer("logowithlinkheader");
		// push pring injected variables to view
		headerVC.contextPut("linkURI", linkURI);
		headerVC.contextPut("imgURI", imgURI);
		headerVC.contextPut("imgAltText", imgAltText);
		putInitialPanel(headerVC);
	}

	@Override
	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		// TODO Auto-generated method stub

	}
}
