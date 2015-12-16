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

package org.olat.course.nodes.members;

import static org.olat.course.nodes.members.MembersCourseNodeEditController.*;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<BR/>
 * Configuration form for the members coursenode
 *
 * Initial Date: Sep 11, 2015
 *
 * @autohr dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class MembersConfigForm extends FormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	private static final String[] onValues = new String[] { "" };
	private static final String[] emailFctKeys = new String[]{ EMAIL_FUNCTION_ALL, EMAIL_FUNCTION_COACH_ADMIN };

	private final ModuleConfiguration config;
	private MultipleSelectionElement showOwners;
	private MultipleSelectionElement showCoaches;
	private MultipleSelectionElement showParticipants;
	private SingleSelection emailFunctionEl;

	/**
	 * Form constructor
	 *
	 * @param name
	 *            The form name
	 * @param config
	 *            The module configuration
	 * @param withCancel
	 *            true: cancel button is rendered, false: no cancel button
	 */
	protected MembersConfigForm(UserRequest ureq, WindowControl wControl,
			ModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		initForm(ureq);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(showOwners == source || showCoaches == source || showParticipants == source) {
			config.setBooleanEntry(CONFIG_KEY_SHOWOWNER, showOwners.isSelected(0));
			config.setBooleanEntry(CONFIG_KEY_SHOWCOACHES, showCoaches.isSelected(0));
			config.setBooleanEntry(CONFIG_KEY_SHOWPARTICIPANTS, showParticipants.isSelected(0));
			updateCheckboxes();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(emailFunctionEl == source && emailFunctionEl.isOneSelected()) {
			if(emailFunctionEl.isSelected(0)) {
				config.setStringValue(CONFIG_KEY_EMAIL_FUNCTION, EMAIL_FUNCTION_ALL);
			} else {
				config.setStringValue(CONFIG_KEY_EMAIL_FUNCTION, EMAIL_FUNCTION_COACH_ADMIN);
			}
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener,
			UserRequest ureq) {
		// set Formtitle and infobar
		setFormTitle("pane.tab.membersconfig");
		setFormInfo("members.info");
		setFormContextHelp("Communication and Collaboration#_teilnehmerliste");
		// Read Configuration
		boolean showOwnerConfig = config.getBooleanSafe(CONFIG_KEY_SHOWOWNER);
		boolean showCoachesConfig = config.getBooleanSafe(CONFIG_KEY_SHOWCOACHES);
		boolean showParticipantsConfig = config.getBooleanSafe(CONFIG_KEY_SHOWPARTICIPANTS);
		// Generate Checkboxes
		showOwners = uifactory.addCheckboxesHorizontal("members.owners", formLayout, onKeys, onValues);
		showCoaches = uifactory.addCheckboxesHorizontal("members.coaches", formLayout, onKeys, onValues);
		showParticipants = uifactory.addCheckboxesHorizontal("members.participants",formLayout, onKeys, onValues);
		showOwners.addActionListener(FormEvent.ONCLICK);
		showCoaches.addActionListener(FormEvent.ONCLICK);
		showParticipants.addActionListener(FormEvent.ONCLICK);
		// select checkboxes according to config
		showOwners.select("on", showOwnerConfig);
		showCoaches.select("on", showCoachesConfig);
		showParticipants.select("on", showParticipantsConfig);
		
		String[] emailFctValues = new String[]{
				translate("email.function.all"), translate("email.function.coachAndAdmin")
		};
		emailFunctionEl = uifactory.addRadiosVertical("emails", "email.function", formLayout, emailFctKeys, emailFctValues);
		emailFunctionEl.addActionListener(FormEvent.ONCLICK);
		String emailFct =  config.getStringValue(CONFIG_KEY_EMAIL_FUNCTION, EMAIL_FUNCTION_COACH_ADMIN);
		if(MembersCourseNodeEditController.EMAIL_FUNCTION_COACH_ADMIN.equals(emailFct)) {
			emailFunctionEl.select(EMAIL_FUNCTION_COACH_ADMIN, true);
		} else if(MembersCourseNodeEditController.EMAIL_FUNCTION_ALL.equals(emailFct)) {
			emailFunctionEl.select(EMAIL_FUNCTION_ALL, true);
		}
		
		updateCheckboxes();
	}

	@Override
	protected void formOK(UserRequest ureq) {

	}

	//method to check if the of of the checkboxes needs to be disabled in order to ensure a valid configuration
	//in the rare case of an invalid config all checkboxes are enabled
	private void updateCheckboxes() {
		if (!(showCoaches.isSelected(0) || showParticipants.isSelected(0))) {
			showOwners.setEnabled(!showOwners.isSelected(0));
		} else {
			showOwners.setEnabled(true);
		}
		if (!(showOwners.isSelected(0) || showParticipants.isSelected(0))) {
			showCoaches.setEnabled(!showCoaches.isSelected(0));
		} else {
			showCoaches.setEnabled(true);
		}
		if (!(showOwners.isSelected(0) || showCoaches.isSelected(0))) {
			showParticipants.setEnabled(!showParticipants.isSelected(0));
		} else {
			showParticipants.setEnabled(true);
		}
	}
}