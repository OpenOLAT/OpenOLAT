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
package org.olat.modules.teams.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroup;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.ui.TeamsMeetingTableModel.MeetingsCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 nov. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsMeetingsController extends FormBasicController {

	private FlexiTableElement pastTableEl;
	private FlexiTableElement upcomingTableEl;
	private TeamsMeetingTableModel pastTableModel;
	private TeamsMeetingTableModel upcomingTableModel;
	
	private final String subIdent;
	private final BusinessGroup group;
	private final RepositoryEntry entry;
	
	@Autowired
	private TeamsService teamsService;
	
	public TeamsMeetingsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdentifier, BusinessGroup group) {
		super(ureq, wControl, "meetings");
		this.entry = entry;
		this.group = group;
		this.subIdent = subIdentifier;
		
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// upcoming meetings table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.subject));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.permanent));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));

		upcomingTableModel = new TeamsMeetingTableModel(columnsModel, getLocale());
		upcomingTableEl = uifactory.addTableElement(getWindowControl(), "upcomingMeetings", upcomingTableModel, getTranslator(), formLayout);
		upcomingTableEl.setEmtpyTableMessageKey("no.upcoming.meetings");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), true));
		upcomingTableEl.setSortSettings(sortOptions);
		upcomingTableEl.setAndLoadPersistedPreferences(ureq, "teams-upcoming-meetings-list");
		
		// upcoming meetings table
		FlexiTableColumnModel pastColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.subject));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.start));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MeetingsCols.end));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));

		pastTableModel = new TeamsMeetingTableModel(columnsModel, getLocale());
		pastTableEl = uifactory.addTableElement(getWindowControl(), "pastMeetings", pastTableModel, getTranslator(), formLayout);
		pastTableEl.setEmptyTableSettings("no.past.meetings", null, "o_icon_calendar");
				
		FlexiTableSortOptions pastSortOptions = new FlexiTableSortOptions();
		pastSortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), true));
		pastTableEl.setSortSettings(pastSortOptions);
		pastTableEl.setAndLoadPersistedPreferences(ureq, "teams-past-meetings-list");
	}
	
	protected void updateModel() {
		List<TeamsMeeting> meetings = teamsService.getMeetings(entry, subIdent, group);

		Date now = new Date();
		List<TeamsMeeting> pastMeetings = new ArrayList<>();
		List<TeamsMeeting> upcomingMeetings = new ArrayList<>();
		for(TeamsMeeting meeting:meetings) {
			if(meeting.getStartDate() == null || meeting.getEndDate() == null
					|| now.compareTo(meeting.getEndDate()) <= 0) {
				upcomingMeetings.add(meeting);
			} else {
				pastMeetings.add(meeting);
			}
		}
		
		upcomingTableModel.setObjects(upcomingMeetings);
		upcomingTableEl.reset(true, true, true);
		pastTableModel.setObjects(pastMeetings);
		pastTableEl.reset(true, true, true);
		pastTableEl.setVisible(!pastMeetings.isEmpty());
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(upcomingTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					doSelect(ureq, upcomingTableModel.getObject(se.getIndex()));
				}
			}
		} else if(pastTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					doSelect(ureq, pastTableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelect(UserRequest ureq, TeamsMeeting meeting) {
		fireEvent(ureq, new SelectTeamsMeetingEvent(meeting));
	}
}
