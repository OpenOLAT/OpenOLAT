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
package org.olat.commons.calendar.ui;

import java.io.IOException;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.manager.ImportCalendarManager;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportCalendarByUrlController extends FormBasicController {

	private TextElement calendarUrl;
	private TextElement calendarName;
	
	private KalendarRenderWrapper importedCalendar;

	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private ImportCalendarManager importCalendarManager;
	
	public ImportCalendarByUrlController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		
		initForm(ureq);
	}
	
	public KalendarRenderWrapper getImportedCalendar() {
		return importedCalendar;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("cal.synchronize.personal.type.url.desc");
		
		int identityLen = ureq.getIdentity().getName().length();
		calendarName = uifactory.addTextElement("calname", "cal.import.calname.prompt", 41-identityLen, "", formLayout);
		calendarUrl = uifactory.addTextElement("url", "cal.import.url.prompt", 200, "", formLayout);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", "cal.form.submitSingle", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		calendarName.clearError();
		if (calendarName.isEmpty()) {
			calendarName.setErrorKey("cal.import.calname.empty.error", null);
			allOk = false;
		} else {
			String calID = ImportCalendarManager.getImportedCalendarID(getIdentity(), calendarName.getValue());
			if (calendarManager.calendarExists(CalendarManager.TYPE_USER, calID)) {
				calendarName.setErrorKey("cal.import.calname.exists.error", null);
				allOk &= false;
			}
		}
		
		calendarUrl.clearError();
		if (calendarUrl.isEmpty()) {
			calendarUrl.setErrorKey("cal.import.url.empty.error", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String name = calendarName.getValue();
		String url = calendarUrl.getValue();
		
		try {
			importedCalendar = importCalendarManager.importCalendar(getIdentity(), name, CalendarManager.TYPE_USER, url);
			fireEvent(ureq, Event.DONE_EVENT);
		} catch (IOException e) {
			showError("cal.import.url.file.write.error");
			logError("", e);
		} catch (OLATRuntimeException e) {
			showError("cal.import.url.content.invalid");
			logError("Invalid calendar: " + url, e);
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
