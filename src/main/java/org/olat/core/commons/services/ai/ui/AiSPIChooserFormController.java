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

import java.util.List;

import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin form to select the AI service provider. Stores the configuration in the AI module
 * 
 * Initial date: 22.05.2024<br>
 * 
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AiSPIChooserFormController extends FormBasicController {
	private SingleSelection serviceEl;
		
	@Autowired
	private AiModule aiModule;

	/**
	 * Standard constructor
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public AiSPIChooserFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("ai.title");		
		setFormDescription("ai.desc");
		setFormInfo("ai.privacy");
		setFormWarning("warn.beta.feature");


		// Use "-" as key for "disabled" on position 0, then list all other available providers
		List<AiSPI> spies = aiModule.getAiProviders();
		String[] serviceKeys = new String[spies.size() + 1];
		String[] serviceValues = new String[spies.size() + 1];
		serviceKeys[0] = "-";
		serviceValues[0] = translate("ai.disabled");

		for(int i=spies.size(); i-->0; ) {
			serviceKeys[i+1] = spies.get(i).getId();
			serviceValues[i+1] = spies.get(i).getName();
		}

		serviceEl = uifactory.addDropdownSingleselect("ai.service", "ai.service", formLayout, serviceKeys, serviceValues, null);
		serviceEl.addActionListener(FormEvent.ONCHANGE);
		// select current provider
		if(aiModule.getAiProvider() != null) {
			String activeServiceId = aiModule.getAiProvider().getId();
			for(int i=serviceKeys.length; i-->0; ) {
				if(serviceKeys[i].equals(activeServiceId)) {
					serviceEl.select(serviceKeys[i], true);
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(serviceEl == source) {
			if(serviceEl.isOneSelected()) {
				// get the currently selected provider or NULL and store the value in the module
				String serviceId = serviceEl.getSelectedKey();
				List<AiSPI> spis = aiModule.getAiProviders();
				AiSPI spi = null;
				for(AiSPI aSpi:spis) {
					if(aSpi.getId().equals(serviceId)) {
						spi = aSpi;
						break;
					}
				}
				aiModule.setAiProvider(spi);
				if (spi == null) {
					logAudit("AI module has been disabled");
				} else {
					logAudit("AI provider changed to " + spi.getName() + "[" + spi.getId() + "]");										
				}
				fireEvent(ureq, Event.DONE_EVENT);		
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// auto-submitted on serviceEl
	}

}
