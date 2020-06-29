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

import org.olat.core.gui.components.table.LabelCellRenderer;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.appointments.Appointment;

/**
 * 
 * Initial date: 14.05.2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AppointmentStatusCellRenderer extends LabelCellRenderer {

	@Override
	protected String getElementCssClass(Object val) {
		if (val instanceof Appointment.Status) {
			Appointment.Status status = (Appointment.Status) val;
			return "o_ap_status_" + status.name();
		}
		return null;
	}
	
	@Override
	protected String getIconCssClass(Object val) {
		return null;
	}

	@Override
	protected String getCellValue(Object val, Translator translator) {
		if (val instanceof Appointment.Status) {
			Appointment.Status status = (Appointment.Status) val;
			return translator.translate("appointment.status." + status.name());
		}
		return null;
	}

}
