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
package org.olat.course.nodes.teams;

import java.util.List;

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
import org.olat.course.nodes.TeamsCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.ui.TeamsMeetingTableModel;
import org.olat.modules.teams.ui.TeamsMeetingTableModel.MeetingsCols;
import org.olat.modules.teams.ui.TeamsMeetingsRunController;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsPeekViewController extends FormBasicController {

	private FlexiTableElement tableEl;
	private TeamsMeetingTableModel tableModel;

	private final String subIdent;
	private final RepositoryEntry courseEntry;
	
	@Autowired
	private TeamsService teamsManager;

	public TeamsPeekViewController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, TeamsCourseNode courseNode) {
		super(ureq, wControl, "peekview", Util.createPackageTranslator(TeamsMeetingsRunController.class, ureq.getLocale()));
		courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		subIdent = courseNode.getIdent();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.subject));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.end));

		tableModel = new TeamsMeetingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "upcomingMeetings", tableModel,
				5, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("no.upcoming.meetings");
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), true));
		tableEl.setSortSettings(sortOptions);
	}
	
	private void loadModel() {
		List<TeamsMeeting> meetings = teamsManager.getUpcomingsMeetings(courseEntry, subIdent, 5);
		tableModel.setObjects(meetings);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}
	

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	
}
