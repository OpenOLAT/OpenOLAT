/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.copy;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.ui.CurriculumComposerController;

/**
 * 
 * Initial date: 21 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ShiftDatesController extends FormBasicController {
	
	private static final String DATE = "date";
	private static final String DAYS = "days";
	
	private SingleSelection shiftToEl;
	private DateChooser dateShiftEl;
	private TextElement daysShiftEl;
	
	private final Date earliestDate;
	private final CopyElementContext context;
	
	public ShiftDatesController(UserRequest ureq, WindowControl wControl, CopyElementContext context, Date earliestDate) {
		super(ureq, wControl, Util.createPackageTranslator(CurriculumComposerController.class, ureq.getLocale()));
		this.context = context;
		this.earliestDate = earliestDate;
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		SelectionValues shiftToPK = new SelectionValues();
		shiftToPK.add(SelectionValues.entry(DATE, translate("shift.to.date")));
		shiftToPK.add(SelectionValues.entry(DAYS, translate("shift.to.day")));
		shiftToEl = uifactory.addRadiosHorizontal("shift.to", formLayout, shiftToPK.keys(), shiftToPK.values());
		if(context.getShiftDateByDays() > 0) {
			shiftToEl.select(DAYS, true);
		} else {
			shiftToEl.select(DATE, true);
		}
		
		shiftToEl.addActionListener(FormEvent.ONCHANGE);
		
		String date = earliestDate == null ? "-" : Formatter.getInstance(getLocale()).formatDate(earliestDate);
		uifactory.addStaticTextElement("shift.earliest.date", date, formLayout);
		
		Date start = DateUtils.getStartOfDay(DateUtils.addDays(new Date(), 1));
		dateShiftEl = uifactory.addDateChooser("shift.start.date", "shift.start.date", start, formLayout);
		
		String shift = context.getShiftDateByDays() >= 0 ? Long.toString(context.getShiftDateByDays()) : "1";
		daysShiftEl = uifactory.addTextElement("shift.num.days", "shift.num.days", 4, shift, formLayout);
		daysShiftEl.setMaxLength(4);
		daysShiftEl.setDisplaySize(4);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("shift.dates", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		dateShiftEl.clearError();
		daysShiftEl.clearError();
		if(dateShiftEl.isVisible()) {
			if(dateShiftEl.getDate() == null) {
				dateShiftEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			}
		} else if(daysShiftEl.isVisible()) {
			if(!StringHelper.containsNonWhitespace(daysShiftEl.getValue())) {
				daysShiftEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if(!StringHelper.isLong(daysShiftEl.getValue())) {
				daysShiftEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(shiftToEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateUI() {
		boolean days = isDays();
		dateShiftEl.setVisible(!days);
		daysShiftEl.setVisible(days);
	}
	
	private boolean isDays() {
		return shiftToEl.isOneSelected() && DAYS.equals(shiftToEl.getSelectedKey());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		context.setShiftDateByDays(0);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean days = isDays();
		long numOfDays = 0l;
		if(days) {
			String daysString = daysShiftEl.getValue();
			if(StringHelper.isLong(daysString)) {
				numOfDays = Long.parseLong(daysString);
			}
		} else {
			Date date = dateShiftEl.getDate();
			if(date != null && this.earliestDate != null) {
				numOfDays = DateUtils.countDays(earliestDate, date);
			}
		}
		context.setShiftDateByDays(numOfDays);
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
