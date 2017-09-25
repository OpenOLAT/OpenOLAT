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
package org.olat.modules.lecture.ui;

import java.util.Calendar;
import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.lecture.ui.event.SearchLecturesBlockEvent;

/**
 * 
 * Initial date: 27 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewSearchController extends FormBasicController {
	
	private TextElement searchEl;
	private DateChooser startEl, endEl;
	
	private final boolean withSearchString;
	
	public TeacherOverviewSearchController(UserRequest ureq, WindowControl wControl, boolean withSearchString) {
		super(ureq, wControl, FormBasicController.LAYOUT_VERTICAL);
		this.withSearchString = withSearchString;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchEl = uifactory.addTextElement("search.text", "search.form.string", 128, "", formLayout);
		searchEl.setHelpText(translate("search.form.string.hint"));
		searchEl.setVisible(withSearchString);
		FormLayoutContainer dateLayout = FormLayoutContainer.createHorizontalFormLayout("dateLayout", getTranslator());
		formLayout.add(dateLayout);
		startEl = uifactory.addDateChooser("start", "search.form.start", null, dateLayout);
		endEl = uifactory.addDateChooser("end", "search.form.end", null, dateLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("search", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String searchString = searchEl.getValue();
		Date startDate = startEl.getDate();
		Date endDate = endEl.getDate();
		if(endDate != null) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDate);
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			endDate = cal.getTime();//end of day
		}
		SearchLecturesBlockEvent searchEvent = new SearchLecturesBlockEvent(searchString, startDate, endDate);
		fireEvent(ureq, searchEvent);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		searchEl.setValue(null);
		startEl.setDate(null);
		endEl.setDate(null);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
