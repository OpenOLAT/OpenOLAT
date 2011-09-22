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

package org.olat.test.guidemo;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.test.GUIDemoMainController;

public class GuiDemoController extends BasicController {
	//how to create a logger
	OLog log = Tracing.createLoggerFor(GuiDemoController.class);

	Panel p;
	VelocityContainer vcMain;
	
	public GuiDemoController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setBasePackage(GUIDemoMainController.class);
		//simple Text as first node in the guidemo tree
		vcMain = createVelocityContainer("guidemo");
		p = putInitialPanel(vcMain);
	}

	@SuppressWarnings("unused")
	public void event(UserRequest ureq, Component source, Event event) {
		//no events to catch
	}

	protected void doDispose() {
		//no resources to dispose
	}

}
