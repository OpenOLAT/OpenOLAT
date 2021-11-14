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
package org.olat.course.nodes.bigbluebutton;

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.nodes.BigBlueButtonCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingRow;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingTableModel;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingTableModel.BMeetingsCols;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonRunController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonPeekViewController extends FormBasicController {
	
	private FlexiTableElement upcomingTableEl;
	private BigBlueButtonMeetingTableModel upcomingTableModel;

	private final String subIdent;
	private final RepositoryEntry courseEntry;
	
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;

	public BigBlueButtonPeekViewController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, BigBlueButtonCourseNode courseNode) {
		super(ureq, wControl, "peekview", Util.createPackageTranslator(BigBlueButtonRunController.class, ureq.getLocale()));
		courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		subIdent = courseNode.getIdent();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.end));

		upcomingTableModel = new BigBlueButtonMeetingTableModel(columnsModel, getLocale());
		upcomingTableEl = uifactory.addTableElement(getWindowControl(), "upcomingMeetings", upcomingTableModel,
				5, false, getTranslator(), formLayout);
		upcomingTableEl.setEmptyTableSettings("no.upcoming.meetings", null, "o_icon_calendar");
		upcomingTableEl.setCustomizeColumns(false);
		upcomingTableEl.setNumOfRowsEnabled(false);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(BMeetingsCols.start.name(), true));
		upcomingTableEl.setSortSettings(sortOptions);
	}
	
	private void loadModel() {
		List<BigBlueButtonMeeting> meetings = bigBlueButtonManager.getUpcomingsMeetings(courseEntry, subIdent, 5);
		List<BigBlueButtonMeetingRow> rows = meetings.stream()
				.map(BigBlueButtonMeetingRow::new)
				.collect(Collectors.toList());
		upcomingTableModel.setObjects(rows);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	
}
