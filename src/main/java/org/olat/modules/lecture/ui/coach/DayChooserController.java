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
package org.olat.modules.lecture.ui.coach;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.lecture.ui.event.ChangeDayEvent;

/**
 * 
 * Initial date: 29 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DayChooserController extends FormBasicController {
	
	private FormLink previousDayButton;
	private FormLink nextDayButton;
	private DateChooser dateEl;
	
	public DayChooserController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "day_chooser");
		
		initForm(ureq);
	}
	
	public Date getCurrentDate() {
		return dateEl.getDate();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		previousDayButton = uifactory.addFormLink("previous.day", "previous", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		previousDayButton.setDomReplacementWrapperRequired(false);
		previousDayButton.setIconLeftCSS("o_icon o_icon_previous_page");
		
		nextDayButton = uifactory.addFormLink("next.day", "next", "", null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
		nextDayButton.setDomReplacementWrapperRequired(false);
		nextDayButton.setIconLeftCSS("o_icon o_icon_next_page");
		
		dateEl = uifactory.addDateChooser("day", null, new Date(), formLayout);
		dateEl.setDomReplacementWrapperRequired(false);
		dateEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	public Date getDate() {
		Date date = dateEl.getDate();
		if(date == null) {
			date = new Date();
			dateEl.setDate(date);
		}
		return date;
	}
	
	public void setDate(Date date) {
		dateEl.setDate(date);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(previousDayButton == source) {
			doDay(ureq, -1);
		} else if(nextDayButton == source) {
			doDay(ureq, 1);
		} else if(dateEl == source) {
			fireEvent(ureq, new ChangeDayEvent(getDate()));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doDay(UserRequest ureq, int day) {
		Date date = getDate();
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, day);
		dateEl.setDate(cal.getTime());
		fireEvent(ureq, new ChangeDayEvent(cal.getTime()));
	}
}
