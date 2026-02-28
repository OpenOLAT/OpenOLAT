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

import org.olat.core.commons.services.ai.AiModule;
import org.olat.core.commons.services.ai.AiSPI;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin form to enable/disable AI service providers. Shows a toggle for each
 * registered SPI. Fires Event.CHANGED_EVENT when a toggle changes so the
 * parent controller can update the visible SPI config sections.
 *
 * Initial date: 25.02.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class AiProvidersFormController extends FormBasicController {

	@Autowired
	private AiModule aiModule;

	/**
	 * Standard constructor
	 *
	 * @param ureq
	 * @param wControl
	 */
	public AiProvidersFormController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("ai.providers.title");
		setFormDescription("ai.providers.desc");
		setFormInfo("ai.privacy");
		setFormWarning("warn.beta.feature");

		for (AiSPI spi : aiModule.getAiProviders()) {
			FormToggle toggle = uifactory.addToggleButton("enabled_" + spi.getId(), null,
					translate("on"), translate("off"), formLayout);
			toggle.setLabel(spi.getName(), null, false);
			toggle.addActionListener(FormEvent.ONCHANGE);
			toggle.setUserObject(spi);
			if (spi.isEnabled()) {
				toggle.toggleOn();
			} else {
				toggle.toggleOff();
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormToggle toggle) {
			AiSPI spi = (AiSPI) toggle.getUserObject();
			boolean newEnabled = toggle.isOn();
			spi.setEnabled(newEnabled);
			logAudit("AI provider " + spi.getName() + " [" + spi.getId() + "] enabled: " + newEnabled);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// auto-submitted by toggles
	}

}
