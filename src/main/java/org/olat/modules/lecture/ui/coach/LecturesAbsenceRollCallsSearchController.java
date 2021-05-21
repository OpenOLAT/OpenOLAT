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

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.event.SearchLecturesBlockEvent;

/**
 * 
 * Initial date: 20 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesAbsenceRollCallsSearchController extends FormBasicController {
	
	private TextElement searchEl;
	private DateChooser dateEl;
	
	private Date currentDate;
	
	public LecturesAbsenceRollCallsSearchController(UserRequest ureq, WindowControl wControl, Date currentDate) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.currentDate = currentDate;
		initForm(ureq);
	}
	
	public void setCurrentDate(Date date) {
		currentDate = date;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchEl = uifactory.addTextElement("search.form.string", 128, "", formLayout);
		searchEl.setHelpTextKey("search.form.string.hint.more", null);
		
		dateEl = uifactory.addDateChooser("search.date", currentDate, formLayout);
		dateEl.setSecondDate(true);
		dateEl.setSecondDate(currentDate);
		dateEl.setSeparator("search.form.till");
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("search", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Date startDate = dateEl.getDate();
		Date endDate = dateEl.getSecondDate();
		if(endDate != null) {
			endDate = CalendarUtils.endOfDay(endDate);
		}

		fireEvent(ureq, new SearchLecturesBlockEvent(searchEl.getValue(), startDate, endDate));
	}
}
