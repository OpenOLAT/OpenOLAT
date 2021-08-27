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
package org.olat.core.commons.controllers.impressum;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * <h3>Description:</h3> This controller displays an impressum which it reads
 * from an external HTML file in the <code>olatdata</code> directory.
 * 
 * 
 * Initial Date: Aug 10, 2009 <br>
 * 
 * @author twuersch, frentix GmbH, http://www.frentix.com
 */
public class EmptyImpressumController extends BasicController {

	private final VelocityContainer mainVc;
	
	public EmptyImpressumController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVc = createVelocityContainer("empty_impressum");
		TextFactory.createTextComponentFromString("empty.impressum.warning", translate("empty.impressum"), "o_error", false, mainVc);
		
		putInitialPanel(mainVc);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		
	}

	@Override
	protected void doDispose() {
		
	}

}
