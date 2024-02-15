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
package org.olat.modules.project.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 27 Feb 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjAppointmentDeleteConfirmationController extends ProjConfirmationController {
	
	public enum Cascade {all, future, single}

	private SingleSelection appointmentEl;
	
	private final boolean reccurence;

	public ProjAppointmentDeleteConfirmationController(UserRequest ureq, WindowControl wControl, String message, boolean reccurence) {
		super(ureq, wControl, message, "appointment.delete.confirmation.confirm", null, "appointment.delete.confirmation.button", true, false);
		this.reccurence = reccurence;
		initForm(ureq);
	}
	
	public Cascade getCascade() {
		return appointmentEl != null && appointmentEl.isOneSelected()
				? Cascade.valueOf(appointmentEl.getSelectedKey())
				: Cascade.all;
	}

	@Override
	protected void initFormElements(FormLayoutContainer confirmCont) {
		if (reccurence) {
			SelectionValues deleteSV = new SelectionValues();
			deleteSV.add(entry(Cascade.all.name(), translate("appointment.delete.appointments.all")));
			deleteSV.add(entry(Cascade.single.name(), translate("appointment.delete.appointments.single")));
			deleteSV.add(entry(Cascade.future.name(), translate("appointment.delete.appointments.future")));
			appointmentEl = uifactory.addRadiosVertical("appointment.delete.appointments", confirmCont, deleteSV.keys(), deleteSV.values());
			appointmentEl.select(Cascade.all.name(), true);
		}
	}
	
}
