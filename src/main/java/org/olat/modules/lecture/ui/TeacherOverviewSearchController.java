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

import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.ui.event.SearchLecturesBlockEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeacherOverviewSearchController extends FormBasicController implements Activateable2 {
	
	private TextElement searchEl;
	private DateChooser startEl, endEl;
	
	private final boolean withSearchString;
	
	@Autowired
	private RepositoryService repositoryService;
	
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
	
	public void setSearch(String text) {
		searchEl.setValue(text);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String name = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("RepositoryEntry".equals(name)) {
			Long id = entries.get(0).getOLATResourceable().getResourceableId();
			RepositoryEntry entry = repositoryService.loadByKey(id);
			if(entry != null) {
				if(StringHelper.containsNonWhitespace(entry.getExternalRef())) {
					searchEl.setValue(entry.getExternalRef());
				} else {
					searchEl.setValue(entry.getDisplayname());
				}
				doSearch(ureq);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSearch(ureq);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		searchEl.setValue(null);
		startEl.setDate(null);
		endEl.setDate(null);
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doSearch(UserRequest ureq) {
		String searchString = searchEl.getValue();
		Date startDate = startEl.getDate();
		Date endDate = endEl.getDate();
		if(endDate != null) {
			endDate = CalendarUtils.endOfDay(endDate);
		}
		SearchLecturesBlockEvent searchEvent = new SearchLecturesBlockEvent(searchString, startDate, endDate);
		fireEvent(ureq, searchEvent);
	}
}
