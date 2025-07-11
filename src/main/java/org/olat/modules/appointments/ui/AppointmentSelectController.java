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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.appointments.Appointment;

/**
 * Initial date: 2025-07-07<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AppointmentSelectController extends FormBasicController {

	private final Appointment appointment;
	private TextAreaElement commentEl;

	public AppointmentSelectController(UserRequest ureq, WindowControl wControl, Appointment appointment) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.appointment = appointment;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		uifactory.addStaticTextElement("text", null, translate("comment.text"), formLayout);
		
		commentEl = uifactory.addTextAreaElement("comment", null, 2000, 5, 80, 
				true, false, "", formLayout);
		
		FormItemContainer buttonContainer = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonContainer);
		uifactory.addFormSubmitButton("comment.send.and.select", buttonContainer);
		uifactory.addFormCancelButton("comment.skip.step.and.select", buttonContainer, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		commentEl.setValue(null);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	public Appointment getAppointment() {
		return appointment;
	}
	
	public String getComment() {
		return StringHelper.xssScan(commentEl.getValue());
	}
}
