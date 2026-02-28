/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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

import java.util.ArrayList;
import java.util.List;

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
 * Admin user interface to configure the AI service. Shows all registered
 * providers with enable/disable toggles, provider-specific config (API keys),
 * and per-feature configuration (which provider/model to use).
 *
 * Initial date: 22.05.2024<br>
 *
 * @author Florian Gnägi, gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AiAdminController extends BasicController {
	@Autowired
	private AiModule aiModule;

	private VelocityContainer mainVC;

	// Providers section
	private AiProvidersFormController providersFormCtr;
	private final List<Controller> spiConfigCtrs = new ArrayList<>();

	// Features section
	private AiFeaturesAdminController featuresFormCtr;

	/**
	 * Constructor, used by AutoCreator.
	 * @param ureq The user request object
	 * @param wControl The window control object
	 */
	public AiAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		mainVC = createVelocityContainer("ai_module");
		putInitialPanel(mainVC);

		// 1) Provider toggles form
		providersFormCtr = new AiProvidersFormController(ureq, wControl);
		listenTo(providersFormCtr);
		mainVC.put("providersForm", providersFormCtr.getInitialComponent());

		// 2) Per-SPI config controllers (API keys) - shown only when SPI is enabled
		doInitSpiConfigControllers(ureq);

		// 3) Features config form
		doInitFeaturesController(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == providersFormCtr && event == Event.CHANGED_EVENT) {
			// A provider toggle changed - refresh SPI configs and features form
			doInitSpiConfigControllers(ureq);
			doInitFeaturesController(ureq);
		}
	}

	/**
	 * (Re-)create SPI config controllers based on current enabled state.
	 */
	private void doInitSpiConfigControllers(UserRequest ureq) {
		// Clean up old controllers
		for (Controller ctrl : spiConfigCtrs) {
			removeAsListenerAndDispose(ctrl);
		}
		spiConfigCtrs.clear();

		List<String> spiConfigCtrlNames = new ArrayList<>();
		for (AiSPI spi : aiModule.getAiProviders()) {
			String ctrlName = "spiConfig_" + spi.getId();
			spiConfigCtrlNames.add(ctrlName);

			if (spi.isEnabled()) {
				Controller spiConfigCtr = spi.createAdminController(ureq, getWindowControl());
				listenTo(spiConfigCtr);
				spiConfigCtrs.add(spiConfigCtr);
				mainVC.put(ctrlName, spiConfigCtr.getInitialComponent());
			} else {
				mainVC.remove(ctrlName);
			}
		}
		mainVC.contextPut("spiConfigCtrlNames", spiConfigCtrlNames);
	}

	/**
	 * (Re-)create the features admin controller with fresh provider data.
	 */
	private void doInitFeaturesController(UserRequest ureq) {
		if (featuresFormCtr != null) {
			removeAsListenerAndDispose(featuresFormCtr);
		}
		featuresFormCtr = new AiFeaturesAdminController(ureq, getWindowControl());
		listenTo(featuresFormCtr);
		mainVC.put("featuresForm", featuresFormCtr.getInitialComponent());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to catch
	}
}
