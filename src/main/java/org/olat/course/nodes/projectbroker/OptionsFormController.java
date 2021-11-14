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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.projectbroker;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.projectbroker.service.ProjectBrokerModuleConfiguration;
import org.olat.course.nodes.projectbroker.service.ProjectGroupManager;

/**
 * 
 * @author guretzki
 */

public class OptionsFormController extends FormBasicController {

	private ProjectBrokerModuleConfiguration config;
	private IntegerElement nbrOfAttendees;	
	private MultipleSelectionElement selectionAccept;
	private MultipleSelectionElement selectionAutoSignOut;
	private MultipleSelectionElement selectionLimitedAttendees;
	private Long projectBrokerId;

	private final static String[] keys = new String[] { "form.modules.enabled.yes" };
	private final static String[] values = new String[] { "" };
	private static final int NBR_PARTICIPANTS_DEFAULT = 1;
	
	private final ProjectGroupManager projectGroupManager;
	/**
	 * Modules selection form.
	 * @param name
	 * @param config
	 */
	public OptionsFormController(UserRequest ureq, WindowControl wControl, ProjectBrokerModuleConfiguration config, Long projectBrokerId) {
		super(ureq, wControl);
		this.config = config;
		this.projectBrokerId = projectBrokerId;
		projectGroupManager = CoreSpringFactory.getImpl(ProjectGroupManager.class);
		initForm(this.flc, this, ureq);
	}

	/**
	 * @see org.olat.core.gui.components.Form#validate(org.olat.core.gui.UserRequest, Identity)
	 */
	public boolean validate() {
		return true;
	}

	/**
	 * Initialize form.
	 */
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		//create form elements
		int nbrOfParticipantsValue = config.getNbrParticipantsPerTopic();
		selectionLimitedAttendees = uifactory.addCheckboxesHorizontal("form.options.number.of.topics.per.participant", formLayout, keys, values);
		nbrOfAttendees = uifactory.addIntegerElement("form.options.number.of.participants.per.topic_nbr", nbrOfParticipantsValue, formLayout);
		nbrOfAttendees.setMinValueCheck(0, null);
		nbrOfAttendees.setDisplaySize(3);
		nbrOfAttendees.addActionListener(FormEvent.ONCHANGE);
		if (nbrOfParticipantsValue == ProjectBrokerModuleConfiguration.NBR_PARTICIPANTS_UNLIMITED) {
			nbrOfAttendees.setVisible(false);
			selectionLimitedAttendees.select(keys[0], false);
		} else {
			selectionLimitedAttendees.select(keys[0], true);
		}
		selectionLimitedAttendees.addActionListener(FormEvent.ONCLICK);
		
		final Boolean selectionAcceptValue = config.isAcceptSelectionManually();
		selectionAccept = uifactory.addCheckboxesHorizontal("form.options.selection.accept", formLayout, keys, values);
		selectionAccept.select(keys[0], selectionAcceptValue);
		selectionAccept.addActionListener(FormEvent.ONCLICK);

		final Boolean autoSignOut = config.isAutoSignOut();
		selectionAutoSignOut = uifactory.addCheckboxesHorizontal("form.options.auto.sign.out", formLayout, keys, values);
		selectionAutoSignOut.select(keys[0], autoSignOut);
		// enable auto-sign-out only when 'accept-selection' is enabled
		selectionAutoSignOut.setVisible(selectionAcceptValue);
		selectionAutoSignOut.addActionListener(FormEvent.ONCLICK);
		
		uifactory.addFormSubmitButton("save", formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// enable auto-sign-out only when 'accept-selection' is enabled
		fireEvent(ureq, Event.DONE_EVENT);
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == selectionAccept) {
			selectionAutoSignOut.setVisible(selectionAccept.isSelected(0));
			if (!selectionAccept.isSelected(0) && projectGroupManager.hasProjectBrokerAnyCandidates(projectBrokerId)) {
				this.showInfo("info.all.candidates.will.be.accepted.automatically");
			}
		} else if (source == selectionLimitedAttendees) {
			if (selectionLimitedAttendees.isSelected(0)) {
				nbrOfAttendees.setVisible(true);
				nbrOfAttendees.setIntValue(NBR_PARTICIPANTS_DEFAULT);
			} else {
				nbrOfAttendees.setVisible(false);
				nbrOfAttendees.setIntValue(ProjectBrokerModuleConfiguration.NBR_PARTICIPANTS_UNLIMITED);
			}
		}
		this.flc.setDirty(true);
	}

	public int getNnbrOfAttendees() {
		return nbrOfAttendees.getIntValue();
	}

	public boolean getSelectionAccept() {
		return selectionAccept.isSelected(0);
	}

	public boolean getSelectionAutoSignOut() {
		return getSelectionAccept() && selectionAutoSignOut.isSelected(0);
	}

}
