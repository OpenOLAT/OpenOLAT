/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.edubase;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.EdubaseCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.edubase.EdubaseModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 21.06.2017<br>
 *
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class EdubaseConfigController extends FormBasicController {

	private final ModuleConfiguration config;

	private FormToggle descriptionEnabledEl;
	private FormToggle multiPakEnabledEl;
	private TextAreaElement multiPakTextAreaEl;

	@Autowired
	private EdubaseModule edubaseModule;

	public EdubaseConfigController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration) {
		super(ureq, wControl);

		this.config = moduleConfiguration;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.config");
		setFormContextHelp("manual_user/learningresources/Course_Elements/#edubase");

		boolean descriptionEnabled = config.getBooleanSafe(EdubaseCourseNode.CONFIG_DESCRIPTION_ENABLED);
		descriptionEnabledEl = uifactory.addToggleButton("edubase.with.description.enabled", "edubase.with.description.enabled",
				null, null, formLayout);
		if (descriptionEnabled) {
			descriptionEnabledEl.toggleOn();
		} else {
			descriptionEnabledEl.toggleOff();
		}

		if (edubaseModule.isMultiPakEnabled()) {
			boolean multiPakEnabled = config.getBooleanSafe(EdubaseCourseNode.CONFIG_MULTI_PAK_ENABLED);
			multiPakEnabledEl = uifactory.addToggleButton("edubase.with.multi.pak.enabled", "edubase.with.multi.pak.enabled", null, null, formLayout);
			if (multiPakEnabled) {
				multiPakEnabledEl.toggleOn();
			} else {
				multiPakEnabledEl.toggleOff();
			}
			multiPakEnabledEl.addActionListener(FormEvent.ONCHANGE);
			multiPakEnabledEl.setHelpTextKey("edubase.with.multi.pak.enabled.help", null);

			String multiPakInitValue = config.getStringValue(EdubaseCourseNode.CONFIG_MULTI_PAKS);
			multiPakTextAreaEl = uifactory.addTextAreaElement("edubase.multi.paks.textarea", null, -1, 6, 10, true, true, multiPakInitValue, formLayout);
			multiPakTextAreaEl.setVisible(multiPakEnabled);
		}

		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == multiPakEnabledEl) {
			multiPakTextAreaEl.setVisible(multiPakEnabledEl.isOn());
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	protected ModuleConfiguration getUpdatedConfig() {
		config.set(EdubaseCourseNode.CONFIG_DESCRIPTION_ENABLED,
				descriptionEnabledEl.isOn() ? Boolean.toString(true) : Boolean.toString(false));
		config.set(EdubaseCourseNode.CONFIG_MULTI_PAK_ENABLED,
				multiPakEnabledEl != null && multiPakEnabledEl.isOn() ? Boolean.toString(true) : Boolean.toString(false));
		config.set(EdubaseCourseNode.CONFIG_MULTI_PAKS,
				multiPakTextAreaEl != null && multiPakTextAreaEl.isVisible() ? multiPakTextAreaEl.getValue().stripTrailing() : "");
		return config;
	}
}