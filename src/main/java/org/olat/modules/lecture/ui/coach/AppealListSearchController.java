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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.event.SearchAppealsEvent;

/**
 * 
 * Initial date: 6 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppealListSearchController extends FormBasicController {
	
	String[] statusKeys = new String[] {
			LectureBlockAppealStatus.pending.name(), LectureBlockAppealStatus.rejected.name(), LectureBlockAppealStatus.approved.name()
	};
	
	private DateChooser dateEl;
	private TextElement searchEl;
	private MultipleSelectionElement statusEl;

	private Date currentDate = new Date();
	
	public AppealListSearchController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchEl = uifactory.addTextElement("search.form.string", 128, "", formLayout);
		searchEl.setHelpTextKey("search.form.string.hint.appeals", null);

		String[] statusValues = new String[statusKeys.length];
		for(int i=statusKeys.length; i-->0; ) {
			statusValues[i] = translate("appeal.".concat(statusKeys[i]));
		}
		statusEl = uifactory.addCheckboxesDropdown("search.form.status", "search.form.status", formLayout,
				statusKeys, statusValues);
		statusEl.select(LectureBlockAppealStatus.pending.name(), true);
		
		dateEl = uifactory.addDateChooser("search.date", currentDate, formLayout);
		dateEl.setSecondDate(true);
		dateEl.setSecondDate(currentDate);
		dateEl.setSeparator("search.form.till");

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("search", buttonsCont);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String searchString = searchEl.getValue();
		Date startDate = dateEl.getDate();
		if(startDate != null) {
			startDate = CalendarUtils.startOfDay(startDate);
		}
		Date endDate = dateEl.getSecondDate();
		if(endDate != null) {
			endDate = CalendarUtils.endOfDay(endDate);
		}
		Collection<String> selectedStatus = statusEl.getSelectedKeys();
		List<LectureBlockAppealStatus> status = selectedStatus.stream()
				.map(LectureBlockAppealStatus::valueOf).collect(Collectors.toList());
		fireEvent(ureq, new SearchAppealsEvent(searchString, status, startDate, endDate));
	}
}
