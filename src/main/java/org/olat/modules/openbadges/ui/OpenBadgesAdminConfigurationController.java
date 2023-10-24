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
package org.olat.modules.openbadges.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.openbadges.OpenBadgesModule;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2023-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesAdminConfigurationController extends FormBasicController {
	private final SelectionValues enabledKV;
	private MultipleSelectionElement enabledEl;

	@Autowired
	private OpenBadgesModule openBadgesModule;

	protected OpenBadgesAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		enabledKV = new SelectionValues();
		enabledKV.add(SelectionValues.entry("on", translate("on")));
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormContextHelp("manual_admin/administration/e-Assessment_openBadges/");
		setFormTitle("openBadges.configuration");
		setFormInfo("admin.info");
		enabledEl = uifactory.addCheckboxesHorizontal("enabled", "admin.menu.openbadges.title",
				formLayout, enabledKV.keys(), enabledKV.values());
		enabledEl.select(enabledKV.keys()[0], openBadgesModule.isEnabled());
		enabledEl.addActionListener(FormEvent.ONCHANGE);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (enabledEl == source) {
			doSetEnabled();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doSetEnabled() {
		openBadgesModule.setEnabled(enabledEl.isAtLeastSelected(1));
	}

	private void updateUI() {
		enabledEl.select(enabledKV.keys()[0], openBadgesModule.isEnabled());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
