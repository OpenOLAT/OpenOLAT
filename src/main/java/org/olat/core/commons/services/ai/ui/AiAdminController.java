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
package org.olat.core.commons.services.ai.ui;

import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin user interface to configure the AI service. Depending on the
 * selected service provider a second config form is displayed.
 * 
 * Initial date: 22.05.2024<br>
 * @author Florian Gnägi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AiAdminController extends BasicController {
	@Autowired
	private AiModule aiModule;

	private VelocityContainer mainVC;
	// the SPI chooser
	private AiSPIChooserFormController spiChooserCtr;
	// the config controller of the choosen SPI
	private Controller spiConfigCtr;
	
	/**
	 * Constructor, used by AutoCreator. 
	 * @param ureq The user request object
	 * @param wControl The window control object
	 */
	public AiAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("ai_module");
		putInitialPanel(mainVC);

		// 1) the SPI chooser
		spiChooserCtr = new AiSPIChooserFormController(ureq, wControl);
		listenTo(spiChooserCtr);
		mainVC.put("spiChooserCtr", spiChooserCtr.getInitialComponent());
		// 2) the currently active SPI configurator
		doInitSpiConfigController(ureq);
	}


	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == spiChooserCtr && event == Event.DONE_EVENT) {
			// to be on the save side we just remove the config controller...
			if (spiConfigCtr != null) {
				removeAsListenerAndDispose(spiConfigCtr);				
			}
			// ... and create it from scratch
			doInitSpiConfigController(ureq);
		}
	}
	
	
	/**
	 * Helper to init the SPI configuration controller based on the currently active configuration
	 * @param ureq
	 */
	private void doInitSpiConfigController(UserRequest ureq) {		
		AiSPI currentSPI =  aiModule.getAiProvider();
		if (currentSPI != null) {
			// use SPI factory method to create the admin controller
			spiConfigCtr = currentSPI.createAdminController(ureq, getWindowControl());
			listenTo(spiConfigCtr);
			mainVC.put("spiConfigCtr", spiConfigCtr.getInitialComponent());
		} else {
			mainVC.remove("spiConfigCtr");
		}
	}

	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// noting to catch
	}
}
