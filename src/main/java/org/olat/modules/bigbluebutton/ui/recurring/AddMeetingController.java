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
package org.olat.modules.bigbluebutton.ui.recurring;

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;

/**
 * 
 * Initial date: 7 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddMeetingController extends FormBasicController {
	
	private DateChooser dateEl;
	private RecurringMeetingsContext meetingsContext;
	
	public AddMeetingController(UserRequest ureq, WindowControl wControl, RecurringMeetingsContext meetingsContext) {
		super(ureq, wControl, Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, ureq.getLocale()));
		this.meetingsContext = meetingsContext;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		dateEl = uifactory.addDateChooser("meeting.day", "meeting.day", null, formLayout);
		dateEl.setMandatory(true);

		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	public Date getDate() {
		return dateEl.getDate();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		dateEl.clearError();
		if(dateEl.getDate() == null) {
			dateEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else if(dateEl.getDate().before(new Date())) {
			dateEl.setErrorKey("error.date.in.past", null);
			allOk &= false;
		} else {
			Date newDate = dateEl.getDate();
			List<RecurringMeeting> rMeetings = meetingsContext.getMeetings();
			for(RecurringMeeting rMeeting:rMeetings) {
				Date date = rMeeting.getStartDate();
				if(CalendarUtils.isSameDay(newDate, date)) {
					dateEl.setErrorKey("error.same.day", null);
					allOk &= false;
					break;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
