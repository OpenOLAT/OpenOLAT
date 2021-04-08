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
package org.olat.course.nodes.livestream.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.LiveStreamCourseNode;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.nodes.livestream.LiveStreamEvent;
import org.olat.course.nodes.livestream.LiveStreamService;
import org.olat.course.nodes.livestream.ui.LiveStreamEventDataModel.EventCols;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LiveStreamListController extends FormBasicController {

	private FlexiTableElement tableEl;
	private LiveStreamEventDataModel dataModel;
	
	private final CourseCalendars calendars;
	private final int bufferBeforeMin;
	
	@Autowired
	private LiveStreamService liveStreamService;

	public LiveStreamListController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfiguration,
			CourseCalendars calendars) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.calendars = calendars;
		
		bufferBeforeMin = moduleConfiguration.getIntegerSafe(LiveStreamCourseNode.CONFIG_BUFFER_BEFORE_MIN, 0);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("list.title");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EventCols.subject));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EventCols.begin));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EventCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EventCols.location));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(EventCols.description));
		
		dataModel = new LiveStreamEventDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "livestream-upcoming");
		tableEl.setEmptyTableSettings("table.empty", null, "o_icon_calendar");

		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		loadModel();
	}
	
	void refreshData() {
		loadModel();
	}

	private void loadModel() {
		List<? extends LiveStreamEvent> upcomingEvents = liveStreamService.getUpcomingEvents(calendars, bufferBeforeMin);
		List<LiveStreamEventRow> rows = new ArrayList<>(upcomingEvents.size());
		for (LiveStreamEvent liveStreamEvent : upcomingEvents) {
			rows.add(new LiveStreamEventRow(liveStreamEvent));
		}
		// ascending
		Collections.sort(rows, (e1, e2) -> e1.getEvent().getBegin().compareTo(e2.getEvent().getBegin()));
		dataModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}
