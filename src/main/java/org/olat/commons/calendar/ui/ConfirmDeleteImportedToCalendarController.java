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

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.manager.ImportToCalendarManager;
import org.olat.commons.calendar.model.ImportedToCalendar;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteImportedToCalendarController extends FormBasicController {
	
	private static final String[] confirmKeys = new String[] { "confirm" };

	private MultipleSelectionElement confirmEl;
	private MultipleSelectionElement calendarEl;

	private final List<ImportedToCalendar> importedCalendars;
	private final CalendarPersonalConfigurationRow calendarRow;
	
	@Autowired
	private ImportToCalendarManager importToCalendarManager;
	
	public ConfirmDeleteImportedToCalendarController(UserRequest ureq, WindowControl wControl, CalendarPersonalConfigurationRow calendarRow) {
		super(ureq, wControl, "confirm_delete_import_to");
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.calendarRow = calendarRow;
		importedCalendars = importToCalendarManager.getImportedCalendarsIn(calendarRow.getWrapper().getKalendar());
		initForm(ureq);
	}
	
	public CalendarPersonalConfigurationRow getCalendarRow() {
		return calendarRow;
	}
	
	public List<ImportedToCalendar> getSelectedImportedToCalendars() {
		Collection<String> selectedKeys = calendarEl.getSelectedKeys();
		return importedCalendars.stream()
				.filter(cal -> selectedKeys.contains(cal.getKey().toString()))
				.collect(Collectors.toList());
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer layoutCont = FormLayoutContainer.createDefaultFormLayout("confirm", getTranslator());
		formLayout.add("confirm", layoutCont);
		layoutCont.setRootForm(mainForm);
		
		SelectionValues calendarKeyValues = new SelectionValues();
		for(ImportedToCalendar importedCalendar:importedCalendars) {
			calendarKeyValues.add(SelectionValues.entry(importedCalendar.getKey().toString(), importedCalendar.getUrl()));
		}
		calendarEl = uifactory.addCheckboxesVertical("calendars", "cal.confirm.delete.imported.to.calendars", layoutCont,
				calendarKeyValues.keys(), calendarKeyValues.values(), 1);
		
		String confirmValue = translate("cal.confirm.delete.imported.to.check");
		confirmEl = uifactory.addCheckboxesHorizontal("confirm", "cal.confirm", layoutCont, confirmKeys, new String[] { confirmValue });
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		layoutCont.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("cal.delete.imported.to.calendar", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		confirmEl.clearError();
		if(!confirmEl.isAtLeastSelected(1)) {
			confirmEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		calendarEl.clearError();
		if(!calendarEl.isAtLeastSelected(1)) {
			calendarEl.setErrorKey("error.atleast.one", null);
			allOk &= false;
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
