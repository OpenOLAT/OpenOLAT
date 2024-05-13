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
package org.olat.core.commons.services.vfs.ui.version;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Initial date: 8 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AdminConfigurationMainController extends BasicController {

	private final AdminConfigurationController configurationCtrl;
	private final TrashConfigurationController trashConfigurationCtrl;

	public AdminConfigurationMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("configuration_main");
		putInitialPanel(mainVC);
		
		configurationCtrl = new AdminConfigurationController(ureq, getWindowControl());
		listenTo(configurationCtrl);
		mainVC.put("config", configurationCtrl.getInitialComponent());
		
		trashConfigurationCtrl = new TrashConfigurationController(ureq, getWindowControl());
		listenTo(trashConfigurationCtrl);
		mainVC.put("trash", trashConfigurationCtrl.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
