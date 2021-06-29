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
package org.olat.modules.bigbluebutton.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingTableModel.BMeetingsCols;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonMeetingsController extends FormBasicController {
	
	private FlexiTableElement pastTableEl;
	private FlexiTableElement upcomingTableEl;
	private BigBlueButtonMeetingTableModel pastTableModel;
	private BigBlueButtonMeetingTableModel upcomingTableModel;
	
	private final boolean guest;
	private final RepositoryEntry entry;
	private final String subIdent;
	private final BusinessGroup businessGroup;
	private final boolean showPermanentCol;

	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlueButtonMeetingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String subIdent,
			BusinessGroup businessGroup, boolean administrator, boolean moderator) {
		super(ureq, wControl, "meetings");
		this.entry = entry;
		this.subIdent = subIdent;
		this.businessGroup = businessGroup;
		guest = ureq.getUserSession().getRoles().isGuestOnly();
		showPermanentCol = (administrator || moderator) && bigBlueButtonModule.isPermanentMeetingEnabled();
		
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// upcoming meetings table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.name));
		if(showPermanentCol) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.permanent));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BMeetingsCols.server, new ServerCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));

		upcomingTableModel = new BigBlueButtonMeetingTableModel(columnsModel, getLocale());
		upcomingTableEl = uifactory.addTableElement(getWindowControl(), "upcomingMeetings", upcomingTableModel, getTranslator(), formLayout);
		upcomingTableEl.setEmptyTableSettings("no.upcoming.meetings", null, "o_icon_calendar");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(BMeetingsCols.start.name(), true));
		upcomingTableEl.setSortSettings(sortOptions);
		upcomingTableEl.setAndLoadPersistedPreferences(ureq, "big-blue-button-upcoming-meetings-list");

		// past meetings
		FlexiTableColumnModel pastColumnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.name));
		if(showPermanentCol) {
			pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.permanent));
		}
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.start));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.end));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BMeetingsCols.server, new ServerCellRenderer()));
		pastColumnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));

		pastTableModel = new BigBlueButtonMeetingTableModel(pastColumnsModel, getLocale());
		pastTableEl = uifactory.addTableElement(getWindowControl(), "pastMeetings", pastTableModel, getTranslator(), formLayout);
		
		FlexiTableSortOptions pastSortOptions = new FlexiTableSortOptions();
		pastSortOptions.setDefaultOrderBy(new SortKey(BMeetingsCols.start.name(), true));
		pastTableEl.setSortSettings(pastSortOptions);
		pastTableEl.setAndLoadPersistedPreferences(ureq, "big-blue-button-past-meetings-list");
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public void updateModel() {
		List<BigBlueButtonMeeting> meetings = bigBlueButtonManager.getMeetings(entry, subIdent, businessGroup, guest);
		
		Date now = new Date();
		List<BigBlueButtonMeetingRow> pastMeetings = new ArrayList<>();
		List<BigBlueButtonMeetingRow> upcomingMeetings = new ArrayList<>();
		for(BigBlueButtonMeeting meeting:meetings) {
			if(meeting.getStartDate() == null || meeting.getEndDate() == null
					|| now.compareTo(meeting.getEndDate()) <= 0) {
				upcomingMeetings.add(new BigBlueButtonMeetingRow(meeting));
			} else {
				pastMeetings.add(new BigBlueButtonMeetingRow(meeting));
			}
		}
		
		upcomingTableModel.setObjects(upcomingMeetings);
		upcomingTableEl.reset(true, true, true);
		pastTableModel.setObjects(pastMeetings);
		pastTableEl.reset(true, true, true);
		pastTableEl.setVisible(!pastMeetings.isEmpty());
	}
	
	public boolean hasMeetingByKey(Long meetingKey) {
		boolean has = upcomingTableModel.getObjects().stream()
			.anyMatch(m -> meetingKey.equals(m.getKey()));
		return has || pastTableModel.getObjects().stream()
				.anyMatch(m -> meetingKey.equals(m.getKey()));
	}
	
	public BigBlueButtonMeeting getMeetingByKey(Long meetingKey) {
		Optional<BigBlueButtonMeetingRow> has = upcomingTableModel.getObjects().stream()
				.filter(m -> meetingKey.equals(m.getKey())).findFirst();
		if(!has.isPresent()) {
			has = pastTableModel.getObjects().stream()
					.filter(m -> meetingKey.equals(m.getKey()))
					.findFirst();
		}
		return has.isPresent() ? has.get().getMeeting() : null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(upcomingTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					doSelect(ureq, upcomingTableModel.getMeeting(se.getIndex()));
				}
			}
		} else if(pastTableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select".equals(se.getCommand())) {
					doSelect(ureq, pastTableModel.getMeeting(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelect(UserRequest ureq, BigBlueButtonMeeting meeting) {
		fireEvent(ureq, new SelectBigBlueButtonMeetingEvent(meeting));
	}
	

}
