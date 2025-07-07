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

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AppointmentsCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Initial date: 17 May 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentsConfigController extends FormBasicController {

	private static final String[] onKeys = new String[]{"on"};

	private MultipleSelectionElement organizersEl;
	private MultipleSelectionElement notificationForOrganizersEl;
	private MultipleSelectionElement participantsCanCommentEl;
	private final ModuleConfiguration configs;

	public AppointmentsConfigController(UserRequest ureq, WindowControl wControl, AppointmentsCourseNode courseNode) {
		super(ureq, wControl);
		configs = courseNode.getModuleConfiguration();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.config");
		setFormContextHelp("manual_user/learningresources/Course_Element_Appointment_Scheduling/");
		
		SelectionValues organizersKV = new SelectionValues();
		organizersKV.add(SelectionValues.entry(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_OWNER, translate("config.edit.owner")));
		organizersKV.add(SelectionValues.entry(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_COACH, translate("config.edit.coach")));
		organizersEl = uifactory.addCheckboxesVertical("config.edit.organizers", formLayout, organizersKV.keys(), organizersKV.values(), 1);
		organizersEl.addActionListener(FormEvent.ONCHANGE);
		organizersEl.select(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_OWNER, configs.getBooleanSafe(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_OWNER));
		organizersEl.select(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_COACH, configs.getBooleanSafe(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_COACH));

		String[] notificationOnValues = new String[] {
				translate("config.edit.send.notification.after.selected.appointment")
		};
		notificationForOrganizersEl = uifactory.addCheckboxesHorizontal("config.edit.notification.for.organizers", 
				formLayout, onKeys, notificationOnValues);
		notificationForOrganizersEl.addActionListener(FormEvent.ONCHANGE);
		boolean notification = configs.getBooleanSafe(AppointmentsCourseNode.CONFIG_KEY_NOTIFICATION_FOR_ORGANIZERS);
		if (notification) {
			notificationForOrganizersEl.select(onKeys[0], true);
		}
		
		String[] commentOnValues = new String[] {
				translate("config.edit.participants.can.comment")
		};
		participantsCanCommentEl = uifactory.addCheckboxesHorizontal("config.edit.comment", formLayout, onKeys,
				commentOnValues);
		participantsCanCommentEl.addActionListener(FormEvent.ONCHANGE);
		boolean canComment = configs.getBooleanSafe(AppointmentsCourseNode.CONFIG_KEY_PARTICIPANT_COMMENT);
		if (canComment) {
			participantsCanCommentEl.select(onKeys[0], true);
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == organizersEl) {
			setOrganizers(ureq);
		} else if (source == notificationForOrganizersEl) {
			setNotificationForOrganizers(ureq);
		} else if (source == participantsCanCommentEl) {
			setParticipantsCanComment(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void setOrganizers(UserRequest ureq) {
		Collection<String> selectedOrganizers = organizersEl.getSelectedKeys();
		configs.setBooleanEntry(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_OWNER, selectedOrganizers.contains(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_OWNER));
		configs.setBooleanEntry(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_COACH, selectedOrganizers.contains(AppointmentsCourseNode.CONFIG_KEY_ORGANIZER_COACH));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	private void setNotificationForOrganizers(UserRequest ureq) {
		configs.setBooleanEntry(AppointmentsCourseNode.CONFIG_KEY_NOTIFICATION_FOR_ORGANIZERS, notificationForOrganizersEl.isAtLeastSelected(1));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);		
	}
	
	private void setParticipantsCanComment(UserRequest ureq) {
		configs.setBooleanEntry(AppointmentsCourseNode.CONFIG_KEY_PARTICIPANT_COMMENT, participantsCanCommentEl.isAtLeastSelected(1));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
