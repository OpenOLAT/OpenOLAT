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
package org.olat.modules.appointments.ui;

import org.olat.core.gui.components.table.IconCssCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.appointments.ui.DuplicateTopicCallback.AppointmentInput;

/**
 * 
 * Initial date: 14 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MeetingValidationRenderer extends IconCssCellRenderer {
	
	private final Translator translator;
	
	public MeetingValidationRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	protected String getIconCssClass(Object val) {
		if (val instanceof AppointmentInput) {
			AppointmentInput appointment = (AppointmentInput)val;
			if (appointment.getMeetingValidation() != null) {
				return appointment.getMeetingValidation().booleanValue()
						? "o_icon o_icon-fw o_icon_ok"
						: "o_icon o_icon-fw o_icon_warn";
			}
		}
		return null;
	}

	@Override
	protected String getCellValue(Object val) {
		String value = null;
		if (val instanceof AppointmentInput) {
			AppointmentInput appointment = (AppointmentInput)val;
			if (appointment.getMeetingValidation() != null) {
				if (appointment.getAppointment().getBBBMeeting() != null) {
					if (appointment.getMeetingValidation().booleanValue()) {
						value = translator.translate("meeting.bbb.available");
					} else {
						value = translator.translate("meeting.bbb.not.available");
						
					}
				} else if (appointment.getAppointment().getTeamsMeeting() != null) {
					if (appointment.getMeetingValidation().booleanValue()) {
						value = translator.translate("meeting.teams.available");
					} else {
						value = translator.translate("meeting.teams.not.available");
					}
				}
			}
		}
		return value;
	}

}
