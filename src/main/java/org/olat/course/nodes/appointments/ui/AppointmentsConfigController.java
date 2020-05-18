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
package org.olat.course.nodes.appointments.ui;

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AppointmentsCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 13 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentsConfigController extends FormBasicController {
	
	private static final String[] ON_KEYS = new String[] { "on" };
	private static final String KEY_COACH = "role.coach";
	private static final String[] EDIT_TOPIC_KEYS = new String[] { KEY_COACH };
	private static final String[] EDIT_APPOINTMENT_KEYS = new String[] { KEY_COACH };

	private MultipleSelectionElement confirmationEl;
	private MultipleSelectionElement editTopicEl;
	private MultipleSelectionElement editAppointmentEl;
	
	private ModuleConfiguration config;

	public AppointmentsConfigController(UserRequest ureq, WindowControl wControl, AppointmentsCourseNode courseNode) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		config = courseNode.getModuleConfiguration();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		formLayout.add(generalCont);
		generalCont.setFormTitle(translate("general"));
		
		confirmationEl = uifactory.addCheckboxesVertical("config.confirmation", generalCont, ON_KEYS,
				translateAll(getTranslator(), ON_KEYS), 1);
		confirmationEl.select(confirmationEl.getKey(0), config.getBooleanSafe(AppointmentsCourseNode.CONFIG_CONFIRMATION));
		confirmationEl.addActionListener(FormEvent.ONCHANGE);
		
		FormLayoutContainer rightsCont = FormLayoutContainer.createDefaultFormLayout("rights", getTranslator());
		formLayout.add(rightsCont);
		rightsCont.setFormTitle(translate("user.rights"));
		
		editTopicEl = uifactory.addCheckboxesVertical("config.edit.topic", rightsCont, EDIT_TOPIC_KEYS,
				translateAll(getTranslator(), EDIT_TOPIC_KEYS), 1);
		editTopicEl.select(KEY_COACH, config.getBooleanSafe(AppointmentsCourseNode.CONFIG_COACH_EDIT_TOPIC));
		editTopicEl.addActionListener(FormEvent.ONCHANGE);
		
		editAppointmentEl = uifactory.addCheckboxesVertical("config.edit.appointment", rightsCont,
				EDIT_APPOINTMENT_KEYS, translateAll(getTranslator(), EDIT_APPOINTMENT_KEYS), 1);
		editAppointmentEl.select(KEY_COACH,
				config.getBooleanSafe(AppointmentsCourseNode.CONFIG_COACH_EDIT_APPOINTMENT));
		editAppointmentEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == confirmationEl) {
			doConfirmation(ureq);
		} else if (source == editTopicEl) {
			doUserRights(ureq);
		} else if (source == editAppointmentEl) {
			doUserRights(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doConfirmation(UserRequest ureq) {
		config.setBooleanEntry(AppointmentsCourseNode.CONFIG_CONFIRMATION, confirmationEl.isAtLeastSelected(1));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void doUserRights(UserRequest ureq) {
		config.setBooleanEntry(AppointmentsCourseNode.CONFIG_COACH_EDIT_TOPIC, editTopicEl.isAtLeastSelected(1));
		config.setBooleanEntry(AppointmentsCourseNode.CONFIG_COACH_EDIT_APPOINTMENT, editAppointmentEl.isAtLeastSelected(1));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
